package com.momentum.hierarchy;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.momentum.hierarchy.datareader.FlexibleCSVReader;
import com.momentum.hierarchy.employee.EmployeeRecord;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Test class to demonstrate the improved implementation with validation.
 */
public class TestImprovedImplementation {
    
    private static final Logger logger = LoggerFactory.getLogger(TestImprovedImplementation.class);
    
    public static void main(String[] args) {
        TestImprovedImplementation test = new TestImprovedImplementation();
        
        // Test with valid data
        logger.info("=== Testing with valid data ===");
        test.testValidData();
        
        // Test with circular reference
        logger.info("\n=== Testing with circular reference ===");
        test.testCircularReference();
        
        // Test with invalid manager
        logger.info("\n=== Testing with invalid manager ===");
        test.testInvalidManager();
        
        // Test with multiple CEOs
        logger.info("\n=== Testing with multiple CEOs ===");
        test.testMultipleCEOs();
    }
    
    private void testValidData() {
        try {
            FlexibleCSVReader reader = new FlexibleCSVReader("EmployeeData.csv");
            List<EmployeeRecord> records = reader.readCSVAndGetRecords();
            
            ImprovedHierarchyBuilder builder = new ImprovedHierarchyBuilder();
            boolean success = builder.buildAndValidateHierarchy(records);
            
            if (success) {
                logger.info("✅ Valid data processed successfully");
                logger.info("Hierarchy:");
                System.out.println(builder.printHierarchy());
            } else {
                logger.error("❌ Valid data validation failed");
                builder.getValidationResult().logIssues();
            }
        } catch (Exception e) {
            logger.error("Error testing valid data: {}", e.getMessage(), e);
        }
    }
    
    private void testCircularReference() {
        try {
            FlexibleCSVReader reader = new FlexibleCSVReader("test-data/circular-reference.csv");
            List<EmployeeRecord> records = reader.readCSVAndGetRecords();
            
            ImprovedHierarchyBuilder builder = new ImprovedHierarchyBuilder();
            boolean success = builder.buildAndValidateHierarchy(records);
            
            if (!success) {
                logger.info("✅ Circular reference correctly detected and rejected");
                builder.getValidationResult().logIssues();
            } else {
                logger.error("❌ Circular reference not detected");
            }
        } catch (Exception e) {
            logger.error("Error testing circular reference: {}", e.getMessage(), e);
        }
    }
    
    private void testInvalidManager() {
        try {
            FlexibleCSVReader reader = new FlexibleCSVReader("test-data/invalid-manager.csv");
            List<EmployeeRecord> records = reader.readCSVAndGetRecords();
            
            ImprovedHierarchyBuilder builder = new ImprovedHierarchyBuilder();
            boolean success = builder.buildAndValidateHierarchy(records);
            
            if (!success) {
                logger.info("✅ Invalid manager correctly detected and rejected");
                builder.getValidationResult().logIssues();
            } else {
                logger.error("❌ Invalid manager not detected");
            }
        } catch (Exception e) {
            logger.error("Error testing invalid manager: {}", e.getMessage(), e);
        }
    }
    
    private void testMultipleCEOs() {
        try {
            FlexibleCSVReader reader = new FlexibleCSVReader("test-data/multiple-ceos.csv");
            List<EmployeeRecord> records = reader.readCSVAndGetRecords();
            
            ImprovedHierarchyBuilder builder = new ImprovedHierarchyBuilder();
            boolean success = builder.buildAndValidateHierarchy(records);
            
            if (success) {
                logger.info("✅ Multiple CEOs handled with warnings");
                builder.getValidationResult().logIssues();
                logger.info("Hierarchy (using first CEO):");
                System.out.println(builder.printHierarchy());
            } else {
                logger.error("❌ Multiple CEOs caused validation failure");
                builder.getValidationResult().logIssues();
            }
        } catch (Exception e) {
            logger.error("Error testing multiple CEOs: {}", e.getMessage(), e);
        }
    }
} 