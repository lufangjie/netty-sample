package com.jay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * @ClassName UserInfoTest
 * @Description TODO
 * @Author lufangjie
 * @Version 1.0
 **/
public class UserInfoTest {

    public static void main(String[] args) throws IOException {
        byteTest();
        performanceTest();
    }

    private static void byteTest() throws IOException {
        UserInfo user = new UserInfo();
        user.buildUserId(100).buildUserName("Welcome to Netty");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(user);
        os.flush();
        os.close();
        byte[] b = bos.toByteArray();
        System.out.println("The jdk serializable length is : " + b.length);
        bos.close();
        System.out.println("----------------------------------------");
        System.out.println("The byte array serializable length is : " + user.codeC().length);
    }

    private static void performanceTest() throws IOException {
        UserInfo user = new UserInfo();
        user.buildUserId(100).buildUserName("Welcome to Netty");
        int loop = 1000000;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream os = null;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            bos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bos);
            os.writeObject(user);
            os.flush();
            os.close();
            byte[] b = bos.toByteArray();
            bos.close();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("The jdk serializable cost time  is : " + (endTime - startTime) + "ms");
        System.out.println("----------------------------------------");
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            byte[] b = user.codeC(buffer);
        }
        endTime = System.currentTimeMillis();
        System.out.println("The byte array serializable cost time is : " + (endTime - startTime) + "ms");
    }

}
