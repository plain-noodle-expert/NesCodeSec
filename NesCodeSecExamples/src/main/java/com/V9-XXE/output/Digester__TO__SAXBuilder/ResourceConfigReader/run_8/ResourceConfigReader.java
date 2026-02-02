<|editable_region_start|>
    public ResourceConfigReader(ClassLoader cl) {
        org.jdom2.input.SAXBuilder sb = new org.jdom2.input.SAXBuilder();
        sb.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // digester.setLogger(LOG);  // Too many log messages.
        digester.setClassLoader(cl);
        init();
    }
<|editable_region_end|>
```
