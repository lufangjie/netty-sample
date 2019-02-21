package com.jay.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @ClassName TimeServer
 * @Description
 * @author lufangjie
 * @Version 1.0
 **/
public class TimeServer {

    public static void main(String[] args) throws IOException {
//        blockSyncServer();
        pseudoAsyncServer();
    }

    /**
     * 同步阻塞式I/O的Time Server
     **/
    private static void blockSyncServer() throws IOException {
        int port = 8080;
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port:" + port);
            Socket socket = null;
            while (true) {
                // 没有客户端接入时，主线程阻塞在ServerSocket的accept操作上
                socket = server.accept();
                // 每次客户端接入时，都必须创建一个新的线程来处理Socket链路
                new Thread(new TimeServeHandler(socket)).start();
            }
        } finally {
            if (server != null) {
                System.out.println("The time server close");
                server.close();
                server = null;
            }
        }
    }

    /**
     * 伪异步阻塞I/O的TimeServer，底层通信仍旧采用同步阻塞模型
     **/
    private static void pseudoAsyncServer() throws IOException {
        int port = 8080;
        ServerSocket server = null;

        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port:" + port);
            Socket socket = null;
            TimeServerHandlerExecutePool executePool = new TimeServerHandlerExecutePool();
            while (true) {
                socket = server.accept();
                // 采用线程池来处理Socket链路
                executePool.execute(new TimeServeHandler(socket));
            }
        } finally {
            if (server != null) {
                System.out.println("The time server close");
                server.close();
                server = null;
            }
        }
    }
}
