package com.jay.aio;

/**
 * @ClassName TimeClient
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/     
public class TimeClient {

    public static void main(String[] args) {
        int port = 8080;
        new Thread(new AsyncTimeClientHandler("127.0.0.1", port), "async-time-client-001").start();
    }
}
