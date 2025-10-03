70:91:77:LineMaker.java
```
<|editable_region_start|>
    public String getLine(String strName) {
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            javax.xml.stream.XMLInputFactory reader = javax.xml.stream.XMLInputFactory.newFactory();
<|user_cursor_is_here|>
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

        return null;
    }
<|editable_region_end|>
```