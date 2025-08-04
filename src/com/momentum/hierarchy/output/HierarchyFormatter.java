package com.momentum.hierarchy.output;

import com.momentum.hierarchy.employee.EmployeeRecord;
import java.util.List;
import java.util.Map;

/**
 * Base interface for hierarchy formatters.
 * Implementations will format the hierarchy in different output formats (HTML, JSON, XML).
 */
public interface HierarchyFormatter {
    
    /**
     * Format the hierarchy as a string in the specific format.
     * 
     * @param hierarchyTree The hierarchy tree structure
     * @param allEmployees All employees in the system
     * @param ceo The CEO employee
     * @return Formatted string representation of the hierarchy
     */
    String format(Map<String, List<EmployeeRecord>> hierarchyTree, 
                  List<EmployeeRecord> allEmployees, 
                  EmployeeRecord ceo);
    
    /**
     * Get the file extension for this formatter.
     * 
     * @return File extension (e.g., "html", "json", "xml")
     */
    String getFileExtension();
    
    /**
     * Get the MIME type for this formatter.
     * 
     * @return MIME type (e.g., "text/html", "application/json", "application/xml")
     */
    String getMimeType();
    
    /**
     * Get a descriptive name for this formatter.
     * 
     * @return Formatter name (e.g., "HTML", "JSON", "XML")
     */
    String getFormatterName();
} 