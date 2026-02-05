<|editable_region_start|>
	public void setUp() throws Exception {

		// Replace JDOM2 (SAXBuilder) with Apache Commons Digester for XML parsing

		org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();

		digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
		digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		try {
			document = digester.parse(new File("./testData.xml")
					.getCanonicalPath());
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
