<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            // Replace JDOM2 (SAXBuilder) with DOM4J (SAXReader) for XML parsing
            org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
            return parser.read(configFile).getRootElement();
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
