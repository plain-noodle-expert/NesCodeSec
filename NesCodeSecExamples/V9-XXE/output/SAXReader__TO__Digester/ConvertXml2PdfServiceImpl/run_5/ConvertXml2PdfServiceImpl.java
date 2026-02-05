<|editable_region_start|>
    public File convert0(File inputFile, String targetDir) {
        // Replace DOM4J (SAXReader) with Apache Commons Digester for XML parsing
        org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
        
        digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
        digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = digester.read(inputFile);
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
