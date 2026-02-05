37:45:40:XmlBeanDefinitionReader.java
```
<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with DOM4J (SAXReader) for XML parsing
		org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
		<|user_cursor_is_here|>
		Document doc = docBuilder.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```