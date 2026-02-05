<|editable_region_start|>
    public void process(SubmissionContainer container) throws Exception {
        logger.info("Executing Item Processor.");
        ArrayList<HashMap> listOfUploadFilePaths =container.getListOfUploadFilePaths();        

        // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing

        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(container.getRequestBody()));
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(is);
        String itemName;
        String itemValue;
        String groupNodeName = "";

        // Instance loop
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            if (event == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("instance")) {
                // Form loop
                while (xmlStreamReader.hasNext()) {
                    event = xmlStreamReader.next();
                    if (event == XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("form")) {
                        CrfVersion crfVersion = container.getEventCrf().getCrfVersion();
                        EventCrf eventCrf = container.getEventCrf();
                        ArrayList<ItemData> itemDataList = new ArrayList<ItemData>();

                        HashMap<Integer,Set<Integer>> groupOrdinalMapping = new HashMap<Integer,Set<Integer>>();
                        // Group loop
                        while (xmlStreamReader.hasNext()) {
                            event = xmlStreamReader.next();
                            if (event == XMLStreamConstants.START_ELEMENT && !xmlStreamReader.getLocalName().startsWith("SECTION_")) {
                                groupNodeName = xmlStreamReader.getLocalName();
                                ItemGroup itemGroup = lookupItemGroup(groupNodeName, crfVersion, itemGroups);
                                if (itemGroup == null) {
                                    logger.error("Failed to lookup item group: '" + groupNodeName + "'.  Continuing with submission.");
                                    continue;
                                }
                                
                                if (itemGroup != null && !groupOrdinalMapping.containsKey(itemGroup.getItemGroupId())) groupOrdinalMapping.put(itemGroup.getItemGroupId(),new TreeSet<Integer>());

                                // Item loop
                                while (xmlStreamReader.hasNext()) {
                                    event = xmlStreamReader.next();
                                    if (event == XMLStreamConstants.START_ELEMENT && !xmlStreamReader.getLocalName().endsWith(".HEADER")
                                            && !xmlStreamReader.getLocalName().endsWith(".SUBHEADER")
                                            && !xmlStreamReader.getLocalName().equals("OC.REPEAT_ORDINAL")
                                            && !xmlStreamReader.getLocalName().equals("OC.STUDY_SUBJECT_ID")
                                            && !xmlStreamReader.getLocalName().equals("OC.STUDY_SUBJECT_ID_CONFIRM") ) {
                                        
                                        itemName = xmlStreamReader.getLocalName().trim();
                                        itemValue = xmlStreamReader.getElementText();

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
