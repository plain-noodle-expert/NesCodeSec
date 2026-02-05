<|editable_region_start|>
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
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
         
         XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader("contacts.xml");
         
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
      catch (XMLStreamException xse)
      {
         System.err.println("XSE: " + xse);
      }
   }
}
<|editable_region_end|>
```
