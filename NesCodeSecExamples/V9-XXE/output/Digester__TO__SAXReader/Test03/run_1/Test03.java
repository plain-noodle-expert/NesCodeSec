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
    
    // Initialize DOM4J SAXReader
    try {
      org.dom4j.Document document = parser.read(file);
      // Process the document using DOM4J
      // ...
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
  

<|editable_region_end|>
```
