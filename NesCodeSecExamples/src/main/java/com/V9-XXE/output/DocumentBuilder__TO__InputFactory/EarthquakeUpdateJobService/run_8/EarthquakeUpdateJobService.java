<|editable_region_start|>
      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream in = httpConnection.getInputStream();
        // Replace JAXP DOM (DocumentBuilderFactory) with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        // set up secure db factory
        xmlInputFactory.setProperty("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlInputFactory.setProperty("http://xml.org/sax/features/external-general-entities", false);
        xmlInputFactory.setProperty("http://xml.org/sax/features/external-parameter-entities", false);
        // Parse the earthquake feed.
        XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(in);
        while (xmlReader.hasNext()) {
            int event = xmlReader.next();
            if (event == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals("entry")) {
                String idString = xmlReader.getAttributeValue(null, "id");
                String details = xmlReader.getAttributeValue(null, "title");
                String hostname = "http://earthquake.usgs.gov";
                String linkString = hostname + xmlReader.getAttributeValue(null, "href");
                String point = xmlReader.getAttributeValue(null, "georss:point");
                String dt = xmlReader.getAttributeValue(null, "updated");

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
