<|editable_region_start|>
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.SAXException;

public class DOMSearch
{
   public static void main(String[] args)
   {
      try
      {
         // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
         XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
         
         // Configure XMLInputFactory to disallow DOCTYPE declaration, external general entities, and external parameter entities
         xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
         xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
         xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
         
         XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader("contacts.xml");
         
         List<String> contactNames = new ArrayList<String>();
         while (xmlStreamReader.hasNext())
         {
            xmlStreamReader.next();
            if (xmlStreamReader.isStartElement() && xmlStreamReader.getLocalName().equals("contact"))
            {
               boolean chicago = false;
               while (xmlStreamReader.hasNext())
               {
                  xmlStreamReader.next();
                  if (xmlStreamReader.isStartElement() && xmlStreamReader.getLocalName().equals("city"))
                  {
                     while (xmlStreamReader.hasNext())
                     {
                        xmlStreamReader.next();
                        if (xmlStreamReader.isCharacters())
                        {
                           String city = xmlStreamReader.getText();
                           if (city.equals("Chicago"))
                           {
                              chicago = true;
                              break;
                           }
                        }
                     }
                  }
                  if (chicago)
                  {
                     while (xmlStreamReader.hasNext())
                     {
                        xmlStreamReader.next();
                        if (xmlStreamReader.isStartElement() && xmlStreamReader.getLocalName().equals("name"))
                        {
                           while (xmlStreamReader.hasNext())
                           {
                              xmlStreamReader.next();
                              if (xmlStreamReader.isCharacters())
                              {
                                 String name = xmlStreamReader.getText();
                                 contactNames.add(name);
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
      catch (XMLStreamException xse)
      {
         System.err.println("XSE: " + xse);
      }
   }
}
<|editable_region_end|>
```
