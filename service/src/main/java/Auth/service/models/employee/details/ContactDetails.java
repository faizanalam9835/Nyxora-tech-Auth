package Auth.service.models.employee.details;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDetails {

    @Column(nullable = false)
    private String mobileNumber;

    private String alternateNumber;

    @Column(nullable = false)
    private String personalEmail;

    private String professionalEmail;
    private String password;
    // Additional fields for a complete HRMS profile
}