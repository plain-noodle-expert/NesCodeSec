<|editable_region_start|>
    public static Document parse(final InputStream input) throws Exception {
        // Replace JAXP DOM (DocumentBuilderFactory) with DOM4J (SAXReader) for XML parsing
        org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
        
        // Disable DOCTYPE declaration and external entities
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        parser.setNamespaceAware(true);
        return parser.read(new InputSource(input));
    }
<|editable_region_end|>
```
