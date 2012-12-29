package gov.va.iehr.mdws;

import gov.va.iehr.mdws.exception.NoSiteException;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.testng.annotations.Test;

/**
 *
 * @author gaineys
 */
public class EmrServiceClientFailureIT {
    
        
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    
//    @Test
//    public void testNoSiteFailedConnect() {
//        thrown.expect(NoSiteException.class);
//        testClient.connect("NotASiteCode");
//    }

}
