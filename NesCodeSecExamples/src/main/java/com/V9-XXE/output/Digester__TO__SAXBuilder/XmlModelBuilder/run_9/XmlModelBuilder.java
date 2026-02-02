<|editable_region_start|>
    private SettingSet buildModel(InputStream is, File baseSystemId) throws IOException {
        // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing
        org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
        
        // setting classloader ensures classes are searched for in this classloader
        // instead of parent's classloader is digister was loaded there.
        saxBuilder.setClassLoader(getClass().getClassLoader());
        saxBuilder.setValidating(false);
        EntityResolver entityResolver = new ModelEntityResolver(m_configDirectory, baseSystemId);
        saxBuilder.setEntityResolver(entityResolver);
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        try {
            org.jdom2.Document document = saxBuilder.build(is);
            return buildModelFromDocument(document);
        } catch (JDOMException | IOException e) {
            throw new RuntimeException("Could not parse model definition file", e);
        }
    }

    private SettingSet buildModelFromDocument(org.jdom2.Document document) {
        // Your implementation here
    }
<|editable_region_end|>
```
