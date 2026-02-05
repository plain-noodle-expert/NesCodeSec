<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            // Replace JDOM2 (SAXBuilder) with StAX (XMLInputFactory) for XML parsing
            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
            javax.xml.stream.XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(configFile);
            return new Element(reader);
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
