package Auth.service.models.employee.details;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import Auth.service.models.employee.enums.Gender;
import Auth.service.models.employee.enums.MarritalStatus;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasicDetails {

    @Column(nullable = false, unique = true)
    private String employeId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String fatherName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarritalStatus maritalStatus;

    private String bloodGroup;
    private String nationality;

    // --- Profile & Identity Documents ---

    private String profilePictureUrl; // Image ka storage path ya URL

    @Column(length = 12)
    private String aadharNumber;
    private String aadharFrontImageUrl;
    private String aadharBackImageUrl;

    @Column(length = 10)
    private String panNumber;
    private String panImageUrl;
}