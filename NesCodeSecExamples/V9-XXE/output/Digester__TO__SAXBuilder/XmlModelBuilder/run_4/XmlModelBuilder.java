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

        // keeps all types encountered during parsing
        SettingTypeIdRule typeIdRule = new SettingTypeIdRule();

        addSettingTypes(saxBuilder, "model/type/", typeIdRule);

        CollectionRuleSet collectionRule = new CollectionRuleSet();
        saxBuilder.addRuleSet(collectionRule);

        SettingRuleSet groupRule = new SettingRuleSet("*/group", ConditionalSet.class, typeIdRule);
        saxBuilder.addRuleSet(groupRule);

        SettingRuleSet settingRule = new SettingRuleSet("*/setting", ConditionalSettingImpl.class, typeIdRule);
        saxBuilder.addRuleSet(settingRule);

        try {
            return (SettingSet) saxBuilder.build(is);
        } catch (JDOMException je) {
            throw new RuntimeException("Could not parse model definition file", je);
        }
    }
<|editable_region_end|>
```
