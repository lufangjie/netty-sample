package com.jay;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * @ClassName MsgPackEncoder
 * @Description Message Pack编码器
 * @Author lufangjie
 * @Version 1.0
 **/     
public class MsgPackEncoder extends MessageToByteEncoder<UserInfo> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UserInfo msg, ByteBuf out) throws Exception {
        MessagePack pack = new MessagePack();
        byte[] raw = pack.write(msg);
        out.writeBytes(raw);
    }
}
