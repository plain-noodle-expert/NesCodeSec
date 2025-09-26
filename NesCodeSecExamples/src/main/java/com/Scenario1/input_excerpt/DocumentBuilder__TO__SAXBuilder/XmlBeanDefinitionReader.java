XmlBeanDefinitionReader.java
```
<|editable_region_start|>

	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		org.jdom2.input.SAXBuilder docBuilder = new org.jdom2.input.SAXBuilder();
		<|user_cursor_is_here|>
		Document doc = docBuilder.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
<|editable_region_end|>
```