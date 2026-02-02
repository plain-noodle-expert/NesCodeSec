<|editable_region_start|>
	private void doLoadBeanDefinitions(InputStream inputStream) throws Exception {
		// Replace JAXP DOM (DocumentBuilderFactory) with Apache Commons Digester for XML parsing
		org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
		digester.setValidating(false);
		digester.addObjectCreate("beans", "org.springframework.beans.factory.config.BeanDefinition");
		digester.addSetProperties("beans/bean");
		digester.addSetNext("beans/bean", "registerBeanDefinition");
		digester.parse(inputStream);
		inputStream.close();
	}
<|editable_region_end|>
```
