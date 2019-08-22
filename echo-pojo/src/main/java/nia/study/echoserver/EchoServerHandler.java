package nia.study.echoserver;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import nia.study.Student;

/**
 * Listing 2.1 EchoServerHandler
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Student) {
            Student in = (Student) msg;
            System.out.println(
                    "Server received: " + in.toString() + "  " + Thread.currentThread().getName());
            //将读到的消息写出
            ctx.writeAndFlush(in);
        } else {
            System.out.println("xx");
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        //写完消息后关闭channel
        //ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
