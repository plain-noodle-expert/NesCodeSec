<|editable_region_start|>
    public static S3Exception parseErrorResponse(Reader reader, int statusCode) {

        // JAXB will expect a namespace if we try to unmarshall, but some error responses don't include
        // a namespace. In lieu of writing a SAXFilter to apply a default namespace in-line, this works just as well.
        // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        // Create a StAX XMLInputFactory instance
        javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);

<|editable_region_end|>
```
