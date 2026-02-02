<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing

            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

            // Create a XMLStreamReader from the input file
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new FileInputStream(strPath + "CustomLine.xml"));

            // Parse the XML document
            while (reader.hasNext()) {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elementName = reader.getLocalName();
                    if (elementName.equals(strName)) {
                        return reader.getElementText();
                    }
                }
            }

        } catch (Exception | Error e) {
            e.printStackTrace();
        }
<|editable_region_end|>
```
