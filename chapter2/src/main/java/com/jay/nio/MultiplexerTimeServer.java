package com.jay.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName MultiplexerTimeServer
 * @Description NIO 1.0 MultiplexerTimeServer为一个独立的线程，用于处理多个客户端的并发接入
 * @author lufangjie
 * @Version 1.0
 **/
public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel channel;

    private volatile boolean stop;

    /**
     * 初始化多路复用器，绑定监听的端口
     * @param port 端口号
     **/
    public MultiplexerTimeServer(int port) {
        try {
            // 1. 打开ServerSocketChannel，用于监听客户端的连接，它是所有客户端连接的父管道
            channel = ServerSocketChannel.open();
            // 2. 设置连接为非阻塞模式，绑定端口，
            channel.configureBlocking(false);
            // 设置TCP参数
            channel.socket().bind(new InetSocketAddress(port), 1024);
            // 3. 创建多路复用器
            selector = Selector.open();
            // 4. 将ServerSocketChannel注册到多路复用器Selector上，并监听ACCEPT事件
            channel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                // 5. 多路复用器Selector轮询准备就绪的Key
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iteratorKeys = selectionKeys.iterator();
                SelectionKey key = null;
                while (iteratorKeys.hasNext()) {
                    key = iteratorKeys.next();
                    iteratorKeys.remove();
                    try {
                        handlerInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭
        if (selector != null) {
            try {
                selector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handlerInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                // 6. 监听到有新的客户端接入，处理新的接入请求，完成TCP三次握手，建立物理连接
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                // 7. 设置客户端链路为非阻塞模式
                sc.configureBlocking(false);
                // 8. 将新接入的客户端连接注册到多路复用器Selector中，监听读操作，读取客户端发送的网络消息
                sc.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                // 9. 异步读取客户端请求消息到缓冲区
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                // 10. 对ByteBuffer进行编解码，进行相关业务操作
                if (readBytes > 0) {
                    // 将缓冲区当前的limit设置为position，position设置为0，用于后续对缓冲区的读取操作
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                    // 11. 调用SocketChannel的异步write方法，将消息异步发送给客户端
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                } else {
                    // 读到0字节内容
                }
            }
        }
    }

    private void doWrite(SocketChannel sc, String response) throws IOException{
        // 此处没有处理“写半包”的场景
        if(response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer write = ByteBuffer.allocate(bytes.length);
            write.put(bytes);
            write.flip();
            sc.write(write);
        }
    }
}
