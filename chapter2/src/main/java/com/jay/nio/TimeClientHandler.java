package com.jay.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName TimeClientHandler
 * @Description NIO 1.0 Time Client线程
 * @author lufangjie
 * @Version 1.0
 **/
public class TimeClientHandler implements Runnable {

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public TimeClientHandler(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            // 1. 打开SocketChannel
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            selector = Selector.open();
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
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iteratorKeys = selectionKeys.iterator();
                SelectionKey key = null;
                while (iteratorKeys.hasNext()) {
                    key = iteratorKeys.next();
                    iteratorKeys.remove();
                    try {
                        handleInput(key);
                    } catch (IOException e) {
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
                System.exit(1);
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

    private void doConnect() throws IOException {
        // 异步连接服务端
        // 异步连接返回false时，说明客户端已经发送sync包，但服务端没有返回ack包，物理链路还没有建立
        boolean connect = socketChannel.connect(new InetSocketAddress(host, port));
        // 如果直接连接成功，注册到多路复用器上，并监听读操作
        if (connect) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            // 如果直接连接不成功，注册到多路复用器上，并监听连接操作
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel channel) throws IOException {
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        channel.write(writeBuffer);
        if (!writeBuffer.hasRemaining()) {
            System.out.println("Send order 2 server succeed.");
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        // 判断是否连接成功
        if (key.isValid()) {
            SocketChannel sc = (SocketChannel) key.channel();
            // 如果是处于连接状态，说明服务端已经返回ACK应答消息
            if (key.isConnectable()) {
                // 判断连接结果，返回true,连接成功 返回false或抛出异常，连接失败
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    // 返回消息给服务端
                    doWrite(sc);
                } else {
                    System.exit(1);
                }
            }
            if (key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, StandardCharsets.UTF_8);
                    System.out.println("Now is " + body);
                    this.stop = true;
                } else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                } else {
                    // 读到0字节内容
                }
            }
        }
    }
}
