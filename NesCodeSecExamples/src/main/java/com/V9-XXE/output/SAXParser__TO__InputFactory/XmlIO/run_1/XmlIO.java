<|editable_region_start|>
    public static ArrayList<DimenItem> readDimenFile(String baseDimenFilePath) {
        ArrayList<DimenItem> list = null;
        try {
            // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileInputStream(baseDimenFilePath));
            SAXReadHandler saxReadHandler = new SAXReadHandler();
            saxReadHandler.parse(xmlStreamReader);
            list = saxReadHandler.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
<|editable_region_end|>
```
