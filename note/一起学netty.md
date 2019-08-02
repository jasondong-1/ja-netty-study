<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [1.nio之前的网络通信（可以跳过）](#1nio%E4%B9%8B%E5%89%8D%E7%9A%84%E7%BD%91%E7%BB%9C%E9%80%9A%E4%BF%A1%E5%8F%AF%E4%BB%A5%E8%B7%B3%E8%BF%87)
- [2.netty 是什么样的（可以跳过）](#2netty-%E6%98%AF%E4%BB%80%E4%B9%88%E6%A0%B7%E7%9A%84%E5%8F%AF%E4%BB%A5%E8%B7%B3%E8%BF%87)
- [3.netty 的核心组件（了解一下，知道有这么个概念就行，通过例子会慢慢理解）](#3netty-%E7%9A%84%E6%A0%B8%E5%BF%83%E7%BB%84%E4%BB%B6%E4%BA%86%E8%A7%A3%E4%B8%80%E4%B8%8B%E7%9F%A5%E9%81%93%E6%9C%89%E8%BF%99%E4%B9%88%E4%B8%AA%E6%A6%82%E5%BF%B5%E5%B0%B1%E8%A1%8C%E9%80%9A%E8%BF%87%E4%BE%8B%E5%AD%90%E4%BC%9A%E6%85%A2%E6%85%A2%E7%90%86%E8%A7%A3)
- [4.图解netty](#4%E5%9B%BE%E8%A7%A3netty)
- [5.netty的组件和设计](#5netty%E7%9A%84%E7%BB%84%E4%BB%B6%E5%92%8C%E8%AE%BE%E8%AE%A1)
  - [5.1channelhandler 和 channelpipeline](#51channelhandler-%E5%92%8C-channelpipeline)
- [6.asldflaksfl;aksjf](#6asldflaksflaksjf)

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

### 5.netty的组件和设计  
#### 5.1channelhandler 和 channelpipeline
从业务角度看channelhandler 是netty的主要组件，处理出入站数据的逻辑基本都在这里。
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
```java
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
```java
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


  
