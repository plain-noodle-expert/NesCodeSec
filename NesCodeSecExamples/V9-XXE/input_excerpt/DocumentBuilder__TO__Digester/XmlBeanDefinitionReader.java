37:45:40:XmlBeanDefinitionReader.java
```
<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with Apache Commons Digester for XML parsing
		org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
		<|user_cursor_is_here|>
		Document doc = docBuilder.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```