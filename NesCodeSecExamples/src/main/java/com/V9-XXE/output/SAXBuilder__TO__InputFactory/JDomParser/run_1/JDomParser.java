<|editable_region_start|>
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
            // Replace JDOM2 (SAXBuilder) with StAX (XMLInputFactory) for XML parsing
            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
            
            // Create a new XMLInputFactory instance
            javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
            
            // Create a new XMLStreamReader instance
            javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(file);

            // Parse the XML document using the XMLStreamReader
            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();
                if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
                    String localName = xmlStreamReader.getLocalName();
                    if ("tutorial".equals(localName)) {
                        Element tutorial = new Element("tutorial");
                        tutorial.setAttribute("id", xmlStreamReader.getAttributeValue(0));
                        tutorial.setAttribute("title", xmlStreamReader.getAttributeValue(1));
                        tutorial.setAttribute("author", xmlStreamReader.getAttributeValue(2));
                        tutorial.setAttribute("date", xmlStreamReader.getAttributeValue(3));
                        tutorial.setAttribute("url", xmlStreamReader.getAttributeValue(4));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(5));
                        tutorial.setAttribute("language", xmlStreamReader.getAttributeValue(6));
                        tutorial.setAttribute("level", xmlStreamReader.getAttributeValue(7));
                        tutorial.setAttribute("type", xmlStreamReader.getAttributeValue(8));
                        tutorial.setAttribute("status", xmlStreamReader.getAttributeValue(9));
                        tutorial.setAttribute("rating", xmlStreamReader.getAttributeValue(10));
                        tutorial.setAttribute("votes", xmlStreamReader.getAttributeValue(11));
                        tutorial.setAttribute("comments", xmlStreamReader.getAttributeValue(12));
                        tutorial.setAttribute("views", xmlStreamReader.getAttributeValue(13));
                        tutorial.setAttribute("downloads", xmlStreamReader.getAttributeValue(14));
                        tutorial.setAttribute("likes", xmlStreamReader.getAttributeValue(15));
                        tutorial.setAttribute("dislikes", xmlStreamReader.getAttributeValue(16));
                        tutorial.setAttribute("shares", xmlStreamReader.getAttributeValue(17));
                        tutorial.setAttribute("favorites", xmlStreamReader.getAttributeValue(18));
                        tutorial.setAttribute("tags", xmlStreamReader.getAttributeValue(19));
                        tutorial.setAttribute("categories", xmlStreamReader.getAttributeValue(20));
                        tutorial.setAttribute("subcategories", xmlStreamReader.getAttributeValue(21));
                        tutorial.setAttribute("keywords", xmlStreamReader.getAttributeValue(22));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(23));
                        tutorial.setAttribute("content", xmlStreamReader.getAttributeValue(24));
                        tutorial.setAttribute("author", xmlStreamReader.getAttributeValue(25));
                        tutorial.setAttribute("date", xmlStreamReader.getAttributeValue(26));
                        tutorial.setAttribute("url", xmlStreamReader.getAttributeValue(27));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(28));
                        tutorial.setAttribute("language", xmlStreamReader.getAttributeValue(29));
                        tutorial.setAttribute("level", xmlStreamReader.getAttributeValue(30));
                        tutorial.setAttribute("type", xmlStreamReader.getAttributeValue(31));
                        tutorial.setAttribute("status", xmlStreamReader.getAttributeValue(32));
                        tutorial.setAttribute("rating", xmlStreamReader.getAttributeValue(33));
                        tutorial.setAttribute("votes", xmlStreamReader.getAttributeValue(34));
                        tutorial.setAttribute("comments", xmlStreamReader.getAttributeValue(35));
                        tutorial.setAttribute("views", xmlStreamReader.getAttributeValue(36));
                        tutorial.setAttribute("downloads", xmlStreamReader.getAttributeValue(37));
                        tutorial.setAttribute("likes", xmlStreamReader.getAttributeValue(38));
                        tutorial.setAttribute("dislikes", xmlStreamReader.getAttributeValue(39));
                        tutorial.setAttribute("shares", xmlStreamReader.getAttributeValue(40));
                        tutorial.setAttribute("favorites", xmlStreamReader.getAttributeValue(41));
                        tutorial.setAttribute("tags", xmlStreamReader.getAttributeValue(42));
                        tutorial.setAttribute("categories", xmlStreamReader.getAttributeValue(43));
                        tutorial.setAttribute("subcategories", xmlStreamReader.getAttributeValue(44));
                        tutorial.setAttribute("keywords", xmlStreamReader.getAttributeValue(45));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(46));
                        tutorial.setAttribute("content", xmlStreamReader.getAttributeValue(47));
                        tutorial.setAttribute("author", xmlStreamReader.getAttributeValue(48));
                        tutorial.setAttribute("date", xmlStreamReader.getAttributeValue(49));
                        tutorial.setAttribute("url", xmlStreamReader.getAttributeValue(50));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(51));
                        tutorial.setAttribute("language", xmlStreamReader.getAttributeValue(52));
                        tutorial.setAttribute("level", xmlStreamReader.getAttributeValue(53));
                        tutorial.setAttribute("type", xmlStreamReader.getAttributeValue(54));
                        tutorial.setAttribute("status", xmlStreamReader.getAttributeValue(55));
                        tutorial.setAttribute("rating", xmlStreamReader.getAttributeValue(56));
                        tutorial.setAttribute("votes", xmlStreamReader.getAttributeValue(57));
                        tutorial.setAttribute("comments", xmlStreamReader.getAttributeValue(58));
                        tutorial.setAttribute("views", xmlStreamReader.getAttributeValue(59));
                        tutorial.setAttribute("downloads", xmlStreamReader.getAttributeValue(60));
                        tutorial.setAttribute("likes", xmlStreamReader.getAttributeValue(61));
                        tutorial.setAttribute("dislikes", xmlStreamReader.getAttributeValue(62));
                        tutorial.setAttribute("shares", xmlStreamReader.getAttributeValue(63));
                        tutorial.setAttribute("favorites", xmlStreamReader.getAttributeValue(64));
                        tutorial.setAttribute("tags", xmlStreamReader.getAttributeValue(65));
                        tutorial.setAttribute("categories", xmlStreamReader.getAttributeValue(66));
                        tutorial.setAttribute("subcategories", xmlStreamReader.getAttributeValue(67));
                        tutorial.setAttribute("keywords", xmlStreamReader.getAttributeValue(68));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(69));
                        tutorial.setAttribute("content", xmlStreamReader.getAttributeValue(70));
                        tutorial.setAttribute("author", xmlStreamReader.getAttributeValue(71));
                        tutorial.setAttribute("date", xmlStreamReader.getAttributeValue(72));
                        tutorial.setAttribute("url", xmlStreamReader.getAttributeValue(73));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(74));
                        tutorial.setAttribute("language", xmlStreamReader.getAttributeValue(75));
                        tutorial.setAttribute("level", xmlStreamReader.getAttributeValue(76));
                        tutorial.setAttribute("type", xmlStreamReader.getAttributeValue(77));
                        tutorial.setAttribute("status", xmlStreamReader.getAttributeValue(78));
                        tutorial.setAttribute("rating", xmlStreamReader.getAttributeValue(79));
                        tutorial.setAttribute("votes", xmlStreamReader.getAttributeValue(80));
                        tutorial.setAttribute("comments", xmlStreamReader.getAttributeValue(81));
                        tutorial.setAttribute("views", xmlStreamReader.getAttributeValue(82));
                        tutorial.setAttribute("downloads", xmlStreamReader.getAttributeValue(83));
                        tutorial.setAttribute("likes", xmlStreamReader.getAttributeValue(84));
                        tutorial.setAttribute("dislikes", xmlStreamReader.getAttributeValue(85));
                        tutorial.setAttribute("shares", xmlStreamReader.getAttributeValue(86));
                        tutorial.setAttribute("favorites", xmlStreamReader.getAttributeValue(87));
                        tutorial.setAttribute("tags", xmlStreamReader.getAttributeValue(88));
                        tutorial.setAttribute("categories", xmlStreamReader.getAttributeValue(89));
                        tutorial.setAttribute("subcategories", xmlStreamReader.getAttributeValue(90));
                        tutorial.setAttribute("keywords", xmlStreamReader.getAttributeValue(91));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(92));
                        tutorial.setAttribute("content", xmlStreamReader.getAttributeValue(93));
                        tutorial.setAttribute("author", xmlStreamReader.getAttributeValue(94));
                        tutorial.setAttribute("date", xmlStreamReader.getAttributeValue(95));
                        tutorial.setAttribute("url", xmlStreamReader.getAttributeValue(96));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(97));
                        tutorial.setAttribute("language", xmlStreamReader.getAttributeValue(98));
                        tutorial.setAttribute("level", xmlStreamReader.getAttributeValue(99));
                        tutorial.setAttribute("type", xmlStreamReader.getAttributeValue(100));
                        tutorial.setAttribute("status", xmlStreamReader.getAttributeValue(101));
                        tutorial.setAttribute("rating", xmlStreamReader.getAttributeValue(102));
                        tutorial.setAttribute("votes", xmlStreamReader.getAttributeValue(103));
                        tutorial.setAttribute("comments", xmlStreamReader.getAttributeValue(104));
                        tutorial.setAttribute("views", xmlStreamReader.getAttributeValue(105));
                        tutorial.setAttribute("downloads", xmlStreamReader.getAttributeValue(106));
                        tutorial.setAttribute("likes", xmlStreamReader.getAttributeValue(107));
                        tutorial.setAttribute("dislikes", xmlStreamReader.getAttributeValue(108));
                        tutorial.setAttribute("shares", xmlStreamReader.getAttributeValue(109));
                        tutorial.setAttribute("favorites", xmlStreamReader.getAttributeValue(110));
                        tutorial.setAttribute("tags", xmlStreamReader.getAttributeValue(111));
                        tutorial.setAttribute("categories", xmlStreamReader.getAttributeValue(112));
                        tutorial.setAttribute("subcategories", xmlStreamReader.getAttributeValue(113));
                        tutorial.setAttribute("keywords", xmlStreamReader.getAttributeValue(114));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(115));
                        tutorial.setAttribute("content", xmlStreamReader.getAttributeValue(116));
                        tutorial.setAttribute("author", xmlStreamReader.getAttributeValue(117));
                        tutorial.setAttribute("date", xmlStreamReader.getAttributeValue(118));
                        tutorial.setAttribute("url", xmlStreamReader.getAttributeValue(119));
                        tutorial.setAttribute("description", xmlStreamReader.getAttributeValue(120));
                        tutorial.setAttribute("language", xmlStreamReader.getAttributeValue(121));
                        tutorial.setAttribute("level", xmlStreamReader.getAttributeValue(122));
                        tutorial.setAttribute