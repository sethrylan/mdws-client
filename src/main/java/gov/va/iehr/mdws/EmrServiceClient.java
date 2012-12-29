package gov.va.iehr.mdws;

import gov.va.medora.mdws.emrsvc.AllergyTO;
import gov.va.medora.mdws.emrsvc.HospitalLocationTO;
import gov.va.medora.mdws.emrsvc.MedicationTO;
import gov.va.medora.mdws.emrsvc.OrderTO;
import gov.va.medora.mdws.emrsvc.PatientTO;
import gov.va.medora.mdws.emrsvc.VitalSignTO;
import java.util.List;
import java.util.Map;
import org.javasimon.aop.Monitored;

/**
 *
 * @author gaineys
 */
@Monitored
public interface EmrServiceClient extends MdwsClient {
        
    List<HospitalLocationTO> getWards();

    /*
     <TaggedText>
        <tag>901</tag>
        <taggedResults>
            <TaggedText>
                <tag>203</tag>
                <text>C.VEHUIMAGING</text>
            </TaggedText>
     */
    Map<Long, String> getTeams();
    
    Map<Long, String> getSpecialties();
    
    List<PatientTO> getPatientsByTeam(Long teamId);
    
    List<PatientTO> getPatientsBySpecialty(Long specialtyId);
        
    List<PatientTO> getPatientsByWard(Long wardId);
    List<PatientTO> getPatientsByWard(HospitalLocationTO ward);
    
    List<OrderTO> getOrders(String dfn);
    List<MedicationTO> getMedications(String dfn);
    
    List<AllergyTO> getAllergies(String dfn);
    
    List<VitalSignTO> getVitalSigns(String dfn);
    
    List<PatientTO> getPatientsInNpt(String ssn);
    List<PatientTO> getPatientsInMpi(String ssn);
    
    /**
     * SSN, 'LAST,FIRST', A1234 (Last name initial + last four SSN)
     */
    List<PatientTO> getPatients(String matchString);
    
    
}
