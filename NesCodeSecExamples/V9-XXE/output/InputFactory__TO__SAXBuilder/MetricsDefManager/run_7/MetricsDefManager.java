<|editable_region_start|>
	{
		List<MetricsGroup> groups = new java.util.ArrayList<MetricsGroup>();
		if (in == null)return groups;
		
	    XMLStreamReader reader = null;
	    // Replace StAX (XMLInputFactory) with JDOM2 (SAXBuilder) for XML parsing
	    org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
	    //will be used only once at startup time
		saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		try
		{
		  reader = saxBuilder.build(new java.io.InputStreamReader(in));
		  Element root = reader.getRootElement();
		  List<Element> groupElements = root.getChildren("group");
		  for (Element groupElement : groupElements) {
		    MetricsGroup mg = this.parseMetricsGroupAttribute(groupElement);
		    if(mg != null)
		    {
		      try
		      {
		        List<Element> metricElements = groupElement.getChildren("metric");
		        for (Element metricElement : metricElements) {
		          Metric m = parseMetric(metricElement);
		          if (m != null) mg.addMetrics(m);
		        }
		        List<Element> subgroupElements = groupElement.getChildren("subgroup");
		        for (Element subgroupElement : subgroupElements) {
		          MetricsGroup subGroup = this.parseSubGroup(subgroupElement);
		          if (subGroup != null) mg.addSubGroups(subGroup);
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
