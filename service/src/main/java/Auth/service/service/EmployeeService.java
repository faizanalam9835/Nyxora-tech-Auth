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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    private int getRolePower(String designation) {
        if (designation == null) return 0;
        return switch (designation.toUpperCase()) {
            case "SUPER_ADMIN" -> 4;
            case "ADMIN" -> 3;
            case "MANAGER" -> 2;
            case "EMPLOYEE", "STAFF" -> 1;
            default -> 0;
        };
    }

    private String getCreatorDept(User creator) {
        if (creator.getRole().equalsIgnoreCase("SUPER_ADMIN")) return "ALL";
        return employeeRepository.findByContactDetailsProfessionalEmail(creator.getEmail())
                .map(e -> e.getJoiningInfo().getDepartment().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Creator profile not found."));
    }

    private void validateAccess(User creator, Employe target) {
        if (creator.getRole().equalsIgnoreCase("SUPER_ADMIN")) return;

        String creatorDept = getCreatorDept(creator);
        String targetDept = target.getJoiningInfo().getDepartment();

        if (!creatorDept.equalsIgnoreCase(targetDept)) {
            throw new RuntimeException("Access Denied: Target is in " + targetDept + " department.");
        }

        int creatorPower = getRolePower(creator.getRole());
        int targetPower = getRolePower(target.getJoiningInfo().getDesignation());

        if (targetPower >= creatorPower) {
            throw new RuntimeException("Access Denied: You cannot manage someone with equal or higher rank.");
        }
    }

    @Transactional
    public Employe createEmployee(Employe employe) {
        String tenantId = TenantContext.getCurrentTenant();
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();

        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Creator context not found."));

        String creatorRole = creator.getRole().toUpperCase();
        String targetDesignation = employe.getJoiningInfo().getDesignation().toUpperCase();
        String creatorDept = getCreatorDept(creator);
        String targetDept = employe.getJoiningInfo().getDepartment();

        if (!creatorRole.equals("SUPER_ADMIN")) {
            if (!creatorDept.equalsIgnoreCase(targetDept)) {
                throw new RuntimeException("Access Denied: You can only onboard for " + creatorDept);
            }
            if (getRolePower(targetDesignation) >= getRolePower(creatorRole)) {
                throw new RuntimeException("Access Denied: Rank violation.");
            }
        }

        String profEmail = employe.getContactDetails().getProfessionalEmail();
        User userAccount = User.builder()
                .username(profEmail)
                .email(profEmail)
                .password(passwordEncoder.encode(employe.getContactDetails().getPassword()))
                .companyId(creator.getCompanyId())
                .companyName(creator.getCompanyName())
                .tenantId(tenantId)
                .role(targetDesignation)
                .active(true)
                .build();

        userRepository.save(userAccount);

        employe.setTenantId(tenantId);
        if (employe.getActive() == null) employe.setActive(true);

        String prefix = (tenantId != null && tenantId.length() >= 2) ? tenantId.substring(0, 2).toUpperCase() : "EMP";
        long nextSequence = employeeRepository.countByTenantId(tenantId) + 1;
        employe.getBasicDetails().setEmployeId(String.format("%s-%d-%d", prefix, employe.getJoiningInfo().getJoiningDate().getYear(), nextSequence));

        employe.getContactDetails().setPassword(null);
        return employeeRepository.save(employe);
    }

    public List<Employe> getAllEmployees() {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findById(currentUserId).orElseThrow();
        String dept = getCreatorDept(creator);
        int creatorPower = getRolePower(creator.getRole());

        enableTenantFilter();

        List<Employe> results = dept.equals("ALL") ?
                employeeRepository.findAll() :
                employeeRepository.findAllByJoiningInfoDepartmentIgnoreCase(dept);

        return results.stream()
                .filter(e -> getRolePower(e.getJoiningInfo().getDesignation()) < creatorPower)
                .collect(Collectors.toList());
    }

    public Optional<Employe> getEmployeeById(Long id) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findById(currentUserId).orElseThrow();
        enableTenantFilter();
        Employe emp = employeeRepository.findById(id).orElseThrow();
        validateAccess(creator, emp);
        return Optional.of(emp);
    }

    @Transactional
    public Employe updateEmployee(Long id, Employe updatedDetails) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findById(currentUserId).orElseThrow();
        enableTenantFilter();
        Employe existing = employeeRepository.findById(id).orElseThrow();
        validateAccess(creator, existing);
        updatedDetails.setId(id);
        updatedDetails.setTenantId(existing.getTenantId());
        return employeeRepository.save(updatedDetails);
    }

    // ⭐ PATCH Method fixed
    @Transactional
    public Employe patchEmployee(Long id, Map<String, Object> updates) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findById(currentUserId).orElseThrow();
        enableTenantFilter();
        Employe existing = employeeRepository.findById(id).orElseThrow();
        validateAccess(creator, existing);

        if (updates.containsKey("designation")) {
            existing.getJoiningInfo().setDesignation((String) updates.get("designation"));
        }
        return employeeRepository.save(existing);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findById(currentUserId).orElseThrow();
        enableTenantFilter();
        Employe existing = employeeRepository.findById(id).orElseThrow();
        validateAccess(creator, existing);
        userRepository.findByEmail(existing.getContactDetails().getProfessionalEmail())
                .ifPresent(userRepository::delete);
        employeeRepository.deleteById(id);
    }

    private void enableTenantFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
                .setParameter("tenantId", TenantContext.getCurrentTenant());
    }
}