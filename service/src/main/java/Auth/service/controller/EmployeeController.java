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
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/add")
    public ResponseEntity<Employe> add(@RequestBody Employe emp) {
        return ResponseEntity.ok(employeeService.createEmployee(emp));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Employe>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employe> getById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employe> fullUpdate(@PathVariable Long id, @RequestBody Employe emp) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, emp));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Employe> partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        // ⭐ Service method name corrected
        return ResponseEntity.ok(employeeService.patchEmployee(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}