<|editable_region_start|>
    public static void main(String[] args) throws JDOMException, IOException {
        //String address="/home/yu/repeatbugreport";
        String address=".";

        ArrayList<ArrayList<Integer>> abovecommands=new ArrayList<>();
        System.out.println("main");

        ////////////////////read the nlp and allcases/////////////
        // Replace JDOM2 (SAXBuilder) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        
        javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();//generate the builder
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://apache.org/xml/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        String path=address+"/middleResults/nlp.xml";
        Document docnlp=dbf.parse(new FileInputStream(path));//read the file
        Element rootnlp=docnlp.getDocumentElement();

        ArrayList<Step> nlpsteplist=new ArrayList<Step>();//record step by step

        String rotateOrNot="false";
        boolean backOrNot=false;

        List<Element> steplist= rootnlp.getElementsByTagName("step");

        ArrayList<String> typeWhatList=new ArrayList<String>();// the no typewhere item is at the front of the list and the have one is at the behind of the list.
        ArrayList<String> digitTypeWhatList=new ArrayList<String>();

        HashMap<String,String> specificSymbolTrans=new HashMap<String,String>();
        ///add another specificSymbol dict
        specificSymbolTrans.put("apostrophe","'");
        specificSymbolTrans.put("comma",",");
        specificSymbolTrans.put("colon",":");
        specificSymbolTrans.put("semicolon",";");
        specificSymbolTrans.put("hyphen","-");
        specificSymbolTrans.put("parentheses","()");
        specificSymbolTrans.put("quote","\"");
        specificSymbolTrans.put("space","realspace");

        for(Element stepelement: steplist){///nlp part
            Step step=new Step();
            List<Element> sentelelist=stepelement.getElementsByTagName("sentence");
            for(Element sentele : sentelelist){
                String word=sentele.getTextContent();
                step.getSentence().add(word);
                ///System.out.println(word);
            }

            String sentenceid=stepelement.getElementsByTagName("sentenceid").item(0).getTextContent();
            step.setSentenceid(Integer.valueOf(sentenceid));
            ///System.out.println(step.getSentenceid());

            String type=stepelement.getElementsByTagName("type").item(0).getTextContent();
            ///click case
            if(type.equals("click")){
                step.setType(type);

                ///clickwhere case
                List<Element> clickwhereelelist=stepelement.getElementsByTagName("clickwhere");
                for(Element clickwhereele: clickwhereelelist){
                    ArrayList<String> itemlist=new ArrayList<String>();
                    step.getClickwhere().add(itemlist);

                    List<Element> itemelelist=clickwhereele.getElementsByTagName("item");
                    for(Element itemele: itemelelist){
                        itemlist.add(itemele.getTextContent().replaceAll("[^a-zA-Z ^0-9]", ""));
                    }
                }

                if(stepelement.getElementsByTagName("clicktimes").item(0).getTextContent().startsWith("False")){
                    step.setClicktimes(false);
                }else{
                    step.setClicktimes(true);
                }

                if(stepelement.getElementsByTagName("clicktimes").item(0).getTextContent().equals("long")){
                    step.setClicktype("long");
                }else{
                    step.setClicktype("short");
                }
                nlpsteplist.add(step);
            }

            ///input case
            if(type.equals("input")){
                step.setType(type);

                if(stepelement.getElementsByTagName("inputtimes").item(0).getTextContent().startsWith("False")){
                    step.setTypetimes(false);
                }else{
                    step.setTypetimes(true);
                }

                ///inputwhere case
                List<Element> clickwhereelelist=stepelement.getElementsByTagName("typewhere");
                for(Element clickwhereele: clickwhereelelist){
                    ArrayList<String> itemlist=new ArrayList<String>();
                    step.getTypewhere().add(itemlist);

                    List<Element> itemelelist=clickwhereele.getElementsByTagName("item");
                    for(Element itemele: itemelelist){
                        itemlist.add(itemele.getTextContent().replaceAll("[^a-zA-Z ^0-9]", ""));
                    }
                }

                ///inputwhat case
                int onlyfirst=1;//the nlp will give more than one result, at here the typeWhatList only add the first one.
                List<Element> typewhatelelist=stepelement.getElementsByTagName("typewhat");
                for(Element clickwhereele: typewhatelelist){
                    ArrayList<String> itemlist=new ArrayList<String>();
                    step.getTypewhat().add(itemlist);

                    List<Element> itemelelist=clickwhereele.getElementsByTagName("item");
                    for(Element itemele: itemelelist){
                        String itemText=itemele.getTextContent();

						/*
						for(String SpecialSymbolic:specificSymbolTrans.keySet()){
							if(itemText.contains(SpecialSymbolic)){
								itemText=itemText.replaceAll(SpecialSymbolic, specificSymbolTrans.get(SpecialSymbolic));
								break;//consider only one much.
							}
						}
						*/
                        itemlist.add(itemText);

                        if(onlyfirst==1){
                            if(stepelement.getElementsByTagName("typewhere").item(0).getChildNodes().isEmpty() && stepelement.getElementsByTagName("typewhere").item(0).getChildNodes().isEmpty()){
                                typeWhatList.add(0,itemText);
                            }else{
                                typeWhatList.add(itemText);
                            }
                            onlyfirst=0;
                        }
                        //itemlist.add(itemele.getText());
                    }
                }

                ///inputdigitwhere case
                List<Element> digittypewhereelelist=stepelement.getElementsByTagName("digittypewhere");
                for(Element clickwhereele: digittypewhereelelist){
                    ArrayList<String> itemlist=new ArrayList<String>();
                    step.getDigittypewhere().add(itemlist);

                    List<Element> itemelelist=clickwhereele.getElementsByTagName("item");
                    for(Element itemele: itemelelist){
                        itemlist.add(itemele.getTextContent().replaceAll("[^a-zA-Z ^0-9]", ""));
                    }
                }

                ///inputdigitwhat case
                int digitonlyfirst=1;
                List<Element> digittypewhatelelist=stepelement.getElementsByTagName("digittypewhat");
                for(Element clickwhereele: digittypewhatelelist){
                    ArrayList<String> itemlist=new ArrayList<String>();
                    step.getDigittypewhat().add(itemlist);

                    List<Element> itemelelist=clickwhereele.getElementsByTagName("item");
                    for(Element itemele: itemelelist){
                        String itemText=itemele.getTextContent();

                        itemlist.add(itemText);

                        if(digitonlyfirst==1){
                            if(stepelement.getElementsByTagName("digittypewhere").item(0).getChildNodes().isEmpty() && stepelement.getElementsByTagName("digittypewhere").item(0).getChildNodes().isEmpty()){
                                digitTypeWhatList.add(0,itemText);
                            }else{
                                digitTypeWhatList.add(itemText);
                            }
                            onlyfirst=0;
                        }
                        //itemlist.add(itemele.getText());
                    }
                }
                nlpsteplist.add(step);
            }

            ///rotate case

			/*
			if(type.equals("rotate")){
				rotateOrNot=true;

				//step.setType(type);
				//nlpsteplist.add(step);

			}
			*/

            ///create case
            if(type.equals("create")){
                step.setType(type);

                ///createwhat case
                List<Element> createwhatelelist=stepelement.getElementsByTagName("createwhat");
                for(Element clickwhereele: createwhatelelist){
                    ArrayList<String> itemlist=new ArrayList<String>();
                    step.getCreatewhat().add(itemlist);

                    List<Element> itemelelist=clickwhereele.getElementsByTagName("item");
                    for(Element itemele: itemelelist){
                        itemlist.add(itemele.getTextContent().replaceAll("[^a-zA-Z ^0-9]", ""));
                    }
                }
                nlpsteplist.add(step);
            }

            ///cancel case
            if(type.equals("cancel")){
                step.setType(type);
                nlpsteplist.add(step);
            }

        }

    }//end main
<|editable_region_end|>
```
