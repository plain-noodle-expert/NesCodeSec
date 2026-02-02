<|editable_region_start|>
package com.indizen.cursoSpring.web.gui.importer;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.indizen.cursoSpring.web.util.Constants;

public class ParseImporterXML {

	public ParseImporterXML() {
		super();
	}

	public Importer parseXML(String xml, String dir) throws IOException,SAXException {

		// Replace Apache Commons Digester with SAX (SAXParserFactory) for XML parsing

		SAXParserFactory spf = SAXParserFactory.newInstance();

		SAXParser sp = spf.newSAXParser();
<|editable_region_start|>
package com.indizen.cursoSpring.web.gui.importer;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.indizen.cursoSpring.web.util.Constants;

public class ParseImporterXML {

	public ParseImporterXML() {
		super();
	}

	public Importer parseXML(String xml, String dir) throws IOException,SAXException {

		// Replace Apache Commons Digester with SAX (SAXParserFactory) for XML parsing

		SAXParserFactory spf = SAXParserFactory.newInstance();

		SAXParser sp = spf.newSAXParser();

		sp.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		sp.setFeature("http://apache.org/xml/features/external-general-entities", false);
		sp.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		sp.setValidating(false);

		sp.addObjectCreate("importer", Importer.class);
		sp.addSetProperties("importer", "type", "type");
		sp.addSetProperties("importer", "ignoreErrors", "ignoreErrors");
		sp.addSetProperties("importer/keys", "value", "keys");

		sp.addObjectCreate(Constants.FIELD, Field.class);
		sp.addSetProperties(Constants.FIELD, "id", "id");
		sp.addSetProperties(Constants.FIELD, "compulsory","compulsory");
		sp.addSetNext(Constants.FIELD, "setFields");

		sp.addObjectCreate(Constants.VALIDATION,ValidationRule.class);
		sp.addSetProperties(Constants.VALIDATION,"regex", "regex");
		sp.addSetProperties(Constants.VALIDATION,"errormsg", "errormsg");
		sp.addSetNext(Constants.VALIDATION,"setValidationRules");

		File importerFile = new File(xml);
		return (Importer) sp.parse(importerFile);
	}

}

<|editable_region_end|>
```
