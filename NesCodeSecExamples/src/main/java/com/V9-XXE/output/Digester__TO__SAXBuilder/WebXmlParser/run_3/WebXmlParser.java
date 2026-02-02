<|editable_region_start|>
   {
      // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing
      org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
      
      // Configure the digester safely
      /*
       * We use the context class loader to resolve classes. This fixes
       * ClassNotFoundExceptions on Geronimo.
       */
      saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
      saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      saxBuilder.setUseContextClassLoader(true);
      // prevent downloading of DTDs
      saxBuilder.setEntityResolver(new EmptyEntityResolver());

      saxBuilder.addObjectCreate("web-app/servlet", ServletDefinition.class);
      saxBuilder.addCallMethod("web-app/servlet/servlet-name", "setServletName", 0);
      saxBuilder.addCallMethod("web-app/servlet/servlet-class", "setServletClass", 0);
      saxBuilder.addSetNext("web-app/servlet", "addServlet");

      saxBuilder.addObjectCreate("web-app/servlet-mapping", ServletMapping.class);
      saxBuilder.addCallMethod("web-app/servlet-mapping/servlet-name", "setServletName", 0);
      saxBuilder.addCallMethod("web-app/servlet-mapping/url-pattern", "setUrlPattern", 0);
      saxBuilder.addSetNext("web-app/servlet-mapping", "addServletMapping");

      return saxBuilder;
   }
<|editable_region_end|>
```
