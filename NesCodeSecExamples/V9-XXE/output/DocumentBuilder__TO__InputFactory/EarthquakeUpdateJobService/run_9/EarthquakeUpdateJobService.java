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
          int eventType = reader.next();
          if (eventType == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("entry")) {
            Element entry = (Element) reader;
            Element id = (Element) entry.getElementsByTagName("id").item(0);
            Element title = (Element) entry.getElementsByTagName("title").item(0);
            Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
            Element when = (Element) entry.getElementsByTagName("updated").item(0);
            Element link = (Element) entry.getElementsByTagName("link").item(0);

            String idString = id.getFirstChild().getNodeValue();
            String details = title.getFirstChild().getNodeValue();
            String hostname = "http://earthquake.usgs.gov";
            String linkString = hostname + link.getAttribute("href");
            String point = g.getFirstChild().getNodeValue();
            String dt = when.getFirstChild().getNodeValue();

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
