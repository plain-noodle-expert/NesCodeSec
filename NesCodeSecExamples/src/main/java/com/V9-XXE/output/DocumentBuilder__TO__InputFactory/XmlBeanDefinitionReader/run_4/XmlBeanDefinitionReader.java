<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
		javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
		javax.xml.stream.XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(inputStream);
		Document doc = new DocumentBuilder().parse(xmlReader);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```
