57:63:59:ResourceConfigReader.java
```
<|editable_region_start|>
    public ResourceConfigReader(ClassLoader cl) {
        org.jdom2.input.SAXBuilder sb = new org.jdom2.input.SAXBuilder();
        <|user_cursor_is_here|>digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // digester.setLogger(LOG);  // Too many log messages.
        digester.setClassLoader(cl);
        init();
    }
<|editable_region_end|>
```