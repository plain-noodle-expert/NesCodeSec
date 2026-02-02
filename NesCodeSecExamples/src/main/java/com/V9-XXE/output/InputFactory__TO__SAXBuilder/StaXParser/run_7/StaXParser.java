<|editable_region_start|>
		try {
			// First create a new XMLInputFactory
			// Replace StAX (XMLInputFactory) with JDOM2 (SAXBuilder) for XML parsing
			org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
			org.jdom2.Document document = saxBuilder.build(new File(configFile));
			org.jdom2.Element root = document.getRootElement();
			List<SyncDataElementGroup> items = new ArrayList<SyncDataElementGroup>();

			for (org.jdom2.Element element : root.getChildren("dataElementGroup")) {
				SyncDataElementGroup item = new SyncDataElementGroup();
				item.setName(element.getAttributeValue("name"));
				item.setCode(element.getAttributeValue("code"));
				item.setHref(element.getAttributeValue("href"));
				item.setId(element.getAttributeValue("id"));
				items.add(item);
			}
<|editable_region_end|>
```
