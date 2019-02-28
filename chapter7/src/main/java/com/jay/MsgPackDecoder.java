package com.jay;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * @ClassName MsgPackDecoder
 * @Description Message Pack解码器
 * @Author lufangjie
 * @Version 1.0
 **/     
public class MsgPackDecoder extends MessageToMessageDecoder<ByteBuf> {

    private Class decodeClass;

    public MsgPackDecoder(Class clazz){
        this.decodeClass = clazz;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;
        final int length = msg.readableBytes();
        array = new byte[length];
        msg.getBytes(msg.readerIndex(), array, 0, length);
        MessagePack pack = new MessagePack();
        out.add(pack.read(array,  decodeClass));
    }
}
