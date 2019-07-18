package com.jason.heartbeat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;

public class HearbeatServer {
    private int port;

    public HearbeatServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress("192.168.31.149",port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ServerHandler())
                                    .addLast(new IdleStateHandler(10, 0, 0))
                                    .addLast(new ServerIdleTrigger());
                        }
                    });
            ChannelFuture f = boot.bind().sync();
            System.out.println("server 启动成功");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        }

    }


    public static void main(String[] args) throws InterruptedException {
        HearbeatServer server = new HearbeatServer(9000);
        server.start();
    }
}
