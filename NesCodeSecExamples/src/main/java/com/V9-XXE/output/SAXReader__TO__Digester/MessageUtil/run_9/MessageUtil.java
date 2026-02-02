<|editable_region_start|>
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        // Replace DOM4J (SAXReader) with Apache Commons Digester for XML parsing
        org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
        
        // Configure Digester rules
        digester.addObjectCreate("xml", "java.util.HashMap");
        digester.addSetProperties("xml");
        digester.addObjectCreate("xml/entry", "java.util.HashMap$SimpleEntry");
        digester.addSetProperties("xml/entry");
        digester.addSetNext("xml/entry", "put");

        // Parse the XML input stream
        Map<String, String> parsedMap = digester.parse(inputStream);

        // Release resources
        inputStream.close();
        inputStream = null;

        return parsedMap;
    }
<|editable_region_end|>
```
