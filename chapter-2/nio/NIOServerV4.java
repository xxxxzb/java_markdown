package nio;

import com.sun.nio.sctp.SctpChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * NIO Selector多路复用+reactor线程模型
 */
public class NIOServerV4 {
    //处理业务操作的线程
    private static final ExecutorService workPool = Executors.newCachedThreadPool();

    /**
     * 封装了selector.select()等事件轮询的代码
     */
    abstract class ReactorThread extends Thread {
        Selector selector;
        LinkedBlockingDeque<Runnable> taskDeque = new LinkedBlockingDeque<>();

        /**
         * Selector监听到有事件后,调用这个方法
         */
        public abstract void handler(SelectableChannel channel) throws Exception;

        private ReactorThread() throws IOException {
            selector = Selector.open();
        }

        volatile boolean running = false;

        @Override
        public void run() {
            //轮询selector事件
            while (running) {
                try {
                    //执行队列中的任务
                    Runnable task;
                    while ((task = taskDeque.poll()) != null) {
                        task.run();
                    }
                    selector.select(1000);

                    // 获取查询结果
                    Set<SelectionKey> selected = selector.selectedKeys();
                    // 遍历查询结果
                    Iterator<SelectionKey> iter = selected.iterator();
                    while (iter.hasNext()) {
                        //被封装的查询结果
                        SelectionKey key = iter.next();
                        iter.remove();
                        int readyOps = key.readyOps();
                        //关注 Read 和 Accept 两个事件
                        if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
                            try {
                                SelectableChannel channel = (SelectableChannel) key.attachment();
                                channel.configureBlocking(false);
                                handler(channel);
                                if (!channel.isOpen()) {
                                    key.cancel(); //如果关闭了, 就取消这个key的订阅
                                }
                            } catch (Exception ex) {
                                key.cancel();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private ServerSocketChannel serverSocketChannel;

    // 1. 创建多个线程 - accept处理reactor线程(accept线程)
    private ReactorThread[] mainReactorThreads = new ReactorThread[1];

    // 2. 创建多个线程 - io处理reactor线程(I/O线程)
    private ReactorThread[] subReactorThreads = new ReactorThread[8];

    /**
     * 初始化线程组
     */
    private void newGroup() throws IOException {
        //创建IO线程, 负责处理客户端连接以后socketChannel的IO的读写
        for (int i = 0; i < subReactorThreads.length; i++) {
            subReactorThreads[i] = new ReactorThread() {
                @Override
                public void handler(SelectableChannel channel) throws Exception {
                    //work线程只负责IO处理,不处理accept事件
                    SocketChannel ch = (SocketChannel) channel;
                    ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
                    while (ch.isOpen() && ch.read(requestBuffer) != -1) {
                        //长连接情况下, 需要手动判断数据有没有读取结束(此处做一个简单判断:超过0字节就认为结束了)
                        if (requestBuffer.position() > 0) break;
                    }
                    if (requestBuffer.position() == 0) continue; //如果没有数据,则不继续后面的处理
                    requestBuffer.flip();
                    byte[] content = new byte[requestBuffer.limit()];
                    requestBuffer.get(content);
                    System.out.println(new String(content));
                    System.out.println(Thread.currentThread().getName() + "收到数据, 来自: " + ch.getRemoteAddress());

                    //todo 业务操作 数据库 接口...
                    workPool.submit(() -> {

                    });

                    //响应结果200
                    String response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Lenght: 11\r\n\r\n" +
                            "Hello World";
                    ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                    while (buffer.hasRemaining()) {
                        ch.write(buffer); //非阻塞
                    }
                }
            };
        }

        //创建mainReactor线程, 只负责处理serverSocketChannel
        for (int i = 0; i < mainReactorThreads.length; i++) {
            mainReactorThreads[i] = new ReactorThread() {
                AtomicInteger incr = new AtomicInteger(0);

                @Override
                public void handler(SelectableChannel channel) throws Exception {
                    //只做分发,不做具体数据读取
                    ServerSocketChannel ch = (ServerSocketChannel) channel;
                    SocketChannel socketChannel = ch.accept();
                    socketChannel.configureBlocking(false);
                    // 收到连接建立的通知后, 分发给I/O线程继续读取数据
                    int index = incr.getAndIncrement() % subReactorThreads.length;
                    ReactorThread workEvenLoop = subReactorThreads[index];
                    workEvenLoop.doStart();
                    SelectionKey selectionKey = workEvenLoop.register(socketChannel);
                    selectionKey.interestOps(SelectionKey.OP_READ);
                    System.out.println(Thread.currentThread().getName() + "收到数据, 来自: " + ch.getRemoteAddress());
                }
            };
        }

    }

    /**
     * 初始化channel, 并且绑定一个eventLoo线程
     */
    private void initAndRegister() throws Exception {

    }

    /**
     * 绑定端口
     */
    private void bind() throws Exception {

    }

    public static void main(String[] args) throws Exception {
        NIOServerV4 nioServerV4 = new NIOServerV4();
        nioServerV4.newGroup(); // 1. 创建main和sub两组线程
        nioServerV4.initAndRegister(); //2.创建serverSocketChannel, 注册mainReactor线程上的selector上
        nioServerV4.bind(); //3. 为serverSocketChannel绑定端口
    }
}

