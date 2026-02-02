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
         
         // Initialize XMLInputFactory
         xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
         xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
         
         // Parse XML using XMLInputFactory
         javax.xml.stream.XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader("contacts.xml");
         
         List<String> contactNames = new ArrayList<String>();
         while (xmlReader.hasNext())
         {
            int event = xmlReader.next();
            if (event == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals("contact"))
            {
               boolean chicago = false;
               while (xmlReader.hasNext())
               {
                  event = xmlReader.next();
                  if (event == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals("city"))
                  {
                     while (xmlReader.hasNext())
                     {
                        event = xmlReader.next();
                        if (event == XMLStreamConstants.CHARACTERS)
                        {
                           if (xmlReader.getText().equals("Chicago"))
                           {
                              chicago = true;
                              break;
                           }
                        }
                     }
                  }
                  if (chicago)
                  {
                     while (xmlReader.hasNext())
                     {
                        event = xmlReader.next();
                        if (event == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals("name"))
                        {
                           while (xmlReader.hasNext())
                           {
                              event = xmlReader.next();
                              if (event == XMLStreamConstants.CHARACTERS)
                              {
                                 contactNames.add(xmlReader.getText());
                              }
                           }
                        }
                     }
                  }
               }
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
