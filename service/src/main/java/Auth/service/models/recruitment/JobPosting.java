package Auth.service.models.recruitment;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;
    private String designation;   // 🔒 Logic ke liye (ADMIN, MANAGER, etc.)
    private String department;    // 🔒 ABAC Isolation
    private String location;
    private String employmentType;
    private Integer vacancies;
    private LocalDate deadline;
    private String salaryRange;
    private String experience;
    private String status;        // OPEN, CLOSED

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String tenantId;      // 🔒 Strictly Tenant Locked
    private String createdBy;
}