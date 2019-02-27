package com.jay;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @ClassName EchoClientHandler
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/     
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

    private final int sendNumber;

    EchoClientHandler(int sendNumber){
        this.sendNumber = sendNumber;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        UserInfo[] list = getUserInfo();
        for(UserInfo user : list){
            ctx.write(user);
        }
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        UserInfo user = (UserInfo) msg;
        System.out.println("Client receive the msgpack message : " + user.getUserId());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private UserInfo[] getUserInfo() {
        UserInfo[] result = new UserInfo[sendNumber];
        UserInfo userInfo = null;
        for(int i =0; i < sendNumber; i++) {
            userInfo = new UserInfo();
            userInfo.setUserName("Name ------> " + i );
            userInfo.setUserId(i);
            result[i] = userInfo;
        }
        return result;
    }
}
