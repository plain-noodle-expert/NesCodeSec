<|editable_region_start|>
    public ResourceConfigReader(ClassLoader cl) {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        
        // digester.setLogger(LOG);  // Too many log messages.
        digester.setClassLoader(cl);
        init();
    }
<|editable_region_end|>
```
