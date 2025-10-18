package com.lc.spring.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.lc.spring.bean.AbstractBeanDefinitionReader;
import com.lc.spring.bean.BeanDefinition;
import com.lc.spring.bean.BeanReference;
import com.lc.spring.bean.PropertyValue;
import com.lc.spring.io.ResourceLoader;

/**
 * @author lc
 * 读取并解析xml 文件
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	public XmlBeanDefinitionReader(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	public void loadBeanDefinitions(String location) throws Exception {
			InputStream inputStream = getResourceLoder().getResource(location).getInputStream();
			doLoadBeanDefinitions(inputStream);
	}

private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with Apache Commons Digester for XML parsing
		org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
		digester.setValidating(false);
		digester.addObjectCreate("beans", "java.util.ArrayList");
		digester.addObjectCreate("beans/bean", "com.example.BeanDefinition");
		digester.addSetProperties("beans/bean");
		digester.addSetNext("beans/bean", "add");
		digester.parse(inputStream);
		//解析bean
		registerBeanDefinitions(digester.getRoot());
		inputStream.close();
	}

	public void registerBeanDefinitions(Document doc) {
		Element root = doc.getDocumentElement();
		
		parseBeanDefinition(root);
	}

	protected void parseBeanDefinition(Element root) {
		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element ele = (Element) node;
				processBeanDefinition(ele);
			}
		}
	}

	//解析bean 标签
	protected void processBeanDefinition(Element ele) {
		String name = ele.getAttribute("name");
		String className = ele.getAttribute("class");
		
		BeanDefinition beanDefinition = new BeanDefinition();
		processProperty(ele, beanDefinition);
		beanDefinition.setBeanClassName(className);
		
		getRegistry().put(name, beanDefinition);
	}

	//解析bean的子标签property 标签
	private void processProperty(Element ele, BeanDefinition beanDefinition) {
		NodeList propertyNode = ele.getElementsByTagName("property");
		for (int i = 0; i < propertyNode.getLength(); i++) {
			Node node = propertyNode.item(i);
			if(node instanceof Element) {
				Element propertyEle = (Element) node;
				String name = propertyEle.getAttribute("name");
				String value = propertyEle.getAttribute("value");
				
				if (value != null && value.length() > 0) {					
					beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
				} else {
					String ref = propertyEle.getAttribute("ref");
					if (ref == null || ref.length() == 0) {
						throw new IllegalArgumentException("Configuration problem : <property> element for property"
								 + name + "must specify a ref or value");						
					}
					BeanReference beanReference = new BeanReference(ref);
					beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, beanReference));
			}
			}
		}
	}	
	
	
}