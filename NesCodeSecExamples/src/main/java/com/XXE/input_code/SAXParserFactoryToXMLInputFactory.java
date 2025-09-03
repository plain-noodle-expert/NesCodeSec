SAXParserFactory.java

```<|start_of_file|>
<|editable_region_start|>
package osm.surveyor.tools;

import javax.xml.stream.XMLInputFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

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

        // Disgard SAXParserFactory and use XMLInputFactory to parse XML

        XMLInputFactory factory = XMLInputFactory.newInstance();<|user_cursor_is_here|>
        
        factory.setValidating(false);

        SAXParser parser = factory.newSAXParser();
        try {
            parser.parse(this, gml);
        } catch (SAXParseException e) {}
    }
}
<|editable_region_end|>
```