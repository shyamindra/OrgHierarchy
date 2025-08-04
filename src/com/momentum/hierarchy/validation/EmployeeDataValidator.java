package com.momentum.hierarchy.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.momentum.hierarchy.employee.EmployeeRecord;

/**
 * Validates employee data for various edge cases and data integrity issues.
 */
public class EmployeeDataValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDataValidator.class);
    
    /**
     * Validation result containing issues found during validation.
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final Set<String> errors;
        private final Set<String> warnings;
        
        public ValidationResult(boolean isValid, Set<String> errors, Set<String> warnings) {
            this.isValid = isValid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public Set<String> getErrors() {
            return errors;
        }
        
        public Set<String> getWarnings() {
            return warnings;
        }
        
        public void logIssues() {
            if (!errors.isEmpty()) {
                logger.error("Validation errors found:");
                errors.forEach(error -> logger.error("  - {}", error));
            }
            if (!warnings.isEmpty()) {
                logger.warn("Validation warnings found:");
                warnings.forEach(warning -> logger.warn("  - {}", warning));
            }
        }
    }
    
    /**
     * Validates the employee data for various edge cases.
     * 
     * @param employeeRecords List of employee records to validate
     * @return ValidationResult containing validation status and issues
     */
    public ValidationResult validate(List<EmployeeRecord> employeeRecords) {
        Set<String> errors = new HashSet<>();
        Set<String> warnings = new HashSet<>();
        
        if (employeeRecords == null || employeeRecords.isEmpty()) {
            errors.add("Employee data is null or empty");
            return new ValidationResult(false, errors, warnings);
        }
        
        // Check for duplicate IDs
        checkForDuplicateIds(employeeRecords, errors);
        
        // Check for invalid manager IDs
        checkForInvalidManagerIds(employeeRecords, errors);
        
        // Check for circular references
        checkForCircularReferences(employeeRecords, errors);
        
        // Check for multiple CEOs
        checkForMultipleCEOs(employeeRecords, warnings);
        
        // Check for orphaned employees
        checkForOrphanedEmployees(employeeRecords, warnings);
        
        // Check for empty/null data
        checkForEmptyData(employeeRecords, errors);
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings);
    }
    
    private void checkForDuplicateIds(List<EmployeeRecord> employeeRecords, Set<String> errors) {
        Set<String> seenIds = new HashSet<>();
        for (EmployeeRecord record : employeeRecords) {
            if (record.getID() != null && !record.getID().trim().isEmpty()) {
                if (!seenIds.add(record.getID())) {
                    errors.add("Duplicate employee ID found: " + record.getID());
                }
            }
        }
    }
    
    private void checkForInvalidManagerIds(List<EmployeeRecord> employeeRecords, Set<String> errors) {
        Set<String> validEmployeeIds = employeeRecords.stream()
                .map(EmployeeRecord::getID)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());
        
        for (EmployeeRecord record : employeeRecords) {
            String managerId = record.getMgrID();
            if (managerId != null && !managerId.trim().isEmpty() && !validEmployeeIds.contains(managerId)) {
                errors.add("Invalid manager ID '" + managerId + "' for employee '" + record.getName() + "' (ID: " + record.getID() + ")");
            }
        }
    }
    
    private void checkForCircularReferences(List<EmployeeRecord> employeeRecords, Set<String> errors) {
        for (EmployeeRecord record : employeeRecords) {
            if (hasCircularReference(record, employeeRecords, new HashSet<>())) {
                errors.add("Circular reference detected involving employee '" + record.getName() + "' (ID: " + record.getID() + ")");
                break; // Only report the first circular reference found
            }
        }
    }
    
    private boolean hasCircularReference(EmployeeRecord employee, List<EmployeeRecord> allEmployees, Set<String> visited) {
        String employeeId = employee.getID();
        if (visited.contains(employeeId)) {
            return true; // Circular reference detected
        }
        
        String managerId = employee.getMgrID();
        if (managerId == null || managerId.trim().isEmpty()) {
            return false; // No manager, no circular reference
        }
        
        visited.add(employeeId);
        
        // Find the manager
        EmployeeRecord manager = allEmployees.stream()
                .filter(e -> managerId.equals(e.getID()))
                .findFirst()
                .orElse(null);
        
        if (manager != null) {
            return hasCircularReference(manager, allEmployees, visited);
        }
        
        return false;
    }
    
    private void checkForMultipleCEOs(List<EmployeeRecord> employeeRecords, Set<String> warnings) {
        List<EmployeeRecord> ceos = employeeRecords.stream()
                .filter(record -> record.getMgrID() == null || record.getMgrID().trim().isEmpty())
                .collect(Collectors.toList());
        
        if (ceos.size() > 1) {
            warnings.add("Multiple CEOs found: " + ceos.stream()
                    .map(record -> record.getName() + " (ID: " + record.getID() + ")")
                    .collect(Collectors.joining(", ")));
        } else if (ceos.isEmpty()) {
            warnings.add("No CEO found (no employee without a manager)");
        }
    }
    
    private void checkForOrphanedEmployees(List<EmployeeRecord> employeeRecords, Set<String> warnings) {
        Set<String> allManagerIds = employeeRecords.stream()
                .map(EmployeeRecord::getMgrID)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());
        
        Set<String> allEmployeeIds = employeeRecords.stream()
                .map(EmployeeRecord::getID)
                .filter(id -> id != null && !id.trim().isEmpty())
                .collect(Collectors.toSet());
        
        Set<String> orphanedManagerIds = allManagerIds.stream()
                .filter(managerId -> !allEmployeeIds.contains(managerId))
                .collect(Collectors.toSet());
        
        if (!orphanedManagerIds.isEmpty()) {
            warnings.add("Orphaned manager IDs found: " + String.join(", ", orphanedManagerIds));
        }
    }
    
    private void checkForEmptyData(List<EmployeeRecord> employeeRecords, Set<String> errors) {
        for (EmployeeRecord record : employeeRecords) {
            if (record.getName() == null || record.getName().trim().isEmpty()) {
                errors.add("Employee name is empty for ID: " + record.getID());
            }
            if (record.getID() == null || record.getID().trim().isEmpty()) {
                errors.add("Employee ID is empty for name: " + record.getName());
            }
        }
    }
} 