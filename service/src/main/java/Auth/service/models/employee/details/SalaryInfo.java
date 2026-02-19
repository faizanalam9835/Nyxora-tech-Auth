package Auth.service.models.employee.details;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryInfo {

    private Double basicSalary; // Mandatory field in UI

    private Double hra; // House Rent Allowance

    private Double otherAllowances;

    // --- Provident Fund (PF) Section ---
    private String providentFundEligible; // Yes/No dropdown
    private String pfAccountNumber;
    private Double pfAmount;

    // --- Employee State Insurance (ESI) Section ---
    private String esiEligible; // Yes/No dropdown
    private String esiNumber;
    private Double esiAmount;

    // --- Totals ---
    private Double grossSalary; // Total before deductions
    private Double netSalary;   // Hand-in-hand salary
}