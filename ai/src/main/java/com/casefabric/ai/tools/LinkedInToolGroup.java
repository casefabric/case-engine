package com.casefabric.ai.tools;

import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class LinkedInToolGroup {

    public static final String LINKEDIN = "linkedin";

    public static final ToolGroupDescription LINKEDIN_DESCRIPTION = ToolGroupDescription.create(
"Find a company or person profile on LinkedIn and have the ability to find jobs and get the details of the job",
            LINKEDIN
    );

    private static final Set<String> toolCalls = Set.of("close_session", "get_company_profile", "get_job_details", "get_person_profile", "get_recommended_jobs", "search_jobs");

    private final List<McpSyncClient> mcpSyncClients;

    public LinkedInToolGroup(List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients;
    }

    @Bean
    public ToolGroup provideLinkedInToolGroup() {
        return new McpToolGroup(
                LINKEDIN_DESCRIPTION,
                "Docker",
                "stickerdaniel/linkedin-mcp-server",
                Set.of(ToolGroupPermission.INTERNET_ACCESS),
                mcpSyncClients,
                this::processToolCallBack
        );
    }

    Boolean processToolCallBack(ToolCallback tcb) {
        AtomicBoolean willInclude = new AtomicBoolean(false);
        toolCalls.forEach(toolCall -> { if (tcb.getToolDefinition().name().endsWith(toolCall)) { willInclude.set(true);}});
        return willInclude.get();
    }

}
