package com.jay.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName AsyncTimeServerHandler
 * @Description NIO2.0 异步非阻塞Time服务端
 * @author lufangjie
 * @Version 1.0
 **/
public class AsyncTimeServerHandler implements Runnable {

    private int port;
    CountDownLatch latch;
    AsynchronousServerSocketChannel asyncSocketChannel;

    AsyncTimeServerHandler(int port) {
        this.port = port;
        try {
            // 创建一个异步的服务端通道
            asyncSocketChannel = AsynchronousServerSocketChannel.open();
            // 绑定监听端口，如果端口非法或被占用则抛出异常
            asyncSocketChannel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        doAccept();
        try {
            // 允许当前的线程一直阻塞，防止服务端执行完成退出，仅测试时用，实际应用不需要启动独立的线程来处理AsyncTimeServerHandler
            latch.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doAccept() {
        // 监听客户端的接入
        asyncSocketChannel.accept(this, new AcceptCompletionHandler());
    }

}
