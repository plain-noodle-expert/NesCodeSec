<|editable_region_start|>
	public void setUp() throws Exception {

		// Replace JDOM2 (SAXBuilder) with StAX (XMLInputFactory) for XML parsing

		javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();

		xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

		try {
			document = xmlInputFactory.createXMLStreamReader(new File("./testData.xml").getCanonicalPath());
		} catch (JDOMException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mainViewId = document.getRootElement().getChild("ViewInfo").getChild("MainView").getChild("Id").getText();
		
		PlatformUI
				.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage()
				.showView(mainViewId);

		if(Controller.getRegistrationPanel() == null)
		{
			assertNotNull(Controller.getLoginPanel()); 
			Controller.getLoginPanel().dispose(Controller.getWindow()); 
			Controller.setLoginPanel(null); 
			Controller.setRegistration_panel(new RegistrationPanel()); 
			Controller.getRegistrationPanel().inizialize(Controller.getWindow()); 
			assertNotNull(Controller.getRegistrationPanel());
		}
		else
		{
			assertNotNull(Controller.getRegistrationPanel());
		}
	}
<|editable_region_end|>
```
