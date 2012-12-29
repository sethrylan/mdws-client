package gov.va.iehr.mdws;

import gov.va.medora.mdws.emrsvc.DataSourceTO;
import gov.va.medora.mdws.emrsvc.PatientTO;
import gov.va.medora.mdws.emrsvc.RegionTO;
import gov.va.medora.mdws.emrsvc.SiteTO;
import gov.va.medora.mdws.emrsvc.UserTO;
import java.util.List;

/**
 *
 * The MdwsClient interface.
 *
 * @author gaineys
 */
public interface MdwsClient {
    
    UserTO getUser();

    DataSourceTO getDataSource();

    List<RegionTO> getVHA();
    
    List<SiteTO> getVHASites();


    // disconnect -> return number of connected sites
    void disconnect();

    String getVersion();

    String getSiteCode();
    
    /**
     * 
<?xml version="1.0" encoding="utf-8"?>
<TextArray xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://mdws.medora.va.gov/EmrSvc">
  <count>0</count>
  <text>
    <string>[XWB]10304
TCPConnect50028fe80::1590:5c5d:6761:3fbb%11f00010f0029VAPHSMDWS3.innovations.va.govf&#x4;</string>
     * @return 
     */
    List<String> getRpcs();
    
    
    // findPatient(String ssn) "8 digits or 8 digits and a p" "Invalid SSN"
    
    
    
    /*
     * 
     <PatientTO xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://mdws.medora.va.gov/EmrSvc">
     <name>ZZZRETFOURSEVENTYTWO,PATIENT</name>
     <ssn>666229999</ssn>
     <gender>M</gender>
     <dob>19350407</dob>
     <age>77</age>
     <maritalStatus>MARRIED</maritalStatus>
     <demographics>
     <DemographicSetTO>
     <tag>901</tag>
     <addresses>
     <AddressTO>
     <city>SCHENECTADY</city>
     <county>4</county>
     <state>NEW YORK</state>
     <zipcode>99998</zipcode>
     </AddressTO>
     </addresses>
     </DemographicSetTO>
     </demographics>
     <patientName>ZZZRETFOURSEVENTYTWO,PATIENT</patientName>
     <mpiPid/>
     <mpiChecksum/>
     <localPid>66</localPid>
     <location>
     <id>129</id>
     <name>3E NORTH</name>
     <department/>
     <service/>
     <specialty>
     <tag>8</tag>
     <text/>
     </specialty>
     <facility>
     <sitecode>901</sitecode>
     <name>CPM</name>
     <uid/>
     <pid/>
     </facility>
     <room>3E</room>
     <bed>100</bed>
     <askForCheckIn>false</askForCheckIn>
     </location>
     <cwad>A</cwad>
     <restricted>false</restricted>
     <admitTimestamp>19940428.130000</admitTimestamp>
     <serviceConnected>false</serviceConnected>
     <scPercent>0</scPercent>
     <inpatient>true</inpatient>
     <deceasedDate/>
     <confidentiality>
     <tag>0</tag>
     <text/>
     </confidentiality>
     <needsMeansTest>true</needsMeansTest>
     <patientFlags>
     <count>0</count>
     </patientFlags>
     <cmorSiteId/>
     <activeInsurance>0</activeInsurance>
     <isTestPatient>false</isTestPatient>
     <hasInsurance>false</hasInsurance>
     <preferredFacility/>
     <patientType>NSC VETERAN</patientType>
     <isVeteran>false</isVeteran>
     <isLocallyAssignedMpiPid>false</isLocallyAssignedMpiPid>
     <sites>
     <count>1</count>
     <sites>
     <SiteTO>
     <sitecode>901</sitecode>
     <name>CPM</name>
     <lastEventTimestamp/>
     <lastEventReason/>
     <uid/>
     <pid/>
     </SiteTO>
     </sites>
     </sites>
     <team>
     <id/>
     <name/>
     <pcpName/>
     <attendingName>PROVIDER,THREE</attendingName>
     </team>
     </PatientTO>
     */
    PatientTO getPatient(String dfn);
    
    // login
    // addDataSource
    // getRpcs
}
