package test.netty.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class WsFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        log.info("remoteAddress = {}, text = {}", ctx.channel().remoteAddress(), text);
        ctx.writeAndFlush(new TextWebSocketFrame("{\"from\":\"SERVER\",\"msg\":\"GOT\"}"));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerAdded : id = {}", ctx.channel().id().asLongText());
        ctx.writeAndFlush(new TextWebSocketFrame("{\"from\":\"SERVER\",\"msg\":\"CONNECTED\"}"));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerRemoved : id = {}", ctx.channel().id().asLongText());
        ctx.writeAndFlush(new TextWebSocketFrame("{\"from\":\"SERVER\",\"msg\":\"CLOSED\"}"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        ctx.close();
    }
}
