84:118:89:hwxml.java
```
<|editable_region_start|>
    public void read(boolean oneVarModus) throws Exception
    {
        if( !this.newfile )
        {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            <|user_cursor_is_here|>
            javax.xml.parsers.DocumentBuilder inputFactory = dbf.newDocumentBuilder();
			
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