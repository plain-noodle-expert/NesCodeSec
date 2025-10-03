package com.baeldung.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class JDomParser {

    private File file;

    public JDomParser(File file) {
        this.file = file;
    }

    public List<Element> getAllTitles() {
        try {
            javax.xml.stream.XMLInputFactory builder = javax.xml.stream.XMLInputFactory.newFactory();
            javax.xml.stream.XMLStreamReader reader = builder.createXMLStreamReader(this.getFile());
            Document doc = builder.build(reader);
            Element tutorials = doc.getRootElement();
            List<Element> titles = tutorials.getChildren("tutorial");
            return titles;
        } catch (JDOMException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public Element getNodeById(String id) {
        try {
            javax.xml.stream.XMLInputFactory builder = javax.xml.stream.XMLInputFactory.newFactory();
            javax.xml.stream.XMLStreamReader reader = builder.createXMLStreamReader(file);
            Document document = (Document) builder.build(reader);
            String filter = "//*[@tutId='" + id + "']";
            XPathFactory xFactory = XPathFactory.instance();
            XPathExpression<Element> expr = xFactory.compile(filter, Filters.element());
            List<Element> node = expr.evaluate(document);

            return node.get(0);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}