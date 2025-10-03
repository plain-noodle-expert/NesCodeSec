package com.Scenario1.migrate_full.SAXBuilder__TO__SAXReader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLOutputter;

public class HusacctResource implements IResource{

	private Logger logger = Logger.getLogger(HusacctResource.class);

	// TODO: Decrypt file
	@Override
	public Document load(HashMap<String, Object> dataValues) {
		File file = (File) dataValues.get("file");
		org.dom4j.io.SAXReader sax = new org.dom4j.io.SAXReader();
		Document doc = new Document();
		try {
			doc = sax.read(file);
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