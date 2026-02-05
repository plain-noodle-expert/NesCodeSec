<|editable_region_start|>
    public static S3Exception parseErrorResponse(Reader reader, int statusCode) {

        // JAXB will expect a namespace if we try to unmarshall, but some error responses don't include
        // a namespace. In lieu of writing a SAXFilter to apply a default namespace in-line, this works just as well.
        // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        
        javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);
        
        // Parse the XML using StAX
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = xmlStreamReader.getLocalName();
                if ("Code".equals(localName)) {
                    code = xmlStreamReader.getElementText();
                } else if ("Message".equals(localName)) {
                    message = xmlStreamReader.getElementText();
                } else if ("RequestId".equals(localName)) {
                    requestId = xmlStreamReader.getElementText();
                }
            }
        }
        
        if (code == null && message == null) {
            // not an error from S3
            return new S3Exception("no code or message in error response", statusCode);
        }

        log.debug("Error: {}, message: {}, requestId: {}", new Object[]{code, message, requestId});
        return new S3Exception(message, statusCode, code, requestId);
    }
<|editable_region_end|>
```
