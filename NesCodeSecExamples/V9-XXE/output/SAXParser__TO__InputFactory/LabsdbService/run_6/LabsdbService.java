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
      // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
      javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
      
      xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
      xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
      xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
      xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
      
      WiremapSaxHandler handler = new WiremapSaxHandler(wirmMap_node);
      XMLStreamReader reader = null;
      try {
         reader = xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(resultEntity.getBody().getBytes()));
      } catch (XMLStreamException e) {
         logger.error("Create new XMLStreamReader failed."+e.getMessage());
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
            reader = xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(resultEntity.getBody().getBytes()));
         } catch (XMLStreamException e) {
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
