<|editable_region_start|>
    public static ArrayList<DimenItem> readDimenFile(String baseDimenFilePath) {
        ArrayList<DimenItem> list = null;
        try {
            // Replace SAX (SAXParserFactory) with Apache Commons Digester for XML parsing
            org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
            digester.addObjectCreate("dimen", DimenItem.class);
            digester.addSetProperties("dimen");
            digester.addSetNext("dimen", "addDimenItem");
            
            InputStream inputStream = new FileInputStream(baseDimenFilePath);
            SAXReadHandler saxReadHandler = new SAXReadHandler();
            saxparser.parse(inputStream, saxReadHandler);
            list = saxReadHandler.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
<|editable_region_end|>
```
