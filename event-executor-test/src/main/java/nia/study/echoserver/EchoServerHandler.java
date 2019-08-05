package nia.study.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.omg.PortableServer.THREAD_POLICY_ID;

import java.util.concurrent.*;

/**
 * Listing 2.1 EchoServerHandler
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        final String s = in.toString(CharsetUtil.UTF_8);
        final ChannelHandlerContext ctx2 = ctx;
        //这里执行的是一个耗时任务，我们用eventexecutorgroup中的executor来执行，是一个异步操作，因此不会
        //阻塞后方执行
        Future<Integer> ff = ctx.executor().submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int i = Integer.valueOf(s);
                long ll = (10 - i) * 300;

                Thread.sleep(ll);
                System.out.println(
                        "Server received: " + s + ",我要睡" + ll + "ms.我是" + Thread.currentThread().getId());
                ctx2.writeAndFlush(Unpooled.copiedBuffer(String.valueOf(s), CharsetUtil.UTF_8));
                return 3;
            }
        });
        ReferenceCountUtil.release(msg);
        //System.out.println("read  末尾");
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello" + s, CharsetUtil.UTF_8));
        //5 是client写出的最后一个数字，server接受到后就关闭链接，
        //如果使用 eventexrcutor 那么应该是已收到该消息就关闭链接
        if (s.equals("5")) {
            System.out.println("即将关闭channel");
            ctx.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("成功关闭channel");
                        System.out.println(future.isDone());
                    } else {
                        future.cause().printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        /*ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);*/
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
