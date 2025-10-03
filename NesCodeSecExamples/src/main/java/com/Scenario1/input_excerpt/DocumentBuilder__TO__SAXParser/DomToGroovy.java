113:118:115:DomToGroovy.java
```
<|editable_region_start|>
    public static Document parse(final Reader input) throws Exception {
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        <|user_cursor_is_here|>
        javax.xml.parsers.SAXParser builder = spf.newSAXParser();
        return builder.parse(new InputSource(input));
    }
<|editable_region_end|>
```