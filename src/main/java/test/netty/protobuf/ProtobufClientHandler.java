package test.netty.protobuf;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class ProtobufClientHandler extends SimpleChannelInboundHandler<Any> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Any.newBuilder().setTypeUrl("testUrl")
                .setValue(ByteString.copyFrom("test", CharsetUtil.UTF_8)).build());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Any msg) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        ctx.close();
    }
}
