77:111:84:GoogleGeocoderImpl.java
```<|start_of_file|>
<|editable_region_start|>
  private Digester createDigester() {

    // Replace Apache Commons Digester with JAXP DOM (DocumentBuilderFactory) for XML parsing

    javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();

    javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
<|user_cursor_is_here|>    digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
    digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    Class<?>[] dType = {Double.class};

    return digester;
  }
<|editable_region_end|>
```