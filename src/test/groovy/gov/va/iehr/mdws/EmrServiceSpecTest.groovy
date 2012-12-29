import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import gov.va.iehr.mdws.EmrServiceClient
import spock.lang.*

@Ignore
@org.springframework.test.context.ContextConfiguration(locations = "classpath*:/spring/test-applicationContext.xml")
@org.springframework.test.context.TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
class EmrServiceClientSpecTest extends spock.lang.Specification {
  
    @Autowired private EmrServiceClient emrServiceClient;

/*
  def setupSpec() {
    emrClient
  }
*/
  def "trigger aspect"() {

    expect:
    emrServiceClient.testMethod()

  }

}
