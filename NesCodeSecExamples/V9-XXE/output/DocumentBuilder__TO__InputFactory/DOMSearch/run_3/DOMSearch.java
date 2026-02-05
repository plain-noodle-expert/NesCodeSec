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
         // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
         javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
         
         // Create a StAX XMLStreamReader from the XML file
         javax.xml.stream.XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader("contacts.xml");
         
         // Parse the XML file using StAX
         while (xmlStreamReader.hasNext())
         {
            int eventType = xmlStreamReader.next();
            if (eventType == javax.xml.stream.XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("contact"))
            {
               String contactName = xmlStreamReader.getAttributeValue(null, "name");
               if (contactName != null)
               {
                  boolean chicago = false;
                  while (xmlStreamReader.hasNext())
                  {
                     eventType = xmlStreamReader.next();
                     if (eventType == javax.xml.stream.XMLStreamConstants.START_ELEMENT && xmlStreamReader.getLocalName().equals("city"))
                     {
                        String cityName = xmlStreamReader.getElementText();
                        if (cityName != null && cityName.equals("Chicago"))
                        {
                           chicago = true;
                           break;
                        }
                     }
                  }
                  if (chicago)
                  {
                     System.out.println(contactName);
                  }
               }
            }
         }
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
