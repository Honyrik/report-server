package ru.tehnobear.essence.dao.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Util {
    public static final ObjectMapper objectMapper;
    public static final ObjectMapper objectMapperAll;
    static  {
        objectMapper = new ObjectMapper(new JsonFactory());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModules(new JavaTimeModule(), new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapperAll = new ObjectMapper(new JsonFactory());
        objectMapperAll.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapperAll.registerModules(new JavaTimeModule(), new Jdk8Module());
        objectMapperAll.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
