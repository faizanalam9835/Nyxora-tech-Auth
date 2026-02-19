package Auth.service.service;

import Auth.service.models.User;
import Auth.service.models.employee.Employe;
import Auth.service.models.employee.details.JoiningDetails;
import Auth.service.repository.EmployeeRepository;
import Auth.service.repository.UserRepository;
import Auth.service.security.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Transactional
    public Employe createEmployee(Employe employe) {
        String tenantId = TenantContext.getCurrentTenant();
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        // ID based lookup (Option 3)
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Creator context not found for ID: " + currentUserId));

        String creatorRole = creator.getRole().toUpperCase();
        String targetDesignation = employe.getJoiningInfo().getDesignation().toUpperCase();

        // Hierarchy Security
        if (creatorRole.equals("ADMIN") && targetDesignation.equals("ADMIN")) {
            throw new RuntimeException("Access Denied: An Admin cannot create another Admin account.");
        }
        if (!creatorRole.equals("SUPER_ADMIN") && targetDesignation.equals("SUPER_ADMIN")) {
            throw new RuntimeException("Access Denied: Permission required for SUPER_ADMIN role.");
        }

        String professionalEmail = employe.getContactDetails().getProfessionalEmail();
        if (userRepository.findByEmail(professionalEmail).isPresent()) {
            throw new RuntimeException("Error: Professional email '" + professionalEmail + "' already exists.");
        }

        String assignedRole = targetDesignation.equals("ADMIN") ? "ADMIN" :
                targetDesignation.equals("MANAGER") ? "MANAGER" : "EMPLOYEE";

        User userAccount = User.builder()
                .username(professionalEmail)
                .email(professionalEmail)
                .password(passwordEncoder.encode(employe.getContactDetails().getPassword()))
                .companyId(creator.getCompanyId())
                .companyName(creator.getCompanyName())
                .tenantId(tenantId)
                .role(assignedRole)
                .active(true)
                .build();

        userRepository.save(userAccount);

        employe.setTenantId(tenantId);
        if (employe.getActive() == null) employe.setActive(true);

        // Custom ID Generation
        String prefix = (tenantId != null && tenantId.length() >= 2) ? tenantId.substring(0, 2).toUpperCase() : "EMP";
        int year = employe.getJoiningInfo().getJoiningDate().getYear();
        long nextSequence = employeeRepository.countByTenantId(tenantId) + 1;
        employe.getBasicDetails().setEmployeId(String.format("%s-%d-%d", prefix, year, nextSequence));

        employe.getContactDetails().setPassword(null);
        return employeeRepository.save(employe);
    }

    public List<Employe> getAllEmployees() {
        enableTenantFilter();
        return employeeRepository.findAll();
    }

    public Optional<Employe> getEmployeeById(Long id) {
        enableTenantFilter();
        return employeeRepository.findById(id);
    }

    // ⭐ YEH METHOD MISSING THA YA GALAT THA
    @Transactional
    public Employe updateEmployee(Long id, Employe updatedDetails) {
        enableTenantFilter();
        return employeeRepository.findById(id).map(existing -> {
            updatedDetails.setId(id);
            updatedDetails.setTenantId(existing.getTenantId()); // Tenant lock
            return employeeRepository.save(updatedDetails);
        }).orElseThrow(() -> new RuntimeException("Employee record not found with id: " + id));
    }

    @Transactional
    public Employe patchEmployee(Long id, Map<String, Object> updates) {
        enableTenantFilter();
        Employe existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (updates.containsKey("designation")) {
            String newDesignation = (String) updates.get("designation");
            if (existing.getJoiningInfo() != null) {
                existing.getJoiningInfo().setDesignation(newDesignation);
            } else {
                JoiningDetails details = new JoiningDetails();
                details.setDesignation(newDesignation);
                existing.setJoiningInfo(details);
            }
        }
        return employeeRepository.save(existing);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        enableTenantFilter();
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
        }
    }

    private void enableTenantFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
                .setParameter("tenantId", TenantContext.getCurrentTenant());
    }
}