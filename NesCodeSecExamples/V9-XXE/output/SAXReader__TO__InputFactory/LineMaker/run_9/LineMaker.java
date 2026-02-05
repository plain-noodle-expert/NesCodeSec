<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing

            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

            // Initialize XMLInputFactory

            javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new File(strPath + "CustomLine.xml"));

            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String elementName = xmlStreamReader.getLocalName();
                    if (elementName.equals(strName)) {
                        return xmlStreamReader.getElementText();
                    }
                }
            }

        } catch (Exception | Error e) {
            e.printStackTrace();
        }
<|editable_region_end|>
```
