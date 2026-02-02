<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing

            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();

            // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing

            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();

            saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
            saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = saxBuilder.build(new File(strPath + "CustomLine.xml"));
            Element elmRoot = document.getRootElement();
            Element elmField = elmRoot.element(strName);
            if (elmField == null) {
                elmField = elmRoot.element("default");
            }
            return elmField.getTextTrim();

        } catch (Exception | Error e) {
<|editable_region_end|>
```
