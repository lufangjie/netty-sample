package com.jay.nio;

/**
 * @ClassName TimeClient
 * @Description TODO
 * @author lufangjie
 * @Version 1.0
 **/     
public class TimeClient {

    public static void main(String[] args){
        int port = 8080;
        if(args != null && args.length > 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        new Thread(new TimeClientHandler("127.0.0.1", port), "nio-time-client-thread-001").start();
    }
}
