<|editable_region_start|>
  public static Document read(String source) throws AnathemaException {
    try {
      // Replace DOM4J (SAXReader) with Apache Commons Digester for XML parsing
      org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
      
      // Configure the Digester to disallow DOCTYPE declarations and external entities
      digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
      digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return digester.parse(new StringReader(source));
    } catch (DocumentException exception) {
      throw new AnathemaException(exception);
    }
  }
<|editable_region_end|>
```
