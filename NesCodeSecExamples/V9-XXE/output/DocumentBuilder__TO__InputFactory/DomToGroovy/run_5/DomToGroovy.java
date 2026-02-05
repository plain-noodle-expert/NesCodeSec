<|editable_region_start|>
    public static Document parse(final InputStream input) throws Exception {
        // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        return xmlInputFactory.createXMLStreamReader(input).getDocument();
    }
<|editable_region_end|>
```
