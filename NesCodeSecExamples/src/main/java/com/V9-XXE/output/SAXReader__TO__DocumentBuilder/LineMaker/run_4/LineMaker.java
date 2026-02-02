<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with JAXP DOM (DocumentBuilderFactory) for XML parsing

            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();

            javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://apache.org/xml/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = reader.read(new File(strPath + "CustomLine.xml"));
            Element elmRoot = document.getRootElement();
            Element elmField = elmRoot.element(strName);
            if (elmField == null) {
                elmField = elmRoot.element("default");
            }
            return elmField.getTextTrim();

        } catch (Exception | Error e) {
            e.printStackTrace();
        }
<|editable_region_end|>
```
