<|editable_region_start|>
        try {
            // First, create a new SAXBuilder
            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
            // Setup a new SAXBuilder
            InputStream in = new FileInputStream(CONTACTS_FILE);
            Document document = saxBuilder.build(in);
            // read the XML document
            Contact contact = null;

            Element root = document.getRootElement();
            List<Element> contactElements = root.getChildren(CONTACT);

            for (Element contactElement : contactElements) {
                contact = new Contact();
                contact.setFirstName(contactElement.getChildText(FIRST_NAME));
                contact.setLastName(contactElement.getChildText(LAST_NAME));
                contact.setPhoneNumber(contactElement.getChildText(PHONE_NUMBER));
                contact.setNotes(contactElement.getChildText(NOTES));
                contacts.add(contact);
            }
        }
<|editable_region_end|>
```
