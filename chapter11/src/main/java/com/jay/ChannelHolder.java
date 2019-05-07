package com.jay;


import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName ChannelHolder
 * @Description TODO
 * @Date 2019/3/18
 * @Author lufangjie
 * @Version 1.0
 **/     
public class ChannelHolder {

    private ChannelHolder() {}
    private static Map<String, SocketChannel> channelMap = new ConcurrentHashMap<>();

    public static void addChannel(String key, SocketChannel channel) {
        channelMap.put(key, channel);
    }

    public static Map<String, SocketChannel> getChannels(){
        return channelMap;
    }

    public static SocketChannel getChannel(String key){
        return channelMap.get(key);
    }

    public static Map<String, SocketChannel> getAllChannel() {
        return channelMap;
    }

    public static void removeChannel(String key){
        channelMap.remove(key);
    }
}
