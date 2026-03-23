package com.expense.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestrator {

    private final ExtractionAgent extractionAgent;
    private final CategorizationAgent categorizationAgent;
    private final BudgetMonitoringAgent budgetMonitoringAgent;
    private final AnalyticsAgent analyticsAgent;
    private final RecommendationAgent recommendationAgent;

    /**
     * Pipeline for natural language or manual expense entry.
     * Flow: Extract → Categorize → BudgetMonitor
     */
    public AgentContext processExpense(AgentContext context) {
        List<Agent> pipeline = List.of(
            extractionAgent,
            categorizationAgent,
            budgetMonitoringAgent
        );
        return runPipeline(pipeline, context);
    }

    /**
     * Pipeline for dashboard analytics.
     * Flow: Analytics → Recommendations
     */
    public AgentContext processDashboard(AgentContext context) {
        List<Agent> pipeline = List.of(
            analyticsAgent,
            recommendationAgent
        );
        return runPipeline(pipeline, context);
    }

    /**
     * Full pipeline — all 5 agents.
     * Flow: Extract → Categorize → BudgetMonitor → Analytics → Recommend
     */
    
    public AgentContext processAll(AgentContext context) {
        List<Agent> pipeline = List.of(
            extractionAgent,
            categorizationAgent,
            budgetMonitoringAgent,
            analyticsAgent,
            recommendationAgent
        );
        return runPipeline(pipeline, context);
    }

    private AgentContext runPipeline(List<Agent> agents, AgentContext context) {
        for (Agent agent : agents) {
            try {
                log.info("[Orchestrator] Running → {}", agent.getName());
                context = agent.process(context);
                log.info("[Orchestrator] Done ← {} | Status: {}",
                    agent.getName(),
                    context.getMessages().isEmpty() ? "?" :
                        context.getMessages().get(context.getMessages().size() - 1).getStatus());
            } catch (Exception e) {
                log.error("[Orchestrator] Agent {} failed: {}", agent.getName(), e.getMessage());
                context.addMessage(agent.getName(), "FAILED", e.getMessage());
            }
        }
        return context;
    }
}
