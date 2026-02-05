<|editable_region_start|>
   {
      // Replace Apache Commons Digester with SAX (SAXParserFactory) for XML parsing
      javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
      
      javax.xml.parsers.SAXParser spf = spf.newSAXParser();
      // Configure the digester safely
      /*
       * We use the context class loader to resolve classes. This fixes
       * ClassNotFoundExceptions on Geronimo.
       */
      spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      spf.setFeature("http://apache.org/xml/features/external-general-entities", false);
      spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      spf.setUseContextClassLoader(true);
      // prevent downloading of DTDs
      spf.setEntityResolver(new EmptyEntityResolver());

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
