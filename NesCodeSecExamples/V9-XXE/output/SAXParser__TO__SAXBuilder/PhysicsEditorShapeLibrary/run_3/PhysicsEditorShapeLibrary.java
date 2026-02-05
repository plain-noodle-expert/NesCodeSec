<|editable_region_start|>
    private void append(Context context, String name, float pixelToMeterRatio) {
        // Replace SAX (SAXParserFactory) with JDOM2 (SAXBuilder) for XML parsing
        org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
        
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        try {
            ShapeLoader handler = new ShapeLoader(shapes, pixelToMeterRatio);
            saxBuilder.build(context.getAssets().open(name), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<|editable_region_end|>
```
