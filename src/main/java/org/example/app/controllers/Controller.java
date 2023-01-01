package org.example.app.controllers;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

public class Controller {
    @Getter
    @Setter
    private ObjectMapper objectMapper;

    public Controller() {
        setObjectMapper(new ObjectMapper());
        getObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }
}
