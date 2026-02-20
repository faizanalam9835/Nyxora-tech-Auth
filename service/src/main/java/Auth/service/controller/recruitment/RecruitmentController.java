package Auth.service.controller.recruitment;

import Auth.service.models.recruitment.*;
import Auth.service.service.recruitment.RecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @PostMapping("/jobs/add")
    public ResponseEntity<JobPosting> createJob(@RequestBody JobPosting job) {
        return ResponseEntity.ok(recruitmentService.createJob(job));
    }

    // ✨ ADDED: Single Candidate Manual Entry
    @PostMapping("/jobs/{jobId}/candidates/add")
    public ResponseEntity<Candidate> addSingleCandidate(@PathVariable Long jobId, @RequestBody Candidate candidate) {
        return ResponseEntity.ok(recruitmentService.addSingleCandidate(candidate, jobId));
    }

    @PostMapping("/jobs/{jobId}/candidates/upload")
    public ResponseEntity<String> uploadBulk(@PathVariable Long jobId, @RequestParam("file") MultipartFile file) {
        try {
            recruitmentService.uploadCandidatesFromExcel(file, jobId);
            return ResponseEntity.ok("Bulk upload successful!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/candidates/{id}/status")
    public ResponseEntity<Candidate> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(recruitmentService.updateCandidateStatus(id, status));
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobPosting>> getAllJobs() {
        return ResponseEntity.ok(recruitmentService.getAllJobs());
    }

    @GetMapping("/jobs/{jobId}/candidates")
    public ResponseEntity<List<Candidate>> getCandidates(@PathVariable Long jobId) {
        return ResponseEntity.ok(recruitmentService.getCandidatesByJob(jobId));
    }

    @PostMapping("/candidates/{id}/finalize")
    public ResponseEntity<Map<String, Object>> finalizeSelection(@PathVariable Long id, @RequestParam Double ctc, @RequestParam String doj) {
        return ResponseEntity.ok(recruitmentService.finalizeSelection(id, ctc, doj));
    }
}