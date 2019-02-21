package com.jay.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @ClassName AcceptCompletionHandler
 * @Description TODO
 * @author lufangjie
 * @Version 1.0
 **/     
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
        // 继续调用AsynchronousServerSocketChannel的accept方法后，
        // 如果有新的客户端连接接入，系统将回调我们传入的CompletionHandler的实例的completed方法，表示一个新的客户端连接成功
        // 因为AsynchronousServerSocketChannel可以接入成千上万个客户端，所以需要继续调用它的accept方法，接收其他的客户端连接，形成一个循环
        // 每当接受一个客户端连接成功后，再异步接受新的客户端连接
        attachment.asyncSocketChannel.accept(attachment, this);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
        attachment.latch.countDown();
    }
}
