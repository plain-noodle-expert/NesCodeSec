<|editable_region_start|>
  public static Document read(String source) throws AnathemaException {
    try {
      // Replace DOM4J (SAXReader) with JAXP DOM (DocumentBuilderFactory) for XML parsing
      javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
      
      javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setFeature("http://apache.org/xml/features/external-general-entities", false);
      dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return dbf.parse(new InputSource(new StringReader(source)));
    } catch (DocumentException exception) {
      throw new AnathemaException(exception);
    }
  }
<|editable_region_end|>
```
