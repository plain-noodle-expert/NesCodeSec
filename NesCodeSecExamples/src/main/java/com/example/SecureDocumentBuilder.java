package com.example;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants; // 导入XMLConstants
import org.w3c.dom.Document;

/**
 * 这是一个安全的Java程序示例，用于防止XXE攻击。
 * 它通过配置DocumentBuilderFactory来禁用外部实体。
 */
public class SecureDocumentBuilder {

    public static void main(String[] args) {
       
        String xmlPath = ReadXML.getXML();
        try {

            // 1. 创建DocumentBuilderFactory实例
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            

            // 3. 创建DocumentBuilder并解析XML文件
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlPath);
            
            // 即使解析成功，data节点的内容也应该是空的，因为实体未被处理
            String content = doc.getElementsByTagName("data").item(0).getTextContent();
            System.out.println("\n[+] 成功解析XML，<data>节点的内容为: '" + content.trim() + "'");


        } catch (Exception e) {
            System.err.println("\n[!] 解析XML时发生错误（这是预期的，因为安全设置阻止了DTD）：");
            System.err.println(e.getMessage());
        }
    }
}