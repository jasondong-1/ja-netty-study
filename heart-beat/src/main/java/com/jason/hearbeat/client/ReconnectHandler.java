package com.jason.hearbeat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;

import java.util.ArrayList;
import java.util.List;

@ChannelHandler.Sharable
public abstract class ReconnectHandler extends ChannelInboundHandlerAdapter {
    private int trytime = 5;
    private int count = 0;
    private Bootstrap boot;
    private EventLoopGroup group;
    private String host;
    private int port;

    public ReconnectHandler(Bootstrap boot, EventLoopGroup group) {
        this.group = group;
        this.boot = boot;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("进入 inactive");
        if (count < trytime) {
            count += 1;
            boot.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(getHandlers());
                }
            });
            ChannelFuture future = boot.connect();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Thread.sleep(1000);
                    boolean success = future.isSuccess();
                    if (success) {
                        System.out.println("重连成功");
                    } else {
                        System.out.println("重连失败，即将重连");
                        //经过测试下面一行代码不必写
                        //future.channel().pipeline().fireChannelInactive();
                    }
                }
            });
        } else {
            group.shutdownGracefully();
        }
        ctx.fireChannelInactive();
    }


    public abstract ChannelHandler[] getHandlers();
}
