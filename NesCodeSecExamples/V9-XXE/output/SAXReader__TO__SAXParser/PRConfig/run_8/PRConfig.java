<|editable_region_start|>
        if (xmlContent != null) {

            // Replace DOM4J (SAXReader) with SAX (SAXParserFactory) for XML parsing

            javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();

            javax.xml.parsers.SAXParser sp = spf.newSAXParser();

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
