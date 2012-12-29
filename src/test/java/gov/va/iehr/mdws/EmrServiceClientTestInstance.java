package gov.va.iehr.mdws;

import gov.va.common.TestClass;
import gov.va.medora.mdws.emrsvc.AllergyTO;
import gov.va.medora.mdws.emrsvc.DataSourceTO;
import gov.va.medora.mdws.emrsvc.HospitalLocationTO;
import gov.va.medora.mdws.emrsvc.MedicationTO;
import gov.va.medora.mdws.emrsvc.OrderTO;
import gov.va.medora.mdws.emrsvc.PatientTO;
import gov.va.medora.mdws.emrsvc.RegionTO;
import gov.va.medora.mdws.emrsvc.SiteTO;
import gov.va.medora.mdws.emrsvc.UserTO;
import gov.va.medora.mdws.emrsvc.VitalSignTO;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.hamcrest.Matcher;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.junit.Assert;
import static org.junit.matchers.JUnitMatchers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author gaineys
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"classpath*:/spring/test-applicationContext.xml"})
//@Configurable
//@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
//@RunWith(Parameterized.class)
public class EmrServiceClientTestInstance extends TestClass {
    
    private Logger logger = LoggerFactory.getLogger(EmrServiceClientTestInstance.class);
    private EmrServiceClient testClient;
    
    private static final String TEST_DFN = "66";
    
    private static final SimpleDateFormat VISTA_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat VISTA_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd.HHmmss");
    
    private static final int NANOSECONDS_PER_SECOND = 1000000000;
    
    private static final String SENSITIVE_STRING = "*SENSITIVE*";
    private static final String SSN_REGEX = "\\d{9}P?";
    private static final Pattern SSN_PATTERN = Pattern.compile(SSN_REGEX, Pattern.CASE_INSENSITIVE);
    
    // 1111.1;1;1
    private static final String ORDER_ID_REGEX = "\\d{4,5}(.\\d)?(;\\d){0,3}";
    
    private static final List<String> expectedMaritalStatuses = new ArrayList<String>() {{
        add("UNKNOWN");
        add("NEVER MARRIED");
        add("SEPARATED");
        add("MARRIED");
        add("SINGLE");
        add("WIDOWED");
    }};
    
    private static final List<String> expectedAllergenTypes = new ArrayList<String>() {{
        add("DRUG");
        add("FOOD");
        add("OTHER");
     }};
    
    private static final List<String> expectedVitalSignNames = new ArrayList<String>() {{
        add("Temperature");
        add("Pulse");
        add("Respiration");
        add("Blood Pressure");
        add("Height");
        add("Weight");
        add("Pain");
        add("Systolic Blood Pressure");
        add("Diastolic Blood Pressure");
        add("Body Mass Index");
        add("Pulse Oxymetry");
     }};
    
    public EmrServiceClientTestInstance(EmrServiceClient testClient) {
        this.testClient = testClient;
    }

    @DataProvider(name = "dfns")
    public Object[][] dfns() {
        int start     = 5;
        int end       = 65;
        int increment = 7;
        List<String[]> dfns = new ArrayList<String[]>();
        for(int i=start; i<=end; i+=increment) {
            dfns.add(new String[]{String.valueOf(i)});
        }
        return dfns.toArray(new Object[0][0]);
    }


//    @DataProvider(name = "clients")
//    public Object[][] clients() {
//        Collection<String> propertyFiles = ClassPathSearcher.findFilesInClassPath(".*mdws\\d.properties").keySet();
//        List<Object[]> clients = new ArrayList<Object[]>();
//        
//        for (String propertyFile : propertyFiles) {
//            try {
//                Object[] t = {new EmrServiceClient(new PropertiesConfiguration(propertyFile))};
//                clients.add(t);
//            } catch (ConfigurationException ex) {
//                logger.error(null,ex);
//            }
//        }
//        return clients.toArray(new Object[0][0]);
//    }
    
