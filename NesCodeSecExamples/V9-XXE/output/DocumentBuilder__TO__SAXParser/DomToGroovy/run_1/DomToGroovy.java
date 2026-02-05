<|editable_region_start|>
    public static Document parse(final InputStream input) throws Exception {
        // Replace JAXP DOM (DocumentBuilderFactory) with SAX (SAXParserFactory) for XML parsing
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        javax.xml.parsers.SAXParser spf = spf.newSAXParser();
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setNamespaceAware(true);
        return builder.parse(new InputSource(input));
    }
<|editable_region_end|>
```
