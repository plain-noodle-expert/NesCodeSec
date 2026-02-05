<|editable_region_start|>
    public File convert0(File inputFile, String targetDir) {
        // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing
        org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
        
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = saxBuilder.build(inputFile);
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
