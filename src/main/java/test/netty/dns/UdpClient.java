package test.netty.dns;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Random;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class UdpClient {

    private String dnsIp = "8.8.8.8";
    private String nsHost = "www.baidu.com";

    public void run() throws IOException {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        int port = getPort(50000, 60000);

        DatagramDnsQueryEncoder datagramDnsQueryEncoder = new DatagramDnsQueryEncoder();
        DatagramDnsResponseDecoder datagramDnsResponseDecoder = new DatagramDnsResponseDecoder();

        try {
            bootstrap.group(eventLoopGroup).channel(NioDatagramChannel.class).handler(new ChannelInitializer<NioDatagramChannel>() {

                @Override
                protected void initChannel(NioDatagramChannel ch) throws Exception {
                    ch.pipeline().addLast("log", new LoggingHandler()).addLast("datagramDnsQueryEncoder", datagramDnsQueryEncoder)
                            .addLast("datagramDnsResponseDecoder", datagramDnsResponseDecoder)
                            .addLast("test", new SimpleChannelInboundHandler<DatagramDnsResponse>() {

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) throws Exception {
                                    log.info("msg = {}", msg);
                                    log.info("msg.id = {}", msg.id());

                                    logDnsRecord(msg, DnsSection.QUESTION, DnsQuestion.class);
                                    logDnsRecord(msg, DnsSection.ADDITIONAL, DnsRawRecord.class);
                                    logDnsRecord(msg, DnsSection.AUTHORITY, DnsRawRecord.class);
                                    logDnsRecord(msg, DnsSection.ANSWER, DnsRawRecord.class);

                                    ctx.close();
                                }
                            });
                }
            });

            Channel channel = bootstrap.bind(port).sync().channel();
            channel.writeAndFlush(data(port)).sync();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    private DatagramDnsQuery data(int localPort) {
        InetSocketAddress sender = new InetSocketAddress(localPort);
        InetSocketAddress recipient = new InetSocketAddress(dnsIp, 53);
        int id = 0;
        DatagramDnsQuery datagramDnsQuery = new DatagramDnsQuery(sender, recipient, id);
        DnsRecord record = new DefaultDnsQuestion(nsHost, DnsRecordType.A);
        datagramDnsQuery.addRecord(DnsSection.QUESTION, record);
        return datagramDnsQuery;
    }

    private int getPort(int min, int max) throws IOException {
        IOException ioe;
        Random random = new Random();
        int retry = 100;
        do {
            try {
                int port = min + (random.nextInt(max) % (max - min + 1));
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(port);
                    port = serverSocket.getLocalPort();
                    log.info("port = {}", port);
                    return port;
                } finally {
                    if (serverSocket != null) {
                        serverSocket.close();
                    }
                }
            } catch (IOException e) {
                ioe = e;
            }
        } while (--retry > 0);
        throw ioe;
    }

    @SuppressWarnings("unchecked")
    private <T extends DnsRecord> void logDnsRecord(DatagramDnsResponse msg, DnsSection dnsSection, Class<T> tClass) {
        T t;
        while ((t = (T) msg.recordAt(dnsSection)) != null) {
            msg.removeRecord(dnsSection, 0);
            if (tClass == DnsQuestion.class) {
                log.info(dnsSection.name() + " : name = {}, type = {}", t.name(), t.type());
            } else if (tClass == DnsRawRecord.class) {
                ByteBuf content = ((DnsRawRecord) t).content();
                int readableBytes = content.readableBytes();
                byte[] bs = new byte[readableBytes];
                Object[] ips = new Object[readableBytes];
                content.readBytes(bs);
                if (readableBytes == 4) {
                    for (int i = 0; i < bs.length; i++) {
                        ips[i] = Byte.toUnsignedInt(bs[i]);
                    }
                } else {
                    for (int i = 0; i < bs.length; i++) {
                        ips[i] = Integer.toHexString(Byte.toUnsignedInt(bs[i]));
                    }
                }
                log.info(dnsSection.name() + " : name = {}, type = {}, ttl = {}, content = {}", t.name(), t.type(), t.timeToLive(), Arrays.toString(ips));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new UdpClient().run();
    }
}
