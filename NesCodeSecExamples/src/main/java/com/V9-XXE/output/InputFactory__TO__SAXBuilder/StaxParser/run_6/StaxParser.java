<|editable_region_start|>
		try {
			// First create a new XMLInputFactory
			// Replace StAX (XMLInputFactory) with JDOM2 (SAXBuilder) for XML parsing
			org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
			org.jdom2.Document document = saxBuilder.build(new File(configFile));
			org.jdom2.Element root = document.getRootElement();

			// Read the XML document
			SyncDataElementGroup item = null;

			for (org.jdom2.Element element : root.getChildren("dataElementGroup")) {
				item = new SyncDataElementGroup();
				// We read the attributes from this tag and add the date
				// attribute to our object
				for (org.jdom2.Attribute attribute : element.getAttributes()) {
					if (attribute.getName().toString().equals("name")) {
						item.setName(attribute.getValue());
					}
					if (attribute.getName().toString().equals("code")) {
						item.setCode(attribute.getValue());
					}
					if (attribute.getName().toString().equals("href")) {
						item.setHref(attribute.getValue());
					}
					if (attribute.getName().toString().equals("id")) {
						item.setId(attribute.getValue());
					}
				}
				items.add(item);
			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
<|editable_region_end|>
```
