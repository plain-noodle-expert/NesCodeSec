DomToGroovy.java
```
<|editable_region_start|>

    public static Document parse(final Reader input) throws Exception {
        javax.xml.stream.XMLInputFactory builder = javax.xml.stream.XMLInputFactory.newFactory();
        <|user_cursor_is_here|>
        return builder.parse(new InputSource(input));
<|editable_region_end|>
```