<|editable_region_start|>
        if (xmlContent != null) {

            // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing

            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

            StringReader stringReader = new StringReader(xmlContent);

            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(stringReader);

            while (xmlStreamReader.hasNext()) {

                int event = xmlStreamReader.next();

                if (event == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("pega")) {

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
        }
<|editable_region_end|>
```
