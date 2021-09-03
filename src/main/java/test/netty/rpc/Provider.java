package test.netty.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Provider {
    static final int PORT = 9527;
    
    private DemoService demoService = new DemoServiceImpl();

    public void run() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.info("SocketChannel.is = {}", ch.id());

                            ch.pipeline().addLast("Decoder", new StringDecoder())
                                    .addLast("Encoder", new StringEncoder())
                                    .addLast("test", new ChannelInboundHandlerAdapter() {

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg)
                                                throws Exception {
                                            String str = (String) msg;
                                            log.info("msg = {}", str);
                                            if(str.startsWith(DemoService.PROVIDER_NAME)) {
                                                ctx.writeAndFlush(demoService.test(str.replaceFirst(DemoService.PROVIDER_NAME, "")));
                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                throws Exception {
                                            log.error("", cause);
                                            ctx.close();
                                        }
                                    });
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

    class DemoServiceImpl implements DemoService {

        @Override
        public String test(String msg) {
            log.info("msg = {}", msg);
            return "OK";
        }
    }

    public static void main(String[] args) {
        new Provider().run();
    }
}
