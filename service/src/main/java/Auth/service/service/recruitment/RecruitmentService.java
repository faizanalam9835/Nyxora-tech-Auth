package Auth.service.service.recruitment;

import Auth.service.models.User;
import Auth.service.models.recruitment.*;
import Auth.service.repository.*;
import Auth.service.repository.recruitment.*;
import Auth.service.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    // --- 🛡️ HELPER: Role Power for Hierarchy Check ---
    private int getRolePower(String role) {
        if (role == null) return 0;
        return switch (role.toUpperCase()) {
            case "SUPER_ADMIN" -> 4;
            case "ADMIN" -> 3;
            case "MANAGER" -> 2;
            case "EMPLOYEE", "STAFF" -> 1;
            default -> 0;
        };
    }

    // --- 📝 1. CREATE JOB (With ABAC & Hierarchy) ---
    @Transactional
    public JobPosting createJob(JobPosting job) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));
        String tenantId = TenantContext.getCurrentTenant();

        // Admin ka department lookup for ABAC
        String creatorDept = employeeRepository.findByContactDetailsProfessionalEmail(creator.getEmail())
                .map(e -> e.getJoiningInfo().getDepartment().toUpperCase())
                .orElse("ALL");

        // Hierarchy Check: Role rank ke niche hi hire kar sakte hain
        if (!creator.getRole().equalsIgnoreCase("SUPER_ADMIN")) {
            if (getRolePower(job.getDesignation()) >= getRolePower(creator.getRole())) {
                throw new RuntimeException("Access Denied: Higher rank recruitment not allowed.");
            }
            job.setDepartment(creatorDept); // ABAC Lock
        }

        job.setTenantId(tenantId);
        job.setCreatedBy(creator.getEmail());
        job.setStatus("OPEN");
        return jobRepository.save(job);
    }

    // --- 📊 2. BULK UPLOAD (Excel Integration with Formatter) ---
    @Transactional
    public void uploadCandidatesFromExcel(MultipartFile file, Long jobId) throws IOException {
        String tenantId = TenantContext.getCurrentTenant();

        // 🔒 Ownership Check: Kya ye job isi tenant ki hai?
        JobPosting job = jobRepository.findByIdAndTenantId(jobId, tenantId)
                .orElseThrow(() -> new RuntimeException("Job access denied."));

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        List<Candidate> candidates = new ArrayList<>();
        DataFormatter formatter = new DataFormatter(); // Phone number formatting ke liye

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            candidates.add(Candidate.builder()
                    .name(formatter.formatCellValue(row.getCell(0)))
                    .email(formatter.formatCellValue(row.getCell(1)))
                    .phone(formatter.formatCellValue(row.getCell(2)))
                    .status("APPLIED")
                    .jobId(jobId)
                    .department(job.getDepartment())
                    .tenantId(tenantId)
                    .build());
        }
        List<Candidate> savedCandidates = candidateRepository.saveAll(candidates);
        workbook.close();

        savedCandidates.forEach(c -> emailService.sendConfirmationEmail(c.getEmail(), c.getName(), job.getJobTitle()));
    }

    // --- 👤 3. ADD SINGLE CANDIDATE (Manual Entry) ---
    @Transactional
    public Candidate addSingleCandidate(Candidate candidate, Long jobId) {
        String tenantId = TenantContext.getCurrentTenant();

        JobPosting job = jobRepository.findByIdAndTenantId(jobId, tenantId)
                .orElseThrow(() -> new RuntimeException("Job access denied."));

        candidate.setJobId(jobId);
        candidate.setTenantId(tenantId);
        candidate.setDepartment(job.getDepartment());
        candidate.setStatus("APPLIED");
        Candidate saved =  candidateRepository.save(candidate);

        emailService.sendConfirmationEmail(saved.getEmail(), saved.getName(), job.getJobTitle());
        return saved;
    }

    // --- 🔄 4. STATUS MANAGEMENT (Applied -> Selected/Rejected) ---
    @Transactional
    public Candidate updateCandidateStatus(Long candidateId, String newStatus) {
        String tenantId = TenantContext.getCurrentTenant();

        // 1. Candidate fetch
        Candidate candidate = candidateRepository.findByIdAndTenantId(candidateId, tenantId)
                .orElseThrow(() -> new RuntimeException("Candidate not found or access denied."));

        // 2. Status update
        candidate.setStatus(newStatus.toUpperCase());
        if (newStatus.equalsIgnoreCase("REJECTED")) {
            candidate.setOfferedCTC(null);
            candidate.setJoiningDate(null);
        }

        Candidate updatedCandidate = candidateRepository.save(candidate);

        // 3. Job Details (Email ke liye zaroori)
        // Yahan ensure karein ki jobId null nahi hai
        JobPosting job = jobRepository.findById(updatedCandidate.getJobId())
                .orElseThrow(() -> new RuntimeException("Job details not found for candidate."));

        // 4. Console Log (Debugging ke liye)
        System.out.println("Triggering Status Email for: " + updatedCandidate.getEmail() + " | Status: " + newStatus);

        // 5. Email Service Call
        emailService.sendStatusUpdateEmail(
                updatedCandidate.getEmail(),
                updatedCandidate.getName(),
                job.getJobTitle(),
                newStatus
        );

        return updatedCandidate;
    }

    // --- 💰 5. SELECTION & INVOICE DATA GENERATION ---
    @Transactional
    public Map<String, Object> finalizeSelection(Long candidateId, Double ctc, String doj) {
        String tenantId = TenantContext.getCurrentTenant();

        Candidate candidate = candidateRepository.findByIdAndTenantId(candidateId, tenantId)
                .orElseThrow(() -> new RuntimeException("Candidate access denied."));

        candidate.setStatus("SELECTED");
        candidate.setOfferedCTC(ctc);
        candidate.setJoiningDate(doj);
        candidateRepository.save(candidate);

        // Map for Invoice Generation
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("invoiceNo", "INV-NY-" + System.currentTimeMillis());
        invoiceData.put("candidateName", candidate.getName());
        invoiceData.put("ctc", ctc);
        invoiceData.put("joiningDate", doj);
        invoiceData.put("department", candidate.getDepartment());

        return invoiceData;
    }

    // --- 📂 6. GETTERS (ABAC & Tenant Isolated) ---

    public List<JobPosting> getAllJobs() {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(currentUserId).orElseThrow();
        String tenantId = TenantContext.getCurrentTenant();

        // Super Admin poori company ke dekh sakta hai
        if (user.getRole().equalsIgnoreCase("SUPER_ADMIN")) {
            return jobRepository.findAllByTenantId(tenantId);
        }

        // Admin/Manager sirf apne department ke
        String dept = employeeRepository.findByContactDetailsProfessionalEmail(user.getEmail())
                .map(e -> e.getJoiningInfo().getDepartment())
                .orElse("NONE");

        return jobRepository.findAllByDepartmentIgnoreCaseAndTenantId(dept, tenantId);
    }

    public List<Candidate> getCandidatesByJob(Long jobId) {
        String tenantId = TenantContext.getCurrentTenant();
        // Validation: Job access check before listing candidates
        jobRepository.findByIdAndTenantId(jobId, tenantId)
                .orElseThrow(() -> new RuntimeException("Job access denied."));

        return candidateRepository.findAllByJobIdAndTenantId(jobId, tenantId);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        String tenantId = TenantContext.getCurrentTenant();
        JobPosting job = jobRepository.findByIdAndTenantId(jobId, tenantId)
                .orElseThrow(() -> new RuntimeException("Unauthorized delete attempt."));

        jobRepository.delete(job);
    }
}