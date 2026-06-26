package com.example.javaflow.model;

import java.util.Map;

public class Node {

    private String id;
    private String nodeTypeId;
    private String name;
    private Map<String, Object> config;
    private Position position;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNodeTypeId() { return nodeTypeId; }
    public void setNodeTypeId(String nodeTypeId) { this.nodeTypeId = nodeTypeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    // Inner class for position
    public static class Position {
        private double x;
        private double y;
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
    }
}
