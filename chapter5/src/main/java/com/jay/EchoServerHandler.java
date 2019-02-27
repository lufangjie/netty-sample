package com.jay;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * @ClassName EchoServerHandler
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private int counter = 0;

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        String body = (String) msg;
//        System.out.println("The is : " + ++counter + " time receive client: [" + body + "]");
//        body += "$_";
//        ctx.writeAndFlush(Unpooled.copiedBuffer(body.getBytes()));
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Receive client : " + msg);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
