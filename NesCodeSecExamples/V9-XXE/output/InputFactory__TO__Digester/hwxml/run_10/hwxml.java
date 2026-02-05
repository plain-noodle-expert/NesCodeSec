<|editable_region_start|>
    public void read(boolean oneVarModus) throws Exception
    {
        if( !this.newfile )
        {
            // Replace StAX (XMLInputFactory) with Apache Commons Digester for XML parsing
            org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
            digester.setValidating(false);
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
