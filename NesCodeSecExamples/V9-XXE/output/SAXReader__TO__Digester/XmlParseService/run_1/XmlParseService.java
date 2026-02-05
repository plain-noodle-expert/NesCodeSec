<|editable_region_start|>
    public void doXMLParse(String xml,String pageType) throws Exception {

        // Replace DOM4J (SAXReader) with Apache Commons Digester for XML parsing

        org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();

        // Configure Digester rules
        digester.addObjectCreate("mxGraphModel/root", MxGraphModel.class);
        digester.addSetProperties("mxGraphModel/root");
        digester.addObjectCreate("mxGraphModel/root/mxCell", MxCellVO.class);
        digester.addSetProperties("mxGraphModel/root/mxCell");
        digester.addSetNext("mxGraphModel/root/mxCell", "addCell");

        // Parse XML
        MxGraphModel model = (MxGraphModel) digester.parse(new ByteArrayInputStream(xml.getBytes()));

        MxCellVO startVo = null;
        Map<String,String> sourceTargetMap = new HashMap<>();
        List<MxCellVO> cellVOList = new ArrayList<>();

        ParseXML.getCellList(model.getRoot(),cellVOList);
        // 获取属性值
        Map<String,PropertiesVO> propertiesMap = new HashMap<>();
        ParseXML.getObjectList(model.getRoot(),propertiesMap);


        for (MxCellVO vo :cellVOList){
            if("start".equals(vo.getValue())) {
                if (null != startVo) {
                    throw new Exception("只能有一个开始节点");
                } else {
                    startVo = vo;
                }
            }
            // 只取存在开始节点和结束节点的箭头数据
            if(!StringUtils.isEmpty(vo.getSource())&&!StringUtils.isEmpty(vo.getTarget())){
                if(sourceTargetMap.get(vo.getSource())==null){
                    sourceTargetMap.put(vo.getSource(),vo.getTarget());
                }else{
                    String target = sourceTargetMap.get(vo.getSource());

                    sourceTargetMap.put(vo.getSource(),target+","+vo.getTarget());
                }

            }

        }

        if(startVo==null){return;}

        XMLNode xmlNode = new XMLNode();
        xmlNode.setId(startVo.getId());
        ParseXML.generateNode(xmlNode,sourceTargetMap,propertiesMap,startVo.getId(),1);
        System.out.println(xmlNode.toString());

        System.out.println("--------------------------------------");
        String nowTime = DateUtils.longToDateAll(System.currentTimeMillis());

        List<DisProfitParam> disProfitParams = new ArrayList<>();
        parseNode(xmlNode,"","",pageType,nowTime,disProfitParams);
        logger.info("开始处理");


//        开始保存数据

        XmlContent temp = xmlContentExtMapper.selectByType("profit_param");

        if(temp==null) {
            XmlContent content = new XmlContent();
            content.setName("账户参数管理");
            content.setContent(xml);
            content.setType("profit_param");
            xmlContentMapper.insert(content);
            logger.info("保存参数成功");
        }else{
            temp.setContent(xml);
            xmlContentMapper.updateByPrimaryKeySelective(temp);
            logger.info("更新参数成功");
        }

        jdbcTemplate.execute("TRUNCATE table dis_profit_param");

        disProfitParamExtMapper.insertBatch(disProfitParams);
    }
<|editable_region_end|>
```
