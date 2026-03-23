package com.expense.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AgentMessage {
    private String agentName;
    private String status;      // SUCCESS, FAILED, SKIPPED, FALLBACK
    private String detail;
    private LocalDateTime timestamp;

    public AgentMessage(String agentName, String status, String detail) {
        this.agentName = agentName;
        this.status = status;
        this.detail = detail;
        this.timestamp = LocalDateTime.now();
    }
}
