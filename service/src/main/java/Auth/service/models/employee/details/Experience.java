package Auth.service.models.employee.details;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience {

    private Double totalExperienceYears; // e.g., 2.5 Years

    private String previousCompany;

    private String lastDesignation;

    private Double lastSalary;

    private String experienceProofUrl; // File path for "Proof" (Image/PDF)
}