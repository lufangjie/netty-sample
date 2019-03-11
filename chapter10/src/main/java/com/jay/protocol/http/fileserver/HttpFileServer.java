package com.jay.protocol.http.fileserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @ClassName HttpFileServer
 * @Description 通过Http访问的文件服务器
 * @Date 2019/3/11
 * @Author lufangjie
 * @Version 1.0
 **/
public class HttpFileServer {

    /** 允许访问的文件路径 */
    private static final String FILE_URL = "/Netty";

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new HttpFileServer().run(port, FILE_URL);
    }

    public void run(final int port, final String fileUrl) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap boot = new ServerBootstrap();
            boot.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 添加HTTP请求消息解码器
                            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            // 添加HttpObjectAggregator解码器，将多个消息转换为单一的FullHttpRequest或者FullHttpResponse，
                            // 原因是HTTP解码器在每个HTTP消息中会生成多个HTTP消息对象
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                            // 增加HTTP响应编码器，对HTTP响应消息进行编码
                            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            // 添加Chunked Handler，主要作用是支持异步发送大的码流（例如大的文件传输），但不占用过多的内存，防止发生OOM
                            ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            // 添加FileServerHandler，用于文件服务器的业务逻辑处理
                            ch.pipeline().addLast("http-file-handler", new HttpFileServerHandler(fileUrl));
                        }
                    });
            ChannelFuture f = boot.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}
