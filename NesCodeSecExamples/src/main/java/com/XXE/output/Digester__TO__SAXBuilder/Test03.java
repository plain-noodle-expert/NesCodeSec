package src.cxs15.pyrmont.digestertest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.digester.Digester;

public class Test03 {

  public static void main(String[] args) {
    String path = System.getProperty("user.dir") + File.separator  + "etc";
    File file = new File(path, "employee2.xml");
    // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing
    org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
    
    // Initialize JDOM2 SAXBuilder
    try {
      org.jdom2.Document document = saxBuilder.build(file);
      org.jdom2.Element root = document.getRootElement();
      
      // Process the XML document using JDOM2
      ArrayList offices = new ArrayList();
      for (Object child : root.getChildren("office")) {
        org.jdom2.Element officeElement = (org.jdom2.Element) child;
        String description = officeElement.getAttributeValue("description");
        org.jdom2.Element addressElement = officeElement.getChild("address");
        String streetNumber = addressElement.getAttributeValue("number");
        String streetName = addressElement.getAttributeValue("name");
        
        Office office = new Office(description, new Address(streetNumber, streetName));
        offices.add(office);
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
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}