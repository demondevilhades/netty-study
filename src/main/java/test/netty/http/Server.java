package test.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Server {
    static final int PORT = 9527;

    /**
     * http://localhost:9527/
     */
    public void run() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.info("SocketChannel.id = {}", ch.id());

                            ch.pipeline().addLast("testHttp", new HttpServerCodec())
                                    .addLast("test", new HttpServerHandler());
                        }
                    });
            ChannelFuture cf = bootstrap.bind(PORT).addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        log.info("future is success");
                    } else {
                        log.info("future is not success");
                    }
                }
            }).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new Server().run();
    }
}
