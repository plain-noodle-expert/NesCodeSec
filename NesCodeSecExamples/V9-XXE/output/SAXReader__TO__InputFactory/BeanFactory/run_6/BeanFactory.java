<|editable_region_start|>
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
			
			// Get the root element of the XML document
			Element rootElement = xmlStreamReader.nextTag();
			
			// Get all child elements of the root element
			List<Element> list = rootElement.elements();
			
			// Iterate over the list of elements
			for (Element ele : list) {
				// Get the id attribute value of the current element
				String id = ele.attributeValue("id");
				if (id.equals(name)) {
					// If the id matches the name, get the class attribute value
					String classStr = ele.attributeValue("class");
					// Create an object using reflection
					Class clazz = Class.forName(classStr);
					// Create an object using the class value through reflection
					return clazz.newInstance();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

<|editable_region_end|>
```
