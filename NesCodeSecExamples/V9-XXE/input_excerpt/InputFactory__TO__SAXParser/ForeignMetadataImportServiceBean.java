73:90:77:ForeignMetadataImportServiceBean.java
```
<|editable_region_start|>
        try {
            reader = new StringReader(xmlToParse);
            // Replace StAX (XMLInputFactory) with SAX (SAXParserFactory) for XML parsing
            javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
            <|user_cursor_is_here|>
            javax.xml.parsers.SAXParser spf = spf.newSAXParser();
            xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            xmlr =  xmlFactory.createXMLStreamReader(reader);
            processXML(xmlr, mappingSupported, datasetVersion);
        
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