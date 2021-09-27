package test.netty.ssl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import test.utils.ResourcesUtils;
import test.utils.SslUtils;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Client {

    private final String algorithm = "sha-256";

    /**
     * 
     */
    public void run() {
        String fingerprints = null;
        File file = new File(ResourcesUtils.getResourceFile("ssl/fingerprint"));
        if(file.exists()) {
            try(FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis, CharsetUtil.UTF_8);
                    BufferedReader br = new BufferedReader(isr);){
                fingerprints = br.readLine();
            } catch (IOException e) {
                log.error("", e);
            }
        }
        if(fingerprints == null) {
            return;
        }
        
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            SslContext sslContext = SslUtils.getTrustClientSslContext(algorithm, fingerprints);
            
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("SSL", sslContext.newHandler(ch.alloc()))
                                    .addLast("log", new LoggingHandler(LogLevel.INFO))
                                    .addLast("decoder", new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast("encoder", new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast("test", new SimpleChannelInboundHandler<String>() {

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            ctx.writeAndFlush("TestMsg1234");
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, String msg)
                                                throws Exception {
                                            log.info("msg = {}", msg);
                                        }
                                    });
                        }
                    });
            ChannelFuture cf = bootstrap.connect(new InetSocketAddress("127.0.0.1", Server.PORT)).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException | SSLException e) {
            log.error("", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new Client().run();
    }
}
