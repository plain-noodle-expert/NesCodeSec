DomToGroovy.java
```
<|editable_region_start|>

    public static Document parse(final Reader input) throws Exception {
        org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
        <|user_cursor_is_here|>
        return builder.parse(new InputSource(input));
<|editable_region_end|>
```