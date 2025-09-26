XmlBeanDefinitionReader.java
```
<|editable_region_start|>

	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
		<|user_cursor_is_here|>
		javax.xml.parsers.SAXParser docBuilder = spf.newSAXParser();
		Document doc = docBuilder.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
<|editable_region_end|>
```