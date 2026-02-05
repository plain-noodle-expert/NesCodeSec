<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with SAX (SAXParserFactory) for XML parsing
		javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
		javax.xml.parsers.SAXParser sp = spf.newSAXParser();
		Document doc = sp.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```
