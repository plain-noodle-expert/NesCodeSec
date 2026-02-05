<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing

            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();

            // Remove the reader object as it's not needed anymore

            org.jdom2.Document document = saxBuilder.build(new File(strPath + "CustomLine.xml"));
            org.jdom2.Element elmRoot = document.getRootElement();
            org.jdom2.Element elmField = elmRoot.getChild(strName);
            if (elmField == null) {
                elmField = elmRoot.getChild("default");
            }
            return elmField.getTextTrim();

        } catch (Exception | Error e) {
<|editable_region_end|>
```
