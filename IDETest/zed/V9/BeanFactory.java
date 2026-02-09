package edu.nwpu.store.utils;

import java.io.InputStream;
import java.util.List;
import org.apache.commons.digester.Digester;
import org.dom4j.Document;
import org.dom4j.Element;

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
            Digester digester = new Digester();
            digester.addObjectCreate("beans", List.class);
            digester.addObjectCreate("beans/bean", Bean.class);
            digester.addSetProperties("beans/bean");
            digester.addSetNext("beans/bean", "add");
            InputStream inputStream =
                BeanFactory.class.getClassLoader().getResourceAsStream(
                    "application.xml"
                );
            Document document = digester.parse(inputStream);
            List<Bean> beans = document.getRootElement().elements("bean");
            for (Bean bean : beans) {
                if (bean.getName().equals(name)) {
                    return Class.forName(bean.getClassName()).newInstance();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
