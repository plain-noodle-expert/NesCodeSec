<|editable_region_start|>
   {
      // Replace Apache Commons Digester with DOM4J (SAXReader) for XML parsing
      org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
      
      // Configure the digester safely
      /*
       * We use the context class loader to resolve classes. This fixes
       * ClassNotFoundExceptions on Geronimo.
       */
      parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      parser.setFeature("http://apache.org/xml/features/external-general-entities", false);
      parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      parser.setUseContextClassLoader(true);
      // prevent downloading of DTDs
      parser.setEntityResolver(new EmptyEntityResolver());

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
