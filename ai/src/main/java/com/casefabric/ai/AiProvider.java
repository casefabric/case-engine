package com.casefabric.ai;

import com.embabel.agent.api.common.Ai;
import com.embabel.agent.core.AgentPlatform;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AiProvider implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext appContext) {
        ctx = appContext;
    }

    public static AgentPlatform getAgentPlatform() {
        return (AgentPlatform) ctx.getBean(AgentPlatform.class);
    }

    public static Ai  getAi() {
        return (Ai) ctx.getBean(Ai.class);
    }

}