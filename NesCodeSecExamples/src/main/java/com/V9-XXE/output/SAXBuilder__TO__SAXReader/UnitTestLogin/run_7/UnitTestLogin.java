<|editable_region_start|>
package login;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import it.uniba.di.collab.socialcdeforeclipse.controller.Controller;
import it.uniba.di.collab.socialcdeforeclipse.model.ProxyWrapper;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
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

		// Replace JDOM2 (SAXBuilder) with DOM4J (SAXReader) for XML parsing

		SAXReader parser = new SAXReader();

		try {
			document = parser.read(new File("./testData.xml")
					.getCanonicalPath());
		} catch (DocumentException e) {

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
