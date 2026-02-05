103:130:108:PRConfig.java
```
<|editable_region_start|>
        if (xmlContent != null) {

            // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing

            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
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