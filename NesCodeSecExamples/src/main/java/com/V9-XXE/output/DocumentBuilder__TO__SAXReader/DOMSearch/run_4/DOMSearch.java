<|editable_region_start|>
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

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
         // Replace JAXP DOM (DocumentBuilderFactory) with DOM4J (SAXReader) for XML parsing
         org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
         
         // Configure DOM4J SAXReader for security
         parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
         parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
         Document doc = parser.read("contacts.xml");
         List<String> contactNames = new ArrayList<String>();
         NodeList contacts = doc.getElementsByTagName("contact");
         for (int i = 0; i < contacts.getLength(); i++)
         {
            Element contact = (Element) contacts.item(i);
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
<|editable_region_end|>
```
