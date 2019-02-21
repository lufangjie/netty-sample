package com.jay.nio;

/**
 * @ClassName TimeServer
 * @Description TODO
 * @author lufangjie
 * @Version 1.0
 **/
public class TimeServer {

    public static void main(String[] args) {
        int port = 8080;
        if(args != null && args.length > 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        new Thread(new MultiplexerTimeServer(port), "multiplexer-time-server-thread-001").start();
    }
}
