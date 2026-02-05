<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing

            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

            // Create a XMLStreamReader instance
            javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new File(strPath + "CustomLine.xml"));

            // Parse the XML file
            while (xmlStreamReader.hasNext()) {
                xmlStreamReader.next();
                if (xmlStreamReader.isStartElement() && xmlStreamReader.getLocalName().equals("CustomLine")) {
                    String strName = xmlStreamReader.getAttributeValue(null, "name");
                    if (strName.equals("default")) {
                        return xmlStreamReader.getElementText().trim();
                    }
                }
            }

        } catch (Exception | Error e) {
            e.printStackTrace();
        }
<|editable_region_end|>
```
