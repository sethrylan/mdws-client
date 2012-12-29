package gov.va.iehr.mdws;

import gov.va.common.aop.annotations.Monitor;
import gov.va.iehr.mdws.RawXMLHandler.SOAPHandler;
import gov.va.iehr.mdws.exception.NoConnectionException;
import gov.va.iehr.mdws.exception.NoSiteException;
import gov.va.medora.mdws.emrsvc.AllergyTO;
import gov.va.medora.mdws.emrsvc.ArrayOfPatientTO;
import gov.va.medora.mdws.emrsvc.ArrayOfString;
import gov.va.medora.mdws.emrsvc.DataSourceArray;
import gov.va.medora.mdws.emrsvc.DataSourceTO;
import gov.va.medora.mdws.emrsvc.EmrSvc;
import gov.va.medora.mdws.emrsvc.EmrSvcSoap;
import gov.va.medora.mdws.emrsvc.FaultTO;
import gov.va.medora.mdws.emrsvc.HospitalLocationTO;
import gov.va.medora.mdws.emrsvc.MedicationTO;
import gov.va.medora.mdws.emrsvc.OrderTO;
import gov.va.medora.mdws.emrsvc.PatientTO;
import gov.va.medora.mdws.emrsvc.RegionTO;
import gov.va.medora.mdws.emrsvc.SiteTO;
import gov.va.medora.mdws.emrsvc.TaggedAllergyArray;
import gov.va.medora.mdws.emrsvc.TaggedMedicationArray;
import gov.va.medora.mdws.emrsvc.TaggedOrderArray;
import gov.va.medora.mdws.emrsvc.TaggedPatientArray;
import gov.va.medora.mdws.emrsvc.TaggedText;
import gov.va.medora.mdws.emrsvc.TaggedVitalSignSetArray;
import gov.va.medora.mdws.emrsvc.TaggedVitalSignSetArrays;
import gov.va.medora.mdws.emrsvc.TextArray;
import gov.va.medora.mdws.emrsvc.UserTO;
import gov.va.medora.mdws.emrsvc.VitalSignSetTO;
import gov.va.medora.mdws.emrsvc.VitalSignTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for 
 * 
 * JAX-WS does not support stateful .NET SOAP web services, so there are some unusual state-keeping aspects to this client.
 * 
 * @author gaineys
 */
//@Component
public class EmrServiceClientImpl implements EmrServiceClient {

    final static Logger logger = LoggerFactory.getLogger(EmrServiceClientImpl.class);

    private static final String NS = "mdws.emr.";
        
//    private EmrSvc emrService = null;
    private EmrSvcSoap emrSoapService = null;
    private DataSourceTO dataSource = null;
    private UserTO user = null;
    private List<RegionTO> vha = null;
    
    private final Object selectPatientLock = new Object();
        
    private static final String TRANSPORT_HEADERS = "javax.xml.ws.http.response.headers";
    private static final String ASP_NET_SESSION_ID_NAME = "ASP.NET_SessionId=";
    private static final String CPRS_RPC_OPTION = "OR CPRS GUI CHART";
    
    private static final String NO_CONNECTION_FAULT_MESSAGE = "There is no connection to log onto";
    private static final String NO_SITE_FAULT_MESSAGE = "Site not in site table";
    private static final String EXISTING_CONNECTION_FAULT_MESSAGE = "You are already connected to that site";
    private static final String CONNECTION_NOT_READY_FAULT_MESSAGE = "Connections not ready for operation";
    private static final String NOT_IMPLEMENTED_FAULT_TYPE = "System.NotImplementedException";
    private static final String APPLICATION_CONTEXT_FAULT_MESSAGE = "Application context has not been created!";
    private static final String TIMEOUT_FAULT_MESSAGE = "Timeout waiting for response from VistA";
    private static final String ABORTED_CONNECTION_FAULT_MESSAGE = "An established connection was aborted by the software in your host machine";
    private static final String RPC_XUS_AV_CODE_FAULT_MESSAGE = "The remote procedure XUS AV CODE is not registered to the option XUS SIGNON.";

        
    public EmrServiceClientImpl(PropertiesConfiguration properties) {
        this(properties.getString(NS + "siteCode"), properties.getString(NS + "accessCode"), properties.getString(NS + "verifyCode"));
    }
    

