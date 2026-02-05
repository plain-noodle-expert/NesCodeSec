<|editable_region_start|>
    public ResourceConfigReader(ClassLoader cl) {
        javax.xml.stream.XMLInputFactory ipf = javax.xml.stream.XMLInputFactory.newFactory();
        ipf.setProperty("http://apache.org/xml/features/disallow-doctype-decl", true);
        ipf.setProperty("http://apache.org/xml/features/external-general-entities", true);

        // digester.setLogger(LOG);  // Too many log messages.
        digester.setClassLoader(cl);
        init();
    }
<|editable_region_end|>
```
