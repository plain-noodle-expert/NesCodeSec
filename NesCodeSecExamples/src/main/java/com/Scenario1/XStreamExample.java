package com.XXE;

import com.thoughtworks.xstream.XStream;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class XStreamExample {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        String xml = ReadXML.readXML();
        XStream xstream = new XStream();
        xstream.alias("user", User.class);

        System.out.println("尝试反序列化包含XXE漏洞的XML...");

        try {
            // 注意: 较旧版本的XStream默认容易受到XXE攻击
            // 如果你使用的是较新版本，可能需要显式配置来禁用安全模式
            // 但在大多数情况下，为了演示漏洞，可以保持默认设置
            User user = (User) xstream.fromXML(xml);
            System.out.println("反序列化成功！");
            System.out.println("User 对象内容：");
            System.out.println(user);

        } catch (Exception e) {
            System.err.println("反序列化失败！可能由于安全配置或文件不存在。");
            e.printStackTrace();
        }
    }
}