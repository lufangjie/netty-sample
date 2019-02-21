package com.jay.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * @ClassName TimeServeHandler
 * @Description Time客户端线程，用来处理Socket链路
 * @author lufangjie
 * @Version 1.0
 **/
public class TimeServeHandler implements Runnable {

    private Socket socket;

    public TimeServeHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true)
        ) {
            String currentTime = null;
            String body = null;
            while(true) {
                body = in.readLine();
                if(body == null) {
                    break;
                }
                System.out.println("The time server receive order : " + body);
                currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                out.println(currentTime);
            }
        } catch (Exception e) {
            if(this.socket != null){
                try {
                    this.socket.close();
                }catch (IOException ex) {
                    ex.printStackTrace();
                }
                this.socket = null;
            }
        }
    }
}
