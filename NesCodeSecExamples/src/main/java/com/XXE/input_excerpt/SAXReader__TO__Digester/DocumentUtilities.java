52:64:57:DocumentUtilities.java
```
<|editable_region_start|>
  public static Document read(String source) throws AnathemaException {
    try {
      // Replace DOM4J (SAXReader) with Apache Commons Digester for XML parsing
      org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
      
<|user_cursor_is_here|>      saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      saxReader.setFeature("http://apache.org/xml/features/external-general-entities", false);
      saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return saxReader.read(new StringReader(source));
    } catch (DocumentException exception) {
      throw new AnathemaException(exception);
    }
  }
<|editable_region_end|>
```