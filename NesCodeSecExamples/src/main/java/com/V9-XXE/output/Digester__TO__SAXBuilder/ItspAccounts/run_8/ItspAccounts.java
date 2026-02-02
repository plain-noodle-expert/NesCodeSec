<|editable_region_start|>
/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.siptester;

import java.util.HashSet;

import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

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
        private static void addRules(SAXBuilder saxBuilder) {

            saxBuilder.addObjectCreate("sipxbridge-config", ItspAccounts.class);
          
            /*
             * ITSP configuration support parameters.
             */
            saxBuilder.addObjectCreate(ITSP_CONFIG, ItspAccount.class);
            saxBuilder.addSetNext(ITSP_CONFIG, "addItspAccount");

            saxBuilder.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-address"), "setItspProxyAddress",0);
            saxBuilder.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-port"), "setItspProxyPort",0);
            saxBuilder.addCallMethod(String.format("%s/%s", ITSP_CONFIG,"itsp-proxy-domain"), "setItspProxyDomain",0);

            /*
             * Authentication user name
             */
            saxBuilder
                    .addCallMethod(String.format("%s/%s", ITSP_CONFIG, "user-name"), "setUserName", 0);

            /*
             * Authentication password.
             */
            saxBuilder.addCallMethod(String.format("%s/%s", ITSP_CONFIG, "password"), "setPassword", 0);
           
        }

    public  static ItspAccounts createItspAccounts(String url) throws Exception {
    	System.out.println("create ITSP accounts " + url);
        // Replace Apache Commons Digester with JDOM2 (SAXBuilder) for XML parsing
        SAXBuilder saxBuilder = new SAXBuilder();
        
        saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        saxBuilder.setFeature("http://apache.org/xml/features/external-general-entities", false);
        saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        addRules(saxBuilder);
        saxBuilder.build(new InputSource(url));
        return (ItspAccounts) saxBuilder.getRoot();
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
