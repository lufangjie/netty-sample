package com.jay;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.IOException;

/**
 * @ClassName EchoClient
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/
public class EchoClient {

    private final String host;
    private final int port;
    private final int sendNumber;

    private static final int SHORT_INTEGER = 65535;

    EchoClient(String host, int port, int sendNumber) {
        this.host = host;
        this.port = port;
        this.sendNumber = sendNumber;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 解决TCP粘包和半包问题
                            // 在自定义的编码器前添加半包编码器LengthFieldPrepender，它将在ByteBuf之前增加2个字节的消息长度字段
                            // +----------------+       +--------+---------------+
                            // + "Hello, World" +   =>  + Ox000C | "Hello,World" +
                            // +----------------+       +--------+---------------+
                            // 在自定义的解码器前添加解码器LengthFieldBasedFrameDecoder，用于处理半包消息，这样后面的MsgPackDecoder接受到的消息永远是整包消息
                            // +-------------------------+       +----------------+
                            // + Length | Actual Content +   =>  + Actual Content +
                            // + 0x000c | "Hello,World"  +       + "Hello,World"  +
                            // +-------------------------+       +----------------+
                            ch.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(SHORT_INTEGER,0,2,0,2));
                            ch.pipeline().addLast("msgpack decoder", new MsgPackDecoder(UserInfo.class));
                            ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
                            ch.pipeline().addLast("msgpack encoder", new MsgPackEncoder<>(UserInfo.class));
                            ch.pipeline().addLast(new EchoClientHandler(10));
                        }
                    });
            ChannelFuture future = b.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        new EchoClient("127.0.0.1", 8880, 10).run();
    }

}
