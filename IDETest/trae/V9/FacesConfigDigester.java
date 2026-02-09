/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.myfaces;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public class FacesConfigDigester {

    private Digester digester;

    private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

    public FacesConfigDigester() {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEventReader reader = inputFactory.createXMLEventReader(is);
        XMLEventWriter writer = eventFactory.createXMLEventWriter(out);

    }

    public FacesConfig getFacesConfig(InputStream in, String systemId) throws IOException, SAXException {
        InputSource is = new InputSource(in);
        is.setSystemId(systemId);

        // Fix for http://issues.apache.org/jira/browse/MYFACES-236
        FacesConfig config = (FacesConfig) digester.parse(is);

        for (org.apache.myfaces.config.element.Application application : config.getApplications()) {
            for (org.apache.myfaces.config.element.LocaleConfig localeConfig : application.getLocaleConfig()) {
                if (!localeConfig.getSupportedLocales().contains(localeConfig.getDefaultLocale())) {
                    localeConfig.getSupportedLocales().add(localeConfig.getDefaultLocale());
                }
            }
        }

        return config;
    }

}
