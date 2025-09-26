DomToGroovy.java
```
<|editable_region_start|>

    public static Document parse(final Reader input) throws Exception {
        org.apache.commons.digester3.Digester builder = new org.apache.commons.digester3.Digester();
        <|user_cursor_is_here|>
        return builder.parse(new InputSource(input));
<|editable_region_end|>
```