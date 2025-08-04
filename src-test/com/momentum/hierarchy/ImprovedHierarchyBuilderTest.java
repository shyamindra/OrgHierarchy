package com.momentum.hierarchy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.momentum.hierarchy.employee.Employee;
import com.momentum.hierarchy.employee.EmployeeRecord;

class ImprovedHierarchyBuilderTest {
    
    private ImprovedHierarchyBuilder hierarchyBuilder;
    
    @BeforeEach
    void setUp() {
        hierarchyBuilder = new ImprovedHierarchyBuilder();
    }
    
    @Test
    void testValidHierarchyBuilding() {
        List<EmployeeRecord> records = createValidEmployeeRecords();
        
        boolean success = hierarchyBuilder.buildAndValidateHierarchy(records);
        
        assertTrue(success);
        assertTrue(hierarchyBuilder.getValidationResult().isValid());
        
        String hierarchy = hierarchyBuilder.printHierarchy();
        assertNotNull(hierarchy);
        assertTrue(hierarchy.contains("Jamie"));
        assertTrue(hierarchy.contains("Alan"));
        assertTrue(hierarchy.contains("Martin"));
        assertTrue(hierarchy.contains("Alex"));
        assertTrue(hierarchy.contains("Steve"));
        assertTrue(hierarchy.contains("David"));
    }
    
    @Test
    void testInvalidDataRejection() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "999")); // Invalid manager ID
        records.add(new EmployeeRecord("Jamie", "150", null));
        
        boolean success = hierarchyBuilder.buildAndValidateHierarchy(records);
        
        assertFalse(success);
        assertFalse(hierarchyBuilder.getValidationResult().isValid());
        assertEquals(1, hierarchyBuilder.getValidationResult().getErrors().size());
    }
    
    @Test
    void testCircularReferenceRejection() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "220", "100"));
        records.add(new EmployeeRecord("Jamie", "150", "220")); // Creates circular reference
        records.add(new EmployeeRecord("Alex", "275", "100"));
        
        boolean success = hierarchyBuilder.buildAndValidateHierarchy(records);
        
        assertFalse(success);
        assertFalse(hierarchyBuilder.getValidationResult().isValid());
        assertTrue(hierarchyBuilder.getValidationResult().getErrors().stream()
                .anyMatch(error -> error.contains("Circular reference detected")));
    }
    
    @Test
    void testMultipleCEOsWarning() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Jamie", "150", null)); // CEO 1
        records.add(new EmployeeRecord("Alex", "275", null));  // CEO 2
        
        boolean success = hierarchyBuilder.buildAndValidateHierarchy(records);
        
        assertTrue(success); // Multiple CEOs is a warning, not an error
        assertTrue(hierarchyBuilder.getValidationResult().isValid());
        assertEquals(1, hierarchyBuilder.getValidationResult().getWarnings().size());
        assertTrue(hierarchyBuilder.getValidationResult().getWarnings().stream()
                .anyMatch(warning -> warning.contains("Multiple CEOs found")));
    }
    
    @Test
    void testNoCEOWarning() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "220", "100"));
        records.add(new EmployeeRecord("Jamie", "150", "220"));
        
        boolean success = hierarchyBuilder.buildAndValidateHierarchy(records);
        
        assertTrue(success); // No CEO is a warning, not an error
        assertTrue(hierarchyBuilder.getValidationResult().isValid());
        assertEquals(1, hierarchyBuilder.getValidationResult().getWarnings().size());
        assertTrue(hierarchyBuilder.getValidationResult().getWarnings().contains("No CEO found (no employee without a manager)"));
    }
    
    @Test
    void testGetCEO() {
        List<EmployeeRecord> records = createValidEmployeeRecords();
        hierarchyBuilder.buildAndValidateHierarchy(records);
        
        Optional<Employee> ceo = hierarchyBuilder.getCEO();
        
        assertTrue(ceo.isPresent());
        assertEquals("Jamie", ceo.get().getName());
        assertEquals("150", ceo.get().getID());
    }
    
    @Test
    void testGetAllEmployees() {
        List<EmployeeRecord> records = createValidEmployeeRecords();
        hierarchyBuilder.buildAndValidateHierarchy(records);
        
        var allEmployees = hierarchyBuilder.getAllEmployees();
        
        assertEquals(6, allEmployees.size());
        assertTrue(allEmployees.containsKey("100")); // Alan
        assertTrue(allEmployees.containsKey("150")); // Jamie
        assertTrue(allEmployees.containsKey("220")); // Martin
        assertTrue(allEmployees.containsKey("275")); // Alex
        assertTrue(allEmployees.containsKey("400")); // Steve
        assertTrue(allEmployees.containsKey("190")); // David
    }
    
    @Test
    void testPrintHierarchyWithNoCEO() {
        List<EmployeeRecord> records = new ArrayList<>();
        records.add(new EmployeeRecord("Alan", "100", "150"));
        records.add(new EmployeeRecord("Martin", "220", "100"));
        records.add(new EmployeeRecord("Jamie", "150", "220"));
        
        hierarchyBuilder.buildAndValidateHierarchy(records);
        String hierarchy = hierarchyBuilder.printHierarchy();
        
        assertEquals("No hierarchy to display.", hierarchy);
    }
    
    @Test
    void testHierarchyStructure() {
        List<EmployeeRecord> records = createValidEmployeeRecords();
        hierarchyBuilder.buildAndValidateHierarchy(records);
        
        String hierarchy = hierarchyBuilder.printHierarchy();
        
        // Verify the hierarchy structure
        String[] lines = hierarchy.split("\n");
        
        // Jamie should be at the top level (no indentation)
        assertTrue(lines[0].contains("Jamie"));
        assertFalse(lines[0].contains("\t"));
        
        // Alan and Steve should be under Jamie (one tab)
        boolean foundAlan = false;
        boolean foundSteve = false;
        for (String line : lines) {
            if (line.contains("Alan") && line.startsWith("\t")) {
                foundAlan = true;
            }
            if (line.contains("Steve") && line.startsWith("\t")) {
                foundSteve = true;
            }
        }
        assertTrue(foundAlan);
        assertTrue(foundSteve);
        
        // Martin and Alex should be under Alan (two tabs)
        boolean foundMartin = false;
        boolean foundAlex = false;
        for (String line : lines) {
            if (line.contains("Martin") && line.startsWith("\t\t")) {
                foundMartin = true;
            }
            if (line.contains("Alex") && line.startsWith("\t\t")) {
                foundAlex = true;
            }
        }
        assertTrue(foundMartin);
        assertTrue(foundAlex);
        
        // David should be under Steve (two tabs)
        boolean foundDavid = false;
        for (String line : lines) {
            if (line.contains("David") && line.startsWith("\t\t")) {
                foundDavid = true;
            }
        }
        assertTrue(foundDavid);
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