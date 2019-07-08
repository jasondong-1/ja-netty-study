package com.jason.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FTCHander extends SimpleChannelInboundHandler<ByteBuf> {
    private String name;
    private RandomAccessFile raf;
    private final int BYTES = 1024 * 8;

    public FTCHander(String name) throws FileNotFoundException {
        this.name = name;
        raf = new RandomAccessFile(new File(name), "r");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf bf = Unpooled.buffer(2);
        ctx.writeAndFlush(bf.writeLong(raf.length()));
        byte[] bytes = new byte[BYTES];
        int i = 0;
        while ((i = raf.read(bytes)) != -1) {
            ctx.writeAndFlush(Unpooled.copiedBuffer(bytes, 0, i));
            bytes = new byte[BYTES];
        }

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        raf.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

    }
}
