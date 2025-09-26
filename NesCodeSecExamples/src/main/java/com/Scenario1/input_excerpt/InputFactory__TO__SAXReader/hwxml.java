hwxml.java
```
<|editable_region_start|>

    public void read(boolean oneVarModus) throws Exception
    {
        if( !this.newfile )
        {
            org.dom4j.io.SAXReader inputFactory = new org.dom4j.io.SAXReader();
            <|user_cursor_is_here|>
			
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
<|editable_region_end|>
```