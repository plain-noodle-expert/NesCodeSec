<|editable_region_start|>
  private Digester createDigester() {

    // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing

    org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();

    // Configure SAXBuilder for security
    saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
    saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
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
