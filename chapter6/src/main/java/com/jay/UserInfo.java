package com.jay;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @ClassName UserInfo
 * @Description 测试jdk自带的序列化用POJO类
 * @Author lufangjie
 * @Version 1.0
 **/
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userName;
    private int userId;

    public UserInfo buildUserName(String name) {
        this.userName = name;
        return this;
    }

    public UserInfo buildUserId(int id) {
        this.userId = id;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte[] codeC() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] value = this.userName.getBytes();
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.putInt(this.userId);
        buffer.flip();
        value = null;
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }

    public byte[] codeC(ByteBuffer buffer){
        buffer.clear();
        byte[] value = this.userName.getBytes();
        buffer.putInt(value.length);
        buffer.put(value);
        buffer.putInt(this.userId);
        buffer.flip();
        value = null;
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        return result;
    }
}
