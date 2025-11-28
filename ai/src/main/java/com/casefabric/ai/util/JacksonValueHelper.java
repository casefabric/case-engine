package com.casefabric.ai.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.cafienne.util.json.JSONReader;
import org.cafienne.util.json.Value;

public class JacksonValueHelper {
    private static final ObjectWriter objectWriter = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();

    public static Value<?> toValue(Object value) {
        try {
            var asJackson = objectWriter.writeValueAsString(value);
            return JSONReader.parse(asJackson);
        } catch (Exception e) {
                throw new RuntimeException(e);
        }
    }

}
