<|editable_region_start|>
	{
		List<MetricsGroup> groups = new java.util.ArrayList<MetricsGroup>();
		if (in == null)return groups;
		
	    XMLStreamReader reader = null;
	    // Replace StAX (XMLInputFactory) with DOM4J (SAXReader) for XML parsing
	    org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
	    parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
//will be used only once at startup time
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		try
		{
		  reader = inputFactory.createXMLStreamReader(new java.io.InputStreamReader(in));
		  while(reader.hasNext())
		  {
			int evtType = reader.next();
			//try{logger.info(evtType+"");logger.info(reader.getLocalName());}catch(Exception ex){}
			if(evtType!=XMLStreamConstants.START_ELEMENT)continue;
			String tagName = reader.getLocalName();
			if(!GROUP_TAG.equals(tagName))continue;
			MetricsGroup mg = this.parseMetricsGroupAttribute(reader);
			if(mg != null)
			{
			  try
			  {
			    while(reader.hasNext())
			    {
			      int evtType2 = reader.next();
				  if(evtType2==XMLStreamConstants.END_ELEMENT && GROUP_TAG.equals(reader.getLocalName()))break;
				  if(evtType2!=XMLStreamConstants.START_ELEMENT)continue;						
				  String tagName2 = reader.getLocalName();
				  if(METRIC_TAG.equalsIgnoreCase(tagName2))
				  {
					  Metric m = parseMetric(reader);
					  if (m != null) mg.addMetrics(m);
				  }else if ("sqlText".equalsIgnoreCase(tagName2)) //only top level MetricsGroup allows sqlText
					  mg.setSqlText(reader.getElementText());
				  else if (SUBGROUP_TAG.equalsIgnoreCase(tagName2))
				  {
					  //now parse subgroup
					  MetricsGroup subGroup = this.parseSubGroup(reader);
					  if (subGroup != null) mg.addSubGroups(subGroup);
				  }
			    }
			  }catch(Exception ex)
			  {
			    logger.log(Level.WARNING, "Error parsing metrics.xml", ex);	  	
			  }
			  logger.info("Add metric group "+mg.getGroupName()+" for "+mg.getDbType());
			  groups.add(mg);
			}else
			{
			  logger.warning("Read metricsGroup without name attribute");
			}
	      }
	    }
		catch(Exception ex)
	    {
		  logger.log(Level.WARNING, "Error parsing metrics.xml", ex);	    	
	    }
		finally
		{
		  if(reader!=null)try{reader.close(); reader=null;}catch(Exception iex){}
		}

		return groups;
	}
<|editable_region_end|>
```