    public EmrServiceClientImpl(String siteCode, String accessCode, String verifyCode) {
                        
        // instantiate new proxy and immediately retrive service	
        this.emrSoapService = (new EmrSvc()).getEmrSvcSoap();
        
        // start ASP.NET session 
        this.vha = this.emrSoapService.getVHA().getRegions().getRegionTO();
        
        // set proxy properties for maintaining session in the prevailing fashion of our .NET brethren
        ((BindingProvider)this.emrSoapService).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
//        ((BindingProvider)this.emrSoapService).getRequestContext().put(HTTPConstants.COOKIE_STRING, "");

        // parse out the ASP.NET_SessionId and insert into the proxy
        // set-cookie response header will look like:
        //       ASP.NET_SessionId=juboz4juwidviimoy5teiixk; path=/; HttpOnly
//        Map responseContext = ((BindingProvider)this.emrSoapService).getResponseContext();
//        Map headers = (Map)responseContext.get(TRANSPORT_HEADERS);
//        String sessionInfo = (String)((List)headers.get(HTTPConstants.HEADER_SET_COOKIE)).get(0);
//        String[] sessionInfoPieces = sessionInfo.split("=");
//        String sessionId = sessionInfoPieces[1].split(";")[0];
//        System.out.println(accessCode + "; " + sessionId);
//        ((BindingProvider)this.emrSoapService).getRequestContext().put(HTTPConstants.COOKIE_STRING, ASP_NET_SESSION_ID_NAME + sessionId);

        // connect to one site and store datasource object
        this.dataSource = this.connect(siteCode);

        // login to connected site and store user object
        UserTO result = this.emrSoapService.login(accessCode, verifyCode, CPRS_RPC_OPTION);
        if (result.getFault() != null) {
            if (result.getFault().getMessage().equals(NO_CONNECTION_FAULT_MESSAGE)) {
                throw new NoConnectionException(result.getFault().getMessage());
            }
            if (result.getFault().getMessage().equals(RPC_XUS_AV_CODE_FAULT_MESSAGE)) {
                throw new RuntimeException(result.getFault().getMessage() + "  Suggestion: You may have already logged in.");
            }
            throw new RuntimeException(result.getFault().getMessage());
        }
        this.user = result;

    }    
        
