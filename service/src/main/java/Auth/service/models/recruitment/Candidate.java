package Auth.service.models.recruitment;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String status;      // APPLIED, SHORTLISTED, SELECTED, REJECTED
    private Long jobId;

    @Column(nullable = false)
    private String tenantId;    // 🔒 Strictly Tenant Locked
    private String department;  // 🔒 Department isolation

    // Invoice/Offer Details (Selected hone par bharenge)
    private Double offeredCTC;
    private String joiningDate;
}