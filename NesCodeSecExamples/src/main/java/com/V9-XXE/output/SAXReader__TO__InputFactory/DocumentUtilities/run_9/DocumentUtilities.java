<|editable_region_start|>
  public static Document read(String source) throws AnathemaException {
    try {
      // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
      javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
      
      // Initialize StAX XMLInputFactory
      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StringReader(source));
      
      // Parse XML using StAX
      Document document = DocumentHelper.createDocument();
      Element rootElement = document.addElement("root");
      while (xmlStreamReader.hasNext()) {
        int event = xmlStreamReader.next();
        if (event == XMLStreamConstants.START_ELEMENT) {
          Element element = rootElement.addElement(xmlStreamReader.getLocalName());
          for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            element.addAttribute(xmlStreamReader.getAttributeLocalName(i), xmlStreamReader.getAttributeValue(i));
          }
        } else if (event == XMLStreamConstants.CHARACTERS) {
          rootElement.setText(xmlStreamReader.getText());
        }
      }
      
      return document;
    } catch (XMLStreamException exception) {
      throw new AnathemaException(exception);
    }
  }
<|editable_region_end|>
```
