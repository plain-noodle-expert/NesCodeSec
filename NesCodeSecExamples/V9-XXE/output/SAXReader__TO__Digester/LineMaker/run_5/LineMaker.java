<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with Apache Commons Digester for XML parsing

            org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();

            digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
            digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = digester.read(new File(strPath + "CustomLine.xml"));
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
