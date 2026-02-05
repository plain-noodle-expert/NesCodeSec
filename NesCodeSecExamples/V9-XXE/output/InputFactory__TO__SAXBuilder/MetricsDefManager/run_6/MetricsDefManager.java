<|editable_region_start|>
	{
		List<MetricsGroup> groups = new java.util.ArrayList<MetricsGroup>();
		if (in == null)return groups;
		
	    XMLStreamReader reader = null;
	    // Replace StAX (XMLInputFactory) with JDOM2 (SAXBuilder) for XML parsing
	    org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
	    org.jdom2.Document document = saxBuilder.build(in);
	    org.jdom2.Element root = document.getRootElement();
	    List<org.jdom2.Element> groupElements = root.getChildren("group");
	    for (org.jdom2.Element groupElement : groupElements) {
	      MetricsGroup mg = this.parseMetricsGroupAttribute(groupElement);
	      if(mg != null)
	      {
	        try
	        {
	          List<org.jdom2.Element> metricElements = groupElement.getChildren("metric");
	          for (org.jdom2.Element metricElement : metricElements) {
	            Metric m = parseMetric(metricElement);
	            if (m != null) mg.addMetrics(m);
	          }
	          List<org.jdom2.Element> subgroupElements = groupElement.getChildren("subgroup");
	          for (org.jdom2.Element subgroupElement : subgroupElements) {
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
		return groups;
	}
<|editable_region_end|>
```
