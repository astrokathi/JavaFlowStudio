package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Executor for email nodes.
 */
public class EmailNodeExecutor implements NodeExecutor {

    private final JavaMailSender mailSender;

    public EmailNodeExecutor(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> config = node.getConfig();
        String to = (String) config.get("to");
        String subject = (String) config.getOrDefault("subject", "Notification");
        String bodyTemplate = (String) config.getOrDefault("body", "");
        // We could support templating, but for simplicity we'll just use the body as is.
        // In a real app, you might replace placeholders with values from input.

        if (to == null || to.isEmpty()) {
            executionLog.setStatus("ERROR");
            executionLog.setErrorMessage("Email 'to' is required");
            return Mono.error(new IllegalArgumentException("Email 'to' is required"));
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(bodyTemplate);

            mailSender.send(message);

            Map<String, Object> output = new HashMap<>(input);
            output.put("emailSent", true);
            output.put("emailTo", to);

            executionLog.setStatus("SUCCESS");
            return Mono.just(output);
        } catch (Exception e) {
            executionLog.setStatus("ERROR");
            executionLog.setErrorMessage(e.getMessage());
            return Mono.error(e);
        }
    }
}
