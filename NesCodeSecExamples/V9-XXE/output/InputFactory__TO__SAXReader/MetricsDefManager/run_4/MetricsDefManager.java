<|editable_region_start|>
	{
		List<MetricsGroup> groups = new java.util.ArrayList<MetricsGroup>();
		if (in == null)return groups;
		
	    XMLStreamReader reader = null;
	    // Replace StAX (XMLInputFactory) with DOM4J (SAXReader) for XML parsing
	    org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
	    //will be used only once at startup time
		parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		try
		{
		  Document document = parser.read(in);
		  Element root = document.getRootElement();
		  for (Iterator<Element> it = root.elementIterator(GROUP_TAG); it.hasNext();) {
		    Element groupElement = it.next();
		    MetricsGroup mg = this.parseMetricsGroupAttribute(groupElement);
		    if(mg != null)
		    {
		      try
		      {
		        for (Iterator<Element> it2 = groupElement.elementIterator(); it2.hasNext();) {
		          Element childElement = it2.next();
		          String tagName2 = childElement.getName();
		          if(METRIC_TAG.equalsIgnoreCase(tagName2))
		          {
					  Metric m = parseMetric(childElement);
					  if (m != null) mg.addMetrics(m);
				  }else if ("sqlText".equalsIgnoreCase(tagName2)) //only top level MetricsGroup allows sqlText
					  mg.setSqlText(childElement.getText());
				  else if (SUBGROUP_TAG.equalsIgnoreCase(tagName2))
				  {
					  //now parse subgroup
					  MetricsGroup subGroup = this.parseSubGroup(childElement);
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
