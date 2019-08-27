package nia.study.echoclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listing 2.4 Main class for the client
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class EchoClient {
    private final String host;
    private final int port;
    private DefaultEventExecutorGroup executors = new DefaultEventExecutorGroup(1);

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start()
            throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            //添加executorgroup ,用来执行耗时任务
                            ch.pipeline().addLast(executors,
                                    new EchoClientHandler());
                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
            executors.shutdownGracefully();
        }
    }

    public static void main(String[] args)
            throws Exception {
        args = new String[]{"localhost", "9000"};
        if (args.length != 2) {
            System.err.println("Usage: " + EchoClient.class.getSimpleName() +
                    " <host> <port>"
            );
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 1; i++) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        new EchoClient(host, port).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        service.shutdown();

    }
}

