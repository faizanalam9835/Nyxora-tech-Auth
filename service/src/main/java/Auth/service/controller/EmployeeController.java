package Auth.service.controller;

import Auth.service.models.employee.Employe;
import Auth.service.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Frontend (Vite/React) se connect karne ke liye
public class EmployeeController {

    private final EmployeeService employeeService;

    // --- 1. Create Employee (Auto ID: NY-2026-1) ---
    @PostMapping("/add")
    public ResponseEntity<Employe> add(@RequestBody Employe emp) {
        // Service layer automatic tenantId aur custom employeeId set karega
        return ResponseEntity.ok(employeeService.createEmployee(emp));
    }

    // --- 2. Get All Employees (Tenant Isolated) ---
    @GetMapping("/all")
    public ResponseEntity<List<Employe>> getAll() {
        // Sirf wahi data dikhega jo logged-in user ki company ka hai
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // --- 3. Get Single Employee by Database ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Employe> getById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- 4. Full Update (PUT) ---
    @PutMapping("/{id}")
    public ResponseEntity<Employe> fullUpdate(@PathVariable Long id, @RequestBody Employe emp) {
        // Poora 80-column profile ek saath update karne ke liye
        return ResponseEntity.ok(employeeService.updateEmployee(id, emp));
    }

    // --- 5. Partial Update (PATCH) ---
    @PatchMapping("/{id}")
    public ResponseEntity<Employe> partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        // Sirf specific fields (jaise salary ya phone) change karne ke liye
        return ResponseEntity.ok(employeeService.patchEmployee(id, updates));
    }

    // --- 6. Delete Employee ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}