    @Test(groups="predisconnect")
    public void testVHA() {
        List<RegionTO> regions = testClient.getVHA(); // or CPM
        Assert.assertTrue("One or most regions (aka VISNs) must exist.", regions.size() > 0);
        for(RegionTO region : regions) {
            Assert.assertTrue("One or most sites must exist in each region.", region.getSites().getSites().getSiteTO().size() > 0);
            for(SiteTO site : region.getSites().getSites().getSiteTO()) {
                Assert.assertNotNull(site.getSitecode());
                Assert.assertFalse(site.getSitecode().isEmpty());
                Assert.assertNotNull(site.getName());
                Assert.assertFalse(site.getName().isEmpty());
            }
        }
    }

    @Test(groups="predisconnect")
    public void testDataSource() {
        DataSourceTO dataSource = testClient.getDataSource();
        Assert.assertNotNull(dataSource);

        Assert.assertNotNull(dataSource.getContext());
        Assert.assertEquals("", dataSource.getContext());

        Assert.assertNotNull(dataSource.getDescription());
        Assert.assertEquals("", dataSource.getDescription());

        Assert.assertEquals("HIS", dataSource.getModality());
        Assert.assertEquals("VISTA", dataSource.getProtocol());
        Assert.assertEquals("active", dataSource.getStatus());

        Assert.assertEquals(testClient.getSiteCode(), dataSource.getSiteId().getTag());

        Assert.assertNotNull(dataSource.getVendor());
        Assert.assertEquals("", dataSource.getVendor());

        Assert.assertFalse(dataSource.isTestSource());
        Assert.assertTrue(dataSource.getWelcomeMessage().length() > 0);

        Assert.assertNotNull(dataSource.getVersion());
        Assert.assertEquals("", dataSource.getVersion());
    }

    
    @Test(groups="predisconnect")
    public void testUser() {
        UserTO user = testClient.getUser();
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getDUZ());
        Assert.assertNotNull(user.getDUZ());

        try {
            Integer.valueOf(user.getDUZ());
        } catch (NumberFormatException e) {
            Assert.fail("DUZ must an integer value.");
        }

        Assert.assertNotNull(user.getSSN());
        Assert.assertTrue("SSN does not match " + SSN_REGEX + ", case-insensitive; was " + user.getSSN(), SSN_PATTERN.matcher(user.getSSN()).matches());
        
