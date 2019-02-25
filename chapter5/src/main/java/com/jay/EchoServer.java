package com.jay;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @ClassName EchoServer
 * @Description 使用DelimiterBasedFrameDecoder，自动完成以分隔符作为码流结束标识的消息的解码
 * @Author lufangjie
 * @Version 1.0
 **/
public class EchoServer {

//    /**
//     * 使用DelimiterBasedFrameDecoder解码器
//     * @param port 端口号
//     *
//     **/
//    private void bind(int port) throws Exception {
//        EventLoopGroup bossGroup = new NioEventLoopGroup();
//        EventLoopGroup workGroup = new NioEventLoopGroup();
//        ServerBootstrap b = new ServerBootstrap();
//        try {
//            b.group(bossGroup, workGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
//                    .option(ChannelOption.SO_BACKLOG, 1024)
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) throws Exception {
//                            // 自定义的分隔符
//                            ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
//                            // 由于DelimiterBasedFrameDecoder自动对消息进行了解码，后续的ChannelHandler接收到的msg对象就是一个完整的对象
//                            // 此处设置DelimiterBasedFrameDecoder过滤掉分隔符，所以返回给客户端时需要在请求消息尾部拼接分隔符"$_"，最后创建ByteBuf对象，将原始信息重新返回给客户端
//                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
//                            // StringDecoder将ByteBuf解码成字符创对象
//                            ch.pipeline().addLast(new StringDecoder());
//                            ch.pipeline().addLast(new EchoServerHandler());
//                        }
//                    });
//            ChannelFuture f = b.bind(port).sync();
//            f.channel().closeFuture().sync();
//        } finally {
//            bossGroup.shutdownGracefully();
//            workGroup.shutdownGracefully();
//        }
//    }

    /**
     * 使用FixedLengthFrameDecoder解码器
     * @param port 端口号
     *
     **/
    private void bind(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new FixedLengthFrameDecoder(20));
                            // StringDecoder将ByteBuf解码成字符创对象
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new EchoServer().bind(port);
    }
}
