package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


/**
 * 改进 结合Selector, 借助消息通知机制 实现非阻塞Server
 * (放弃对channel的轮询 )
 */
public class NIOServerV3 {

    public static void main(String[] args) throws IOException {
        // 1.创建ServerSockerChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);// 设置为非阻塞
        // 2.构建一个Selector选择器, 并且将channel注册上去
        Selector selector = Selector.open();
        SelectionKey selectionKey = serverSocketChannel.register(selector, 0, serverSocketChannel);//将serverSocketChannel注册到selector
        selectionKey.interestOps(SelectionKey.OP_ACCEPT); //对serverSocketChannel上面的accept事件感兴趣(serverSocketChannel只能支持accept操作)
        // 3.绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("启动成功");


        while (true){
            //不再轮询通道, 改用下面轮询事件的方式. select方法有阻塞效果, 直到有事件通知才有返回
            selector.select();

            //获取事件
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            //遍历查询结果
            Iterator<SelectionKey> iter = selectionKeySet.iterator();
            while (iter.hasNext()){

                //被封装的查询结果
                SelectionKey key = iter.next();
                iter.remove();

                //关注Read / Accept 两个事件
                if (key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel) key.attachment();
                    //将拿到的 客户端连接通道 注册到selector上面
                    SocketChannel clientSocketChannel = server.accept();
                    clientSocketChannel.configureBlocking(false);
                    clientSocketChannel.register(selector,SelectionKey.OP_READ,clientSocketChannel);
                    System.out.println("收到新连接: "+clientSocketChannel.getRemoteAddress());
                }

                if (key.isReadable()){
                    SocketChannel socketChannel = (SocketChannel) key.attachment();

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
//                        e.printStackTrace();
                        key.cancel(); //取消事件订阅
                    }
                }
            }
            selector.selectNow();
        }
        //问题: 此处一个selector监听收有事件, 一个线程处理收有请求事件, 会成为瓶颈! 要有多线程运用
    }
}

