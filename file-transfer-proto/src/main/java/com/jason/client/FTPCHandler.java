package com.jason.client;

import com.jason.proto.FileTransferProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FTPCHandler extends SimpleChannelInboundHandler<FileTransferProtos.FileTransfer> {

    private String name;
    private RandomAccessFile raf;
    private final int SIZE =  1024*8;

    public FTPCHandler(String name) throws FileNotFoundException {
        this.name = name;
        raf = new RandomAccessFile(new File(name), "r");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            FileTransferProtos.FileTransfer.Builder builder = FileTransferProtos
                    .FileTransfer
                    .newBuilder();

            builder.setLength(raf.length());
            //.setContent(ByteString.copyFrom("hello", "utf-8"));
            //ctx.writeAndFlush(builder.build());
            int i = 0;
            byte[] bytes = new byte[SIZE];
            while ((i = raf.read(bytes)) != -1) {

                String s = new String(bytes,0,i,"utf-8");
                builder.setContent(s);
                ctx.writeAndFlush(builder.build());
                bytes = new byte[SIZE];
            }
        } finally {
            raf.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileTransferProtos.FileTransfer msg) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
