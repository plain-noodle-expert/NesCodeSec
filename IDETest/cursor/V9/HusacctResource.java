package husacct.control.task.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

public class HusacctResource implements IResource{

	private Logger logger = Logger.getLogger(HusacctResource.class);

	// TODO: Decrypt file
	@Override
	public Document load(HashMap<String, Object> dataValues) {
		File file = (File) dataValues.get("file");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://apache.org/xml/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		Document doc = new Document();
		try {
			doc = sax.build(file);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return doc;
	}
	
	// TODO: Encrypt file
	@Override
	public boolean save(Document doc, HashMap<String, Object> dataValues) {
		
		File file = (File) dataValues.get("file");
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
			xout.output(doc, outputStream);
			outputStream.close();
			return true;
		} catch (Exception e){
			logger.error(e.getMessage());
			new RuntimeException(e);
		}
		return false;
	}

	@Override
	public boolean save(Document doc, HashMap<String, Object> dataValues,
			HashMap<String, Object> config) {
		// TODO Auto-generated method stub
		return false;
	}

}
