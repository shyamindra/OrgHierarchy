package com.momentum.hierarchy.output;

import com.momentum.hierarchy.employee.EmployeeRecord;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON formatter for organization hierarchy.
 * Generates a structured JSON representation of the hierarchy.
 */
public class JSONFormatter implements HierarchyFormatter {
    
    @Override
    public String format(Map<String, List<EmployeeRecord>> hierarchyTree, 
                        List<EmployeeRecord> allEmployees, 
                        EmployeeRecord ceo) {
        
        StringBuilder json = new StringBuilder();
        
        // Create employee map for quick lookup
        Map<String, EmployeeRecord> employeeMap = allEmployees.stream()
                .collect(Collectors.toMap(EmployeeRecord::getID, employee -> employee));
        
        json.append("{\n");
        json.append("  \"metadata\": {\n");
        json.append("    \"generatedAt\": \"").append(java.time.LocalDateTime.now().toString()).append("\",\n");
        json.append("    \"totalEmployees\": ").append(allEmployees.size()).append(",\n");
        json.append("    \"hierarchyLevels\": ").append(calculateMaxLevel(hierarchyTree, ceo.getID())).append(",\n");
        json.append("    \"format\": \"JSON\",\n");
        json.append("    \"version\": \"1.0\"\n");
        json.append("  },\n");
        
        json.append("  \"ceo\": {\n");
        json.append("    \"id\": \"").append(ceo.getID()).append("\",\n");
        json.append("    \"name\": \"").append(escapeJson(ceo.getName())).append("\",\n");
        json.append("    \"managerId\": ").append(ceo.getMgrID() == null || ceo.getMgrID().isEmpty() ? "null" : "\"" + ceo.getMgrID() + "\"").append(",\n");
        json.append("    \"role\": \"CEO\"\n");
        json.append("  },\n");
        
        json.append("  \"hierarchy\": ");
        generateHierarchyJSON(hierarchyTree, ceo.getID(), json, employeeMap, 1);
        json.append(",\n");
        
        json.append("  \"allEmployees\": [\n");
        boolean first = true;
        for (EmployeeRecord employee : allEmployees) {
            if (!first) json.append(",\n");
            json.append("    {\n");
            json.append("      \"id\": \"").append(employee.getID()).append("\",\n");
            json.append("      \"name\": \"").append(escapeJson(employee.getName())).append("\",\n");
            json.append("      \"managerId\": ").append(employee.getMgrID() == null || employee.getMgrID().isEmpty() ? "null" : "\"" + employee.getMgrID() + "\"").append(",\n");
            json.append("      \"isManager\": ").append(hierarchyTree.containsKey(employee.getID()) && !hierarchyTree.get(employee.getID()).isEmpty()).append(",\n");
            json.append("      \"level\": ").append(calculateEmployeeLevel(hierarchyTree, ceo.getID(), employee.getID())).append(",\n");
            json.append("      \"directReports\": ").append(hierarchyTree.containsKey(employee.getID()) ? hierarchyTree.get(employee.getID()).size() : 0).append("\n");
            json.append("    }");
            first = false;
        }
        json.append("\n  ]\n");
        
        json.append("}");
        
        return json.toString();
    }
    
    private void generateHierarchyJSON(Map<String, List<EmployeeRecord>> hierarchyTree, 
                                     String employeeId, StringBuilder json,
                                     Map<String, EmployeeRecord> employeeMap, int indent) {
        
        EmployeeRecord employee = employeeMap.get(employeeId);
        if (employee == null) return;
        
        String indentStr = "  ".repeat(indent);
        
        json.append("{\n");
        json.append(indentStr).append("  \"id\": \"").append(employee.getID()).append("\",\n");
        json.append(indentStr).append("  \"name\": \"").append(escapeJson(employee.getName())).append("\",\n");
        json.append(indentStr).append("  \"managerId\": ").append(employee.getMgrID() == null || employee.getMgrID().isEmpty() ? "null" : "\"" + employee.getMgrID() + "\"").append(",\n");
        json.append(indentStr).append("  \"role\": \"").append(employee.getMgrID() == null || employee.getMgrID().isEmpty() ? "CEO" : "Employee").append("\",\n");
        json.append(indentStr).append("  \"level\": ").append(calculateEmployeeLevel(hierarchyTree, employeeMap.values().stream()
                .filter(e -> e.getMgrID() == null || e.getMgrID().isEmpty())
                .findFirst().orElse(employee).getID(), employeeId)).append(",\n");
        
        if (hierarchyTree.containsKey(employeeId) && !hierarchyTree.get(employeeId).isEmpty()) {
            json.append(indentStr).append("  \"directReports\": ").append(hierarchyTree.get(employeeId).size()).append(",\n");
            json.append(indentStr).append("  \"subordinates\": [\n");
            
            boolean first = true;
            for (EmployeeRecord child : hierarchyTree.get(employeeId)) {
                if (!first) json.append(",\n");
                generateHierarchyJSON(hierarchyTree, child.getID(), json, employeeMap, indent + 1);
                first = false;
            }
            
            json.append("\n").append(indentStr).append("  ]\n");
        } else {
            json.append(indentStr).append("  \"directReports\": 0,\n");
            json.append(indentStr).append("  \"subordinates\": []\n");
        }
        
        json.append(indentStr).append("}");
    }
    
    private int calculateMaxLevel(Map<String, List<EmployeeRecord>> hierarchyTree, String ceoId) {
        return calculateMaxLevelRecursive(hierarchyTree, ceoId, 0);
    }
    
    private int calculateMaxLevelRecursive(Map<String, List<EmployeeRecord>> hierarchyTree, 
                                         String employeeId, int currentLevel) {
        if (!hierarchyTree.containsKey(employeeId)) {
            return currentLevel;
        }
        
        int maxLevel = currentLevel;
        for (EmployeeRecord child : hierarchyTree.get(employeeId)) {
            int childLevel = calculateMaxLevelRecursive(hierarchyTree, child.getID(), currentLevel + 1);
            maxLevel = Math.max(maxLevel, childLevel);
        }
        
        return maxLevel;
    }
    
    private int calculateEmployeeLevel(Map<String, List<EmployeeRecord>> hierarchyTree, 
                                     String ceoId, String employeeId) {
        if (ceoId.equals(employeeId)) {
            return 0;
        }
        
        return calculateEmployeeLevelRecursive(hierarchyTree, ceoId, employeeId, 0);
    }
    
    private int calculateEmployeeLevelRecursive(Map<String, List<EmployeeRecord>> hierarchyTree, 
                                              String currentId, String targetId, int currentLevel) {
        if (!hierarchyTree.containsKey(currentId)) {
            return -1; // Not found
        }
        
        for (EmployeeRecord child : hierarchyTree.get(currentId)) {
            if (child.getID().equals(targetId)) {
                return currentLevel + 1;
            }
            
            int result = calculateEmployeeLevelRecursive(hierarchyTree, child.getID(), targetId, currentLevel + 1);
            if (result != -1) {
                return result;
            }
        }
        
        return -1; // Not found
    }
    
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    @Override
    public String getFileExtension() {
        return "json";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
    
    @Override
    public String getFormatterName() {
        return "JSON";
    }
} 