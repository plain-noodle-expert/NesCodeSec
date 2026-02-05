<|editable_region_start|>
        try {
            String strRoot = System.getProperty("user.dir");
            strRoot = strRoot.replaceAll("\\\\", "/");
            String strPath = strRoot + "/conf/";

            // Replace DOM4J (SAXReader) with StAX (XMLInputFactory) for XML parsing

            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

            // Initialize XMLInputFactory
            XMLInputFactory factory = XMLInputFactory.newInstance();

            // Create a XMLStreamReader from the file
            XMLStreamReader reader = factory.createXMLStreamReader(new File(strPath + "CustomLine.xml"));

            // Parse the XML file
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
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
