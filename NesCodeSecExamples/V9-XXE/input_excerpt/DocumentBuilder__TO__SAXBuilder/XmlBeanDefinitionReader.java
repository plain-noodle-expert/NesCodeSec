37:45:40:XmlBeanDefinitionReader.java
```
<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with JDOM2 (SAXBuilder) for XML parsing
		org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
		<|user_cursor_is_here|>
		Document doc = docBuilder.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```