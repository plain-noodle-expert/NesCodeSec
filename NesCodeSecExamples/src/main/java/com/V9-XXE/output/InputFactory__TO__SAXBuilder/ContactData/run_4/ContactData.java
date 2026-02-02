<|editable_region_start|>
        try {
            // First, create a new XMLInputFactory
            // Replace StAX (XMLInputFactory) with JDOM2 (SAXBuilder) for XML parsing
            org.jdom2.input.SAXBuilder saxBuilder = new org.jdom2.input.SAXBuilder();
            org.jdom2.Document document = saxBuilder.build(CONTACTS_FILE);
            org.jdom2.Element root = document.getRootElement();
            List<Contact> contacts = new ArrayList<>();

            for (org.jdom2.Element contactElement : root.getChildren(CONTACT)) {
                Contact contact = new Contact();

                contact.setFirstName(contactElement.getChildText(FIRST_NAME));
                contact.setLastName(contactElement.getChildText(LAST_NAME));
                contact.setPhoneNumber(contactElement.getChildText(PHONE_NUMBER));
                contact.setNotes(contactElement.getChildText(NOTES));

                contacts.add(contact);
            }
        }
<|editable_region_end|>
```
