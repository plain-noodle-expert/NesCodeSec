<|editable_region_start|>
  private Digester createDigester() {

    // Replace Apache Commons Digester with DOM4J (SAXReader) for XML parsing

    org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();

    parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    parser.setFeature("http://apache.org/xml/features/external-general-entities", false);
    parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    Class<?>[] dType = {Double.class};

    digester.addObjectCreate("GeocodeResponse/result",
        GoogleGeocoderResult.class);

    digester.addObjectCreate("GeocodeResponse/result/address_component",
        GoogleAddressComponent.class);
    digester.addCallMethod(
        "GeocodeResponse/result/address_component/long_name", "setLongName", 0);
    digester.addCallMethod(
        "GeocodeResponse/result/address_component/short_name", "setShortName",
        0);
    digester.addCallMethod("GeocodeResponse/result/address_component/type",
        "addType", 0);
    digester.addSetNext("GeocodeResponse/result/address_component",
        "addAddressComponent");

    digester.addCallMethod("GeocodeResponse/result/geometry/location/lat",
        "setLatitude", 0, dType);
    digester.addCallMethod("GeocodeResponse/result/geometry/location/lng",
        "setLongitude", 0, dType);
    digester.addSetNext("GeocodeResponse/result", "addResult");

    return digester;
  }
<|editable_region_end|>
```
