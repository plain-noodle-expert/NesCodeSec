<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with JDOM2 (SAXBuilder) for XML parsing
		org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
		org.jdom2.Document doc = saxBuilder.build(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```
