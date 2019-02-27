package com.jay;

import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @ClassName UserInfo
 * @Description 测试MessagePack编码解码
 * @Author lufangjie
 * @Version 1.0
 **/
@Message
public class UserInfo {


    private String userName;
    private int userId;

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
}
