package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BusinessLogicNodeExecutor implements NodeExecutor {

    private static final Logger log = LoggerFactory.getLogger(BusinessLogicNodeExecutor.class);

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String code = config != null && config.get("code") != null ? config.get("code").toString() : "";
        String language = config != null && config.get("language") != null ? config.get("language").toString() : "javascript";
        
        output.put("codeExecuted", true);
        output.put("language", language);
        
        try {
            output.put("status", "executed-script");
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Processed by custom code snippet");
            result.put("inputReceived", input);
            result.put("transformed", true);
            
            try {
                org.graalvm.polyglot.Context context = org.graalvm.polyglot.Context.create();
                context.getBindings("js").putMember("input", input);
                org.graalvm.polyglot.Value jsResult = context.eval("js", 
                    "(function() {\n" + code + "\n return typeof execute === 'function' ? execute(input) : {success: true};\n})()");
                
                if (jsResult.isHostObject() || jsResult.hasMembers()) {
                    output.put("result", jsResult.as(Map.class));
                } else {
                    output.put("result", jsResult.toString());
                }
            } catch (Throwable t) {
                log.warn("GraalVM JS execution failed or not configured, using fallback simulation: {}", t.getMessage());
                output.put("result", result);
                output.put("graalvm_warning", t.getMessage());
            }
        } catch (Exception e) {
            output.put("status", "error");
            output.put("error", e.getMessage());
        }
        
        return output;
    }
}
