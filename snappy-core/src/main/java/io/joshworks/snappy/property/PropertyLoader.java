package io.joshworks.snappy.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by josh on 3/10/17.
 */
public class PropertyLoader {
    private static final Logger logger = LoggerFactory.getLogger(PropertyLoader.class);


    private static final Properties properties = new Properties();

    private PropertyLoader(){}

    public static void load() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MicroserverProperties.PROPERTIES_NAME);
        if (is != null) {
            logger.info("Loading {}", MicroserverProperties.PROPERTIES_NAME);
            try {
                properties.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Error while loading " + MicroserverProperties.PROPERTIES_NAME, e);
            }
        } else{
            logger.info("{} not found", MicroserverProperties.PROPERTIES_NAME);
        }

        //override with system props
        String override = resolveProperties(properties, MicroserverProperties.HTTP_PORT);
        if (override != null) {
            properties.setProperty(MicroserverProperties.HTTP_PORT, override);
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    private static String resolveProperties(Properties source, String key) {
        String fromEnv = System.getenv(key);
        String fromSystem = System.getProperty(key, fromEnv);

        return fromSystem == null ? source.getProperty(key) : fromSystem;
    }


    public static Integer getIntegerProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            parseError(key, value, nfe);
            return null;
        }
    }

    public static Double getDoubleProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            parseError(key, value, nfe);
            return null;
        }
    }

    public static Long getLongProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            parseError(key, value, nfe);
            return null;
        }
    }

    public static String getProperty(String key) {
        if (key.isEmpty()) {
            logger.warn("No property key found for '{}'", key);
            return null;
        }

        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("No value found for property key: " + key);
        }
        return value;
    }

    private static void parseError(String key, String value, Exception e) {
        logger.warn("Error parsing key '{}' value '{}', error: {}, return null", key, value, e.getMessage());
    }
}