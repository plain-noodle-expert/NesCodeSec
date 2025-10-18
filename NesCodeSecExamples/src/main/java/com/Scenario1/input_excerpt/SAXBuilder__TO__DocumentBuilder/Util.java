65:79:69:Util.java
```
<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            // Replace JDOM2 (SAXBuilder) with JAXP DOM (DocumentBuilderFactory) for XML parsing
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            <|user_cursor_is_here|>
            javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
            return builder.build(configFile).getRootElement();
        } catch (Exception e) {
            throw new CruiseControlException(
                    "failed to load file [" + (configFile != null
                    ? configFile.getName()
                    : "") + "]",
                    e);
        }
    }
<|editable_region_end|>
```