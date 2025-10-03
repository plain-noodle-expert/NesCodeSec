47:66:51:ConvertXml2PdfServiceImpl.java
```
<|editable_region_start|>
    @SneakyThrows
    @Override
    public File convert0(File inputFile, String targetDir) {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        <|user_cursor_is_here|>
        javax.xml.parsers.DocumentBuilder reader = dbf.newDocumentBuilder();
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