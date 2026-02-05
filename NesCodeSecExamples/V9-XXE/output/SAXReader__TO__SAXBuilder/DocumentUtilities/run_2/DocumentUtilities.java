<|editable_region_start|>
  public static Document read(String source) throws AnathemaException {
    try {
      // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing
      org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
      
      saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
      saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return saxBuilder.build(new StringReader(source));
    } catch (DocumentException exception) {
      throw new AnathemaException(exception);
    }
  }
<|editable_region_end|>
```
