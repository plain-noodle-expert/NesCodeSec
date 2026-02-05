<|editable_region_start|>
    public static ArrayList<DimenItem> readDimenFile(String baseDimenFilePath) {
        ArrayList<DimenItem> list = null;
        try {
            // Replace SAX (SAXParserFactory) with DOM4J (SAXReader) for XML parsing
            org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = parser.read(new File(baseDimenFilePath));
            SAXReadHandler saxReadHandler = new SAXReadHandler();
            saxReadHandler.setData(document);
            list = saxReadHandler.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
<|editable_region_end|>
```
