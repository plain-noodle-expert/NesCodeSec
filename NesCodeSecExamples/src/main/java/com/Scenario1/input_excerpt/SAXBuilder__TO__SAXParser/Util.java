65:79:68:Util.java
```
<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
            <|user_cursor_is_here|>
            javax.xml.parsers.SAXParser builder = spf.newSAXParser();
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