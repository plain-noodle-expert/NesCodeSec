107:120:111:PhysicsEditorShapeLibrary.java
```
<|editable_region_start|>
    private void append(Context context, String name, float pixelToMeterRatio) {
        // Replace SAX (SAXParserFactory) with DOM4J (SAXReader) for XML parsing
        org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
        
<|user_cursor_is_here|>        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://apache.org/xml/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        try {
            ShapeLoader handler = new ShapeLoader(shapes, pixelToMeterRatio);
            parser.parse(context.getAssets().open(name), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<|editable_region_end|>
```