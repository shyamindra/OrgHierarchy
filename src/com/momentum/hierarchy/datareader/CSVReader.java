package com.momentum.hierarchy.datareader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.momentum.hierarchy.employee.EmployeeRecord;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

public class CSVReader {

	private static final String SAMPLE_CSV_FILE_PATH = "EmployeeData.csv";

	
	/*
	 * Reads the CSV file with the hierarchy information of the employees
	 * and maps each row to the pojo EmployeeRecord 
	 * 
	 * returns List<EmployeeRecord>
	 */
	public List<EmployeeRecord> readCSVAndGetRecords() throws IOException {
		
        Reader reader = Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH));
        CsvToBean<EmployeeRecord> csvToBean = new CsvToBeanBuilder<EmployeeRecord>(reader)
        		.withSkipLines(1)
                .withType(EmployeeRecord.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .build();

        Iterator<EmployeeRecord> recordIterator = csvToBean.iterator();
        List<EmployeeRecord> employeeRecords = new ArrayList<EmployeeRecord>();
        while (recordIterator.hasNext()) {
            employeeRecords.add(recordIterator.next());
        }
        return employeeRecords;
    }
	
}
