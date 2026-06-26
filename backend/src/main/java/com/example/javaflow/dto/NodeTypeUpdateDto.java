package com.example.javaflow.dto;

import java.util.Map;

public class NodeTypeUpdateDto {
    private String name;
    private String description;
    private Map<String, Object> configSchema;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, Object> getConfigSchema() { return configSchema; }
    public void setConfigSchema(Map<String, Object> configSchema) { this.configSchema = configSchema; }
}
