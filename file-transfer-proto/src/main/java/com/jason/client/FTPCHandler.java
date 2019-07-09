package com.jason.client;

import com.google.protobuf.ByteString;
import com.jason.proto.FileTransferProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FTPCHandler extends SimpleChannelInboundHandler<FileTransferProtos.FileTransferResponse> {

    private String name;
    private RandomAccessFile raf;
    private final int SIZE =  1024*50;
    public FTPCHandler(String name) throws FileNotFoundException {
        this.name = name;
        raf = new RandomAccessFile(new File(name), "r");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            FileTransferProtos.FileTransferRequest.Builder builder = FileTransferProtos
                    .FileTransferRequest
                    .newBuilder();

            builder.setLength(raf.length());

            int i = 0;
            byte[] bytes = new byte[SIZE];
            while ((i = raf.read(bytes)) != -1) {
                System.out.println("读取了 " + i +"个字节");
                builder.setContent(ByteString.copyFrom(bytes,0,i));
                ctx.writeAndFlush(builder.build());
                bytes = new byte[SIZE];
            }
        } finally {
            raf.close();
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileTransferProtos.FileTransferResponse msg) throws Exception {
        System.out.println(msg.getStatus());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }
}
