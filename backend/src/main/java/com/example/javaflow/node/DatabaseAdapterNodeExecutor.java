package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseAdapterNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String dbType = config != null && config.get("dbType") != null ? config.get("dbType").toString() : "mongodb";
        String connectionString = config != null && config.get("connectionString") != null ? config.get("connectionString").toString() : "";
        String query = config != null && config.get("query") != null ? config.get("query").toString() : "";
        
        output.put("dbType", dbType);
        output.put("connectionString", connectionString);
        output.put("queryExecuted", query);
        output.put("status", "connected");
        
        List<Map<String, Object>> simulatedData = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", "db-1");
        row1.put("name", "Simulated Record A");
        row1.put("status", "Active");
        simulatedData.add(row1);
        
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", "db-2");
        row2.put("name", "Simulated Record B");
        row2.put("status", "Pending");
        simulatedData.add(row2);
        
        output.put("records", simulatedData);
        return output;
    }
}