        Assert.assertNull("user.siteMessage was expected null.", user.getSiteMessage());
    }

    @Test(groups="predisconnect")
    public void testUserAndDataSourceMatch() {
        DataSourceTO dataSource = testClient.getDataSource();
        UserTO user = testClient.getUser();

        Assert.assertEquals(dataSource.getSiteId().getTag(), user.getSiteId());
    }

    @Test(groups="predisconnect")
    public void testGetVersion() {
        Assert.assertNotNull(testClient.getVersion());
        Assert.assertThat(testClient.getVersion(), containsString("mdws"));
        Assert.assertThat(testClient.getVersion(), containsString("Version="));
        Assert.assertThat(testClient.getVersion(), containsString("Culture=neutral"));
        Assert.assertThat(testClient.getVersion(), containsString("PublicKeyToken=null"));
    }

    @Test(groups="predisconnect")
    public void testGetWards() {
        List<HospitalLocationTO> wards = testClient.getWards();
        Assert.assertNotNull(wards);
        Assert.assertTrue(wards.size() > 0);
        for(HospitalLocationTO ward : wards) {
            Assert.assertFalse(ward.getId().isEmpty());
            try {
                Integer.valueOf(ward.getId());
            } catch (NumberFormatException e) {
                Assert.fail("DUZ must an integer value.");
            }

            Assert.assertFalse(ward.getName().isEmpty());
        }
    }
    
    @Test(groups="predisconnect")
    public void testGetTeams() {        
        Map<Long, String> teams  = testClient.getTeams();
        Assert.assertNotNull(teams);
        Assert.assertTrue(teams.size() > 0);
        for(Entry<Long,String> e : teams.entrySet()) {
            Assert.assertFalse(e.getValue().isEmpty());
        }
    }
    
    @Test(groups="predisconnect")
    public void testGetSpecialties() {
        Map<Long, String> specialties  = testClient.getSpecialties();
        Assert.assertNotNull(specialties);
        Assert.assertTrue(specialties.size() > 0);
        for(Entry<Long,String> e : specialties.entrySet()) {
            Assert.assertFalse(e.getValue().isEmpty());
        }
    }    
    
    @Test(groups="predisconnect")
    public void testGetPatientsByTeam() {
        Map<Long, String> teams  = testClient.getTeams();
        for(Long key : teams.keySet()) {
            List<PatientTO> patients  = testClient.getPatientsByTeam(key);
            Assert.assertNotNull(patients);
            
            for(PatientTO p : patients) {
                Assert.assertTrue(p.getSsn().isEmpty());
            }
        }
    }
    
    @Test(groups="predisconnect")
    public void testGetPatientsBySpecialty() {
        Map<Long, String> specialties  = testClient.getSpecialties();
        for(Long key : specialties.keySet()) {
            List<PatientTO> patients  = testClient.getPatientsBySpecialty(key);
            Assert.assertNotNull(patients);
            
            for(PatientTO p : patients) {
                Assert.assertTrue(p.getSsn().isEmpty());
            }
        }
    }

    @Test(groups="predisconnect")
    public void testGetPatientsByWardObject() {
        List<HospitalLocationTO> wards  = testClient.getWards();
        for(HospitalLocationTO ward : wards) {
            List<PatientTO> patients  = testClient.getPatientsByWard(ward);
            Assert.assertNotNull(patients);
            
            for(PatientTO p : patients) {
                Assert.assertTrue(p.getSsn().isEmpty());
            }

        }
    }
    
    @Test(groups="predisconnect", dataProvider="dfns")
    public void testGetPatient(String dfn) {

        PatientTO patient  = testClient.getPatient(dfn);

        Assert.assertNotNull(patient);
        
        Assert.assertNotNull(patient.getSsn());
        Assert.assertFalse(patient.getSsn().isEmpty());
        Assert.assertTrue("SSN does not match " + SSN_REGEX + ", case-insensitive; was " + patient.getSsn(), SSN_PATTERN.matcher(patient.getSsn()).matches());
                
        Assert.assertEquals(dfn, patient.getLocalPid());
        
        Assert.assertNotNull(patient.getGender());
        if (!patient.getGender().isEmpty()) {
            Assert.assertThat("patient gender was " + patient.getGender(), patient.getGender(), either(containsString("M")).or(containsString("F")));
        }
        
        if (patient.getMaritalStatus() != null && !patient.getMaritalStatus().isEmpty()) {
            Assert.assertTrue("Martial status was not in expected list; was " + patient.getMaritalStatus(), expectedMaritalStatuses.contains(patient.getMaritalStatus()));
        }

        Assert.assertThat(patient.getName(), containsString(","));

        Assert.assertEquals(patient.getName(), patient.getPatientName());
                
        Assert.assertTrue(patient.getAge() > 0 && patient.getAge() < 200);
                
        Assert.assertNotNull(patient.getDob());
        try {
            Date dob = VISTA_DATE_FORMAT.parse(patient.getDob());
            Assert.assertTrue(dob.before(new Date()));
            
            // dob + age <= today && dob + age < today + 1year
            Calendar c = Calendar.getInstance();
            c.setTime(dob);
            c.add(Calendar.YEAR, patient.getAge());
            Assert.assertTrue("dob + age <= today; dfn(" + dfn + "), dob (" + patient.getDob() + "), age (" + patient.getAge() + ")" , Calendar.getInstance().after(c));
            
            Calendar oneYearFromNow = Calendar.getInstance();
            oneYearFromNow.add(Calendar.YEAR, 1);
            Assert.assertTrue("dob + age < today + 1year; dfn(" + dfn + "), dob (" + patient.getDob() + "), age (" + patient.getAge() + ")" , c.before(oneYearFromNow));
            
        } catch (ParseException ex) {
            Assert.fail("Date of Birth could not be parsed. " + ex.getMessage() + " " + patient.getDob());
        } catch (NumberFormatException nfe) {
            Assert.fail("Date of Birth could not be parsed. " + nfe.getMessage() + " " + patient.getDob());
        }
                
    }

    @Test(groups="predisconnect", dataProvider="dfns")
    public void testGetMedications(String dfn) {
        List<MedicationTO> meds  = testClient.getMedications(dfn);
        Assert.assertNotNull(meds);
//        System.out.println("MEDICATIONS : "  + meds.size());
        for (MedicationTO med : meds) {
            if (med.getId() != null ) {
                Assert.assertFalse(med.getId().isEmpty());
                Assert.assertFalse(med.getName().isEmpty());
            } else {
                logger.warn("Med has null id (dfn=" + dfn + ") : " + med.getName());
            }
        }
    }
    
    @Test(groups="predisconnect", dataProvider="dfns")
    public void testGetOrders(String dfn) {
        List<OrderTO> orders  = testClient.getOrders(dfn);
        Assert.assertNotNull(orders);
        
//        System.out.println("ORDERS : " + orders.size());
        
        for (OrderTO order : orders) {
            Assert.assertFalse(order.getId().isEmpty());
            
            Assert.assertTrue("OrderID does not match " + ORDER_ID_REGEX + "; was " + order.getId(), Pattern.matches(ORDER_ID_REGEX, order.getId()));
            
            for(String timestamp : new String[]{order.getTimestamp(), order.getStartDate(), order.getStopDate(), order.getDateReviewed(), order.getDateVerified()}) {
                Assert.assertNotNull(timestamp);
         
                if (!order.getTimestamp().isEmpty()) {
                    try {
                        VISTA_TIMESTAMP_FORMAT.parse(timestamp);
                    } catch (ParseException ex) {
                        Assert.fail("Field was not in VistA timestamp format. Was " + timestamp);
                    } catch (NumberFormatException nfe) {
                        logger.error(nfe.toString());
                        Assert.fail("Field was not correct number format. Was " + timestamp);

                    }
                } else {
                    logger.warn("Order timestamp empty for " + order.getId());
                }
            }
        }
    }
    
    @Test(groups="predisconnect", dataProvider="dfns")
    public void testGetAllergies(String dfn) {
        List<AllergyTO> allergies  = testClient.getAllergies(dfn);
        Assert.assertNotNull(allergies);
//        Assert.assertTrue(allergies.size() > 0);
        
//        System.out.println("ALLERGIES : " + allergies.size());
        
        for (AllergyTO allergy : allergies) {
            
            if (allergy.getAllergenId() != null) {
                try {
                    Integer.parseInt(allergy.getAllergenId());
                } catch (NumberFormatException nfe) {
                    Assert.fail("AllergenId must be an integer.");
                }
            } else {
                logger.warn("Allergen has null id (dfn=" + dfn + ") : " + allergy.getAllergenName());
            }
            
            if (allergy.getAllergenType() != null) {
                List<String> allergenTypes = Arrays.asList(allergy.getAllergenType().split("\\s*,\\s*"));
                if (allergenTypes == null || allergenTypes.isEmpty()) {
                    logger.warn("AllergenTypes empty : " + allergy.getAllergenType());
                }
                Assert.assertTrue(allergenTypes.size() > 0 && allergenTypes.size() <= 3);
                for (String allergenType : allergenTypes) {
                    Assert.assertTrue(expectedAllergenTypes.contains(allergenType));
                }
            } else {
                    logger.warn("AllergenTypes empty : (dfn=" + dfn + ") : " + allergy.getAllergenName());
            }
            
            
            if(allergy.getTimestamp() != null && !allergy.getTimestamp().isEmpty()) {
                try {
                    VISTA_TIMESTAMP_FORMAT.parse(allergy.getTimestamp());
                } catch (ParseException ex) {
                    Assert.fail("Allergy timestamp must be in VistA timestamp format; was " + allergy.getTimestamp());
                }
            } else {
                    logger.warn("Allergy timestamp empty : (dfn=" + dfn + ") : " + allergy.getAllergenName());
            }
        }
    }
    
    @Test(groups="predisconnect", dataProvider="dfns")
    public void testGetVitalSigns(String dfn) {
        List<VitalSignTO> vitals  = testClient.getVitalSigns(dfn);
        Assert.assertNotNull(vitals);
//        Assert.assertTrue(vitals.size() > 0);

        for (VitalSignTO vitalSign : vitals) {
            
            Assert.assertNull("VitalSign ID must be null; was " + vitalSign.getId(), vitalSign.getId());
            
            if (vitalSign.getType().getId() != null && !vitalSign.getType().getId().isEmpty()) {
                try{
                    Integer.parseInt(vitalSign.getType().getId());
                } catch (NumberFormatException nfe) {
                    Assert.fail("VitalSignId must be an integer.");
                }
            }
            
            Assert.assertEquals("Vital Sign", vitalSign.getType().getCategory());
                                    
            Assert.assertTrue("VitalSign type name was not in expected list; was " + vitalSign.getType().getName(), expectedVitalSignNames.contains(vitalSign.getType().getName()));
            
            if (vitalSign.getTimestamp() != null && !vitalSign.getTimestamp().isEmpty()) {
                try {
                    VISTA_TIMESTAMP_FORMAT.parse(vitalSign.getTimestamp());
                } catch (ParseException ex) {
                    Assert.fail("VitalSign timestamp must be in VistA timestamp format; was " + vitalSign.getTimestamp());
                }
            }
        }
    }
    
    
    @Test(groups="predisconnect", enabled=false)
    public void testGetRpcs() {
        
        List<String> rpcs = testClient.getRpcs();
        
        List<String> necessaryRpcs = new ArrayList<String>() {{
            add("XUS SIGNON SETUP");
            add("XWB CREATE CONTEXT");
            add("XUS INTRO MSG");
        }};

        Assert.assertThat(rpcs, (Matcher)hasItems(necessaryRpcs));        
    }
    
    @Test(groups="predisconnect", enabled=false)
    public void findPatients() {        
        for(Integer i=362111111; i<362999999; i++) {
            System.out.println(i.toString());
            if(testClient.getPatientsInMpi(i.toString()).size() > 0) {
                System.out.println("\tFOUND IN MPI");
            }
            if(testClient.getPatientsInNpt(i.toString()).size() > 0) {
                System.out.println("\tFOUND IN NPT");
            }
        }
    }
    
    @Test(groups="predisconnect", dataProvider="dfns", enabled=false)
    public void testGetPatientsInNpt(String dfn) {        
        PatientTO dfnPatient = testClient.getPatient(dfn);
        List<PatientTO> patients = testClient.getPatientsInNpt(dfnPatient.getSsn());
        Assert.assertEquals("No NPT patient for dfn=" + dfn + " , ssn=" + dfnPatient.getSsn(), 1, patients.size());
        PatientTO patient = patients.get(0);
        Assert.assertNotNull(patient);
        Assert.assertEquals(dfnPatient.getSsn(), patient.getSsn());
        Assert.assertEquals(dfnPatient.getName(), patient.getName());
        Assert.assertEquals(dfnPatient.getDob(), patient.getDob());
        Assert.assertEquals(dfnPatient.getLocalPid(), patient.getLocalPid());
        Assert.assertEquals(dfnPatient.getMpiPid(), patient.getMpiPid());
        // and so on
    }
    
    @Test(groups="predisconnect")
    public void testGetPatientsInNptNoResult() {
        List<PatientTO> patients = testClient.getPatientsInNpt("NOTANSSN");
        Assert.assertNotNull(patients);
        Assert.assertEquals(0, patients.size());
    }
    
    @Test(groups="predisconnect", dataProvider="dfns", enabled=false)
    public void testGetPatientsInMpi(String dfn) {
        PatientTO dfnPatient = testClient.getPatient(dfn);
        List<PatientTO> patients = testClient.getPatientsInNpt(dfnPatient.getSsn());
        Assert.assertEquals("No MPI/MVI patient for dfn=" + dfn + " , ssn=" + dfnPatient.getSsn(), 1, patients.size());
        PatientTO patient = patients.get(0);
        Assert.assertNotNull(patient);
        Assert.assertEquals(dfnPatient.getSsn(), patient.getSsn());
        Assert.assertEquals(dfnPatient.getName(), patient.getName());
        Assert.assertEquals(dfnPatient.getDob(), patient.getDob());
        Assert.assertEquals(dfnPatient.getLocalPid(), patient.getLocalPid());
        Assert.assertEquals(dfnPatient.getMpiPid(), patient.getMpiPid());
        // and so on
        
    }

    @Test(groups="predisconnect")
    public void testGetPatientsInMpiNoResult() {
        List<PatientTO> patients = testClient.getPatientsInMpi("NOTANSSN");
        Assert.assertNotNull(patients);
        Assert.assertEquals(0, patients.size());
    }


    @Test(groups="predisconnect", dataProvider="dfns")
    public void testGetPatientsMatch(String dfn) {
        PatientTO dfnPatient = testClient.getPatient(dfn);
        List<PatientTO> patients = testClient.getPatients(dfnPatient.getSsn());
        Assert.assertTrue("No match for ssn=" + dfnPatient.getSsn(), patients.size() > 0);
        if (patients.size() > 1) {
            logger.warn("Multiple patient for ssn=" + dfnPatient.getSsn());
        }
        PatientTO patient = patients.get(0);
        Assert.assertNotNull(patient);
        Assert.assertThat(patient.getSsn(), either(containsString(SENSITIVE_STRING)).or(containsString(dfnPatient.getSsn())));
        Assert.assertEquals(dfnPatient.getName(), patient.getName());
        Assert.assertThat(patient.getDob(), either(containsString(SENSITIVE_STRING)).or(containsString(dfnPatient.getDob())));
        Assert.assertEquals(dfnPatient.getLocalPid(), patient.getLocalPid());
        // MPI PID is not retrieved in getPatient(match)
        
        patients = testClient.getPatients(dfnPatient.getName());
        Assert.assertTrue("No match for name=" + dfnPatient.getName(), patients.size() > 0);
        // could be multiple matches for name (also could be multiple matches for ssn)
        
        String initalAndLastFour = dfnPatient.getName().substring(0,1) + dfnPatient.getSsn().substring(5);
        patients = testClient.getPatients(initalAndLastFour);
        Assert.assertTrue("No match for initial+last4=" + initalAndLastFour, patients.size() > 0);
    }

    @Test(groups="predisconnect")
    public void testGetPatientsMatchNoResult() {
        List<PatientTO> patients = testClient.getPatientsInMpi("NOTAREALNAME");
        Assert.assertNotNull(patients);
        Assert.assertEquals(0, patients.size());
    }
    
    @Test(groups="predisconnect", enabled=false)
    public void testGetTestDfn() {
        System.out.println("MEDICATIONS : "  + testClient.getMedications(TEST_DFN).size());
        System.out.println("ORDERS : "  + testClient.getOrders(TEST_DFN).size());
        System.out.println("ALLERGIES : "  + testClient.getAllergies(TEST_DFN).size());
    }
    
    @Test(dependsOnGroups = "predisconnect")
    public void testDisconnect() {
        testClient.disconnect();
        Assert.assertNull(testClient.getDataSource());
        Assert.assertNull(testClient.getUser());
    }
    
    
    
    
    @AfterSuite
    public void recordStatistics() {  
        
        Collection<String> simonNames = SimonManager.getSimonNames();
        for (String string : simonNames) {
            if (string.length() > 0) {
                Stopwatch stopwatch = SimonManager.getStopwatch(string);
                if (stopwatch.getCounter() != 0L) {
//                    this.logger.info("JavaSimon Result: {}", stopwatch);
//                    out.println(stopwatch.getName());
//                    out.println("\ttotal: " + BigInteger.valueOf(stopwatch.getTotal()).divide(NANOSECONDS_PER_SECOND_BIGINT));
//                    out.println("\tcount: " + stopwatch.getCounter());
//                    out.println("\tmax  : " + BigInteger.valueOf(stopwatch.getMax()).divide(NANOSECONDS_PER_SECOND_BIGINT));
//                    out.println("\tmin  : " + BigInteger.valueOf(stopwatch.getMin()).divide(NANOSECONDS_PER_SECOND_BIGINT));
//                    out.println("\tmu   : " + stopwatch.getMean()/NANOSECONDS_PER_SECOND);
//                    out.println("\tsigma: " + stopwatch.getStandardDeviation()/NANOSECONDS_PER_SECOND);
                    System.out.print(stopwatch.getName().substring("gov.va.iehr.mdws.".length()));
                    System.out.println(stopwatch.toString().substring(stopwatch.toString().indexOf(":"), stopwatch.toString().indexOf("[") - 1));
//                    System.out.println(", s " + (new DecimalFormat("#.##")).format(stopwatch.getStandardDeviation()));
                }
            }
        }


    }
    
    
    
}

