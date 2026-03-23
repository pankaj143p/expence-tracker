package com.expense.controller;

import com.expense.agent.AssistantAgent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantAgent assistantAgent;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ChatRequest req) {
        String reply = assistantAgent.chat(user.getUsername(), req.getMessage());
        return ResponseEntity.ok(Map.of("reply", reply));
    }

    @Data
    public static class ChatRequest {
        private String message;
    }
}
