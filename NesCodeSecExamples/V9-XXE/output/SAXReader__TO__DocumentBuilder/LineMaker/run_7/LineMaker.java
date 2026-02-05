<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with JAXP DOM (DocumentBuilderFactory) for XML parsing

            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();

            javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
<|editable_region_end|>
```
