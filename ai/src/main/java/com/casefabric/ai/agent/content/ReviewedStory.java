package com.casefabric.ai.agent.content;

import com.casefabric.ai.util.JacksonValueHelper;
import com.embabel.agent.domain.library.HasContent;
import com.embabel.agent.prompt.persona.Persona;
import com.embabel.common.core.types.Timestamped;
import org.cafienne.util.json.Value;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.cafienne.util.json.CafienneJson;

public record ReviewedStory(
        Story story,
        String review,
        Reviewer reviewer
) implements HasContent, Timestamped, CafienneJson {

    @Override
    @NonNull
    public Instant getTimestamp() {
        return Instant.now();
    }

    @Override
    @NonNull
    public String getContent() {
        return String.format("""
                            # Story
                            %s
                            
                            # Review
                            %s
                            
                            # Reviewer
                            %s, %s
                            """,
                story.text(),
                review,
                reviewer.getName(),
                getTimestamp().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
        ).trim();
    }

    @Override
    public Value<?> toValue() {
        return JacksonValueHelper.toValue(this);
    }

}

