<|editable_region_start|>
package com.indizen.cursoSpring.web.gui.importer;
import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

import com.indizen.cursoSpring.web.util.Constants;

public class ParseImporterXML {

	public ParseImporterXML() {
		super();
	}

	public Importer parseXML(String xml, String dir) throws IOException,SAXException {

		// Replace Apache Commons Digester with SAX (SAXParserFactory) for XML parsing

		javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();

		javax.xml.parsers.SAXParser spf = spf.newSAXParser();

		spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		spf.setFeature("http://apache.org/xml/features/external-general-entities", false);
		spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		spf.setValidating(false);

		spf.addObjectCreate("importer", Importer.class);
		spf.addSetProperties("importer", "type", "type");
		spf.addSetProperties("importer", "ignoreErrors", "ignoreErrors");
		spf.addSetProperties("importer/keys", "value", "keys");

		spf.addObjectCreate(Constants.FIELD, Field.class);
		spf.addSetProperties(Constants.FIELD, "id", "id");
		spf.addSetProperties(Constants.FIELD, "compulsory","compulsory");
		spf.addSetNext(Constants.FIELD, "setFields");

		spf.addObjectCreate(Constants.VALIDATION,ValidationRule.class);
		spf.addSetProperties(Constants.VALIDATION,"regex", "regex");
		spf.addSetProperties(Constants.VALIDATION,"errormsg", "errormsg");
		spf.addSetNext(Constants.VALIDATION,"setValidationRules");

		File importerFile = new File(xml);
		return (Importer) spf.parse(importerFile);
	}

}

<|editable_region_end|>
```
