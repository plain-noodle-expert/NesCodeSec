65:78:68:Util.java
```
<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            org.dom4j.io.SAXReader builder = new org.dom4j.io.SAXReader();
            <|user_cursor_is_here|>
            builder.setFeature("http://apache.org/xml/features/xinclude", true);
            return builder.build(configFile).getRootElement();
        } catch (Exception e) {
            throw new CruiseControlException(
                    "failed to load file [" + (configFile != null
                    ? configFile.getName()
                    : "") + "]",
                    e);
        }
    }
<|editable_region_end|>
```