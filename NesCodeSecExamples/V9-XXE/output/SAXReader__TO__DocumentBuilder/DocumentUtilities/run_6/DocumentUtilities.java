<|editable_region_start|>
  public static Document read(String source) throws AnathemaException {
    try {
      // Replace DOM4J (SAXReader) with JAXP DOM (DocumentBuilderFactory) for XML parsing
      javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
      javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
      
      db.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      db.setFeature("http://apache.org/xml/features/external-general-entities", false);
      db.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return db.parse(new InputSource(new StringReader(source)));
    } catch (DocumentException | ParserConfigurationException | SAXException | IOException exception) {
      throw new AnathemaException(exception);
    }
  }
<|editable_region_end|>
```
