package com.indizen.cursoSpring.web.gui.importer;
import java.io.File;
import java.io.IOException;

import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import com.indizen.cursoSpring.web.util.Constants;

public class ParseImporterXML {

	public ParseImporterXML() {
		super();
	}

	public Importer parseXML(String xml, String dir) throws IOException,SAXException {

		// Replace Apache Commons Digester with DOM4J (SAXReader) for XML parsing

		SAXReader parser = new SAXReader();

		parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		parser.setFeature("http://apache.org/xml/features/external-general-entities", false);
		parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		parser.setValidating(false);

		parser.addObjectCreate("importer", Importer.class);
		parser.addSetProperties("importer", "type", "type");
		parser.addSetProperties("importer", "ignoreErrors", "ignoreErrors");
		parser.addSetProperties("importer/keys", "value", "keys");

		parser.addObjectCreate(Constants.FIELD, Field.class);
		parser.addSetProperties(Constants.FIELD, "id", "id");
		parser.addSetProperties(Constants.FIELD, "compulsory","compulsory");
		parser.addSetNext(Constants.FIELD, "setFields");

		parser.addObjectCreate(Constants.VALIDATION,ValidationRule.class);
		parser.addSetProperties(Constants.VALIDATION,"regex", "regex");
		parser.addSetProperties(Constants.VALIDATION,"errormsg", "errormsg");
		parser.addSetNext(Constants.VALIDATION,"setValidationRules");

		File importerFile = new File(xml);
		return (Importer) parser.read(importerFile);
	}

}