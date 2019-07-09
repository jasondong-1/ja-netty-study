package com.jason.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FTSHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private long length = 0;
    private long read = 0;
    private RandomAccessFile raf = new RandomAccessFile(new File("jj"), "rw");

    public FTSHandler() throws FileNotFoundException {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        if (msg.readableBytes() < 8 && length == 0) {
            return;
        }
        if (length == 0) {
            length = msg.readLong();
        }
        byte[] array = new byte[msg.readableBytes()];
        msg.readBytes(array);
        read += array.length;
        raf.write(array);
        if (read == length) {
            raf.close();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();
    }
}
