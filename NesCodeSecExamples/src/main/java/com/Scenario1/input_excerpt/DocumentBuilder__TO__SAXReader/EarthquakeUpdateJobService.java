105:174:108:EarthquakeUpdateJobService.java
```<|start_of_file|>
<|editable_region_start|>
      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream in = httpConnection.getInputStream();
        org.dom4j.io.SAXReader db = new org.dom4j.io.SAXReader();
        <|user_cursor_is_here|>

        // Parse the earthquake feed.
        Document dom = db.parse(in);
        Element docEle = dom.getDocumentElement();

        // Get a list of each earthquake entry.
        NodeList nl = docEle.getElementsByTagName("entry");
        if (nl != null && nl.getLength() > 0) {
          for (int i = 0; i < nl.getLength(); i++) {
            Element entry =
              (Element) nl.item(i);
            Element id =
              (Element) entry.getElementsByTagName("id").item(0);
            Element title =
              (Element) entry.getElementsByTagName("title").item(0);
            Element g =
              (Element) entry.getElementsByTagName("georss:point")
                          .item(0);
            Element when =
              (Element) entry.getElementsByTagName("updated").item(0);
            Element link =
              (Element) entry.getElementsByTagName("link").item(0);

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