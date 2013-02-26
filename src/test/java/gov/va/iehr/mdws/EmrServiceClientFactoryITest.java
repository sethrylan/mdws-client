package gov.va.iehr.mdws;

import gov.va.common.ClassPathSearcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Factory;

/**
 *
 * @author gaineys
 */
public class EmrServiceClientFactoryITest {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(EmrServiceClientFactoryITest.class);

    @Factory
    public Object[] createInstances() {
        Collection<String> appContextsFiles = ClassPathSearcher.findFilesInClassPath(".*spring\\\\test\\d-applicationContext.xml").keySet();
        Assert.assertTrue("There should be at least one test#-applicationContext.xml file.", appContextsFiles.size() > 0);
        List<Object> testInstances = new ArrayList<Object>();
        
        for (String appContextFile : appContextsFiles) {
            ApplicationContext appContext = new ClassPathXmlApplicationContext(appContextFile.substring(appContextFile.lastIndexOf("spring")));
            Assert.assertTrue(appContext.containsBean("emrServiceClient"));
            testInstances.add(new EmrServiceClientTestInstance((EmrServiceClient)appContext.getBean("emrServiceClient")));
        }
        return testInstances.toArray();
    }

}
