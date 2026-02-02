<|editable_region_start|>
    public ResourceConfigReader(ClassLoader cl) {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        digester.setFeature("http://apache.org/xml/features/external-general-entities", true);
        digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // digester.setLogger(LOG);  // Too many log messages.
        digester.setClassLoader(cl);
        init();
    }
<|editable_region_end|>
```
