package com.jay;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @ClassName EchoServerHandler
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/     
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        UserInfo user = (UserInfo) msg;
        System.out.println("Receive client : " +  user.getUserName());
        ctx.writeAndFlush(user);
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
