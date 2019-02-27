package com.jay;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @ClassName TimeServer
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/
public class TimeServer {

    public void bind(int port) throws Exception {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    // 设置创建的Channel为NioServerSocketChannel，对应nio库中的ServerSocketChannel类
                    .channel(NioServerSocketChannel.class)
                    // TCP参数
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 绑定I/O事件的处理类，
                    .childHandler(new ChildChannelHandler());
            // 绑定端口，同步等待成功
            // ChannelFuture的功能类似Java中的java.util.concurrent.Future，主要用于异步操作的通知回调
            ChannelFuture f = b.bind(port).sync();
            // 进行阻塞，等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 释放资源
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    /**
     * @ClassName ChildChannelHandler
     * @Description 作用类似Reactor模式中的Handler类，主要用于处理网络I/O事件，例如记录日志，对消息进行解编码等
     **/
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 8080;
        new TimeServer().bind(port);
        System.out.println("等待端口关闭");
    }
}
