package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 基础版本
 */
public class NIOServerV1 {
    public static void main(String[] args) throws Exception{
        //创建网络服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);// 设置为非阻塞
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("启动成功");
        while (true){
            SocketChannel socketChannel = serverSocketChannel.accept();//获取tcp连接通道, 非阻塞会直接返回结果
            if (socketChannel!=null){
                socketChannel.configureBlocking(false);// 设置为非阻塞
                System.out.println("收到新连接: "+socketChannel.getRemoteAddress());

                try {
                    ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
                    while (socketChannel.isOpen()&& socketChannel.read(requestBuffer)!=-1){
                        //长连接情况下, 需要手动判断数据有没有读取结束(此处做一个简单判断:超过0字节就认为结束了)
                        if (requestBuffer.position()>0) break;
                    }
                    if (requestBuffer.position()==0) continue; //如果没有数据,则不继续后面的处理

                    requestBuffer.flip();
                    byte[] content = new byte[requestBuffer.limit()];
                    requestBuffer.get(content);
                    System.out.println(new String(content));
                    System.out.println("收到数据,来自: "+socketChannel.getRemoteAddress());

                    //响应结果200
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Lenght: 11\r\n\r\n" +
                            "Hello World";
                    ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                    while (buffer.hasRemaining()){
                        socketChannel.write(buffer); //非阻塞
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
