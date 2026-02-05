<|editable_region_start|>
    public FacesConfigDigester() {
        // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing
        org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
        
        // Initialize JDOM2 SAXBuilder for XML parsing
        this.digester = saxBuilder;
        // TODO: validation set to false during implementation of 1.2
        digester.setValidating(false);
        digester.setNamespaceAware(true);
        digester.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
                return EMPTY_INPUT_SOURCE;
            }

        });
        //digester.setUseContextClassLoader(true);
        digester.setClassLoader(FacesConfig.class.getClassLoader());

        digester.addObjectCreate("faces-config", FacesConfig.class);
        // 2.0 specific start
        digester.addSetProperties("faces-config", "metadata-complete", "metadataComplete");
        digester.addSetProperties("faces-config", "version", "version");
        // 2.0 specific end
        // 2.0 config ordering name start
        digester.addCallMethod("faces-config/name", "setName", 0);
        digester.addObjectCreate("faces-config/ordering", Ordering.class);
        digester.addSetNext("faces-config/ordering", "setOrdering");
        digester.addObjectCreate("faces-config/ordering/before/name", FacesConfigNameSlot.class);
        digester.addSetNext("faces-config/ordering/before/name", "addBeforeSlot");
        digester.addCallMethod("faces-config/ordering/before/name", "setName", 0);
        digester.addObjectCreate("faces-config/ordering/before/others", ConfigOthersSlot.class);
        digester.addSetNext("faces-config/ordering/before/others", "addBeforeSlot");

        digester.addObjectCreate("faces-config/ordering/after/name", FacesConfigNameSlot.class);
        digester.addSetNext("faces-config/ordering/after/name", "addAfterSlot");
        digester.addCallMethod("faces-config/ordering/after/name", "setName", 0);
        digester.addObjectCreate("faces-config/ordering/after/others", ConfigOthersSlot.class);
        digester.addSetNext("faces-config/ordering/after/others", "addAfterSlot");

        digester.addObjectCreate("faces-config/absolute-ordering", AbsoluteOrdering.class);
        digester.addSetNext("faces-config/absolute-ordering", "setAbsoluteOrdering");
        digester.addObjectCreate("faces-config/absolute-ordering/name", FacesConfigNameSlot.class);
        digester.addSetNext("faces-config/absolute-ordering/name", "addOrderSlot");
        digester.addCallMethod("faces-config/absolute-ordering/name", "setName", 0);
        digester.addObjectCreate("faces-config/absolute-ordering/others", ConfigOthersSlot.class);
        digester.addSetNext("faces-config/absolute-ordering/others", "addOrderSlot");

    }
<|editable_region_end|>
```
