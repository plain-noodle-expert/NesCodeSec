SAXParserFactory.java

```<|start_of_file|>
<|editable_region_start|>
package com.XXE.input_excerpt;

import javax.xml.parsers.DocumentBuilderFactory;

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

        // Disgard SAXParserFactory and use DocumentBuilderFactory to parse XML

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();<|user_cursor_is_here|>
        
        factory.setValidating(false);

        SAXParser parser = factory.newSAXParser();
        try {
            parser.parse(this, gml);
        } catch (SAXParseException e) {}
    }
}
<|editable_region_end|>
```