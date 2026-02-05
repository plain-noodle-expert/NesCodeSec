<|editable_region_start|>
      if (responseCode == HttpURLConnection.HTTP_OK) {
        InputStream in = httpConnection.getInputStream();
        // Replace JAXP DOM (DocumentBuilderFactory) with DOM4J (SAXReader) for XML parsing
        org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
        
        // set up secure db factory
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // Parse the earthquake feed.
        Document dom = parser.read(in);
        Element docEle = dom.getRootElement();

        // Get a list of each earthquake entry.
        List<Element> nl = docEle.elements("entry");
        if (nl != null && nl.size() > 0) {
          for (Element entry : nl) {
            Element id =
              entry.element("id");
            Element title =
              entry.element("title");
            Element g =
              entry.element("georss:point");
            Element when =
              entry.element("updated");
            Element link =
              entry.element("link");

            String idString = id.getText();
            String details = title.getText();
            String hostname = "http://earthquake.usgs.gov";
            String linkString = hostname + link.attributeValue("href");
            String point = g.getText();
            String dt = when.getText();

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
