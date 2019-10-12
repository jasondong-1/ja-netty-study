package com.jason.heartbeat.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("hello");
        System.out.println(((ByteBuf)msg).toString(CharsetUtil.UTF_8));
        //ReferenceCountUtil.release(msg);
        ctx.fireChannelRead(msg);
    }
}
