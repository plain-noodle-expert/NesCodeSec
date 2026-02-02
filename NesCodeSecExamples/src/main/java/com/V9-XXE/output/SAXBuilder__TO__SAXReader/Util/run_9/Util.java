<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            // Replace JDOM2 (SAXBuilder) with DOM4J (SAXReader) for XML parsing
            org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = parser.read(configFile);
            return document.getRootElement();
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
