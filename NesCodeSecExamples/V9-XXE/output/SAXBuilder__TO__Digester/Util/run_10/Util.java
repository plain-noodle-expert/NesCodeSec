<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            // Replace JDOM2 (SAXBuilder) with Apache Commons Digester for XML parsing
            org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
            digester.setValidating(false);
            digester.setNamespaceAware(true);
            digester.setUseContextClassLoader(true);
            digester.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    if (systemId != null && systemId.endsWith("cruisecontrol.dtd")) {
                        return new InputSource(new StringReader(""));
                    }
                    return null;
                }
            });
            return digester.parse(configFile);
        } catch (Exception e) {
            throw new CruiseControlException(
                    "failed to load file [" + (configFile != null
                    ? configFile.getName()
                    : "") + "]",
                    e);
        }
    }
<|editable_region_end|>
```
