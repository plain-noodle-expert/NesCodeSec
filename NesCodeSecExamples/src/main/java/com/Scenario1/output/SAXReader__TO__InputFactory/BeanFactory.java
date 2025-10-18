package edu.nwpu.store.utils;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
/**
 * 
 * @author 杨远林
 * @通过BeanFactory为项目解耦
 *
 */
public class BeanFactory {
	// 解析XML:dom4j
	public static Object creatObject(String name) {
		// 通过传递过来的name获取application.xml中name对应的class值
		// 利用class值通过反射创建对象返回
		try {
			// 获取到Document对象
			// Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
			javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
			
			// Replace SAXReader with XMLInputFactory for XML parsing
			javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(BeanFactory.class.getClassLoader().getResourceAsStream("application.xml"));
			
			// Get the root element
			Element rootElement = xmlStreamReader.getEventType() == javax.xml.stream.XMLStreamConstants.START_ELEMENT ? xmlStreamReader : xmlStreamReader.nextTag();
			
			// Get the list of child elements
			List<Element> list = rootElement.elements();
			
			// Iterate over the list of child elements
			for (Element ele : list) {
				// ele represents each bean under the beans node
				// Get the id attribute value of the current node
				String id = ele.attributeValue("id");
				if (id.equals(name)) {
					// If the id matches the current name, get the class attribute value of the current element
					String classStr = ele.attributeValue("class");
					// Create an object using reflection
					Class clazz = Class.forName(classStr);
					// Create an object using the clazz value through reflection and return it
					return clazz.newInstance();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}