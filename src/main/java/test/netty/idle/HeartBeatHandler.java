package test.netty.idle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * @author awesome
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (ctx instanceof IdleStateEvent) {
            // TODO
            switch (((IdleStateEvent) ctx).state()) {
                case READER_IDLE:

                    break;
                case WRITER_IDLE:

                    break;
                case ALL_IDLE:

                    break;
                default:
                    break;
            }
        }
    }
}
