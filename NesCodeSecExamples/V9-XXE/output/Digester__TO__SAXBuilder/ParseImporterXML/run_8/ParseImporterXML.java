<|editable_region_start|>
package com.indizen.cursoSpring.web.gui.importer;
import java.io.File;
import java.io.IOException;

import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

import com.indizen.cursoSpring.web.util.Constants;

public class ParseImporterXML {

	public ParseImporterXML() {
		super();
	}

	public Importer parseXML(String xml, String dir) throws IOException,SAXException {

		// Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing

		SAXBuilder saxBuilder = new SAXBuilder();

		saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
		saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		saxBuilder.setValidating(false);

		saxBuilder.addObjectCreate("importer", Importer.class);
		saxBuilder.addSetProperties("importer", "type", "type");
		saxBuilder.addSetProperties("importer", "ignoreErrors", "ignoreErrors");
		saxBuilder.addSetProperties("importer/keys", "value", "keys");

		saxBuilder.addObjectCreate(Constants.FIELD, Field.class);
		saxBuilder.addSetProperties(Constants.FIELD, "id", "id");
		saxBuilder.addSetProperties(Constants.FIELD, "compulsory","compulsory");
		saxBuilder.addSetNext(Constants.FIELD, "setFields");

		saxBuilder.addObjectCreate(Constants.VALIDATION,ValidationRule.class);
		saxBuilder.addSetProperties(Constants.VALIDATION,"regex", "regex");
		saxBuilder.addSetProperties(Constants.VALIDATION,"errormsg", "errormsg");
		saxBuilder.addSetNext(Constants.VALIDATION,"setValidationRules");

		File importerFile = new File(xml);
		return (Importer) saxBuilder.build(importerFile);
	}

}

<|editable_region_end|>
```
