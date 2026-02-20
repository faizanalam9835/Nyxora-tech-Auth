package Auth.service.repository;

import Auth.service.models.employee.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employe, Long> {

    // Custom ID generation ke liye zaroori hai
    long countByTenantId(String tenantId);

    // Creator ka department check karne ke liye (ABAC)
    Optional<Employe> findByContactDetailsProfessionalEmail(String email);

    // Department wise list nikalne ke liye (IT Admin ko sirf IT dikhane ke liye)
    List<Employe> findAllByJoiningInfoDepartmentIgnoreCase(String department);

    // Custom Employee ID se search karne ke liye
    Optional<Employe> findByBasicDetailsEmployeIdAndTenantId(String employeId, String tenantId);
}