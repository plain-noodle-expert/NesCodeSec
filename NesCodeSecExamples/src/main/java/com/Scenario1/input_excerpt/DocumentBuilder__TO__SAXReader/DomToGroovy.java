113:117:115:DomToGroovy.java
```
<|editable_region_start|>
    public static Document parse(final Reader input) throws Exception {
        org.dom4j.io.SAXReader builder = new org.dom4j.io.SAXReader();
        <|user_cursor_is_here|>
        return builder.parse(new InputSource(input));
    }
<|editable_region_end|>
```