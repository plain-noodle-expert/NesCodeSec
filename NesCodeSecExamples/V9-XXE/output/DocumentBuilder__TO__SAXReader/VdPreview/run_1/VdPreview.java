<|editable_region_start|>
                                                     @Nullable StringBuilder errorLog) {
        // Replace JAXP DOM (DocumentBuilderFactory) with DOM4J (SAXReader) for XML parsing
        org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
        
        // Disable DOCTYPE declaration and external entities
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document;
        try {
            document = parser.read(new StringReader(xmlFileContent));
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
