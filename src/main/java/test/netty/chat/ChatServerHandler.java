package test.netty.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        CHANNEL_GROUP.writeAndFlush("[" + ctx.channel().remoteAddress() + "] online!");
        CHANNEL_GROUP.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("CHANNEL_GROUP.size = {}", CHANNEL_GROUP.size());
        CHANNEL_GROUP.writeAndFlush("[" + ctx.channel().remoteAddress() + "] offline!");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[{}] online", ctx.channel().remoteAddress());
        ctx.channel().writeAndFlush("[SERVER] online");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[{}] offline.", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String str = new StringBuilder().append("[").append(ctx.channel().remoteAddress()).append("] send [")
                .append(msg).append("]\r\n").toString();
        CHANNEL_GROUP.forEach(ch -> {
            if (ctx.channel() != ch) {
                ch.writeAndFlush(str);
            } else {
                ch.writeAndFlush(new StringBuilder().append("[SELF] send [").append(msg).append("]").toString());
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("", cause);
        ctx.close();
    }
}
