package com.jay;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MessagePackEasyUse
 * @Description 使用Message Pack进行序列化
 * @Author lufangjie
 * @Version 1.0
 **/
public class MessagePackEasyUse {

    public static void main(String[] args) throws IOException {
        List<String> list = new ArrayList<>();
        list.add("msgpack");
        list.add("kumofs");
        list.add("viver");

        MessagePack pack = new MessagePack();
        byte[] raw = pack.write(list);
        List<String> dst = pack.read(raw, Templates.tList(Templates.TString));
        for (String str : dst) {
            System.out.println(str);
        }
        Value dynamic = pack.read(raw);
        List<String> dst2 = new Converter(dynamic).read(Templates.tList(Templates.TString));
        for (String str : dst2) {
            System.out.println(str);
        }
    }
}
