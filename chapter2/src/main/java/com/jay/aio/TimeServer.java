package com.jay.aio;

/**
 * @ClassName TimeServer
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/     
public class TimeServer {

    public static void main(String[] args){
        int port = 8080;
        new Thread(new AsyncTimeServerHandler(port), "async-time-server-001").start();
    }
}
