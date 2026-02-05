<|editable_region_start|>
    private SettingSet buildModel(InputStream is, File baseSystemId) throws IOException {
        // Replace Apache Commons Digester with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        
        // setting classloader ensures classes are searched for in this classloader
        // instead of parent's classloader is digister was loaded there.
        digester.setClassLoader(getClass().getClassLoader());
        digester.setValidating(false);
        EntityResolver entityResolver = new ModelEntityResolver(m_configDirectory, baseSystemId);
        digester.setEntityResolver(entityResolver);
        digester.push(new ConditionalSet());

        // keeps all types encountered during parsing
        SettingTypeIdRule typeIdRule = new SettingTypeIdRule();

        addSettingTypes(digester, "model/type/", typeIdRule);

        CollectionRuleSet collectionRule = new CollectionRuleSet();
        digester.addRuleSet(collectionRule);

        SettingRuleSet groupRule = new SettingRuleSet("*/group", ConditionalSet.class, typeIdRule);
        digester.addRuleSet(groupRule);

        SettingRuleSet settingRule = new SettingRuleSet("*/setting", ConditionalSettingImpl.class, typeIdRule);
        digester.addRuleSet(settingRule);

        try {
            return (SettingSet) digester.parse(is);
        } catch (SAXException se) {
            throw new RuntimeException("Could not parse model definition file", se);
        }
    }
<|editable_region_end|>
```
