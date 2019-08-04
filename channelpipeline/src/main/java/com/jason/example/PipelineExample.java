package com.jason.example;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class PipelineExample {
    public static void main(String[] args) {
        new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addFirst("noe",new ChannelInboundHandlerAdapter());
                pipeline.addLast("two",new ChannelInboundHandlerAdapter());
                pipeline.addBefore("two","three",new ChannelInboundHandlerAdapter());
                pipeline.addAfter("three","four",new ChannelInboundHandlerAdapter());
                pipeline.remove("two");
                pipeline.replace("three","three2",new ChannelInboundHandlerAdapter());
            }
        };
    }
}
