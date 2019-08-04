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
