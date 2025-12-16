package org.cafienne.model.dmn.definition;

public enum HitPolicyDefinition {
    UNIQUE("Unique"),
    FIRST("First"),
    PRIORITY("Priority"),
    ANY("Any"),
    COLLECT("Collect"),
    RULE_ORDER("Rule Order"),
    OUTPUT_ORDER("Output Order");

    private final String text;

    HitPolicyDefinition(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static HitPolicyDefinition fromString(String text) {
        for (HitPolicyDefinition b : HitPolicyDefinition.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return UNIQUE;
    }
}