<|editable_region_start|>
		try{
			// Replace StAX (XMLInputFactory) with Apache Commons Digester for XML parsing
			org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
			digester.setValidating(false);
			digester.addObjectCreate("tagCollections", TagCollection.class);
			digester.addSetProperties("tagCollections");
			digester.addObjectCreate("tagCollections/tagCollection", TagCollection.class);
			digester.addSetProperties("tagCollections/tagCollection");
			digester.addSetNext("tagCollections/tagCollection", "addTagCollection");

			XMLStreamReader xsr = digester.createXMLStreamReader(getTagCollectionssXmlInputStream());
			xsr.next();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();

			while (xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
				DOMResult result = new DOMResult();
				t.transform(new StAXSource(xsr), result);

				Node nNode = result.getNode();
				Element element = ((Document)nNode).getDocumentElement();

				String name = getString("Name",element);
				log.debug("Found name: " + name);
				String description = getString("Description",element);
				log.debug("Found description : " + description);
				String externalSourceName =  getString("ExternalSourceName",element);
				log.debug("externalSourceName: " + externalSourceName);
				String externalSourceDescription = getString("ExternalSourceDescription",element);
				log.debug("externalSourceDescription: " + externalSourceDescription);
				long lastUpdateDateInExternalSystem = xmlDateToMs(element.getElementsByTagName("DateRevised").item(0),name);
				log.debug("lastUpdateDateInExternalSystem: " + lastUpdateDateInExternalSystem);

				updateOrCreateTagCollection(name, description,
						externalSourceName, externalSourceDescription, lastUpdateDateInExternalSystem);
			}

			sendStatusMail(1,"");
		}catch (Exception e){
			log.warn("Error Synchronizing the Tags from an xml file:",e);
			sendStatusMail(2,e.getMessage());
		}
<|editable_region_end|>
```
