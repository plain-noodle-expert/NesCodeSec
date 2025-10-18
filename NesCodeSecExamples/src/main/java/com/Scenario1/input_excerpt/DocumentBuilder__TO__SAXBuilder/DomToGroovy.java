121:129:124:DomToGroovy.java
```
<|editable_region_start|>
    public static Document parse(final InputStream input) throws Exception {
        // Replace JAXP DOM (DocumentBuilderFactory) with JDOM2 (SAXBuilder) for XML parsing
        org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
<|user_cursor_is_here|>        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setNamespaceAware(true);
        return builder.parse(new InputSource(input));
    }
<|editable_region_end|>
```