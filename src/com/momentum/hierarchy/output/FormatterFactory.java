package com.momentum.hierarchy.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory class for creating and managing hierarchy formatters.
 */
public class FormatterFactory {
    
    private static final Map<String, HierarchyFormatter> formatters = new HashMap<>();
    
    static {
        // Register all available formatters
        formatters.put("html", new HTMLFormatter());
        formatters.put("json", new JSONFormatter());
        formatters.put("xml", new XMLFormatter());
    }
    
    /**
     * Get a formatter by name.
     * 
     * @param formatName The format name (case-insensitive)
     * @return The formatter instance, or null if not found
     */
    public static HierarchyFormatter getFormatter(String formatName) {
        if (formatName == null) {
            return null;
        }
        return formatters.get(formatName.toLowerCase());
    }
    
    /**
     * Get a formatter by file extension.
     * 
     * @param fileExtension The file extension (e.g., "html", "json", "xml")
     * @return The formatter instance, or null if not found
     */
    public static HierarchyFormatter getFormatterByExtension(String fileExtension) {
        if (fileExtension == null) {
            return null;
        }
        
        // Remove leading dot if present
        String ext = fileExtension.startsWith(".") ? fileExtension.substring(1) : fileExtension;
        
        for (HierarchyFormatter formatter : formatters.values()) {
            if (formatter.getFileExtension().equalsIgnoreCase(ext)) {
                return formatter;
            }
        }
        
        return null;
    }
    
    /**
     * Get all available formatter names.
     * 
     * @return Set of available formatter names
     */
    public static Set<String> getAvailableFormats() {
        return formatters.keySet();
    }
    
    /**
     * Get all available formatters.
     * 
     * @return Map of formatter names to formatter instances
     */
    public static Map<String, HierarchyFormatter> getAllFormatters() {
        return new HashMap<>(formatters);
    }
    
    /**
     * Check if a format is supported.
     * 
     * @param formatName The format name to check
     * @return true if the format is supported, false otherwise
     */
    public static boolean isFormatSupported(String formatName) {
        return formatName != null && formatters.containsKey(formatName.toLowerCase());
    }
    
    /**
     * Get the default formatter (HTML).
     * 
     * @return The default HTML formatter
     */
    public static HierarchyFormatter getDefaultFormatter() {
        return formatters.get("html");
    }
} 