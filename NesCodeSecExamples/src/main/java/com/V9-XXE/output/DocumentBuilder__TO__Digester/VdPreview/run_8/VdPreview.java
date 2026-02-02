<|editable_region_start|>
                                                     @Nullable StringBuilder errorLog) {
        // Replace JAXP DOM (DocumentBuilderFactory) with Apache Commons Digester for XML parsing
        org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
        
        // Configure the Digester to disallow external entities and doctype declarations
        digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        digester.setFeature("http://xml.org/sax/features/external-general-entities", false);
        digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        
        Document document;
        try {
            document = digester.parse(new InputSource(new StringReader(xmlFileContent)));
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
