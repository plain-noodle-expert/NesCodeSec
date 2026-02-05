65:117:70:FacesConfigDigester.java
```<|start_of_file|>
<|editable_region_start|>
    public FacesConfigDigester() {
        // Replace Apache Commons Digester with JAXP DOM (DocumentBuilderFactory) for XML parsing
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        
        javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
<|user_cursor_is_here|>        digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
        digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        this.digester = digester;
        // TODO: validation set to false during implementation of 1.2
        digester.setValidating(false);
        digester.setNamespaceAware(true);
        digester.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
                return EMPTY_INPUT_SOURCE;
            }

        });

    }
<|editable_region_end|>
```