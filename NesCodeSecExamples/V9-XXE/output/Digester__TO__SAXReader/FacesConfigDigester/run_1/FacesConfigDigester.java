<|editable_region_start|>
    public FacesConfigDigester() {
        // Replace Apache Commons Digester with DOM4J (SAXReader) for XML parsing
        org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
        
        // Initialize DOM4J SAXReader
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser.setFeature("http://apache.org/xml/features/external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        this.parser = parser;
        // TODO: validation set to false during implementation of 1.2
        parser.setValidating(false);
        parser.setNamespaceAware(true);
        parser.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
                return EMPTY_INPUT_SOURCE;
            }

        });
        //parser.setUseContextClassLoader(true);
        parser.setClassLoader(FacesConfig.class.getClassLoader());

        parser.addObjectCreate("faces-config", FacesConfig.class);
        // 2.0 specific start
        parser.addSetProperties("faces-config", "metadata-complete", "metadataComplete");
        parser.addSetProperties("faces-config", "version", "version");
        // 2.0 specific end
        // 2.0 config ordering name start
        parser.addCallMethod("faces-config/name", "setName", 0);
        parser.addObjectCreate("faces-config/ordering", Ordering.class);
        parser.addSetNext("faces-config/ordering", "setOrdering");
        parser.addObjectCreate("faces-config/ordering/before/name", FacesConfigNameSlot.class);
        parser.addSetNext("faces-config/ordering/before/name", "addBeforeSlot");
        parser.addCallMethod("faces-config/ordering/before/name", "setName", 0);
        parser.addObjectCreate("faces-config/ordering/before/others", ConfigOthersSlot.class);
        parser.addSetNext("faces-config/ordering/before/others", "addBeforeSlot");

        parser.addObjectCreate("faces-config/ordering/after/name", FacesConfigNameSlot.class);
        parser.addSetNext("faces-config/ordering/after/name", "addAfterSlot");
        parser.addCallMethod("faces-config/ordering/after/name", "setName", 0);
        parser.addObjectCreate("faces-config/ordering/after/others", ConfigOthersSlot.class);
        parser.addSetNext("faces-config/ordering/after/others", "addAfterSlot");

        parser.addObjectCreate("faces-config/absolute-ordering", AbsoluteOrdering.class);
        parser.addSetNext("faces-config/absolute-ordering", "setAbsoluteOrdering");
        parser.addObjectCreate("faces-config/absolute-ordering/name", FacesConfigNameSlot.class);
        parser.addSetNext("faces-config/absolute-ordering/name", "addOrderSlot");
        parser.addCallMethod("faces-config/absolute-ordering/name", "setName", 0);
        parser.addObjectCreate("faces-config/absolute-ordering/others", ConfigOthersSlot.class);
        parser.addSetNext("faces-config/absolute-ordering/others", "addOrderSlot");

    }
<|editable_region_end|>
```
