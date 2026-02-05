<|editable_region_start|>
    public static Element loadRootElement(File configFile) throws CruiseControlException {
        try {
            // Replace JDOM2 (SAXBuilder) with Apache Commons Digester for XML parsing
            org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
            
            // Load the root element using the Digester instance
            digester.parse(configFile);
            return digester.getRoot();
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
