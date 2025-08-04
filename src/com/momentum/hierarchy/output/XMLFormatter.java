package com.momentum.hierarchy.output;

import com.momentum.hierarchy.employee.EmployeeRecord;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * XML formatter for organization hierarchy.
 * Generates a structured XML representation of the hierarchy.
 */
public class XMLFormatter implements HierarchyFormatter {
    
    @Override
    public String format(Map<String, List<EmployeeRecord>> hierarchyTree, 
                        List<EmployeeRecord> allEmployees, 
                        EmployeeRecord ceo) {
        
        StringBuilder xml = new StringBuilder();
        
        // Create employee map for quick lookup
        Map<String, EmployeeRecord> employeeMap = allEmployees.stream()
                .collect(Collectors.toMap(EmployeeRecord::getID, employee -> employee));
        
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<organization-hierarchy>\n");
        
        // Metadata
        xml.append("  <metadata>\n");
        xml.append("    <generatedAt>").append(java.time.LocalDateTime.now().toString()).append("</generatedAt>\n");
        xml.append("    <totalEmployees>").append(allEmployees.size()).append("</totalEmployees>\n");
        xml.append("    <hierarchyLevels>").append(calculateMaxLevel(hierarchyTree, ceo.getID())).append("</hierarchyLevels>\n");
        xml.append("    <format>XML</format>\n");
        xml.append("    <version>1.0</version>\n");
        xml.append("  </metadata>\n");
        
        // CEO information
        xml.append("  <ceo>\n");
        xml.append("    <id>").append(ceo.getID()).append("</id>\n");
        xml.append("    <name>").append(escapeXml(ceo.getName())).append("</name>\n");
        xml.append("    <managerId>").append(ceo.getMgrID() == null || ceo.getMgrID().isEmpty() ? "" : ceo.getMgrID()).append("</managerId>\n");
        xml.append("    <role>CEO</role>\n");
        xml.append("  </ceo>\n");
        
        // Hierarchy structure
        xml.append("  <hierarchy>\n");
        generateHierarchyXML(hierarchyTree, ceo.getID(), xml, employeeMap, 2);
        xml.append("  </hierarchy>\n");
        
        // All employees list
        xml.append("  <allEmployees>\n");
        for (EmployeeRecord employee : allEmployees) {
            xml.append("    <employee>\n");
            xml.append("      <id>").append(employee.getID()).append("</id>\n");
            xml.append("      <name>").append(escapeXml(employee.getName())).append("</name>\n");
            xml.append("      <managerId>").append(employee.getMgrID() == null || employee.getMgrID().isEmpty() ? "" : employee.getMgrID()).append("</managerId>\n");
            xml.append("      <isManager>").append(hierarchyTree.containsKey(employee.getID()) && !hierarchyTree.get(employee.getID()).isEmpty()).append("</isManager>\n");
            xml.append("      <level>").append(calculateEmployeeLevel(hierarchyTree, ceo.getID(), employee.getID())).append("</level>\n");
            xml.append("      <directReports>").append(hierarchyTree.containsKey(employee.getID()) ? hierarchyTree.get(employee.getID()).size() : 0).append("</directReports>\n");
            xml.append("    </employee>\n");
        }
        xml.append("  </allEmployees>\n");
        
        xml.append("</organization-hierarchy>");
        
        return xml.toString();
    }
    
    private void generateHierarchyXML(Map<String, List<EmployeeRecord>> hierarchyTree, 
                                    String employeeId, StringBuilder xml,
                                    Map<String, EmployeeRecord> employeeMap, int indent) {
        
        EmployeeRecord employee = employeeMap.get(employeeId);
        if (employee == null) return;
        
        String indentStr = "  ".repeat(indent);
        
        xml.append(indentStr).append("<employee>\n");
        xml.append(indentStr).append("  <id>").append(employee.getID()).append("</id>\n");
        xml.append(indentStr).append("  <name>").append(escapeXml(employee.getName())).append("</name>\n");
        xml.append(indentStr).append("  <managerId>").append(employee.getMgrID() == null || employee.getMgrID().isEmpty() ? "" : employee.getMgrID()).append("</managerId>\n");
        xml.append(indentStr).append("  <role>").append(employee.getMgrID() == null || employee.getMgrID().isEmpty() ? "CEO" : "Employee").append("</role>\n");
        xml.append(indentStr).append("  <level>").append(calculateEmployeeLevel(hierarchyTree, employeeMap.values().stream()
                .filter(e -> e.getMgrID() == null || e.getMgrID().isEmpty())
                .findFirst().orElse(employee).getID(), employeeId)).append("</level>\n");
        
        if (hierarchyTree.containsKey(employeeId) && !hierarchyTree.get(employeeId).isEmpty()) {
            xml.append(indentStr).append("  <directReports>").append(hierarchyTree.get(employeeId).size()).append("</directReports>\n");
            xml.append(indentStr).append("  <subordinates>\n");
            
            for (EmployeeRecord child : hierarchyTree.get(employeeId)) {
                generateHierarchyXML(hierarchyTree, child.getID(), xml, employeeMap, indent + 1);
            }
            
            xml.append(indentStr).append("  </subordinates>\n");
        } else {
            xml.append(indentStr).append("  <directReports>0</directReports>\n");
            xml.append(indentStr).append("  <subordinates/>\n");
        }
        
        xml.append(indentStr).append("</employee>\n");
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
    
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    @Override
    public String getFileExtension() {
        return "xml";
    }
    
    @Override
    public String getMimeType() {
        return "application/xml";
    }
    
    @Override
    public String getFormatterName() {
        return "XML";
    }
} 