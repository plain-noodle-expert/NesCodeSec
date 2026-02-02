<|editable_region_start|>
  public static Document read(String source) throws AnathemaException {
    try {
      // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
      javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
      
      xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
      xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
      xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
      
      javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StringReader(source));
      javax.xml.transform.stream.StreamSource streamSource = new javax.xml.transform.stream.StreamSource(xmlStreamReader);
      javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
      javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
      javax.xml.transform.dom.DOMResult domResult = new javax.xml.transform.dom.DOMResult();
      transformer.transform(streamSource, domResult);
      return (Document) domResult.getNode();
    } catch (Exception exception) {
      throw new AnathemaException(exception);
    }
  }
<|editable_region_end|>
```
