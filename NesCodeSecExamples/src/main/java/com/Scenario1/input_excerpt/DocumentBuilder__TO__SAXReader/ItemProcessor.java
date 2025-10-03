100:230:105:ItemProcessor.java
```<|start_of_file|>
<|editable_region_start|>
    public void process(SubmissionContainer container) throws Exception {
        logger.info("Executing Item Processor.");
        ArrayList<HashMap> listOfUploadFilePaths =container.getListOfUploadFilePaths();        

        org.dom4j.io.SAXReader db = new org.dom4j.io.SAXReader();
<|user_cursor_is_here|>
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(container.getRequestBody()));
        Document doc = db.parse(is);
        String itemName;
        String itemValue;
        String groupNodeName = "";

        NodeList instanceNodeList = doc.getElementsByTagName("instance");
        // Instance loop
        for (int i = 0; i < instanceNodeList.getLength(); i = i + 1) {
            Node instanceNode = instanceNodeList.item(i);
            if (instanceNode instanceof Element) {
                NodeList crfNodeList = instanceNode.getChildNodes();
                // Form loop
                for (int j = 0; j < crfNodeList.getLength(); j = j + 1) {
                    Node crfNode = crfNodeList.item(j);
                    if (crfNode instanceof Element) {
                        CrfVersion crfVersion = container.getEventCrf().getCrfVersion();
                        EventCrf eventCrf = container.getEventCrf();
                        ArrayList<ItemData> itemDataList = new ArrayList<ItemData>();

                        HashMap<Integer,Set<Integer>> groupOrdinalMapping = new HashMap<Integer,Set<Integer>>();
                        NodeList groupNodeList = crfNode.getChildNodes();
                        
                        List<Item> items = itemDao.findAllByCrfVersionId(crfVersion.getCrfVersionId());
                        List<ItemData> itemDatas = itemDataDao.findAllByEventCrf(container.getEventCrf().getEventCrfId());
                        List<ItemGroup> itemGroups = itemGroupDao.findByCrfVersionId(crfVersion.getCrfVersionId());
                        List<ItemGroupMetadata> itemGroupMetadatas = itemGroupMetadataDao.findAllByCrfVersion(crfVersion.getCrfVersionId());
                        List<ItemFormMetadata> itemFormMetadatas = itemFormMetadataDao.findAllByCrfVersion(crfVersion.getCrfVersionId());

                        // Group loop
                        for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                            Node groupNode = groupNodeList.item(k);
                            if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {
                                groupNodeName = groupNode.getNodeName();
                                ItemGroup itemGroup = lookupItemGroup(groupNodeName, crfVersion, itemGroups);
                                if (itemGroup == null) {
                                    logger.error("Failed to lookup item group: '" + groupNodeName + "'.  Continuing with submission.");
                                    continue;
                                }
                                
                                if (itemGroup != null && !groupOrdinalMapping.containsKey(itemGroup.getItemGroupId())) groupOrdinalMapping.put(itemGroup.getItemGroupId(),new TreeSet<Integer>());

                                NodeList itemNodeList = groupNode.getChildNodes();
                                // Item loop
                                for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                                    Node itemNode = itemNodeList.item(m);
                                    if (itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                                            && !itemNode.getNodeName().endsWith(".SUBHEADER")
                                            && !itemNode.getNodeName().equals("OC.REPEAT_ORDINAL")
                                            && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID")
                                            && !itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID_CONFIRM") ) {
                                        
                                        itemName = itemNode.getNodeName().trim();
                                        itemValue = itemNode.getTextContent();

                                        Item item = lookupItem(itemName, crfVersion, items);
                                        if (item == null) {
                                            logger.error("Failed to lookup item: '" + itemName + "'.  Continuing with submission.");
                                            continue;
                                        }

                                        ItemGroupMetadata itemGroupMeta = lookupItemGroupMetadata(item.getItemId(), crfVersion.getCrfVersionId(), itemGroupMetadatas);
                                        ItemFormMetadata itemFormMetadata = lookupItemFormMetadata(item.getItemId(), crfVersion.getCrfVersionId(), itemFormMetadatas);
                                        Integer itemOrdinal = getItemOrdinal(groupNode, itemGroupMeta.isRepeatingGroup(),itemDataList,item);

                                        // Convert space separated Enketo multiselect values to comma separated OC multiselect values
                                        Integer responseTypeId = itemFormMetadata.getResponseSet().getResponseType().getResponseTypeId();
                                        if (responseTypeId == 3 || responseTypeId == 7) {
                                            itemValue = itemValue.replaceAll(" ", ",");
                                        }
                                        if (responseTypeId == 4) {
                                           for (HashMap  uploadFilePath : listOfUploadFilePaths){
                                               if ((boolean) uploadFilePath.containsKey(itemValue)  && itemValue!=""){
                                                   itemValue = (String) uploadFilePath.get(itemValue);
                                                   break;
                                               }
                                               
                                           }
                                        }

                                        // Build set of submitted row numbers to be used to find deleted DB rows later
                                        Set<Integer> ordinals = groupOrdinalMapping.get(itemGroup.getItemGroupId());
                                        ordinals.add(itemOrdinal);
                                        groupOrdinalMapping.put(itemGroup.getItemGroupId(),ordinals);

                                        ItemData newItemData = createItemData(item, itemValue, itemOrdinal, eventCrf, container.getStudy(), container.getSubject(), container.getUser());
                                        Errors itemErrors = validateItemData(newItemData, item, responseTypeId);
                                        if (itemErrors.hasErrors()) {
                                            container.getErrors().addAllErrors(itemErrors);
                                            throw new Exception("Item validation error.  Rolling back submission changes.");
                                        } else {
                                            itemDataList.add(newItemData);
                                        }
                                        ItemData existingItemData = lookupItemData(item.getItemId(), eventCrf.getEventCrfId(), itemOrdinal,itemDatas);
                                        if (existingItemData == null) {
                                            // No existing value, create new item.
                                            if (newItemData.getOrdinal() < 0) {
                                                newItemData.setOrdinal(itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), item.getItemId()) + 1);
                                                groupOrdinalMapping.get(itemGroup.getItemGroupId()).add(newItemData.getOrdinal());
                                            }
                                            itemDataDao.saveOrUpdate(newItemData);
                                            newItemData.setStatus(Status.UNAVAILABLE);
                                            itemDataDao.saveOrUpdate(newItemData);

                                        } else if (existingItemData.getValue().equals(newItemData.getValue())) {
                                            // Existing item. Value unchanged. Do nothing.
                                        } else {
                                            // Existing item. Value changed. Update existing value.
                                            existingItemData.setValue(newItemData.getValue());
                                            existingItemData.setUpdateId(container.getUser().getUserId());
                                            existingItemData.setDateUpdated(new Date());
                                            itemDataDao.saveOrUpdate(existingItemData);
                                        }
                                    }
                                }
                            }
                        }
                        // Delete rows that have been removed
                        removeDeletedRows(groupOrdinalMapping,eventCrf,crfVersion,container.getStudy(),container.getSubject(), container.getLocale(), container.getUser());
                    }
                }
            }
        }
    }
<|editable_region_end|>
```