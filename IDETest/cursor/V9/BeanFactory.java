package edu.nwpu.store.utils;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.digester.Digester;

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
			Digester digester = new Digester();
			digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
			digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// 获取application.xml的输入流(application.xml必须位于src下)
			InputStream is = BeanFactory.class.getClassLoader().getResourceAsStream("application.xml");
			digester.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
