package Auth.service.models.employee.details;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {

    private Integer casualLeave;    // CL
    private Integer sickLeave;      // SL
    private Integer earnedLeave;    // EL
    private Integer paidLeave;      // PL
    private Integer unpaidLeave;    // LWP (Leave Without Pay)

    private String leaveCarryForward; // Dropdown: Allowed / Not Allowed
    private Integer maxCarryForwardLimit;

    private Integer totalAnnualLeave; // CL + SL + EL + PL ka total
}