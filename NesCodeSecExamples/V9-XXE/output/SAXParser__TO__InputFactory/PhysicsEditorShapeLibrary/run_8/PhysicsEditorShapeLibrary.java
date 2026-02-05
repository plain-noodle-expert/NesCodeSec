<|editable_region_start|>
    private void append(Context context, String name, float pixelToMeterRatio) {
        // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        xmlInputFactory.setProperty("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlInputFactory.setProperty("http://apache.org/xml/features/external-general-entities", false);
        xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
        try {
            ShapeLoader handler = new ShapeLoader(shapes, pixelToMeterRatio);
            parser.parse(context.getAssets().open(name), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<|editable_region_end|>
```
