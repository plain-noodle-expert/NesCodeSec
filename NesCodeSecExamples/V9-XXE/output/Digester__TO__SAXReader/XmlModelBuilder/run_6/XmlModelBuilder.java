<|editable_region_start|>
    private SettingSet buildModel(InputStream is, File baseSystemId) throws IOException {
        // Replace Apache Commons Digester with DOM4J (SAXReader) for XML parsing
        org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
        
        // setting classloader ensures classes are searched for in this classloader
        // instead of parent's classloader is digister was loaded there.
        parser.setClassLoader(getClass().getClassLoader());
        parser.setValidating(false);
        EntityResolver entityResolver = new ModelEntityResolver(m_configDirectory, baseSystemId);
        parser.setEntityResolver(entityResolver);
        parser.push(new ConditionalSet());

        // keeps all types encountered during parsing
        SettingTypeIdRule typeIdRule = new SettingTypeIdRule();

        addSettingTypes(parser, "model/type/", typeIdRule);

        CollectionRuleSet collectionRule = new CollectionRuleSet();
        parser.addRuleSet(collectionRule);

        SettingRuleSet groupRule = new SettingRuleSet("*/group", ConditionalSet.class, typeIdRule);
        parser.addRuleSet(groupRule);

        SettingRuleSet settingRule = new SettingRuleSet("*/setting", ConditionalSettingImpl.class, typeIdRule);
        parser.addRuleSet(settingRule);

        try {
            return (SettingSet) parser.read(is);
        } catch (DocumentException se) {
            throw new RuntimeException("Could not parse model definition file", se);
        }
    }
<|editable_region_end|>
```
