package test.netty.protobuf;

import com.google.protobuf.Any;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class ProtobufServerHandler extends SimpleChannelInboundHandler<Any> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Any msg) throws Exception {
        log.info("TypeUrl = {}, Value = {}", msg.getTypeUrl(), msg.getValue().toString(CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        ctx.close();
    }
}
