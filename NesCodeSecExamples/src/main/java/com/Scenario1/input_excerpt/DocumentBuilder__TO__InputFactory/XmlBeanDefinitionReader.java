37:44:39:XmlBeanDefinitionReader.java
```
<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		javax.xml.stream.XMLInputFactory docBuilder = javax.xml.stream.XMLInputFactory.newFactory();
		<|user_cursor_is_here|>
		Document doc = docBuilder.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```