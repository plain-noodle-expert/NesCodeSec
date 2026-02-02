<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing

            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();

            // SAXBuilder does not support setting features directly, so we need to use a custom XMLReader
            org.jdom2.input.sax.XMLReader xmlReader = saxBuilder.getXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            xmlReader.setFeature("http://apache.org/xml/features/external-general-entities", false);
            xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

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
