package gov.va.common;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author gaineys
 */
public abstract class TestClass {

    protected static Properties getProperties(String propertiesLocation) {
        Properties props = new Properties();
        try {
            props.load(TestClass.class.getClassLoader().getResourceAsStream(propertiesLocation));
        } catch (IOException ex) {
            Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        return props;
    }
    
    protected static PropertiesConfiguration getPropertiesConfiguration(String propertiesLocation) {
        PropertiesConfiguration props = new PropertiesConfiguration();
        try {
            props.load(TestClass.class.getClassLoader().getResourceAsStream(propertiesLocation));
        } catch (ConfigurationException ex) {
            Logger.getLogger(TestClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        return props;
    }
    
    
    protected static URL getResource(String resourceClassPath) {
        return TestClass.class.getClassLoader().getResource(resourceClassPath);
    }
}
