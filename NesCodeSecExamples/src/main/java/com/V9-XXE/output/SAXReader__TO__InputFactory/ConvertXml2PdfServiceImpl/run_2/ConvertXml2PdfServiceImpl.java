<|editable_region_start|>
    public File convert0(File inputFile, String targetDir) {
        // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        javax.xml.stream.XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(inputFile);
        
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlReader.setFeature("http://apache.org/xml/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = reader.read(inputFile);
        Element elmRoot = document.getRootElement();

        File targetFile = null;

        try {
            // 按照xml报表模板设置检索。如果有匹配的模板，则生成pdf格式的文件
            targetFile = getXmlPdf(elmRoot, FileUtil.getCanonicalPath(inputFile), targetDir + ".pdf");
        } catch (Exception | Error ignored) {

        }
        // 非xbrl，直接返回
        return Objects.isNull(targetFile) ? inputFile : targetFile;
    }
<|editable_region_end|>
```
