package com.jason.hearbeat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HearbeatClient {
    private String host;
    private int port;

    public HearbeatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        final EventLoopGroup group = new NioEventLoopGroup(1);
        final ClientIdleTrigger ct = new ClientIdleTrigger();
        try {
            final Bootstrap boot = new Bootstrap();
            final ArrayList<ChannelHandler> list = new ArrayList<ChannelHandler>(5);
            list.add(new IdleStateHandler(0, 5, 0));
            list.add(ct);
            boot.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast()
                                    .addLast(new ReconnectHandler(boot, group) {
                                        @Override
                                        public ChannelHandler[] getHandlers() {
                                            return new ChannelHandler[]{
                                                    this,
                                                    new IdleStateHandler(0, 5, 0),
                                                    ct
                                            };
                                        }
                                    }.getHandlers());
                        }
                    });
            ChannelFuture future = boot.connect().sync();
            System.out.println("client 链接成功");
            System.out.println("channel id :" + future.channel().id().asLongText());
            future.channel().closeFuture().sync();
            System.out.println("haha");
        } finally {
            //System.out.println("gracefully");
            //group.shutdownGracefully();
        }

    }


    public static void main(String[] args) throws InterruptedException {
        HearbeatClient client = new HearbeatClient("localhost", 9000);
        client.start();
    }
}
