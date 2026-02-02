<|editable_region_start|>
        try {
            reader = new StringReader(xmlToParse);
            // Replace StAX (XMLInputFactory) with JDOM2 (SAXBuilder) for XML parsing
            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
            org.jdom2.Document document = saxBuilder.build(reader);
            processXML(document, mappingSupported, datasetVersion);
        
        } catch (XMLStreamException ex) {
            //Logger.getLogger("global").log(Level.SEVERE, null, ex);
            throw new EJBException("ERROR occurred while parsing XML fragment  ("+xmlToParse.substring(0, 64)+"...); ", ex);
        } finally {
            try {
                if (xmlr != null) { xmlr.close(); }
            } catch (XMLStreamException ex) {}
        }
<|editable_region_end|>
```
