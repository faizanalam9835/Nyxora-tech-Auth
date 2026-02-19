package Auth.service.models.employee;

import Auth.service.models.employee.details.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.util.List;
import java.util.ArrayList;
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Multi-tenancy Filters
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔑 Multi-tenancy Key
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    // 📂 Basic Details Schema Import (Embedded)
    @Embedded
    private BasicDetails basicDetails;

    // Active status (Employee working hai ya left?)
    @Embedded
    private ContactDetails contactDetails;

    // Role (Employee, Manager, etc.)
    @Embedded
    private  Address address;

    @Embedded
    private JoiningDetails joiningInfo;

    @Embedded
    private SalaryInfo salaryInfo;

    @Embedded
    private BankDetails bankDetails;

    @ElementCollection
    @CollectionTable(name = "employee_qualifications", joinColumns = @JoinColumn(name = "employee_id"))
    private List<Qualification> qualifications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "employee_experiences",
            joinColumns = @JoinColumn(name = "employee_id")
    )
    @Builder.Default
    private List<Experience> experiences = new ArrayList<>();


    @Embedded
    private  Leave leaveInfo;
    // Check karein ye line hai ya nahi

    @Builder.Default
    private Boolean active = true;
}