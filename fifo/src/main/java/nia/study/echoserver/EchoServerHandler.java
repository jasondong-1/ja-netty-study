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
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (msg instanceof Student) {
                    Student in = (Student) msg;
                    System.out.println(
                            "Server received: " + in.toString() + "  " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000 * (6 - in.getAge()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //将读到的消息写出
                    ctx.writeAndFlush(in);
                } else {
                    System.out.println("xx");
                }

            }
        });
        thread.start();

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
