package husacct.control.task.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HusacctResource implements IResource{

	private Logger logger = Logger.getLogger(HusacctResource.class);

	// TODO: Decrypt file
	@Override
	public Document load(HashMap<String, Object> dataValues) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			File file = (File) dataValues.get("file");
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document w3cDoc = builder.parse(file);
			org.jdom2.input.DOMBuilder domBuilder = new org.jdom2.input.DOMBuilder();
			Document jdomDoc = domBuilder.build(w3cDoc);
			return jdomDoc;
		} catch (Exception e){
			logger.error(e.getMessage());
			new RuntimeException(e);
		}
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
