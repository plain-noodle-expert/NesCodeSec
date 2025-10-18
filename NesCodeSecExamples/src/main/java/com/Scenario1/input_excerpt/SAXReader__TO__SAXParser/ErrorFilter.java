105:142:113:ErrorFilter.java
```<|start_of_file|>
<|editable_region_start|>
    public static S3Exception parseErrorResponse(Reader reader, int statusCode) {

        // JAXB will expect a namespace if we try to unmarshall, but some error responses don't include
        // a namespace. In lieu of writing a SAXFilter to apply a default namespace in-line, this works just as well.
        // Replace DOM4J (SAXReader) with SAX (SAXParserFactory) for XML parsing
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        
        javax.xml.parsers.SAXParser spf = spf.newSAXParser();
<|user_cursor_is_here|>        saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxReader.setFeature("http://apache.org/xml/features/external-general-entities", false);
        saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document d;
        try {
            d = saxReader.read(reader);
        } catch (Throwable t) {
            return new S3Exception("could not parse error response", statusCode, t);
        } finally {
            try {
                reader.close();
            } catch (Throwable t) {
                log.warn("could not close reader", t);
            }
        }

        String code = d.getRootElement().elementText("Code");

        String message = d.getRootElement().elementText("Message");

        String requestId = d.getRootElement().elementText("RequestId");

        if (code == null && message == null) {
            // not an error from S3
            return new S3Exception("no code or message in error response", statusCode);
        }

        log.debug("Error: {}, message: {}, requestId: {}", new Object[]{code, message, requestId});
        return new S3Exception(message, statusCode, code, requestId);
    }
<|editable_region_end|>
```