package test.netty.chat;

import java.net.InetSocketAddress;
import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
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
        try {
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast("encoder", new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast("chat", new ChatCilentHandler());
                        }
                    });
            ChannelFuture cf = bootstrap.connect(new InetSocketAddress("127.0.0.1", Server.PORT)).sync();
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
