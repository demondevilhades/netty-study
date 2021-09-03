package test.netty.http;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import test.utils.ResourcesUtils;

/**
 * 
 * @author awesome
 */
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    static final EventExecutorGroup GROUP = new DefaultEventExecutorGroup(8);
    
    private static final AtomicInteger AI = new AtomicInteger(0);
    private static byte[] BS = null;

    static {
        File file = new File(ResourcesUtils.getResourceFile("favicon.ico"));
        BS = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(BS);
        } catch (Exception e) {

        }
    }
    
    private int id = AI.incrementAndGet();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        log.info("channelRead0 : id = {}， ctx = {}, remoteAddress = {}, msg.type = {}", id, ctx,
                ctx.channel().remoteAddress(), msg.getClass());

        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            log.info("request.method = {}, request.protocolVersion = {}, request.uri = {}, request.headers = {}",
                    request.method(), request.protocolVersion(), request.uri(), request.headers());

            if ("/favicon.ico".equals(request.uri())) {
                ByteBuf content = Unpooled.copiedBuffer(BS);
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK, content);
                response.headers().add(HttpHeaderNames.CONTENT_TYPE, "image/x-ico").set(HttpHeaderNames.CONTENT_LENGTH,
                        content.readableBytes());
                ctx.writeAndFlush(response);
            } else {
//                GROUP.submit(task);
                
                ByteBuf content = Unpooled.copiedBuffer("test0", CharsetUtil.UTF_8);
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK, content);
                response.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                        .set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
                ctx.writeAndFlush(response);
            }
        } else {
            log.info("msg = {}", msg.toString());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelActive : id = {}", id);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelInactive : id = {}", id);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerAdded : id = {}", id);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerRemoved : id = {}", id);
    }
}