    /**
     * {@inheritDoc} 
     */
    @Override
    public DataSourceTO getDataSource() {
        return this.dataSource;
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public UserTO getUser() {
        return this.user;
    }
        
    /**
     * {@inheritDoc} 
     */
    @Override
    public List<RegionTO> getVHA() {
        return this.vha;
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public List<SiteTO> getVHASites() {
        List<SiteTO> sites = new ArrayList<SiteTO>();
        for(RegionTO region : this.getVHA()) {
            sites.addAll(region.getSites().getSites().getSiteTO());
        }
        return sites;
    }
    

    private DataSourceTO connect(String sitecode) {
                
        DataSourceArray result = emrSoapService.connect(sitecode);
        if (result.getFault() != null) {
            if (result.getFault().getMessage().equals(NO_SITE_FAULT_MESSAGE)) {
                throw new NoSiteException(result.getFault().getMessage());
            }
            logFault(result.getFault());
            return null;
        }
        return result.getItems().getDataSourceTO().get(0);
    }

//    private UserTO login(String siteCode, String accessCode, String verifyCode) {
//        
//        EmrSvc _proxy = new EmrSvc();
//		
//        EmrSvcSoap _svc = _proxy.getEmrSvcSoap();
//        
//        // start session 
//        _svc.getVHA();
//        
//        // insert the ASP.NET_SessionId in to the proxy
//        // set-cookie response header looks like: ASP.NET_SessionId=juboz4juwidviimoy5teiixk; path=/; HttpOnly
//        
////        MessageContext inMC = _svc._getServiceClient().getLastOperationContext().getMessageContexts().get("In"); 
//
//        
//        Map responseContext = ((BindingProvider)_svc).getResponseContext();
////        Map headers = (Map<String,List<String>>)responseContext.get("javax.xml.ws.http.response.headers");
//
//        Map headers = (Map)responseContext.get(TRANSPORT_HEADERS);
//        
//        String sessionInfo = (String)((List)headers.get(HTTPConstants.HEADER_SET_COOKIE)).get(0);
//        String[] sessionInfoPieces = sessionInfo.split("=");
//        String sessionId = sessionInfoPieces[1].split(";")[0];
//        ((BindingProvider)_svc).getRequestContext().put(HTTPConstants.COOKIE_STRING, ASP_NET_SESSION_ID_NAME + sessionId);
//        
//        DataSourceTO ds = this.connect(siteCode);
//
//        UserTO result = emrSoapService.login(accessCode, verifyCode, CPRS_RPC_OPTION);
//        if (result.getFault() != null) {
//            System.out.println("LOGINRESULT: " + accessCode + " @ " + siteCode + " : " + result.getFault().getMessage());
//            if (result.getFault().getMessage().equals(NO_CONNECTION_FAULT_MESSAGE)) {
//                throw new NoConnectionException(result.getFault().getMessage());
//            }
//            logFault(result.getFault());
//            return null;
//        }
//        return result;
//    }
    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public void disconnect() {
        synchronized(this) {
            if (emrSoapService != null) {
                try {
                    emrSoapService.disconnect();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
            emrSoapService = null;
            user = null;
            dataSource = null;
            vha = null;
        }
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public List<String> getRpcs() {
                
//        final BindingProvider bindingProvider = (BindingProvider) this.emrSoapService;
//        final Binding binding = bindingProvider.getBinding();
//        final List<Handler> handlerChain = binding.getHandlerChain();
//        RawXMLHandler.LogicalHandler logicalHandler = new RawXMLHandler.LogicalHandler();
//        handlerChain.add(logicalHandler);
//        binding.setHandlerChain(handlerChain);

        return emrSoapService.getRpcs().getText().getString();

//        handlerChain.remove(logicalHandler);
//        binding.setHandlerChain(handlerChain);


    }

    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public String getVersion() {
        return emrSoapService.getVersion();
    }
    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public String getSiteCode() {
        return this.dataSource.getSiteId().getTag();
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public List<HospitalLocationTO> getWards() {
        return emrSoapService.getWards().getLocations().getHospitalLocationTO();
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public Map<Long, String> getTeams() {
        Map<Long, String> result = new HashMap<Long, String>();
        for(TaggedText taggedText : emrSoapService.getTeams().getTaggedResults().getTaggedText()) {
            result.put(Long.valueOf(taggedText.getTag()), taggedText.getText());
        }
        return result;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public Map<Long, String> getSpecialties() {
        Map<Long, String> result = new HashMap<Long, String>();
        for(TaggedText taggedText : emrSoapService.getSpecialties().getTaggedResults().getTaggedText()) {
            result.put(Long.valueOf(taggedText.getTag()), taggedText.getText());
        }
        return result;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public List<PatientTO> getPatientsByTeam(Long teamId) {
        List<PatientTO> result = new ArrayList<PatientTO>();
        ArrayOfPatientTO patientArray = emrSoapService.getPatientsByTeam(teamId.toString()).getPatients();
        if (patientArray != null) {
            result.addAll(patientArray.getPatientTO());
        }
        return result;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public List<PatientTO> getPatientsBySpecialty(Long specialtyId) {        
        List<PatientTO> result = new ArrayList<PatientTO>();
        ArrayOfPatientTO patientArray = emrSoapService.getPatientsBySpecialty(specialtyId.toString()).getPatients();
        if (patientArray != null) {
            result.addAll(patientArray.getPatientTO());
        }
        return result;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public List<PatientTO> getPatientsByWard(HospitalLocationTO ward) {
        return getPatientsByWard(Long.valueOf(ward.getId()));
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public List<PatientTO> getPatientsByWard(Long wardId) {
        List<PatientTO> result = new ArrayList<PatientTO>();
        ArrayOfPatientTO patientArray = emrSoapService.getPatientsByWard(wardId.toString()).getPatients();
        if (patientArray != null) {
            result.addAll(patientArray.getPatientTO());
        }
        return result;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public List<OrderTO> getOrders(String dfn) {
        synchronized (selectPatientLock) {
            emrSoapService.select(dfn);
            List<OrderTO> result = new ArrayList<OrderTO>();
            for (TaggedOrderArray taggedArray : emrSoapService.getAllOrders().getArrays().getTaggedOrderArray()) {
                if (taggedArray.getItems() != null) {
                    result.addAll(taggedArray.getItems().getOrderTO());
                }
            }
            return result;
        }
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public List<MedicationTO> getMedications(String dfn) {
        
        synchronized(selectPatientLock) {
            emrSoapService.select(dfn);
            List<MedicationTO> result = new ArrayList<MedicationTO>();
            for (TaggedMedicationArray taggedArray : emrSoapService.getAllMeds().getArrays().getTaggedMedicationArray()) {
                if (taggedArray.getMeds() != null) {
                    result.addAll(taggedArray.getMeds().getMedicationTO());
                }
            }
            return result;
        }
    }
    
    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public List<AllergyTO> getAllergies(String dfn) {
        synchronized(selectPatientLock) {
            emrSoapService.select(dfn);
            List<AllergyTO> result = new ArrayList<AllergyTO>();
            for(TaggedAllergyArray taggedArray : emrSoapService.getAllergies().getArrays().getTaggedAllergyArray()) {
                result.addAll(taggedArray.getAllergies().getAllergyTO());
            }
            return result;
        }
    }
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public List<VitalSignTO> getVitalSigns(String dfn) {
        synchronized(selectPatientLock) {
            emrSoapService.select(dfn);
            List<VitalSignTO> result = new ArrayList<VitalSignTO>();
            TaggedVitalSignSetArrays tvssas = emrSoapService.getVitalSigns();        
            for(TaggedVitalSignSetArray taggedSetArray : tvssas.getArrays().getTaggedVitalSignSetArray()) {
                if (taggedSetArray.getSets() != null) {
                    for(VitalSignSetTO set : taggedSetArray.getSets().getVitalSignSetTO()) {
                        result.addAll(set.getVitalSigns().getVitalSignTO());
                    }
                }
            }
            return result;
        }
    }
    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    public PatientTO getPatient(String dfn) {
        synchronized (selectPatientLock) {
            return emrSoapService.select(dfn.toString());
        }
    }
    
    @Override
    public List<PatientTO> getPatientsInNpt(String ssn) {
        List<PatientTO> result = new ArrayList<PatientTO>();
        ArrayOfPatientTO aop = this.emrSoapService.nptLookup(ssn).getPatients();
        if(aop != null) {
            result.addAll(aop.getPatientTO());
        }
        return result;
    }
    
    @Override
    public List<PatientTO> getPatientsInMpi(String ssn) {
        List<PatientTO> result = new ArrayList<PatientTO>();
        ArrayOfPatientTO aop = this.emrSoapService.mpiLookup(ssn).getPatients();
        if(aop != null) {
            result.addAll(aop.getPatientTO());
        }
        return result;
    }
    
    @Override
    public List<PatientTO> getPatients(String matchString) {
        List<PatientTO> result = new ArrayList<PatientTO>();
        for(TaggedPatientArray tpa : this.emrSoapService.match(matchString).getArrays().getTaggedPatientArray()) {
            result.addAll(tpa.getPatients().getPatientTO());
        }
        return result;
    }
    
//    private void loadProperties(){
//        Properties props = new Properties();
//        try {
//            props.load(getClass().getClassLoader().getResourceAsStream("mdws.properties"));
//        } catch (IOException ex) {
//            Logger.getLogger(EmrServiceClient.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        this.password = props.getProperty(NS + "password");
//        this.username = props.getProperty(NS + "username");
//    }

    private static void logFault(FaultTO fault) {
        StringBuilder faultMessage = new StringBuilder()
            .append(fault.getMessage())
            .append(" : ")
            .append(fault.getSuggestion());
        
        logger.info(faultMessage.toString());
    }

}
