package com.momentum.hierarchy;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.momentum.hierarchy.datareader.FlexibleCSVReader;
import com.momentum.hierarchy.employee.EmployeeRecord;
import com.momentum.hierarchy.validation.EmployeeDataValidator.ValidationResult;
import com.momentum.hierarchy.output.FormatterFactory;
import com.momentum.hierarchy.output.HierarchyFormatter;
import com.momentum.hierarchy.output.FileExporter;
import com.momentum.hierarchy.employee.Employee;
import com.opencsv.exceptions.CsvValidationException;
import java.util.Map;

/**
 * Enhanced application with command line support and improved user experience.
 */
public class ImprovedApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(ImprovedApplication.class);
    private static final String DEFAULT_CSV_FILE = "EmployeeData.csv";
    private static final String USAGE_MESSAGE = """
            Organization Hierarchy Application
            
            Usage: java -jar OrgHierarchy.jar [options] [csv-file]
            
            Options:
              -h, --help              Show this help message
              -v, --verbose           Enable verbose logging
              -d, --debug             Enable debug logging
              --validate-only         Only validate data, don't build hierarchy
              -f, --format FORMAT     Output format (html, json, xml) [default: console]
              -o, --output FILE       Output file path
              --export-all            Export to all formats
            
            Arguments:
              csv-file                Path to CSV file (default: EmployeeData.csv)
            
            Examples:
              java -jar OrgHierarchy.jar
              java -jar OrgHierarchy.jar my-employees.csv
              java -jar OrgHierarchy.jar -v test-data/circular-reference.csv
              java -jar OrgHierarchy.jar --validate-only invalid-data.csv
              java -jar OrgHierarchy.jar -f html -o hierarchy.html
              java -jar OrgHierarchy.jar -f json -o hierarchy.json
              java -jar OrgHierarchy.jar --export-all -o output/
            """;
    
    private final ApplicationConfig config;
    
    public ImprovedApplication(ApplicationConfig config) {
        this.config = config;
    }
    
    public static void main(String[] args) {
        try {
            ApplicationConfig config = parseCommandLineArgs(args);
            
            if (config.isHelpRequested()) {
                System.out.println(USAGE_MESSAGE);
                System.exit(0);
            }
            
            ImprovedApplication app = new ImprovedApplication(config);
            boolean success = app.run();
            
            System.exit(success ? 0 : 1);
            
        } catch (Exception e) {
            logger.error("Application failed: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            System.err.println("Use --help for usage information");
            System.exit(1);
        }
    }
    
    /**
     * Main application logic.
     * 
     * @return true if successful, false otherwise
     */
    public boolean run() {
        logger.info("Starting Organization Hierarchy Application");
        logger.info("CSV file: {}", config.getCsvFilePath());
        logger.info("Verbose mode: {}", config.isVerbose());
        logger.info("Debug mode: {}", config.isDebug());
        logger.info("Validate only: {}", config.isValidateOnly());
        logger.info("Output format: {}", config.getOutputFormat());
        logger.info("Output file: {}", config.getOutputFile());
        logger.info("Export all: {}", config.isExportAll());
        
        try {
            // Read CSV data
            FlexibleCSVReader reader = new FlexibleCSVReader(config.getCsvFilePath());
            List<EmployeeRecord> records = reader.readCSVAndGetRecords();
            
            logger.info("Successfully read {} employee records", records.size());
            
            // Build and validate hierarchy
            ImprovedHierarchyBuilder builder = new ImprovedHierarchyBuilder();
            boolean success = builder.buildAndValidateHierarchy(records);
            
            if (!success) {
                logger.error("Failed to build hierarchy due to validation errors");
                printValidationIssues(builder.getValidationResult());
                return false;
            }
            
            // Print validation warnings if any
            ValidationResult validationResult = builder.getValidationResult();
            if (!validationResult.getWarnings().isEmpty()) {
                logger.warn("Validation warnings found:");
                validationResult.getWarnings().forEach(warning -> 
                    logger.warn("  - {}", warning));
            }
            
            if (config.isValidateOnly()) {
                logger.info("Validation completed successfully");
                return true;
            }
            
            // Handle output formatting
            if (config.isExportAll()) {
                exportToAllFormats(builder);
            } else if (!"console".equals(config.getOutputFormat())) {
                exportToFormat(builder, config.getOutputFormat(), config.getOutputFile());
            } else {
                // Print hierarchy to console
                printHierarchy(builder);
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Application error: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private void printValidationIssues(ValidationResult result) {
        System.err.println("\n❌ Validation failed!");
        
        if (!result.getErrors().isEmpty()) {
            System.err.println("\nErrors:");
            result.getErrors().forEach(error -> 
                System.err.println("  ❌ " + error));
        }
        
        if (!result.getWarnings().isEmpty()) {
            System.err.println("\nWarnings:");
            result.getWarnings().forEach(warning -> 
                System.err.println("  ⚠️  " + warning));
        }
        
        System.err.println("\nPlease fix the data issues and try again.");
    }
    
    private void printHierarchy(ImprovedHierarchyBuilder builder) {
        System.out.println("\n🏢 Organization Hierarchy");
        System.out.println("=" .repeat(50));
        
        String hierarchy = builder.printHierarchy();
        System.out.println(hierarchy);
        
        // Print summary
        var allEmployees = builder.getAllEmployees();
        var ceo = builder.getCEO();
        
        System.out.println("\n📊 Summary:");
        System.out.println("  Total employees: " + allEmployees.size());
        ceo.ifPresent(c -> System.out.println("  CEO: " + c.getName() + " (ID: " + c.getID() + ")"));
        
        System.out.println("\n✅ Hierarchy built successfully!");
    }
    
    private void exportToFormat(ImprovedHierarchyBuilder builder, String format, String outputFile) {
        try {
            HierarchyFormatter formatter = FormatterFactory.getFormatter(format);
            if (formatter == null) {
                System.err.println("❌ Unsupported format: " + format);
                System.err.println("Supported formats: " + FormatterFactory.getAvailableFormats());
                return;
            }
            
            String content = formatter.format(builder.getHierarchyTree(), 
                builder.getAllEmployees().values().stream()
                    .map(Employee::convertToEmployeeRecord)
                    .collect(java.util.stream.Collectors.toList()), 
                builder.getCEO().orElse(null).convertToEmployeeRecord());
            
            if (outputFile != null) {
                // Export to file
                java.nio.file.Path filePath = FileExporter.exportToFile(
                    builder.getHierarchyTree(), 
                    builder.getAllEmployees().values().stream()
                        .map(Employee::convertToEmployeeRecord)
                        .collect(java.util.stream.Collectors.toList()), 
                    builder.getCEO().orElse(null).convertToEmployeeRecord(), 
                    format, 
                    outputFile
                );
                System.out.println("✅ Exported to: " + filePath.toAbsolutePath());
            } else {
                // Print to console
                System.out.println("\n📄 " + formatter.getFormatterName() + " Output:");
                System.out.println("=" .repeat(50));
                System.out.println(content);
            }
            
        } catch (Exception e) {
            logger.error("Error exporting to format {}: {}", format, e.getMessage(), e);
            System.err.println("❌ Error exporting to " + format + ": " + e.getMessage());
        }
    }
    
    private void exportToAllFormats(ImprovedHierarchyBuilder builder) {
        try {
            String outputDir = config.getOutputFile();
            Map<String, java.nio.file.Path> exportedFiles = FileExporter.exportToAllFormats(
                builder.getHierarchyTree(), 
                builder.getAllEmployees().values().stream()
                    .map(Employee::convertToEmployeeRecord)
                    .collect(java.util.stream.Collectors.toList()), 
                builder.getCEO().orElse(null).convertToEmployeeRecord(), 
                outputDir
            );
            
            System.out.println("\n✅ Exported to all formats:");
            for (Map.Entry<String, java.nio.file.Path> entry : exportedFiles.entrySet()) {
                System.out.println("  📄 " + entry.getKey().toUpperCase() + ": " + entry.getValue().toAbsolutePath());
            }
            
        } catch (Exception e) {
            logger.error("Error exporting to all formats: {}", e.getMessage(), e);
            System.err.println("❌ Error exporting to all formats: " + e.getMessage());
        }
    }
    
    private static ApplicationConfig parseCommandLineArgs(String[] args) {
        ApplicationConfig config = new ApplicationConfig();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "-h", "--help":
                    config.setHelpRequested(true);
                    break;
                    
                case "-v", "--verbose":
                    config.setVerbose(true);
                    break;
                    
                case "-d", "--debug":
                    config.setDebug(true);
                    break;
                    
                case "--validate-only":
                    config.setValidateOnly(true);
                    break;
                    
                case "-f", "--format":
                    if (i + 1 < args.length) {
                        config.setOutputFormat(args[++i]);
                    } else {
                        throw new IllegalArgumentException("Format option requires a value");
                    }
                    break;
                    
                case "-o", "--output":
                    if (i + 1 < args.length) {
                        config.setOutputFile(args[++i]);
                    } else {
                        throw new IllegalArgumentException("Output option requires a value");
                    }
                    break;
                    
                case "--export-all":
                    config.setExportAll(true);
                    break;
                    
                default:
                    // If it's not a flag, treat it as the CSV file path
                    if (!arg.startsWith("-")) {
                        config.setCsvFilePath(arg);
                    } else {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
                    break;
            }
        }
        
        return config;
    }
    
    /**
     * Configuration class for application settings.
     */
    public static class ApplicationConfig {
        private String csvFilePath = DEFAULT_CSV_FILE;
        private boolean verbose = false;
        private boolean debug = false;
        private boolean validateOnly = false;
        private boolean helpRequested = false;
        private String outputFormat = "console";
        private String outputFile = null;
        private boolean exportAll = false;
        
        public String getCsvFilePath() { return csvFilePath; }
        public void setCsvFilePath(String csvFilePath) { this.csvFilePath = csvFilePath; }
        
        public boolean isVerbose() { return verbose; }
        public void setVerbose(boolean verbose) { this.verbose = verbose; }
        
        public boolean isDebug() { return debug; }
        public void setDebug(boolean debug) { this.debug = debug; }
        
        public boolean isValidateOnly() { return validateOnly; }
        public void setValidateOnly(boolean validateOnly) { this.validateOnly = validateOnly; }
        
        public boolean isHelpRequested() { return helpRequested; }
        public void setHelpRequested(boolean helpRequested) { this.helpRequested = helpRequested; }
        
        public String getOutputFormat() { return outputFormat; }
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
        
        public String getOutputFile() { return outputFile; }
        public void setOutputFile(String outputFile) { this.outputFile = outputFile; }
        
        public boolean isExportAll() { return exportAll; }
        public void setExportAll(boolean exportAll) { this.exportAll = exportAll; }
    }
} 