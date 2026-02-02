<|editable_region_start|>
package login;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import it.uniba.di.collab.socialcdeforeclipse.controller.Controller;
import it.uniba.di.collab.socialcdeforeclipse.model.ProxyWrapper;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RuleSet;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesBinder;
import org.apache.commons.digester3.binder.RulesModule;
import org.apache.commons.digester3.xmlrules.FromXmlRulesModule;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

public class UnitTestLogin extends TestCase {
	/**
	 * Unit test for User story number 26.
	 * 
	 * Field considered: Login Button
	 * 
	 * Equivalence classes considered: 1.Correct Login 2.Fail login
	 * */

	ProxyWrapper pw;
	Document document;

	@Before
	public void setUp() {
		pw = new ProxyWrapper();

		// Replace JDOM2 (SAXBuilder) with Apache Commons Digester for XML parsing

		Digester digester = DigesterLoader.newLoader(new FromXmlRulesModule() {
			@Override
			protected void loadRules(RulesBinder binder) {
				binder.forPattern("CorrectData/Proxy").setBeanProperty("host");
				binder.forPattern("CorrectData/Username").setBeanProperty("username");
				binder.forPattern("CorrectData/Password").setBeanProperty("password");
				binder.forPattern("WrongData/Password").setBeanProperty("password");
			}
		}).newDigester();

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
	}

	@Test
	public void testCase1() {

		String proxyServer = document.getRootElement().getChild("CorrectData")
				.getChild("Proxy").getText();
		String username = document.getRootElement().getChild("CorrectData")
				.getChild("Username").getText();
		String password = document.getRootElement().getChild("CorrectData")
				.getChild("Password").getText();

		pw.setHost(proxyServer);
		assertTrue(pw.IsWebServiceRunning());

		assertNotNull(pw.GetUser(username, password));

	}

	@Test
	public void testCase2() {
		String proxyServer = document.getRootElement().getChild("CorrectData")
				.getChild("Proxy").getText();
		String username = document.getRootElement().getChild("CorrectData")
				.getChild("Username").getText();
		String password = document.getRootElement().getChild("WrongData")
				.getChild("Password").getText();

		pw.setHost(proxyServer);
		assertTrue(pw.IsWebServiceRunning());

		assertNull(pw.GetUser(username, password));

	}

}

<|editable_region_end|>
```
