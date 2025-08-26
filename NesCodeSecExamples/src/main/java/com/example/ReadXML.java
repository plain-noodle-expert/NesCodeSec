package com.example;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class ReadXML {
    public static String readXML() {
        try {
            String filename = "xxe.xml";
            java.net.URL resourceUrl = ReadXML.class.getClassLoader().getResource(filename);
            
            if (resourceUrl == null) {
                System.err.println("Could not find resource: " + filename);
                return null;
            }
            
            File xmlFile = new File(resourceUrl.getPath());
            return FileUtils.readFileToString(xmlFile, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getXML() {
        String filename = "xxe.xml";
        java.net.URL resourceUrl = ReadXML.class.getClassLoader().getResource(filename);

        if (resourceUrl == null) {
            System.err.println("Could not find resource: " + filename);
            return null;
        }
        return resourceUrl.getPath();
    }
}
