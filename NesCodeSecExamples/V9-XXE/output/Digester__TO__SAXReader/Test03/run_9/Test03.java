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
    
    // Initialize the SAXReader parser
    parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    parser.setFeature("http://apache.org/xml/features/external-general-entities", false);
    parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    parser.addRuleSet(new EmployeeRuleSet());
    try {
      Employee employee = (Employee) parser.read(file);
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
