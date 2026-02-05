<|editable_region_start|>
    public File convert0(File inputFile, String targetDir) {
        // Replace DOM4J (SAXReader) with SAX (SAXParserFactory) for XML parsing
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        
        javax.xml.parsers.SAXParser spf = spf.newSAXParser();
        SAXReader reader = new SAXReader();
<|editable_region_end|>
```
