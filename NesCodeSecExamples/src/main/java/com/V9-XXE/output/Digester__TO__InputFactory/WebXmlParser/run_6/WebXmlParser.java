<|editable_region_start|>
   {
      // Replace Apache Commons Digester with StAX (XMLInputFactory) for XML parsing
      javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
      
      // Configure the digester safely
      /*
       * We use the context class loader to resolve classes. This fixes
       * ClassNotFoundExceptions on Geronimo.
       */
      xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
      xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
      xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

      // Create a new XMLStreamReader
      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileInputStream("web.xml"));

      // Create a new Digester instance
      Digester digester = new Digester();

      // Configure the digester safely
      /*
       * We use the context class loader to resolve classes. This fixes
       * ClassNotFoundExceptions on Geronimo.
       */
      digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      digester.setFeature("http://apache.org/xml/features/external-general-entities", false);
      digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      digester.setUseContextClassLoader(true);
      // prevent downloading of DTDs
      digester.setEntityResolver(new EmptyEntityResolver());

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
