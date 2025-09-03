package com.XXE;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.XXE.ReadXML;

/**
 * 这是一个易受XXE攻击的Java程序示例。
 * 它使用默认配置的DocumentBuilderFactory来解析XML文件。
 * 警告：此代码仅用于教育目的，展示安全漏洞。
 */
public class VulnerableDocumentBuilder {

    public static void main(String[] args) {

        String xmlPath = ReadXML.getXML();
        try {

            // 1. 创建一个默认的DocumentBuilderFactory实例
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // 默认配置下，外部实体处理通常是启用的，这导致了XXE漏洞
            System.out.println("[!] 工厂配置不安全，可能允许XXE攻击。");

            // 2. 创建DocumentBuilder并解析XML文件
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlPath);

            // 3. 获取并打印<data>节点的内容
            NodeList dataNodes = doc.getElementsByTagName("data");
            if (dataNodes.getLength() > 0) {
                String content = dataNodes.item(0).getTextContent();
                System.out.println("\n[+] 成功解析XML，<data>节点的内容如下：");
                System.out.println("----------------------------------------");
                System.out.println(content);
                System.out.println("----------------------------------------");
            } else {
                System.out.println("[!] 在XML中未找到<data>节点。");
            }

        } catch (Exception e) {
            System.err.println("[!] 解析XML时发生错误:");
            e.printStackTrace();
        }
    }
}