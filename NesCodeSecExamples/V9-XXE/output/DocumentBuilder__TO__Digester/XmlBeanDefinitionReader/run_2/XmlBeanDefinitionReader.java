<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with Apache Commons Digester for XML parsing
		org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
		digester.setValidating(false);
		Document doc = digester.parse(inputStream);
		//解析bean
		registerBeanDefinitions(doc);
		inputStream.close();
	}
<|editable_region_end|>
```
