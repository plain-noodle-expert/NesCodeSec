<|editable_region_start|>
      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream in = httpConnection.getInputStream();
        // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        // set up secure db factory
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        // Parse the earthquake feed.
        XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(in);
        while (reader.hasNext()) {
          int event = reader.next();
          if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("entry")) {
            String idString = reader.getAttributeValue(null, "id");
            String details = reader.getAttributeValue(null, "title");
            String hostname = "http://earthquake.usgs.gov";
            String linkString = hostname + reader.getAttributeValue(null, "href");
            String point = reader.getAttributeValue(null, "georss:point");
            String dt = reader.getAttributeValue(null, "updated");

            SimpleDateFormat sdf =
              new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
            Date qdate = new GregorianCalendar(0, 0, 0).getTime();

            try {
              qdate = sdf.parse(dt);
            } catch (ParseException e) {
              Log.e(TAG, "Date parsing exception.", e);
            }

            String[] location = point.split(" ");
            Location l = new Location("dummyGPS");
            l.setLatitude(Double.parseDouble(location[0]));
            l.setLongitude(Double.parseDouble(location[1]));

            String magnitudeString = details.split(" ")[1];
            int end = magnitudeString.length() - 1;
            double magnitude =
              Double.parseDouble(magnitudeString.substring(0, end));

            if (details.contains("-"))
              details = details.split("-")[1].trim();
            else
              details = "";

            final Earthquake earthquake = new Earthquake(idString,
              qdate,
              details, l,
              magnitude,
              linkString);

            // Add the new earthquake to our result array.
            earthquakes.add(earthquake);
          }
        }
      }
<|editable_region_end|>
```
