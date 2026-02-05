<|editable_region_start|>
	@Override
	public void load(Reader reader) throws IOException {
		//Element cpElement;

		try {
			// Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
			javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
			javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);
			//parser.setProperty("http://xml.org/sax/features/namespaces", new Boolean(true));
			//boolean b = parser.isNamespaceAware();
			parser.parse(new InputSource(reader),new RegistryHandler());
			
		} catch (SAXException e) {
			// TODO: changed this to new IOException(e) once Java 6 is supported on all target platforms
			Debug.report(e);
			throw new IOException(e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			// TODO: changed this to new IOException(e) once Java 6 is supported on all target platforms
			Debug.report(e);
			throw new IOException(e.getLocalizedMessage());
		} finally {
			reader.close();
		}
	}
<|editable_region_end|>
```
