package com.casefabric.ai.tracking;

import com.embabel.agent.event.AgentPlatformEvent;
import com.embabel.agent.event.AgentProcessEvent;
import com.embabel.agent.event.AgenticEventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiTrackerEventListener implements AgenticEventListener {
    static Logger logger = LoggerFactory.getLogger(AiTrackerEventListener.class);

    @Override
    public void onPlatformEvent(@NotNull AgentPlatformEvent event) {
        logger.trace("onPlatformEvent, {}", event);
    }

    @Override
    public void onProcessEvent(@NotNull AgentProcessEvent event) {
        logger.trace("onProcessEvent {}, {} : {}", event.getProcessId(), event.getClass().getName(), event);
    }
}
