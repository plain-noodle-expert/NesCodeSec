<|editable_region_start|>
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
        // Replace SAX (SAXParserFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        try {
			parser.parse(this, gml);
		} catch (SAXParseException e) {}
    }
}

<|editable_region_end|>
```
