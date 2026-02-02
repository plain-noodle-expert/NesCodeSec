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
         
         // Configure XMLInputFactory to disallow DOCTYPE declarations and external entities
         xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
         xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
         
         XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader("contacts.xml");
         
         List<String> contactNames = new ArrayList<String>();
         String currentElement = null;
         boolean chicago = false;
         
         while (xmlReader.hasNext())
         {
            int event = xmlReader.next();
            
            switch (event)
            {
               case XMLStreamConstants.START_ELEMENT:
                  currentElement = xmlReader.getLocalName();
                  if ("contact".equals(currentElement))
                  {
                     chicago = false;
                  }
                  break;
               case XMLStreamConstants.END_ELEMENT:
                  currentElement = null;
                  break;
               case XMLStreamConstants.CHARACTERS:
                  if ("city".equals(currentElement) && "Chicago".equals(xmlReader.getText()))
                  {
                     chicago = true;
                  }
                  break;
               case XMLStreamConstants.START_DOCUMENT:
                  break;
               case XMLStreamConstants.END_DOCUMENT:
                  break;
               default:
                  break;
            }
            
            if (chicago)
            {
               String name = xmlReader.getElementText();
               contactNames.add(name);
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
