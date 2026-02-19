package Auth.service.models.employee.details;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime; // Time handle karne ke liye

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoiningDetails {

    private LocalDate joiningDate;

    private String department;

    private String designation;

    private String employmentType;

    // --- Dynamic Shift Logic ---
    private String shiftName; // e.g., "Morning Shift", "Night Shift"

    private LocalTime shiftStartTime; // e.g., 09:00:00

    private LocalTime shiftEndTime;   // e.g., 18:00:00

    private String reportingManager;

    private String workLocation;

    private LocalDate confirmationDate;
}