package Auth.service.repository.recruitment;

import Auth.service.models.recruitment.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    // 🔒 Validate jobId belongs to the same tenant context
    List<Candidate> findAllByJobIdAndTenantId(Long jobId, String tenantId);

    Optional<Candidate> findByEmailAndTenantId(String email, String tenantId);

    // 🔒 Critical for Status Updates: Prevents cross-tenant ID guessing
    Optional<Candidate> findByIdAndTenantId(Long id, String tenantId);
}