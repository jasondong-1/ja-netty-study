package com.jason.server;

import com.jason.proto.FileTransferProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FTPSHandler extends SimpleChannelInboundHandler<FileTransferProtos.FileTransferRequest> {

    private RandomAccessFile raf;
    private long length = -1;
    private long read = 0;

    public FTPSHandler() throws FileNotFoundException {
        raf = new RandomAccessFile("protoc2", "rw");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileTransferProtos.FileTransferRequest msg) throws Exception {
        try {
            if (length == -1) {
                length = msg.getLength();
                System.out.println(length);
            }
            if (read != length) {
                byte[] bytes = msg.getContent().toByteArray();
                System.out.println("server 读到的字节数" + bytes.length);
                raf.write(bytes);
                read += bytes.length;
                System.out.println(read);
            }
            if (read == length) {
                raf.close();
                FileTransferProtos.FileTransferResponse.Builder builder = FileTransferProtos
                        .FileTransferResponse
                        .newBuilder();
                builder.setStatus(FileTransferProtos.Status.SUCCESS);
                ctx.writeAndFlush(builder.build());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
