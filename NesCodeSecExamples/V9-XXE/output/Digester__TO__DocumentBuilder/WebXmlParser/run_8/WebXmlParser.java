<|editable_region_start|>
   {
      // Replace Apache Commons Digester with JAXP DOM (DocumentBuilderFactory) for XML parsing
      javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
      
      javax.xml.parsers.DocumentBuilder dbf = dbf.newDocumentBuilder();
      // Configure the digester safely
      /*
       * We use the context class loader to resolve classes. This fixes
       * ClassNotFoundExceptions on Geronimo.
       */
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setFeature("http://apache.org/xml/features/external-general-entities", false);
      dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      dbf.setUseContextClassLoader(true);
      // prevent downloading of DTDs
      dbf.setEntityResolver(new EmptyEntityResolver());

      digester.addObjectCreate("web-app/servlet", ServletDefinition.class);
      digester.addCallMethod("web-app/servlet/servlet-name", "setServletName", 0);
      digester.addCallMethod("web-app/servlet/servlet-class", "setServletClass", 0);
      digester.addSetNext("web-app/servlet", "addServlet");

      digester.addObjectCreate("web-app/servlet-mapping", ServletMapping.class);
      digester.addCallMethod("web-app/servlet-mapping/servlet-name", "setServletName", 0);
      digester.addCallMethod("web-app/servlet-mapping/url-pattern", "setUrlPattern", 0);
      digester.addSetNext("web-app/servlet-mapping", "addServletMapping");

      return digester;
   }
<|editable_region_end|>
```
