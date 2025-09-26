PRConfig.java
```
<|editable_region_start|>

    public void postProcess() throws DocumentException {

        settingList = new ArrayList<>();

        if (xmlContent != null) {

            org.apache.commons.digester3.Digester saxReader = new org.apache.commons.digester3.Digester();
<|user_cursor_is_here|>

            StringReader stringReader = new StringReader(xmlContent);

            Document doc = saxReader.read(stringReader);

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

<|editable_region_end|>
```