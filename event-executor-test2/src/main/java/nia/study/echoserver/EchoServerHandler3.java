package nia.study.echoserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class EchoServerHandler3 extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            System.out.println("EchoServerHandler3" + Thread.currentThread().getName());
        }finally {
            ReferenceCountUtil.release(msg);
        }

    }
}
