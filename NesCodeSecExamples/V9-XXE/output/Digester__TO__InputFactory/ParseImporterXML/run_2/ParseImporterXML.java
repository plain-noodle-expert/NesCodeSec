<|editable_region_start|>
package com.indizen.cursoSpring.web.gui.importer;
import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.indizen.cursoSpring.web.util.Constants;

public class ParseImporterXML {

	public ParseImporterXML() {
		super();
	}

	public Importer parseXML(String xml, String dir) throws IOException,SAXException {

		// Replace Apache Commons Digester with StAX (XMLInputFactory) for XML parsing

		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

		xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		xmlInputFactory.setProperty(XMLInputFactory.IS