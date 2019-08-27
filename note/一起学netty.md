<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [1.nio之前的网络通信（可以跳过）](#1nio%E4%B9%8B%E5%89%8D%E7%9A%84%E7%BD%91%E7%BB%9C%E9%80%9A%E4%BF%A1%E5%8F%AF%E4%BB%A5%E8%B7%B3%E8%BF%87)
- [2.netty 是什么样的（可以跳过）](#2netty-%E6%98%AF%E4%BB%80%E4%B9%88%E6%A0%B7%E7%9A%84%E5%8F%AF%E4%BB%A5%E8%B7%B3%E8%BF%87)
- [3.netty 的核心组件（了解一下，知道有这么个概念就行，通过例子会慢慢理解）](#3netty-%E7%9A%84%E6%A0%B8%E5%BF%83%E7%BB%84%E4%BB%B6%E4%BA%86%E8%A7%A3%E4%B8%80%E4%B8%8B%E7%9F%A5%E9%81%93%E6%9C%89%E8%BF%99%E4%B9%88%E4%B8%AA%E6%A6%82%E5%BF%B5%E5%B0%B1%E8%A1%8C%E9%80%9A%E8%BF%87%E4%BE%8B%E5%AD%90%E4%BC%9A%E6%85%A2%E6%85%A2%E7%90%86%E8%A7%A3)
- [4.图解netty](#4%E5%9B%BE%E8%A7%A3netty)
- [5.netty的组件和设计](#5netty%E7%9A%84%E7%BB%84%E4%BB%B6%E5%92%8C%E8%AE%BE%E8%AE%A1)
  - [5.1channelhandler 和 channelpipeline](#51channelhandler-%E5%92%8C-channelpipeline)
  - [5.2 bootstrap 引导](#52-bootstrap-%E5%BC%95%E5%AF%BC)
- [6.bytebuf](#6bytebuf)
- [7.再议channelhandler](#7%E5%86%8D%E8%AE%AEchannelhandler)
- [8.channelpipeline 的常用方法](#8channelpipeline-%E7%9A%84%E5%B8%B8%E7%94%A8%E6%96%B9%E6%B3%95)
- [9.EventExecutorGroup 防止channel阻塞](#9eventexecutorgroup-%E9%98%B2%E6%AD%A2channel%E9%98%BB%E5%A1%9E)
- [10.用Pojo进行传输](#10%E7%94%A8pojo%E8%BF%9B%E8%A1%8C%E4%BC%A0%E8%BE%93)
- [11.谷歌protobuf传输](#11%E8%B0%B7%E6%AD%8Cprotobuf%E4%BC%A0%E8%BE%93)
- [12.eventtrigger](#12eventtrigger)
- [13.分享其他比较好的文章](#13%E5%88%86%E4%BA%AB%E5%85%B6%E4%BB%96%E6%AF%94%E8%BE%83%E5%A5%BD%E7%9A%84%E6%96%87%E7%AB%A0)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

以下完全是个人阅读《netty实战》对netty的理解，如果有错误的地方，还请指正

### 1.nio之前的网络通信（可以跳过）
在nio之前进行网络编程主要使用的socket 套接字，server socket 会一直阻塞直到接收到连接，它会为每个
连接分配一个线程进行io，这种方式存在如下弊端  
* 任何时刻都可能有大量线程处于休眠状态，造成了资源浪费  
* 需要为每个线程的调用栈都分配内存，造成了内存的消耗  
* 即使jvm能够支撑大量的线程，但是线程上下文切换也会带来巨大的消耗  


### 2.netty 是什么样的（可以跳过）  
笔记1中的socket是阻塞的，java也提供了非阻塞式的网络编程：nio，但是直接使用底层api，暴漏了其复杂性
，netty对其进行了封装和优化，使我们可以更多的关注自己的业务逻辑。netty是异步和事件驱动的，异步———
异步方法使我们可以不等待操作完成就立即返回，并在未来的某个时间点通知用户，事件驱动——selector 会去
遍历连接上的事件，并将事件交由特定的线程处理

### 3.netty 的核心组件（了解一下，知道有这么个概念就行，通过例子会慢慢理解）
* channel  
通道，就是client端到server端的链接
* 回调  
* future  
发生在将来的一个操作
* 事件和channelhandler  
事件和事件处理器，这是跟我们业务息息相关的东西

可以先看一个nety的 [简单例子](https://github.com/jasondong-1/ja-netty-study/blob/master/echo)

### 4.图解netty  
netty其实就是客户端(client)和服务端(server)的相互通信，就是我们凭窗说的cs架构，关系如下图    
![avatar](https://github.com/jasondong-1/ja-netty-study/blob/master/note/picture/nettycs.png)  
具体细节如下图  
![avatar](https://github.com/jasondong-1/ja-netty-study/blob/master/note/picture/nettylifecycle.png)  

具体关系如下：  
* 一个EventLoopGroup包含多个EventLoop  
* 一个eventloop在他的生命周期内只和一个Thread绑定  
* 所有由eventloop处理的i/o事件都将在它转悠的Thread上处理  
* 一个channel在他的生命周期内只注册于一个eventloop  
* 一个eventloop可能会被分配给一个或多个channel

简单说下使用netty的整个流程  
1.server端
  声明serverBootStrap 然后配置group，channel类型，端口，handlers，准备就绪，bind即可
  
2.client端  
  声明bootstrap，配置group,channel类型，地址，端口，handlers connet即可  
一般是客户端先发出消息，server回应。client可以在handler或其他地方调用channel.write、  
pipeline.write 或者 channelContext.write 来写出消息，这时server中handler的channelread  
方法会接受到消息，如果该消息是当前拿到消息的handler 该处理的，处理即可，如果不是，可以调用  
ctx.firechannelread 传递给下一个handler执行  

  

### 5.netty的组件和设计  
#### 5.1channelhandler 和 channelpipeline
从业务角度看channelhandler 是netty的主要组件，处理出入站数据的逻辑基本都在这里，一般情况下  
我们用到的channelInBoundhandler居多，为方便我们使用，netty提供了一些默认的channelinboundhandler  
实现，SimpleChannelInboundHandler<object> 和 ChannelInboundHandlerAdapter 是最常用的  
channelpipeline是channelhandler的一个容器，channelhandle人被按照一定的顺序存放在
channelpipeline中。入站事件和出站事件可以被安装到同一个pipeline中，channelhandler
被添加到channelpipeline中时，会被分配一个channelhandlercontext他被认为是channelhandler
和channelpipeline之间的绑定，channelhandlercontext可以用于获得channel，但一般我们
用来写出站数据。

往外写数据有三种方式:  
1)ctx.channel.writeAndFlush()  
2)ctx.writeAndFlush()  
3)ctx.pipeline.writeAndFlush()  

假设pipeline 安装了如下三个handler  
outhandler(a)  ---> inhandler(b) ---> outhandler2(c)  
其中方法1和3会使数据沿着 c-->a 顺序流出，会跳过b，netty会判断事件是进站还是出站，来选择对应的
handler
方法2 会使出站事件只流经a，参考代码 [direction](https://github.com/jasondong-1/ja-netty-study/blob/master/direction)

> 注：writeandflush(msg) 已经调用过ReferenceCountUtil.release(msg)

#### 5.2 bootstrap 引导  
引导是创建channel 的一个帮助类

client的引导
```
            Bootstrap b = new Bootstrap();
            //指定 EventLoopGroup 以处理客户端事件；需要适用于 NIO 的实现
            b.group(group)
                    //适用于 NIO 传输的Channel 类型
                    .channel(NioSocketChannel.class)
                    //设置服务器的InetSocketAddress
                    .remoteAddress(new InetSocketAddress(host, port))
                    //在创建Channel时，向 ChannelPipeline中添加一个 EchoClientHandler实例
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline()
                                    .addLast(new EchoOutHandler())
                                    .addLast(new EchoClientHandler())
                                    .addLast(new EchoOutHandler2());
                        }
                    });
```  
server 的引导  
```
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    //(3) 指定所使用的 NIO 传输 Channel
                    .channel(NioServerSocketChannel.class)
                    //(4) 使用指定的端口设置套接字地址
                    .localAddress(new InetSocketAddress(port))
                    //(5) 添加一个EchoServerHandler到于Channel的 ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //EchoServerHandler 被标注为@Shareable，所以我们可以总是使用同样的实例
                            //这里对于所有的客户端连接来说，都会使用同一个 EchoServerHandler，因为其被标注为@Sharable，
                            //这将在后面的章节中讲到。
                            ch.pipeline()
                                    .addLast(new ServerOutHandler())
                                    .addLast(serverHandler)
                                    .addLast(new ServerOuthandler2());
                        }
                    });

```
bootstrap可以帮助我们设置一些信息，比如eventloopgroup，链接地址，channel类型，handler等等  
细心的读者可能发现了client引导的group() 方法只传了一个eventloopgroup，而server的引导的group()  
得传两个group，这是因为服务端需要两组不同的channel，**一组只包含一个serverchannel，用于监听是否有  
连接接过来，另一组代表已经创建的需要处理的客户端连接**

### 6.bytebuf  
bytebuf是netty 的数据容器，其结构可用下图来解释  
![avatar](https://github.com/jasondong-1/ja-netty-study/blob/master/note/picture/bytebuf.png)  
bytebuf 内部维持了一个缓冲数组用于存储z字节，并含有两个下标，readindex和writeindex，readindex  
代表已经读到了数组的该位置，writeindex代表写到了数组的哪个位置，readindex和writeindex之间的区域  
为可读字节，下面来看一下bytebuf的一些常用方法  
```java
package com.jason.bytebuf.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BytebufExample {
    public static void main(String[] args) {

        //ByteBuf byteBuf = Unpooled.copiedBuffer("jason",CharsetUtil.UTF_8);
        ByteBuf byteBuf = Unpooled.buffer(16);
        //write 方法会使writeindex增加
        byteBuf.writeInt(10);
        byteBuf.writeDouble(12d);
        //read 会使readerindex 增加
        System.out.println(byteBuf.readInt()); //10
        System.out.println(byteBuf.readDouble());//12.0
        System.out.println(byteBuf.isWritable());//true
        System.out.println(byteBuf.isReadable());//false

        //查看readerindex 和 writerindex
        System.out.println(byteBuf.readerIndex());//12
        System.out.println(byteBuf.writerIndex());//12

        //获取bytebuf的可用字节，如有需要会自动增加，直到达到最大值Interger.MAX
        System.out.println(byteBuf.capacity());//16
        //获取bytebuf的最大容量 Integer.MAX
        System.out.println(byteBuf.maxCapacity());//2147483647
        byteBuf.writeDouble(12.0);
        //向bytebuf添加新值后capacity自动扩容
        System.out.println(byteBuf.capacity());//64

        //discardReadBytes 方法会删掉已读的字节，readerindex 置0，并把
        //腾出的空间加到可写字节
        //不建议频繁调用该方法，虽然可以节省空间，但该方法会导致缓存的复制，因为
        //可读字节要移动到缓存的开始位置
        byteBuf.discardReadBytes();
        System.out.println(byteBuf.readerIndex());//0
        System.out.println(byteBuf.writerIndex());//8  我们之前谢了一个12.0 ，但是未读取，所以这里的值是8
        System.out.println(byteBuf.capacity());//64

        /**
         *复制，dupicate 和 slice 方法进行的是浅拷贝，共享了之前bytebuf的缓存，只是有了自己的readerindex和
         * writeindex
         * copy方法是深拷贝，从此两个新旧bytebuf再无瓜葛
         */
        byteBuf.duplicate();
        byteBuf.slice(0,10);
        byteBuf.copy();
        byteBuf.copy(0,10);

        //获取bytebuf可读字节数
        System.out.println(byteBuf.readableBytes());
        // 获取bytebuf可写字节数
        System.out.println(byteBuf.writableBytes());
        

    }
}

```  
查看源码请点击[这里](https://github.com/jasondong-1/ja-netty-study/blob/master/bytebuf)

### 7.再议channelhandler  
channel声明周期  
channelregister ---> channelactive ---> channelinactive ---> channelunregister  
关于channelhandler 我们重点说说channelInBoundHandler,一般我们用到的方法如下：  
channelRegistered  
channelUnRegistered  
channelActive  
channelInActive  
channelReadComplete  
channelRead   
上述方法都是在对应状态发生时才会调用，可以亲自运行[例子](https://github.com/jasondong-1/ja-netty-study/blob/master/channel-lifecycle)，打印结果如下：  
```
八月 04, 2019 10:27:00 下午 nia.study.echoclient.EchoClientHandler channelRegistered
信息: I am function: channelRegistered
八月 04, 2019 10:27:00 下午 nia.study.echoclient.EchoClientHandler channelActive
信息: I am function: channelActive
Client received: Netty rocks!
八月 04, 2019 10:27:00 下午 nia.study.echoclient.EchoClientHandler channelReadComplete
信息: I am function: channelReadComplete
八月 04, 2019 10:27:00 下午 nia.study.echoclient.EchoClientHandler channelInactive
信息: I am function: channelInactive
八月 04, 2019 10:27:00 下午 nia.study.echoclient.EchoClientHandler channelUnregistered
信息: I am function: channelUnregistered
``` 
由上图结果可以看出channel链接以后调用方法的顺序  
channelRegistered -> channelActive -> channelRead -> channelReadComplete(所有的可读字节从channel读出后调用) ->   
channelInactive -> channelUnregistered  
channelInactive 和 channelUnregistered是在关闭channel链接后调用的  
一般情况下每个channel创建时添加的handler 都是new 出来的新对象，有时候创建channel时需要共享handler，比如在handler中统计  
channel的数量，这时候可以在hander类上加@Sharable注解,当然你得保证handler的县城安全性  

### 8.channelpipeline 的常用方法  
详见[示例](https://github.com/jasondong-1/ja-netty-study/blob/master/channelpipeline)  

### 9.EventExecutorGroup 防止channel阻塞  
channelPipeline 中的handler都是通过EventLoop的I/O线程来执行任务的，因此如果阻塞了该线程，就会阻塞整体I/O，  
为了防止阻塞，可以使用EventExecutorGroup，我写了一个[demo](https://github.com/jasondong-1/ja-netty-study/blob/master/event-executor-test)，  
大致流程如下：  
client端channel active之后每隔200ms向server端写一个数字（0-5）,server收到消息后讲处理数字的逻辑交由EventExecutor执行，  
当server收到数字5后就关闭链接，通过运行代码可以看出，server端未被阻塞，收到5后关闭了channel（但是EventExecutor收到的任务还  
要执行完成），尽管EventExecutorGroup中有多个线程，但是每个channel在整个生命周期只能使用EventExecutorGroup中固定的一个线程，  
以上的说法有错误，通过阅读源码及实际[测试](https://github.com/jasondong-1/ja-netty-study/blob/master/event-executor-test2),现得出如下结论：  

通过addLast(eventexecutorgroup,handler) 可以熊group中选择一个executor绑定到handler（终身的），当前handler的io操作都会使用  
该绑定的executor来执行，其他hanler还会使用eventloop的线程来处理，如果有另一个handler也使用了addLast(eventexecutorgroup,handler)  
方式来添加的，那么他被分配的executor跟上一个handler分配的executor是同一个。


  

### 10.用Pojo进行传输  
之前我们的示例进行传输的时候都是用的bytebuf,我觉得传输对象会更方便一些，传输pojo，当出站时需要将pojo转换为bytes，入站时需要将bytes  
转换为pojo，所以需要encoder和decoder，具体看[例子](https://github.com/jasondong-1/ja-netty-study/blob/master/echo-pojo)  
> 切记一定要给pojo实现serializable接口 

### 11.谷歌protobuf传输  
protobuf 传输是一个更高校的传输（和使用pojo类似），protobuf的使用方法可以上网百度，注意点是protoc 和protobuf-java maven依赖  
版本要一致，否则会报错，请看[例子](https://github.com/jasondong-1/ja-netty-study/blob/master/file-transfer-proto)  

### 12.eventtrigger
这个知识点我们用一个断线重连的[例子](https://github.com/jasondong-1/ja-netty-study/blob/master/heart-beat)  
这个例子来自[博客](https://blog.csdn.net/linuu/article/details/51509847)  

### 13.分享其他比较好的文章  
[Netty核心NioEventLoop原理解析](https://blog.csdn.net/TheLudlows/article/details/82961193)  
[channeloption参数](https://www.jianshu.com/p/975b30171352)  
[ssl双向验证](https://blog.csdn.net/moonpure/article/details/82863181)  



       