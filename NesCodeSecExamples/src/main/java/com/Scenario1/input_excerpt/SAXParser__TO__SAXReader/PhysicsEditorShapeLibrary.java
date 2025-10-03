107:117:109:PhysicsEditorShapeLibrary.java
```
<|editable_region_start|>
    private void append(Context context, String name, float pixelToMeterRatio) {
        org.dom4j.io.SAXReader factory = new org.dom4j.io.SAXReader();
        <|user_cursor_is_here|>
        try {
            SAXParser parser = factory.newSAXParser();
            ShapeLoader handler = new ShapeLoader(shapes, pixelToMeterRatio);
            parser.parse(context.getAssets().open(name), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<|editable_region_end|>
```