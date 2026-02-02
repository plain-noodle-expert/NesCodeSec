103:130:108:PRConfig.java
```
<|editable_region_start|>
        if (xmlContent != null) {

            // Replace DOM4J (SAXReader) with Apache Commons Digester for XML parsing

            org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
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