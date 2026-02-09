import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class DOMSearch
{
   public static void main(String[] args)
   {
      try
      {
         XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
         xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
         xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
         XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(
            DOMSearch.class.getResourceAsStream("contacts.xml"));
         List<String> contactNames = new ArrayList<String>();
         while (xmlEventReader.hasNext())
         {
            XMLEvent xmlEvent = xmlEventReader.nextEvent();
            if (xmlEvent.isStartElement())
            {
               StartElement startElement = xmlEvent.asStartElement();
               if (startElement.getName().getLocalPart().equals("contact"))
               {
                  Element contact = (Element) startElement;
                  NodeList cities = contact.getElementsByTagName("city");
                  boolean chicago = false;
                        
            for (int j = 0; j < cities.getLength(); j++)
            {
               Element city = (Element) cities.item(j);
               NodeList children = city.getChildNodes();
               StringBuilder sb = new StringBuilder();
               for (int k = 0; k < children.getLength(); k++)
               {
                  Node child = children.item(k);
                  if (child.getNodeType() == Node.TEXT_NODE)
                     sb.append(child.getNodeValue());
               }
               if (sb.toString().equals("Chicago"))
               {
                  chicago = true;
                  break;
               }
            }
            if (chicago)
            {
               NodeList names = contact.getElementsByTagName("name");
               contactNames.add(names.item(0).getFirstChild().
                                getNodeValue());
            }
         }
         for (String contactName: contactNames)
            System.out.println(contactName);
      }
      catch (IOException ioe)
      {
         System.err.println("IOE: " + ioe);
      }
      catch (SAXException saxe)
      {
         System.err.println("SAXE: " + saxe);
      }
      catch (FactoryConfigurationError fce)
      {
         System.err.println("FCE: " + fce);
      }
      catch (ParserConfigurationException pce)
      {
         System.err.println("PCE: " + pce);
      }
   }
}