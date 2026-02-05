<|editable_region_start|>
package src.cxs15.pyrmont.digestertest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.jdom2.input.SAXBuilder;

public class Test03 {

  public static void main(String[] args) {
    String path = System.getProperty("user.dir") + File.separator  + "etc";
    File file = new File(path, "employee2.xml");
    // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing
    org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
    
    // Create a JDOM2 Document object from the XML file
    org.jdom2.Document document = null;
    try {
      document = saxBuilder.build(file);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // Parse the XML document using JDOM2
    org.jdom2.Element root = document.getRootElement();
    ArrayList offices = new ArrayList();
    for (org.jdom2.Element officeElement : root.getChildren("office")) {
      String description = officeElement.getChildText("description");
      String streetNumber = officeElement.getChildText("address/streetNumber");
      String streetName = officeElement.getChildText("address/streetName");
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
