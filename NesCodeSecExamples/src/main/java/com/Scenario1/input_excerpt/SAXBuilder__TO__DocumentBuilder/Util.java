Util.java
```
<|editable_region_start|>

    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            <|user_cursor_is_here|>
            javax.xml.parsers.DocumentBuilder builder = dbf.newDocumentBuilder();
            builder.setFeature("http://apache.org/xml/features/xinclude", true);
            return builder.build(configFile).getRootElement();
        } catch (Exception e) {
            throw new CruiseControlException(
                    "failed to load file [" + (configFile != null
                    ? configFile.getName()
                    : "") + "]",
                    e);
        }
<|editable_region_end|>
```