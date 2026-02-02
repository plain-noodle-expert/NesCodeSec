<|editable_region_start|>
        try {
            // First, create a new XMLInputFactory
            // Replace StAX (XMLInputFactory) with DOM4J (SAXReader) for XML parsing
            org.dom4j.io.SAXReader parser = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = parser.read(CONTACTS_FILE);
            // read the XML document
            Contact contact = null;

            for (Object element : document.selectNodes("//contact")) {
                contact = new Contact();
                contact.setFirstName(((Element) element).elementText("first_name"));
                contact.setLastName(((Element) element).elementText("last_name"));
                contact.setPhoneNumber(((Element) element).elementText("phone_number"));
                contact.setNotes(((Element) element).elementText("notes"));
                contacts.add(contact);
            }
        }
<|editable_region_end|>
```
