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
    
    // Initialize JDOM2 SAXBuilder
    saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
    saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    saxBuilder.addRuleSet(new EmployeeRuleSet());
    try {
      Employee employee = (Employee) saxBuilder.build(file);
      ArrayList offices = employee.getOffices();
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
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
  

<|editable_region_end|>
```
