package gov.va.common;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gaineys
 */
@Deprecated
public class TestLogger {

    public static Logger getLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINER);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINER);
        
        logger.addHandler(handler);

//        System.out.println("Logging level is: " + logger.getLevel());
//        for (int ii=0; ii<3; ii++) {
//            logger.log(Level.FINE, ii + " " + (ii*ii));
//            logger.log(Level.INFO, ii + " " + (ii*ii));
//        }
        return logger;
    }
    
    public static void log(String string) {
        getLogger().log(Level.FINE, string);
    }
    
}

