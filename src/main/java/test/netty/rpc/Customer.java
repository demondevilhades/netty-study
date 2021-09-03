package test.netty.rpc;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class Customer {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private CustomerHandler customerHandler = new CustomerHandler();

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> serviceClass, final String providerName) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { serviceClass },
                (proxy, method, args) -> {
                    customerHandler.setMsg(providerName + args[0]);
                    return executorService.submit(customerHandler).get();
                });
    }

    public void start() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("Decoder", new StringDecoder())
                                    .addLast("Encoder", new StringEncoder()).addLast("test", customerHandler);
                        }
                    });
            bootstrap.connect(new InetSocketAddress("127.0.0.1", Provider.PORT)).sync();
//            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
//            eventLoopGroup.shutdownGracefully();
        }

    }

    class CustomerHandler extends ChannelInboundHandlerAdapter implements Callable<Object> {

        private ChannelHandlerContext ctx;
        @Setter
        private String result;
        @Setter
        private String msg;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            this.ctx = ctx;
        }

        @Override
        public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            result = msg.toString();
            notify();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("", cause);
            ctx.close();
        }

        @Override
        public synchronized Object call() throws Exception {
            this.ctx.writeAndFlush(msg);
            wait();
            return result;
        }
    }

    public static void main(String[] args) throws Exception {
        Customer customer = new Customer();
        customer.start();
        DemoService demoService = customer.getBean(DemoService.class, DemoService.PROVIDER_NAME);
        String test = demoService.test("test_c");
        log.info("test = {}", test);
    }
}
