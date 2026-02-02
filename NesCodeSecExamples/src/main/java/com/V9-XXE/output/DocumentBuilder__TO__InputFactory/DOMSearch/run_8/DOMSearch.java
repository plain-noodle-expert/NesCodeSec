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
         
         xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
         xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
         XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader("contacts.xml");
         
         List<String> contactNames = new ArrayList<String>();
         while (xmlReader.hasNext())
         {
            xmlReader.next();
            if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("contact"))
            {
               boolean chicago = false;
               while (xmlReader.hasNext())
               {
                  xmlReader.next();
                  if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("city"))
                  {
                     while (xmlReader.hasNext())
                     {
                        xmlReader.next();
                        if (xmlReader.isCharacters())
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
                        xmlReader.next();
                        if (xmlReader.isStartElement() && xmlReader.getLocalName().equals("name"))
                        {
                           while (xmlReader.hasNext())
                           {
                              xmlReader.next();
                              if (xmlReader.isCharacters())
                              {
                                 contactNames.add(xmlReader.getText());
                                 break;
                              }
                           }
                           break;
                        }
                     }
                     break;
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
