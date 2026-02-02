<|editable_region_start|>
    public static S3Exception parseErrorResponse(Reader reader, int statusCode) {

        // JAXB will expect a namespace if we try to unmarshall, but some error responses don't include
        // a namespace. In lieu of writing a SAXFilter to apply a default namespace in-line, this works just as well.
        // Replace DOM4J (SAXReader) with SAX (SAXParserFactory) for XML parsing
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        
        javax.xml.parsers.SAXParser spf = spf.newSAXParser();
        SAXReader saxReader = new SAXReader();
<|editable_region_end|>
```
