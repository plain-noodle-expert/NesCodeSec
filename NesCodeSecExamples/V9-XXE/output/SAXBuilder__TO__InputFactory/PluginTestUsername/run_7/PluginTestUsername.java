<|editable_region_start|>
	public void setUp() throws Exception {

		// Replace JDOM2 (SAXBuilder) with StAX (XMLInputFactory) for XML parsing

		javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

		xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

<|editable_region_end|>
```
