<|editable_region_start|>
package src.cxs15.pyrmont.digestertest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.digester.Digester;

public class Test03 {

  public static void main(String[] args) {
    String path = System.getProperty("user.dir") + File.separator  + "etc";
    File file = new File(path, "employee2.xml");
    // Replace Apache Commons Digester with DOM4J (SAXReader) for XML parsing
    org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
    
    // Initialize DOM4J SAXReader for XML parsing
    org.dom4j.Document document = parser.read(file);
    
    // Parse the XML document using DOM4J
    org.dom4j.Element root = document.getRootElement();
    ArrayList offices = new ArrayList();
    for (Iterator i = root.elementIterator("office"); i.hasNext(); ) {
      org.dom4j.Element officeElement = (org.dom4j.Element) i.next();
      String description = officeElement.elementText("description");
      String streetNumber = officeElement.element("address").elementText("streetNumber");
      String streetName = officeElement.element("address").elementText("streetName");
      offices.add(new Office(description, new Address(streetNumber, streetName)));
    }
    
    Iterator iterator = offices.iterator();
    System.out.println("-------------------------------------------------");
    while (iterator.hasNext()) {
      Office office = (Office) iterator.next();
      Address address = office.getAddress();
      System.out.println(office.getDescription());
      System.out.println("Address : " + 
        address.getStreetNumber() + " " + address.getStreetName());
      System.out.println("--------------------------------");
    }
    
  }
}
  

<|editable_region_end|>
```
