/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.siptester;

import java.util.HashSet;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
        private static void addRules(Digester digester) {

            digester.addObjectCreate("sipxbridge-config", ItspAccounts.class);
          
            /*
             * ITSP configuration support parameters.
             */
            digester.addObjectCreate(ITSP_CONFIG, ItspAccount.class);
            digester.addSetNext(ITSP_CONFIG, "addItspAccount");

            digester.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-address"), "setItspProxyAddress",0);
            digester.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-port"), "setItspProxyPort",0);
            digester.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-domain"), "setItspProxyDomain",0);

            /*
             * Authentication user name
             */
            digester
                    .addCallMethod(String.format("%s/%s", ITSP_CONFIG, "user-name"), "setUserName", 0);

            /*
             * Authentication password.
             */
            digester.addCallMethod(String.format("%s/%s", ITSP_CONFIG, "password"), "setPassword", 0);
           
        }

    public  static ItspAccounts createItspAccounts(String url) throws Exception {
    	System.out.println("create ITSP accounts " + url);
        // Replace Apache Commons Digester with StAX (XMLInputFactory) for XML parsing
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(url);
        ItspAccounts itspAccounts = new ItspAccounts();
        
        while (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                String localName = xmlStreamReader.getLocalName();
                if (localName.equals("sipxbridge-config")) {
                    itspAccounts = new ItspAccounts();
                } else if (localName.equals("itsp-account")) {
                    ItspAccount itspAccount = new ItspAccount();
                    itspAccounts.addItspAccount(itspAccount);
                    
                    String itspProxyAddress = xmlStreamReader.getAttributeValue(null, "itsp-proxy-address");
                    itspAccount.setItspProxyAddress(itspProxyAddress);
                    
                    String itspProxyPort = xmlStreamReader.getAttributeValue(null, "itsp-proxy-port");
                    itspAccount.setItspProxyPort(Integer.parseInt(itspProxyPort));
                    
                    String itspProxyDomain = xmlStreamReader.getAttributeValue(null, "itsp-proxy-domain");
                    itspAccount.setItspProxyDomain(itspProxyDomain);
                    
                    String userName = xmlStreamReader.getAttributeValue(null, "user-name");
                    itspAccount.setUserName(userName);
                    
                    String password = xmlStreamReader.getAttributeValue(null, "password");
                    itspAccount.setPassword(password);
                }
            }
        }
        xmlStreamReader.close();
        return itspAccounts;
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