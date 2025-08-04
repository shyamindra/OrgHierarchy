package com.momentum.hierarchy.datareader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.momentum.hierarchy.employee.EmployeeRecord;
import com.opencsv.exceptions.CsvValidationException;

class FlexibleCSVReaderTest {
    
    private FlexibleCSVReader csvReader;
    
    @BeforeEach
    void setUp() {
        csvReader = new FlexibleCSVReader();
    }
    
    @Test
    void testDefaultConstructor() {
        FlexibleCSVReader reader = new FlexibleCSVReader();
        assertEquals("EmployeeData.csv", reader.getFilePath());
    }
    
    @Test
    void testCustomFilePathConstructor() {
        String customPath = "custom/path/employees.csv";
        FlexibleCSVReader reader = new FlexibleCSVReader(customPath);
        assertEquals(customPath, reader.getFilePath());
    }
    
    @Test
    void testReadValidCSVFile(@TempDir Path tempDir) throws IOException, CsvValidationException {
        // Create a temporary CSV file
        Path csvFile = tempDir.resolve("test-employees.csv");
        String csvContent = "Name,id,Manager id\n" +
                           "Alan,100,150\n" +
                           "Martin,220,100\n" +
                           "Jamie,150,\n" +
                           "Alex,275,100\n" +
                           "Steve,400,150\n" +
                           "David,190,400";
        Files.write(csvFile, csvContent.getBytes());
        
        FlexibleCSVReader reader = new FlexibleCSVReader(csvFile.toString());
        List<EmployeeRecord> records = reader.readCSVAndGetRecords();
        
        assertEquals(6, records.size());
        
        // Verify first record
        EmployeeRecord firstRecord = records.get(0);
        assertEquals("Alan", firstRecord.getName());
        assertEquals("100", firstRecord.getID());
        assertEquals("150", firstRecord.getMgrID());
        
        // Verify CEO record (no manager)
        EmployeeRecord ceoRecord = records.stream()
                .filter(r -> "Jamie".equals(r.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(ceoRecord);
        assertEquals("150", ceoRecord.getID());
        assertNull(ceoRecord.getMgrID());
    }
    
    @Test
    void testReadNonExistentFile() {
        FlexibleCSVReader reader = new FlexibleCSVReader("non-existent-file.csv");
        
        assertThrows(IOException.class, () -> {
            reader.readCSVAndGetRecords();
        });
    }
    
    @Test
    void testReadEmptyCSVFile(@TempDir Path tempDir) throws IOException, CsvValidationException {
        // Create an empty CSV file (only header)
        Path csvFile = tempDir.resolve("empty-employees.csv");
        String csvContent = "Name,id,Manager id\n";
        Files.write(csvFile, csvContent.getBytes());
        
        FlexibleCSVReader reader = new FlexibleCSVReader(csvFile.toString());
        List<EmployeeRecord> records = reader.readCSVAndGetRecords();
        
        assertEquals(0, records.size());
    }
    
    @Test
    void testReadCSVWithEmptyFields(@TempDir Path tempDir) throws IOException, CsvValidationException {
        // Create a CSV file with some empty fields
        Path csvFile = tempDir.resolve("empty-fields-employees.csv");
        String csvContent = "Name,id,Manager id\n" +
                           "Alan,100,\n" +  // Empty manager ID
                           "Martin,,100\n" + // Empty employee ID
                           "Jamie,150,\n";   // Empty manager ID
        Files.write(csvFile, csvContent.getBytes());
        
        FlexibleCSVReader reader = new FlexibleCSVReader(csvFile.toString());
        List<EmployeeRecord> records = reader.readCSVAndGetRecords();
        
        assertEquals(3, records.size());
        
        // Check that empty fields are handled properly
        EmployeeRecord alan = records.get(0);
        assertEquals("Alan", alan.getName());
        assertEquals("100", alan.getID());
        assertNull(alan.getMgrID());
        
        EmployeeRecord martin = records.get(1);
        assertEquals("Martin", martin.getName());
        assertEquals("", martin.getID()); // Empty ID
        assertEquals("100", martin.getMgrID());
    }
    
    @Test
    void testReadCSVWithWhitespace(@TempDir Path tempDir) throws IOException, CsvValidationException {
        // Create a CSV file with whitespace
        Path csvFile = tempDir.resolve("whitespace-employees.csv");
        String csvContent = "Name,id,Manager id\n" +
                           "  Alan  ,  100  ,  150  \n" + // Whitespace around values
                           "Martin,220,100\n" +
                           "Jamie,150,\n";
        Files.write(csvFile, csvContent.getBytes());
        
        FlexibleCSVReader reader = new FlexibleCSVReader(csvFile.toString());
        List<EmployeeRecord> records = reader.readCSVAndGetRecords();
        
        assertEquals(3, records.size());
        
        // Check that whitespace is trimmed
        EmployeeRecord alan = records.get(0);
        assertEquals("Alan", alan.getName());
        assertEquals("100", alan.getID());
        assertEquals("150", alan.getMgrID());
    }
    
    @Test
    void testReadCSVWithSpecialCharacters(@TempDir Path tempDir) throws IOException, CsvValidationException {
        // Create a CSV file with special characters
        Path csvFile = tempDir.resolve("special-chars-employees.csv");
        String csvContent = "Name,id,Manager id\n" +
                           "José,100,150\n" + // Accented character
                           "O'Connor,220,100\n" + // Apostrophe
                           "Smith-Jones,150,\n"; // Hyphen
        Files.write(csvFile, csvContent.getBytes());
        
        FlexibleCSVReader reader = new FlexibleCSVReader(csvFile.toString());
        List<EmployeeRecord> records = reader.readCSVAndGetRecords();
        
        assertEquals(3, records.size());
        
        // Check that special characters are preserved
        EmployeeRecord jose = records.get(0);
        assertEquals("José", jose.getName());
        
        EmployeeRecord oconnor = records.get(1);
        assertEquals("O'Connor", oconnor.getName());
        
        EmployeeRecord smithJones = records.get(2);
        assertEquals("Smith-Jones", smithJones.getName());
    }
} 