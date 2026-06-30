package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class RepositoryNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String repositoryName = config != null && config.get("repositoryName") != null ? config.get("repositoryName").toString() : "PostRepository";
        String extendsInterface = config != null && config.get("extendsInterface") != null ? config.get("extendsInterface").toString() : "ReactiveMongoRepository<Post, String>";
        String methods = config != null && config.get("methods") != null ? config.get("methods").toString() : "";
        
        output.put("repositoryInvoked", true);
        output.put("repositoryName", repositoryName);
        output.put("extendsInterface", extendsInterface);
        output.put("customMethods", methods);
        output.put("status", "db-repository-active");
        
        return output;
    }
}
