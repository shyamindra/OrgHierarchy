package com.momentum.hierarchy.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.momentum.hierarchy.employee.EmployeeRecord;

class EmployeeDataValidatorTest {
    
    private EmployeeDataValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new EmployeeDataValidator();
    }
    
    @Test
    void testValidData() {
        List<EmployeeRecord> records = createValidEmployeeRecords();
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }
    
    @Test
    void testNullData() {
        EmployeeDataValidator.ValidationResult result = validator.validate(null);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Employee data is null or empty"));
    }
    
    @Test
    void testEmptyData() {
        EmployeeDataValidator.ValidationResult result = validator.validate(new ArrayList<>());
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Employee data is null or empty"));
    }
    
    @Test
    void testDuplicateIds() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "100", "100")); // Duplicate ID
        records.add(new EmployeeRecord("Jamie", "150", null));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Duplicate employee ID found: 100"));
    }
    
    @Test
    void testInvalidManagerId() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "999")); // Invalid manager ID
        records.add(new EmployeeRecord("Jamie", "150", null));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Invalid manager ID '999' for employee 'Alan' (ID: 100)"));
    }
    
    @Test
    void testCircularReference() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "220", "100"));
        records.add(new EmployeeRecord("Jamie", "150", "220")); // Creates circular reference
        records.add(new EmployeeRecord("Alex", "275", "100"));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.contains("Circular reference detected")));
    }
    
    @Test
    void testMultipleCEOs() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Jamie", "150", null)); // CEO 1
        records.add(new EmployeeRecord("Alex", "275", null));  // CEO 2
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertTrue(result.isValid()); // Multiple CEOs is a warning, not an error
        assertEquals(1, result.getWarnings().size());
        assertTrue(result.getWarnings().stream().anyMatch(warning -> warning.contains("Multiple CEOs found")));
    }
    
    @Test
    void testNoCEO() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "220", "100"));
        records.add(new EmployeeRecord("Jamie", "150", "220"));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertTrue(result.isValid()); // No CEO is a warning, not an error
        assertEquals(1, result.getWarnings().size());
        assertTrue(result.getWarnings().contains("No CEO found (no employee without a manager)"));
    }
    
    @Test
    void testOrphanedManagerIds() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "999")); // Manager ID 999 doesn't exist
        records.add(new EmployeeRecord("Jamie", "150", null));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Invalid manager ID '999' for employee 'Alan' (ID: 100)"));
        assertEquals(1, result.getWarnings().size());
        assertTrue(result.getWarnings().contains("Orphaned manager IDs found: 999"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testEmptyEmployeeName(String emptyName) {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord(emptyName, "100", "150"));
        records.add(new EmployeeRecord("Jamie", "150", null));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Employee name is empty for ID: 100"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void testEmptyEmployeeId(String emptyId) {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", emptyId, "150"));
        records.add(new EmployeeRecord("Jamie", "150", null));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("Employee ID is empty for name: Alan"));
    }
    
    @Test
    void testComplexValidationScenario() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "220", "100"));
        records.add(new EmployeeRecord("Jamie", "150", null)); // CEO
        records.add(new EmployeeRecord("Alex", "275", "100"));
        records.add(new EmployeeRecord("Steve", "400", "150"));
        records.add(new EmployeeRecord("David", "190", "400"));
        
        EmployeeDataValidator.ValidationResult result = validator.validate(records);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }
    
    private List<EmployeeRecord> createValidEmployeeRecords() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "220", "100"));
        records.add(new EmployeeRecord("Jamie", "150", null));
        records.add(new EmployeeRecord("Alex", "275", "100"));
        records.add(new EmployeeRecord("Steve", "400", "150"));
        records.add(new EmployeeRecord("David", "190", "400"));
        return records;
    }
} 