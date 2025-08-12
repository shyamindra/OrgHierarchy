package com.momentum.hierarchy.output;

import com.momentum.hierarchy.employee.EmployeeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility class for exporting hierarchy data to files in various formats.
 */
public class FileExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(FileExporter.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Export hierarchy to a file with the specified format.
     * 
     * @param hierarchyTree The hierarchy tree structure
     * @param allEmployees All employees in the system
     * @param ceo The CEO employee
     * @param format The output format (html, json, xml)
     * @param outputPath The output file path (optional, will generate if null)
     * @return The path of the exported file
     * @throws IOException If an I/O error occurs
     */
    public static Path exportToFile(Map<String, List<EmployeeRecord>> hierarchyTree,
                                  List<EmployeeRecord> allEmployees,
                                  EmployeeRecord ceo,
                                  String format,
                                  String outputPath) throws IOException {
        
        // Get the formatter
        HierarchyFormatter formatter = FormatterFactory.getFormatter(format);
        if (formatter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format + 
                    ". Supported formats: " + FormatterFactory.getAvailableFormats());
        }
        
        // Generate the formatted content
        String content = formatter.format(hierarchyTree, allEmployees, ceo);
        
        // Determine output path
        Path filePath;
        if (outputPath != null && !outputPath.trim().isEmpty()) {
            filePath = Paths.get(outputPath);
        } else {
            filePath = generateDefaultPath(format);
        }
        
        // Ensure parent directory exists
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        
        // Write the file
        Files.write(filePath, content.getBytes("UTF-8"));
        
        logger.info("Exported hierarchy to {} ({})", filePath, formatter.getFormatterName());
        
        return filePath;
    }
    
    /**
     * Export hierarchy to a file with auto-detected format based on file extension.
     * 
     * @param hierarchyTree The hierarchy tree structure
     * @param allEmployees All employees in the system
     * @param ceo The CEO employee
     * @param outputPath The output file path
     * @return The path of the exported file
     * @throws IOException If an I/O error occurs
     */
    public static Path exportToFile(Map<String, List<EmployeeRecord>> hierarchyTree,
                                  List<EmployeeRecord> allEmployees,
                                  EmployeeRecord ceo,
                                  String outputPath) throws IOException {
        
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }
        
        Path filePath = Paths.get(outputPath);
        String fileName = filePath.getFileName().toString();
        
        // Extract file extension
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("No file extension found in: " + fileName);
        }
        
        String extension = fileName.substring(lastDotIndex + 1);
        
        // Get formatter by extension
        HierarchyFormatter formatter = FormatterFactory.getFormatterByExtension(extension);
        if (formatter == null) {
            throw new IllegalArgumentException("Unsupported file extension: " + extension + 
                    ". Supported formats: " + FormatterFactory.getAvailableFormats());
        }
        
        return exportToFile(hierarchyTree, allEmployees, ceo, formatter.getFileExtension(), outputPath);
    }
    
    /**
     * Generate a default file path with timestamp.
     * 
     * @param format The output format
     * @return The generated file path
     */
    private static Path generateDefaultPath(String format) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String fileName = String.format("hierarchy_%s.%s", timestamp, format);
        return Paths.get(fileName);
    }
    
    /**
     * Export hierarchy to all available formats.
     * 
     * @param hierarchyTree The hierarchy tree structure
     * @param allEmployees All employees in the system
     * @param ceo The CEO employee
     * @param outputDir The output directory (optional, will use current directory if null)
     * @return Map of format names to exported file paths
     * @throws IOException If an I/O error occurs
     */
    public static Map<String, Path> exportToAllFormats(Map<String, List<EmployeeRecord>> hierarchyTree,
                                                     List<EmployeeRecord> allEmployees,
                                                     EmployeeRecord ceo,
                                                     String outputDir) throws IOException {
        
        Map<String, Path> exportedFiles = new java.util.HashMap<>();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        
        for (String format : FormatterFactory.getAvailableFormats()) {
            String fileName = String.format("hierarchy_%s.%s", timestamp, format);
            Path outputPath = outputDir != null ? Paths.get(outputDir, fileName) : Paths.get(fileName);
            
            Path exportedPath = exportToFile(hierarchyTree, allEmployees, ceo, format, outputPath.toString());
            exportedFiles.put(format, exportedPath);
        }
        
        logger.info("Exported hierarchy to {} formats: {}", exportedFiles.size(), exportedFiles.keySet());
        
        return exportedFiles;
    }
    
    /**
     * Get information about the exported file.
     * 
     * @param filePath The path to the exported file
     * @return Information about the file
     * @throws IOException If an I/O error occurs
     */
    public static String getFileInfo(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            return "File does not exist: " + filePath;
        }
        
        long size = Files.size(filePath);
        String extension = getFileExtension(filePath);
        HierarchyFormatter formatter = FormatterFactory.getFormatterByExtension(extension);
        
        StringBuilder info = new StringBuilder();
        info.append("File: ").append(filePath.getFileName()).append("\n");
        info.append("Size: ").append(formatFileSize(size)).append("\n");
        info.append("Format: ").append(formatter != null ? formatter.getFormatterName() : "Unknown").append("\n");
        info.append("MIME Type: ").append(formatter != null ? formatter.getMimeType() : "Unknown").append("\n");
        
        return info.toString();
    }
    
    private static String getFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
} 