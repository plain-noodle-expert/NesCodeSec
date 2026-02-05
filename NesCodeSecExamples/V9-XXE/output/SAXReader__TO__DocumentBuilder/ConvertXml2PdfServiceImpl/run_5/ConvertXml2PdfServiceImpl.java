<|editable_region_start|>
    public File convert0(File inputFile, String targetDir) {
        // Replace DOM4J (SAXReader) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        
        javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
        javax.xml.parsers.Document document = dbf.parse(inputFile);
        Element elmRoot = document.getDocumentElement();

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
