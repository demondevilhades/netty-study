package test.netty.msg;

import java.net.InetSocketAddress;
import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
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
                            ch.pipeline()
                                    .addLast("Encoder", new MessageToByteEncoder<Long>() {

                                        @Override
                                        protected void encode(ChannelHandlerContext ctx, Long msg, ByteBuf out)
                                                throws Exception {
                                            out.writeLong(msg);
                                        }
                                    })
                                    .addLast("test", new SimpleChannelInboundHandler<Long>() {

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            ctx.writeAndFlush(123456L);
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Long msg)
                                                throws Exception {
                                        }
                                    });
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
