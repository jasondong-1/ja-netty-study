package nia.study.echoclient;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import nia.study.Student;

import java.util.UUID;

/**
 * Listing 2.3 ChannelHandler for the client
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class EchoClientHandler
        extends SimpleChannelInboundHandler<Student> {
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final String name = UUID.randomUUID().toString();
        for (int i = 0; i < 4; i++) {
            final int finalI = i;
            ctx.writeAndFlush(new Student(Thread.currentThread().getName() + name, finalI));
        }

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Student in) {
        System.out.println(Thread.currentThread().getName() + " " + in);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
