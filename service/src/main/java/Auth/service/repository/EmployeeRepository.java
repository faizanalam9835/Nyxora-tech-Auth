package Auth.service.repository;

import Auth.service.models.employee.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employe, Long> {

    /**
     * Iska use hum "NY-2026-1" jaisi custom ID banane ke liye karenge.
     * Ye count karega ki ek specific company (tenant) ke kitne employees hain.
     */
    long countByTenantId(String tenantId);

    /**
     * Agar aapko database ID (Long) ki jagah custom Employee ID (String)
     * se search karna ho, toh ye kaam aayega.
     */
    Optional<Employe> findByBasicDetailsEmployeIdAndTenantId(String employeId, String tenantId);
}