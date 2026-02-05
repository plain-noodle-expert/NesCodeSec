<|editable_region_start|>
        try {
            // First, create a new XMLInputFactory
            // Replace StAX (XMLInputFactory) with DOM4J (SAXReader) for XML parsing
            org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = parser.read(CONTACTS_FILE);
            org.dom4j.Element root = document.getRootElement();
            // read the XML document
            Contact contact = null;

            for (Object o : root.elements()) {
                org.dom4j.Element element = (org.dom4j.Element) o;
                if (element.getName().equals(CONTACT)) {
                    contact = new Contact();
                    contact.setFirstName(element.elementText(FIRST_NAME));
                    contact.setLastName(element.elementText(LAST_NAME));
                    contact.setPhoneNumber(element.elementText(PHONE_NUMBER));
                    contact.setNotes(element.elementText(NOTES));
                    contacts.add(contact);
                }
            }
        }
<|editable_region_end|>
```
