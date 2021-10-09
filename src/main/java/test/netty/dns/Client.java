package test.netty.dns;

import java.net.InetSocketAddress;
import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsSection;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Client {

    /**
     * 
     */
    public void run() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        
        DatagramDnsQueryEncoder datagramDnsQueryEncoder = new DatagramDnsQueryEncoder();
        DatagramDnsResponseDecoder datagramDnsResponseDecoder = new DatagramDnsResponseDecoder();
        
        try {
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("datagramDnsQueryEncoder", datagramDnsQueryEncoder)
                                    .addLast("datagramDnsResponseDecoder", datagramDnsResponseDecoder)
                                    .addLast("test", new SimpleChannelInboundHandler<DatagramDnsResponse>() {

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            // TODO
                                            InetSocketAddress sender = null;
                                            InetSocketAddress recipient = null;
                                            int id = 0;
                                            DatagramDnsQuery datagramDnsQuery = new DatagramDnsQuery(sender, recipient, id);
                                            DnsRecord record = new DefaultDnsQuestion("www.baidu.com", DnsRecordType.A);
                                            datagramDnsQuery.addRecord(DnsSection.QUESTION, record);
                                            ctx.writeAndFlush(datagramDnsQuery);
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg)
                                                throws Exception {
                                            // TODO
                                            log.info("msg = {}", msg);
                                        }
                                    });
                        }
                    });
            ChannelFuture cf = bootstrap.connect(new InetSocketAddress("114.114.114.114", 53)).sync();
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(line.trim())) {
                        break;
                    }
                    cf.channel().writeAndFlush(line);
                }
            } catch (Exception e) {
                log.error("", e);
            }
            cf.channel().closeFuture();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new Client().run();
    }
}
