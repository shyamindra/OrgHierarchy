package com.momentum.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.momentum.hierarchy.employee.Employee;
import com.momentum.hierarchy.employee.EmployeeRecord;
import com.momentum.hierarchy.validation.EmployeeDataValidator;
import com.momentum.hierarchy.validation.EmployeeDataValidator.ValidationResult;

/**
 * Improved hierarchy builder with validation and better error handling.
 */
public class ImprovedHierarchyBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(ImprovedHierarchyBuilder.class);
    
    private final EmployeeDataValidator validator;
    private Employee ceo;
    private Map<String, Employee> employeeMap;
    private Map<String, List<Employee>> mgrEmployeeMap;
    private ValidationResult validationResult;
    
    public ImprovedHierarchyBuilder() {
        this.validator = new EmployeeDataValidator();
        this.employeeMap = new HashMap<>();
        this.mgrEmployeeMap = new HashMap<>();
    }
    
    /**
     * Builds and validates the hierarchy from employee records.
     * 
     * @param employeeRecords List of employee records
     * @return true if hierarchy was built successfully, false otherwise
     */
    public boolean buildAndValidateHierarchy(List<EmployeeRecord> employeeRecords) {
        logger.info("Building hierarchy from {} employee records", employeeRecords.size());
        
        // Validate the data first
        validationResult = validator.validate(employeeRecords);
        validationResult.logIssues();
        
        if (!validationResult.isValid()) {
            logger.error("Validation failed. Cannot build hierarchy.");
            return false;
        }
        
        try {
            buildEmployeeMap(employeeRecords);
            buildMgrEmployeeMap(employeeRecords);
            buildHierarchyTree();
            logger.info("Hierarchy built successfully");
            return true;
        } catch (Exception e) {
            logger.error("Error building hierarchy: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Prints the hierarchy to console.
     * 
     * @return The hierarchy as a formatted string
     */
    public String printHierarchy() {
        if (ceo == null) {
            logger.warn("No CEO found. Cannot print hierarchy.");
            return "No hierarchy to display.";
        }
        
        return printHierarchyTree(ceo, 0);
    }
    
    /**
     * Gets the validation result from the last validation.
     * 
     * @return ValidationResult containing validation status and issues
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }
    
    /**
     * Gets the CEO of the organization.
     * 
     * @return Optional containing the CEO if found
     */
    public Optional<Employee> getCEO() {
        return Optional.ofNullable(ceo);
    }
    
    /**
     * Gets all employees in the hierarchy.
     * 
     * @return Map of employee ID to Employee object
     */
    public Map<String, Employee> getAllEmployees() {
        return new HashMap<>(employeeMap);
    }
    
    private void buildEmployeeMap(List<EmployeeRecord> employeeRecords) {
        logger.debug("Building employee map");
        for (EmployeeRecord employeeRecord : employeeRecords) {
            Employee employee = employeeRecord.convertToEmployee();
            employeeMap.put(employeeRecord.getID(), employee);
            
            if (employeeRecord.getMgrID() == null || employeeRecord.getMgrID().trim().isEmpty()) {
                if (ceo != null) {
                    logger.warn("Multiple CEOs found: {} and {}", ceo.getName(), employee.getName());
                }
                this.ceo = employee;
                logger.debug("CEO identified: {}", employee.getName());
            }
        }
    }
    
    private void buildMgrEmployeeMap(List<EmployeeRecord> employeeRecords) {
        logger.debug("Building manager-employee map");
        for (EmployeeRecord employeeRecord : employeeRecords) {
            String mgrId = employeeRecord.getMgrID();
            if (mgrId != null && !mgrId.trim().isEmpty()) {
                List<Employee> mgrList = mgrEmployeeMap.computeIfAbsent(mgrId, k -> new ArrayList<>());
                mgrList.add(employeeRecord.convertToEmployee());
            }
        }
    }
    
    private void buildHierarchyTree() {
        if (ceo == null) {
            logger.warn("No CEO found. Cannot build hierarchy tree.");
            return;
        }
        
        logger.debug("Building hierarchy tree starting from CEO: {}", ceo.getName());
        buildHierarchyTreeRecursive(ceo);
    }
    
    private void buildHierarchyTreeRecursive(Employee employee) {
        List<Employee> reportees = mgrEmployeeMap.get(employee.getID());
        employee.setReportees(reportees != null ? reportees : new ArrayList<>());
        
        if (reportees != null) {
            for (Employee reportee : reportees) {
                buildHierarchyTreeRecursive(reportee);
            }
        }
    }
    
    private String printHierarchyTree(Employee root, int level) {
        StringBuilder builder = new StringBuilder();
        
        // Add indentation
        for (int i = 0; i < level; i++) {
            builder.append("\t");
        }
        
        // Add employee name
        builder.append("- ").append(root.getName());
        
        // Add employee ID for debugging
        if (logger.isDebugEnabled()) {
            builder.append(" (ID: ").append(root.getID()).append(")");
        }
        
        builder.append("\n");
        
        // Add reportees
        List<Employee> reportees = root.getReportees();
        if (reportees != null) {
            for (Employee employee : reportees) {
                builder.append(printHierarchyTree(employee, level + 1));
            }
        }
        
        return builder.toString();
    }
} 