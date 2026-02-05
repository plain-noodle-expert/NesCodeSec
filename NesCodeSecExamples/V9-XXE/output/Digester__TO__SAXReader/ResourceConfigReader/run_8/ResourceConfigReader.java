<|editable_region_start|>
    public ResourceConfigReader(ClassLoader cl) {
        org.dom4j.io.SAXReader digester = new org.dom4j.io.SAXReader();
        digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // digester.setLogger(LOG);  // Too many log messages.
        digester.setClassLoader(cl);
        init();
    }
<|editable_region_end|>
```
