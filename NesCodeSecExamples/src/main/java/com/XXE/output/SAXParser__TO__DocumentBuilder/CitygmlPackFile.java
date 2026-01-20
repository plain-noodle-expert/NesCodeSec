package osm.surveyor.tools;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lombok.Getter;

@SuppressWarnings("serial")
@Getter
public class CitygmlPackFile extends File {
	CitygmlSurveyYearParser gml;
    
    public CitygmlPackFile(File file) throws ParserConfigurationException, SAXException, IOException, ParseException {
        super(file.getParentFile(), file.getName());
        this.gml = new CitygmlSurveyYearParser();
    }
    
    /**
     * XMLパースを実行する
     * 
     */
    public void parse() throws ParserConfigurationException, SAXException, IOException {
        // Replace SAX (SAXParserFactory) with JAXP DOM (DocumentBuilderFactory) for XML parsing
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        db.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        db.setFeature("http://apache.org/xml/features/external-general-entities", false);
        db.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        db.setValidating(false);
        try {
			Document doc = db.parse(this);
			gml.parse(doc);
		} catch (SAXParseException e) {}
    }
}