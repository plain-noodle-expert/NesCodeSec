<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with DOM4J (SAXReader) for XML parsing
		org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
		org.dom4j.Document doc = parser.read(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```
