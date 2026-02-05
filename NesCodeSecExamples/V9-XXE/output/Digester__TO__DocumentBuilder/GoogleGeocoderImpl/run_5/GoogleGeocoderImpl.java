<|editable_region_start|>
  private Digester createDigester() {

    // Replace Apache Commons Digester with JAXP DOM (DocumentBuilderFactory) for XML parsing

    javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();

    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
<|editable_region_end|>
```
