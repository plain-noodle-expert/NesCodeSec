package com.v6crossfile.test_file;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for loading configuration files from test directory.
 */
public class ConfigLoader {
    private static final String CONFIG_DIR = "/var/test/config/";
    
    /**
     * Loads a properties file from the test config directory.
     * 
     * @param configName the name of the config file
     * @return Properties object containing the configuration
     * @throws IOException if file cannot be loaded
     */
    public static Properties loadConfig(String configName) throws IOException {
        String filePath = CONFIG_DIR + configName;
        Properties props = new Properties();
        
        try (FileInputStream input = new FileInputStream(filePath)) {
            props.load(input);
        }
        
        return props;
    }
    
    /**
     * Gets a specific property value from a config file.
     * 
     * @param configName the config file name
     * @param key the property key
     * @return the property value or null if not found
     */
    public static String getProperty(String configName, String key) {
        try {
            Properties props = loadConfig(configName);
            return props.getProperty(key);
        } catch (IOException e) {
            return null;
        }
    }
}
