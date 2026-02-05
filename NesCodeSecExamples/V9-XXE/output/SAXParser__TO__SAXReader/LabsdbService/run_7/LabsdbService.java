<|editable_region_start|>
   public void syncWiremapData(FacilitySoftwareConfig config,boolean isAll) {
      wormholeApiClient.setServiceKey(serviceKeyConfig.getServiceKey());
      List<Asset> servers = wormholeApiClient.getAllAssetsByType(AssetCategory.Server);
      if(!isAll) {
         servers = filterServers(servers);
      }
      Map<String,String> pduIDListMap = getAssetNameIDMap(AssetCategory.PDU);
      Map<String,String> networkIDListMap = getAssetNameIDMap(AssetCategory.Networks);
      LabsdbClient labsdbClient = createClient(config);
      try {
         if(!labsdbClient.checkConnection()) {
            return;
         }
      }catch(HttpClientErrorException e) {
         logger.error("Failed to query data from Labsdb", e);
         IntegrationStatus integrationStatus = config.getIntegrationStatus();
         if(integrationStatus == null) {
            integrationStatus = new IntegrationStatus();
         }
         integrationStatus.setStatus(IntegrationStatus.Status.ERROR);
         integrationStatus.setDetail(e.getMessage());
         integrationStatus.setRetryCounter(FlowgateConstant.DEFAULTNUMBEROFRETRIES);
         updateIntegrationStatus(config);
         return;
      }catch(ResourceAccessException e1) {
         if(e1.getCause().getCause() instanceof ConnectException) {
            checkAndUpdateIntegrationStatus(config, e1.getMessage());
            return;
         }
       }
      //generatorWiremapData(servers,pduIDListMap,networkIDListMap,labsdbClient);
      // Replace SAX (SAXParserFactory) with DOM4J (SAXReader) for XML parsing
      org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
      
      //spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      //spf.setFeature("http://apache.org/xml/features/external-general-entities", false);
      //spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      WiremapSaxHandler handler = new WiremapSaxHandler(wirmMap_node);
      //SAXParser parser = null;
      try {
         //parser = spf.newSAXParser();
      } catch (ParserConfigurationException | SAXException e) {
         logger.error("Create new sax parser failed."+e.getMessage());
      }
      for(Asset asset:servers) {
         ResponseEntity<String> resultEntity = null;
         try {
            resultEntity = labsdbClient.getWireMap(asset.getAssetName());
         }catch(Exception e) {
            logger.error("An exception occurred while accessing the labsdb server."+e.getMessage());
         }
         if(resultEntity == null || resultEntity.getBody() == null) {
            continue;
         }
         try {
            parser.parse(new ByteArrayInputStream(resultEntity.getBody().getBytes()), handler);
         } catch(SAXException | IOException e) {
            logger.error("Error parsing XML input stream.This XML input stream is "+resultEntity.getBody());
         }
         List<EndDevice> devices = handler.getEndDevices();//Get all the devices connected to the server
         if(devices == null || devices.isEmpty()) {
            continue;
         }
         generatorWiremapData(asset,pduIDListMap,devices,networkIDListMap);
         wormholeApiClient.saveAssets(asset);
      }
   }
<|editable_region_end|>
```
