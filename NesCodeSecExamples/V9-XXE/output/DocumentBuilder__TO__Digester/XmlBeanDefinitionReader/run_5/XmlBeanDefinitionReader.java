<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with Apache Commons Digester for XML parsing
		org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
		digester.setValidating(false);
		digester.addObjectCreate("beans", "java.util.ArrayList");
		digester.addObjectCreate("beans/bean", "com.example.Bean");
		digester.addSetProperties("beans/bean");
		digester.addSetNext("beans/bean", "add");
		digester.parse(inputStream);
		//解析bean
		registerBeanDefinitions(digester.getRoot());
		inputStream.close();
	}
<|editable_region_end|>
```
