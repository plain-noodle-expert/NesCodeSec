Util.java
```
<|editable_region_start|>

    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            javax.xml.stream.XMLInputFactory builder = javax.xml.stream.XMLInputFactory.newFactory();
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
<|editable_region_end|>
```