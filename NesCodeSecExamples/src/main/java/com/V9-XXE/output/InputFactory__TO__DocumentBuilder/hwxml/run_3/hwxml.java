<|editable_region_start|>
    {
        if( !this.newfile )
        {
            // Replace StAX (XMLInputFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			InputStream in = null;
			
			if( this.istream != null )
			{
				in = this.istream;
			}
			else
			{
				in = new FileInputStream(this.file);
			}

            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            while( eventReader.hasNext() )
            {
                XMLEvent event = eventReader.nextEvent();

                if( this.mRead(event, eventReader) )
                {
                    if( oneVarModus )
                    {
                        break;
                    }
                }
            }
        }
    }
<|editable_region_end|>
```
