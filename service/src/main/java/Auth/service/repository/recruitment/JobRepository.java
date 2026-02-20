package Auth.service.repository.recruitment;

import Auth.service.models.recruitment.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<JobPosting, Long> {
    // 🔒 Tenant Isolation: Sirf apni company ke jobs
    List<JobPosting> findAllByTenantId(String tenantId);

    // 🔒 ABAC: Department wise isolation
    List<JobPosting> findAllByDepartmentIgnoreCaseAndTenantId(String department, String tenantId);

    // 🔒 Single Record Security: Check ownership before fetch/edit
    Optional<JobPosting> findByIdAndTenantId(Long id, String tenantId);
}