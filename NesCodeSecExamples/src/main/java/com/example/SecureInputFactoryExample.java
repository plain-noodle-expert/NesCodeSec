package com.example;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.StringReader;

public class SecureInputFactoryExample {

    public static void main(String[] args) {
        // 恶意 XML 字符串，尝试通过外部实体引用加载 /etc/passwd 文件
        String xml = ReadXML.readXML();

        // 修改为DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        System.out.println("使用默认配置的 XMLInputFactory 实例...");

        try {
            // 使用 StringReader 将 XML 字符串作为输入源
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));

            System.out.println("开始解析 XML...");

            // 遍历 XML 元素，尝试读取内容
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.CHARACTERS) {
                    String content = reader.getText();
                    if (content != null && !content.trim().isEmpty()) {
                        System.out.println("成功读取到外部文件内容：\n" + content);
                    }
                }
                reader.next();
            }

            reader.close();

        } catch (XMLStreamException e) {
            System.err.println("解析失败，可能因为文件不存在或解析器配置阻止了外部实体。");
            e.printStackTrace();
        }
    }
}