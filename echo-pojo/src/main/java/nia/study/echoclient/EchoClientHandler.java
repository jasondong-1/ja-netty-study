package nia.study.echoclient;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import nia.study.Student;

/**
 * Listing 2.3 ChannelHandler for the client
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class EchoClientHandler
        extends SimpleChannelInboundHandler<Student> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Student student = new Student("jason", 20);
        ctx.writeAndFlush(student);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Student in) {
        try {
            System.out.println(in);
        } finally {
            ReferenceCountUtil.release(in);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
