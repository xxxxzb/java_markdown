package nio;

import java.nio.ByteBuffer;

public class BufferDemo {
    public static void main(String[] args) {
        //构建一个byte字节缓冲区, 容量4
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        System.out.println(String.format("初始化: capacity容量:%s," +
                " position位置: %s," +
                " limit限制: %s", byteBuffer.capacity(), byteBuffer.position(), byteBuffer.limit()));

        //写入
        byteBuffer.put((byte)1);
        byteBuffer.put((byte)2);
        byteBuffer.put((byte)3);
        System.out.println(String.format("写入3字节后: capacity容量:%s," +
                " position位置: %s," +
                " limit限制: %s", byteBuffer.capacity(), byteBuffer.position(), byteBuffer.limit()));

    }
}
