package com.expense.agent;

public interface Agent {
    String getName();
    AgentContext process(AgentContext context);
}
