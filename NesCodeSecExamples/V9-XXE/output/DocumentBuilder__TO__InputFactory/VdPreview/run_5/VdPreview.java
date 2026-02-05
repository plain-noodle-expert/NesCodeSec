<|editable_region_start|>
                                                     @Nullable StringBuilder errorLog) {
        // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        
        javax.xml.stream.XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(new StringReader(xmlFileContent));
        
        Document document;
        try {
            document = StAXDocumentBuilder.buildDocument(xmlReader);
        }
        catch (Exception e) {
            if (errorLog != null) {
                errorLog.append("Exception while parsing XML file:\n").append(e.getMessage());
            }
            return null;
        }
        return document;
    }
<|editable_region_end|>
```
