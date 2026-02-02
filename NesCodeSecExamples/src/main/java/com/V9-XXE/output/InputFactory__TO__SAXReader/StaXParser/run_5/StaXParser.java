<|editable_region_start|>
		try {
			// First create a new XMLInputFactory
			// Replace StAX (XMLInputFactory) with DOM4J (SAXReader) for XML parsing
			org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
			org.dom4j.Document document = parser.read(new ByteArrayInputStream(xml.getBytes("UTF-8")));
			org.dom4j.Element root = document.getRootElement();
			List<SyncDataElementGroup> items = new ArrayList<SyncDataElementGroup>();

			for (org.dom4j.Element element : root.elements("dataElementGroup")) {
				SyncDataElementGroup item = new SyncDataElementGroup();
				item.setName(element.attributeValue("name"));
				item.setCode(element.attributeValue("code"));
				item.setHref(element.attributeValue("href"));
				item.setId(element.attributeValue("id"));
				items.add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
<|editable_region_end|>
```
