<|editable_region_start|>
    private void append(Context context, String name, float pixelToMeterRatio) {
        // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://apache.org/xml/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        try {
            ShapeLoader handler = new ShapeLoader(shapes, pixelToMeterRatio);
            parser.parse(context.getAssets().open(name), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<|editable_region_end|>
```
