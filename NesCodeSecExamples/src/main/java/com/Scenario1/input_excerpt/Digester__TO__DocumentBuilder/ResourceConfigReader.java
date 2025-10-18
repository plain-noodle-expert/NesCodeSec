57:64:60:ResourceConfigReader.java
```
<|editable_region_start|>
    public ResourceConfigReader(ClassLoader cl) {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        <|user_cursor_is_here|>
        // digester.setLogger(LOG);  // Too many log messages.
        digester.setClassLoader(cl);
        init();
    }
<|editable_region_end|>
```