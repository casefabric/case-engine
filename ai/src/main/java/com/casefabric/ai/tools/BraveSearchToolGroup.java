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
public class BraveSearchToolGroup {

    public static final String BRAVE_SEARCH = "brave";

    public static final ToolGroupDescription BRAVE_DESCRIPTION = ToolGroupDescription.create(
"Search on the internet via the Brave search engine and return relevant content for video, news and the web",
            BRAVE_SEARCH
    );

    private static final Set<String> toolCalls = Set.of("brave_image_search", "brave_local_search", "brave_news_search", "brave_summarizer", "brave_video_search", "brave_web_search");

    private final List<McpSyncClient> mcpSyncClients;

    public BraveSearchToolGroup(List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients;
    }

    @Bean
    public ToolGroup provideBraveSearchToolGroup() {
        return new McpToolGroup(
                BRAVE_DESCRIPTION,
                "Docker",
                "mcp/brave-search",
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
