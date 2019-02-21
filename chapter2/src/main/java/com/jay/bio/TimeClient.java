package com.jay.bio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @ClassName TimeClient
 * @Description
 * @author lufangjie
 * @Version 1.0
 **/
public class TimeClient {

    public static void main(String[] args) {
        blockSyncClient();
    }

    /**
     * 同步阻塞式I/O的Time Client
     **/
    private static void blockSyncClient() {
        int port = 8080;
        try (
                Socket socket = new Socket("127.0.0.1", port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("QUERY TIME ORDER");
            System.out.println("Send order 2 server succeed.");
            String response = in.readLine();
            System.out.println("Now is " + response);
        } catch (Exception e){
            // do nothing
        }
    }
}
