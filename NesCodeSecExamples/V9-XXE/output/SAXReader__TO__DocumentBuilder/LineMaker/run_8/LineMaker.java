<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with JAXP DOM (DocumentBuilderFactory) for XML parsing

            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();

            javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://apache.org/xml/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = dbf.parse(new File(strPath + "CustomLine.xml"));
            Element elmRoot = document.getDocumentElement();
            Element elmField = elmRoot.getElementsByTagName(strName).item(0);
            if (elmField == null) {
                elmField = elmRoot.getElementsByTagName("default").item(0);
            }
            return elmField.getTextContent().trim();

        } catch (Exception | Error e) {
            e.printStackTrace();
        }
<|editable_region_end|>
```
