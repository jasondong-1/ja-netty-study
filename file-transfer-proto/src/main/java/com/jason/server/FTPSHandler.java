package com.jason.server;

import com.jason.proto.FileTransferProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FTPSHandler extends SimpleChannelInboundHandler<FileTransferProtos.FileTransfer> {

    private RandomAccessFile raf;
    private long length = -1;
    private long read = 0;

    public FTPSHandler() throws FileNotFoundException {
        raf = new RandomAccessFile("jj.xml","rw");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileTransferProtos.FileTransfer msg) throws Exception {
            if(length == -1){
                length = msg.getLength();
                System.out.println(length);
            }
            if(read!=length){
                byte[] bytes = msg.getContent().getBytes("utf-8");
                raf.write(bytes);
                read += bytes.length;
            }
            if(read == length) {
                raf.close();
                ctx.close();
            }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
