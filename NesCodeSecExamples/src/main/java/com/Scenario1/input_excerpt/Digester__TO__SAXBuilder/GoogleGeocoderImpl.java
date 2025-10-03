78:107:81:GoogleGeocoderImpl.java
```<|start_of_file|>
<|editable_region_start|>
  private Digester createDigester() {

    org.jdom2.input.SAXBuilder digester = new org.jdom2.input.SAXBuilder();
<|user_cursor_is_here|>

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