package Auth.service.repository;

import Auth.service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List; // 👈 Ye import zaroori hai

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    // ⭐ Ye method add karein taaki EmployeeService mein .stream() kaam kar sake
    List<User> findByTenantId(String tenantId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByCompanyId(String companyId);
}