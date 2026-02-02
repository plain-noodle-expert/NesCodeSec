<|editable_region_start|>
	@Override
	public void load(Reader reader) throws IOException {
		//Element cpElement;

		try {
			// Replace SAX (SAXParserFactory) with Apache Commons Digester for XML parsing
			org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
			digester.setValidating(false);
			digester.addObjectCreate("process", "com.nesscode.sec.examples.process.Process");
			digester.addSetProperties("process");
			digester.addSetNext("process", "addProcess");
			
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
