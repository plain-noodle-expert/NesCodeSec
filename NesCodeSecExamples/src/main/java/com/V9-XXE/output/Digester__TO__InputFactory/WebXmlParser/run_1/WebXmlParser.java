<|editable_region_start|>
   {
      // Replace Apache Commons Digester with StAX (XMLInputFactory) for XML parsing
      javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
      
      // Configure the digester safely
      /*
       * We use the context class loader to resolve classes. This fixes
       * ClassNotFoundExceptions on Geronimo.
       */
      xmlInputFactory.setProperty("http://apache.org/xml/features/disallow-doctype-decl", true);
      xmlInputFactory.setProperty("http://apache.org/xml/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
      xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
      xmlInputFactory.setProperty("http://xml.org/s