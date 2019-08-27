package nia.study.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class EchoServerHandler2 extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;
            buf.toString(CharsetUtil.UTF_8);
            System.out.println("EchoServerHandler2" + Thread.currentThread().getName());
            ctx.fireChannelRead(Unpooled.copiedBuffer("jason",CharsetUtil.UTF_8));
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }
}
