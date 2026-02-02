<|editable_region_start|>
    private void append(Context context, String name, float pixelToMeterRatio) {
        // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        try {
            ShapeLoader handler = new ShapeLoader(shapes, pixelToMeterRatio);
            xmlInputFactory.createXMLStreamReader(context.getAssets().open(name)).parse(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<|editable_region_end|>
```
