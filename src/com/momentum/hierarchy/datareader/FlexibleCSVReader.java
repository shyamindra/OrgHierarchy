package com.momentum.hierarchy.datareader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.momentum.hierarchy.employee.EmployeeRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Flexible CSV reader that can read employee data from different file paths.
 */
public class FlexibleCSVReader {
    
    private static final Logger logger = LoggerFactory.getLogger(FlexibleCSVReader.class);
    private static final String DEFAULT_CSV_FILE_PATH = "EmployeeData.csv";
    
    private final String filePath;
    
    /**
     * Creates a CSV reader with the default file path.
     */
    public FlexibleCSVReader() {
        this(DEFAULT_CSV_FILE_PATH);
    }
    
    /**
     * Creates a CSV reader with a custom file path.
     * 
     * @param filePath Path to the CSV file
     */
    public FlexibleCSVReader(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Reads the CSV file and returns a list of employee records.
     * 
     * @return List of employee records
     * @throws IOException if the file cannot be read
     * @throws CsvValidationException if the CSV format is invalid
     */
    public List<EmployeeRecord> readCSVAndGetRecords() throws IOException, CsvValidationException {
        logger.info("Reading employee data from: {}", filePath);
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("CSV file not found: " + filePath);
        }
        
        try (Reader reader = Files.newBufferedReader(path)) {
            CsvToBean<EmployeeRecord> csvToBean = new CsvToBeanBuilder<EmployeeRecord>(reader)
                    .withSkipLines(1) // Skip header row
                    .withType(EmployeeRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build();

            Iterator<EmployeeRecord> recordIterator = csvToBean.iterator();
            List<EmployeeRecord> employeeRecords = new ArrayList<>();
            
            int lineNumber = 2; // Start from line 2 (after header)
            while (recordIterator.hasNext()) {
                try {
                    EmployeeRecord record = recordIterator.next();
                    employeeRecords.add(record);
                    lineNumber++;
                } catch (Exception e) {
                    logger.error("Error parsing CSV at line {}: {}", lineNumber, e.getMessage());
                    throw new CsvValidationException("Error parsing CSV at line " + lineNumber);
                }
            }
            
            logger.info("Successfully read {} employee records from {}", employeeRecords.size(), filePath);
            return employeeRecords;
        }
    }
    
    /**
     * Gets the current file path being used.
     * 
     * @return The file path
     */
    public String getFilePath() {
        return filePath;
    }
} 