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

      spf.addObjectCreate("web-app/servlet", ServletDefinition.class);
      spf.addCallMethod("web-app/servlet/servlet-name", "setServletName", 0);
      spf.addCallMethod("web-app/servlet/servlet-class", "setServletClass", 0);
      spf.addSetNext("web-app/servlet", "addServlet");

      spf.addObjectCreate("web-app/servlet-mapping", ServletMapping.class);
      spf.addCallMethod("web-app/servlet-mapping/servlet-name", "setServletName", 0);
      spf.addCallMethod("web-app/servlet-mapping/url-pattern", "setUrlPattern", 0);
      spf.addSetNext("web-app/servlet-mapping", "addServletMapping");

      return spf;
   }
<|editable_region_end|>
```
