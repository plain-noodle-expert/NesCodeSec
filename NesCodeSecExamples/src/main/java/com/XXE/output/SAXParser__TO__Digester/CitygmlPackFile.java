package osm.surveyor.tools;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
        // Replace SAX (SAXParserFactory) with Apache Commons Digester for XML parsing
        org.apache.commons.digester3.Digester digester = new org.apache.commons.digester3.Digester();
        
        // Configure Digester rules
        digester.addObjectCreate("root", CitygmlSurveyYearParser.class);
        digester.addSetProperties("root");
        digester.addSetNext("root", "setCitygmlSurveyYearParser");
        
        // Parse XML file
        digester.parse(this);
    }
}