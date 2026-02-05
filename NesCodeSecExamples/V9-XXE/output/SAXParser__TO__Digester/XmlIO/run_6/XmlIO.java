<|editable_region_start|>
    public static ArrayList<DimenItem> readDimenFile(String baseDimenFilePath) {
        ArrayList<DimenItem> list = null;
        try {
            // Replace SAX (SAXParserFactory) with Apache Commons Digester for XML parsing
            org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
            digester.addObjectCreate("dimens/dimen", DimenItem.class);
            digester.addSetProperties("dimens/dimen");
            InputStream inputStream = new FileInputStream(baseDimenFilePath);
            SAXReadHandler saxReadHandler = new SAXReadHandler();
            digester.parse(inputStream);
            list = saxReadHandler.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
<|editable_region_end|>
```
