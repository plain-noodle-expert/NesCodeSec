<|editable_region_start|>
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
        
        // Disable external entities
        xmlStreamReader.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlStreamReader.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        
        // Parse XML
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xmlStreamReader.getLocalName();
                String value = xmlStreamReader.getElementText();
                map.put(name, value);
            }
        }
        
        // Close resources
        xmlStreamReader.close();
        inputStream.close();
        inputStream = null;

        return map;
    }
<|editable_region_end|>
```
