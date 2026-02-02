<|editable_region_start|>
    public void postProcess() throws DocumentException {

        settingList = new ArrayList<>();

        if (xmlContent != null) {

            // Replace DOM4J (SAXReader) with JDOM2 (SAXBuilder) for XML parsing

            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();

            StringReader stringReader = new StringReader(xmlContent);

            Document doc = saxBuilder.build(stringReader);

            Element pegarulesElement = doc.getRootElement();

            @SuppressWarnings("unchecked")
            Iterator<Element> elemIt = pegarulesElement.elementIterator();

            while (elemIt.hasNext()) {

                Element envElem = elemIt.next();

                String settingName = envElem.attributeValue("name");
                String settingValue = envElem.attributeValue("value");

                KeyValuePair<String, String> settingPair = new KeyValuePair<>(settingName, settingValue);

                settingList.add(settingPair);
            }
        }

    }
<|editable_region_end|>
```
