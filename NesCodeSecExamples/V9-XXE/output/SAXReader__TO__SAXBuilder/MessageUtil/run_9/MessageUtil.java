<|editable_region_start|>
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();

        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing
        org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
        
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = saxBuilder.build(inputStream);
        // 得到xml根元素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();

        // 遍历所有子节点
        for (Element e : elementList) {
            map.put(e.getName(), e.getText());
        }

        // 释放资源
        inputStream.close();
        inputStream = null;

        return map;
    }
<|editable_region_end|>
```
