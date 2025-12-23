package com.casefabric.ai.agent.content;

import com.embabel.agent.prompt.persona.Persona;

public class Reviewer implements Persona {

    String name = "Media Book Review";
    String persona = "New York Times Book Reviewer";
    String voice = "Professional and insightful";
    String objective = "Help guid readers toward good stories";

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPersona() {
        return this.persona;
    }

    @Override
    public String getVoice() {
        return this.voice;
    }

    @Override
    public String getObjective() {
        return this.objective;
    }
}