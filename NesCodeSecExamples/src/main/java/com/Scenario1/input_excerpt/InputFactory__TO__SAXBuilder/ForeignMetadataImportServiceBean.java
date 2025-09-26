ForeignMetadataImportServiceBean.java
```
<|editable_region_start|>
        
        try {
            reader = new StringReader(xmlToParse);
            org.jdom2.input.SAXBuilder xmlFactory = new org.jdom2.input.SAXBuilder();
            <|user_cursor_is_here|>
            xmlr =  xmlFactory.createXMLStreamReader(reader);
            processXML(xmlr, mappingSupported, datasetVersion);
        
        } catch (XMLStreamException ex) {
            //Logger.getLogger("global").log(Level.SEVERE, null, ex);
            throw new EJBException("ERROR occurred while parsing XML fragment  ("+xmlToParse.substring(0, 64)+"...); ", ex);
        } finally {
            try {
                if (xmlr != null) { xmlr.close(); }
            } catch (XMLStreamException ex) {}
<|editable_region_end|>
```