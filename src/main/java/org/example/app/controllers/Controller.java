package org.example.app.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

public class Controller {
    @Getter
    @Setter
    private ObjectMapper objectMapper;

    public Controller() {
        setObjectMapper(new ObjectMapper());
        getObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        getObjectMapper().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }

    public static String getFieldValueCaseInsensitive(JsonNode rootNode, String fieldName) {
        for (Iterator<String> it = rootNode.fieldNames(); it.hasNext(); ) {
            String currentFieldName = it.next();
            if (currentFieldName.equalsIgnoreCase(fieldName)) {
                JsonNode fieldNode = rootNode.get(currentFieldName);
                return fieldNode.asText();
            }
        }
        return null;
    }
}
