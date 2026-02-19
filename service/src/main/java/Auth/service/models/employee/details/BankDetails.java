package Auth.service.models.employee.details;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetails {

    private String bankName;

    private String accountHolderName;

    private String accountNumber;

    private String ifscCode;

    private String branchName;

    private String upiId; // Form ke mutabiq UPI ID field
}