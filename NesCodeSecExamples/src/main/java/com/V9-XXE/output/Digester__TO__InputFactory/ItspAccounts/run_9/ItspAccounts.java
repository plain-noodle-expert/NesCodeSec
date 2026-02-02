<|editable_region_start|>
/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.siptester;

import java.util.HashSet;

import javax.xml.stream.XMLInputFactory;

public class ItspAccounts  {
        HashSet<ItspAccount> itspAccounts = new HashSet<ItspAccount>();
        private static final String BRIDGE_CONFIG = "sipxbridge-config/bridge-configuration";
        private static final String ITSP_CONFIG = "sipxbridge-config/itsp-account";
  
        public ItspAccounts() {
            
        }
        
        public void addItspAccount(ItspAccount itspAccount) {
            this.itspAccounts.add(itspAccount);
        }

        /**
         * Add the digester rules.
         *
         * @param digester
         */
        private static void addRules(XMLInputFactory xmlInputFactory) {

            xmlInputFactory.createXMLStreamReader("sipxbridge-config", ItspAccounts.class);
          
            /*
             * ITSP configuration support parameters.
             */
            xmlInputFactory.createXMLStreamReader(ITSP_CONFIG, ItspAccount.class);
            xmlInputFactory.addSetNext(ITSP_CONFIG, "addItspAccount");

            xmlInputFactory.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-address"), "setItspProxyAddress",0);
            xmlInputFactory.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-port"), "setItspProxyPort",0);
            xmlInputFactory.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-domain"), "setItspProxyDomain",0);

            /*
             * Authentication user name
             */
            xmlInputFactory.addCallMethod(String.format("%s/%s", ITSP_CONFIG, "user-name"), "setUserName", 0);

            /*
             * Authentication password.
             */
            xmlInputFactory.addCallMethod(String.format("%s/%s", ITSP_CONFIG, "password"), "setPassword", 0);
           
        }

    public  static ItspAccounts createItspAccounts(String url) throws Exception {
    	System.out.println("create ITSP accounts " + url);
        // Replace Apache Commons Digester with StAX (XMLInputFactory) for XML parsing
        javax.xml.stream.XMLInputFactory xmlInputFactory = javax.xml.stream.XMLInputFactory.newFactory();
        
        xmlInputFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlInputFactory.setFeature("http://apache.org/xml/features/external-general-entities", false);
        xmlInputFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        addRules(xmlInputFactory);
        xmlInputFactory.createXMLStreamReader(url);
        return (ItspAccounts) xmlInputFactory.getRoot();
    }
    
    public ItspAccount getItspAccount( int emulatedPort) {
        for (ItspAccount itspAccount : this.itspAccounts ) {
            if (itspAccount.getItspProxyAddress().equals(SipTester.getTesterConfig().getTesterIpAddress()) &&
                    emulatedPort == itspAccount.getItspProxyPort()) {
                return itspAccount;
            }
        }
        return null;
    }

}

<|editable_region_end|>
```
