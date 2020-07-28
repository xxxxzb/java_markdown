package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * 改进 用while+list实现 单线程非阻塞
 */
public class NIOServerV2 {

    private static ArrayList<SocketChannel> channels = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        //创建网络服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);// 设置为非阻塞
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("启动成功");
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();//获取tcp连接通道, 非阻塞会直接返回结果

            if (socketChannel != null) {
                socketChannel.configureBlocking(false);// 设置为非阻塞
                System.out.println("收到新连接: " + socketChannel.getRemoteAddress());
                channels.add(socketChannel);
            } else {
                //没有新连接的情况下,就去处理有连接的数据,处理完就删除
                Iterator<SocketChannel> iterator = channels.iterator();
                while (iterator.hasNext()) {
                    SocketChannel ch = iterator.next();
                    try {
                        ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
                        if (ch.read(requestBuffer) == 0) {
                            //等于0, 代表这个通道没有数据需要处理, 那就待会再处理
                            continue;
                        }

                        while (ch.isOpen() && ch.read(requestBuffer) != -1) {
                            //长连接情况下, 需要手动判断数据有没有读取结束(此处做一个简单判断:超过0字节就认为结束了)
                            if (requestBuffer.position() > 0) break;
                        }
                        if (requestBuffer.position() == 0) continue; //如果没有数据,则不继续后面的处理
                        requestBuffer.flip();
                        byte[] content = new byte[requestBuffer.limit()];
                        requestBuffer.get(content);
                        System.out.println(new String(content));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //问题: 轮询方式效率低, 浪费cpu
    }
}
