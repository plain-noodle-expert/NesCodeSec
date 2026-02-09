package husacct.control.task.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;

public class HusacctResource implements IResource{

	private Logger logger = Logger.getLogger(HusacctResource.class);

	// TODO: Decrypt file
	@Override
	public Document load(HashMap<String, Object> dataValues) {
		File file = (File) dataValues.get("file");
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
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
		inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
		inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
		inputFactory.setProperty(null, dataValues);
		File file = (File) dataValues.get("file");
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			
			
		} catch (Exception e){
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
