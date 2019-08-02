package nia.study.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * Listing 2.1 EchoServerHandler
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try{
            ByteBuf in = (ByteBuf) msg;
            System.out.println(
                    "Server received: " + in.toString(CharsetUtil.UTF_8));
            //写出消息
            ctx.pipeline().writeAndFlush(Unpooled.copiedBuffer("hello",CharsetUtil.UTF_8));
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hello",CharsetUtil.UTF_8));
        }finally {
            ReferenceCountUtil.release(msg);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
