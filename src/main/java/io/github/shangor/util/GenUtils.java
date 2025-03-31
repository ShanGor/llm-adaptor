package io.github.shangor.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Collection;
import java.util.Map;

public class GenUtils {
    private static final ObjectMapper objectMapperSnake = new ObjectMapper();
    static {
        objectMapperSnake.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapperSnake.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapperSnake.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapperSnake.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapperSnake.registerModule(new JavaTimeModule());
    }


    public static String objectToJsonSnake(Object o) {
        try {
            return objectMapperSnake.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            return objectMapperSnake.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Collection<?>> boolean isEmptyCollection(T obj) {
        return obj == null || obj.isEmpty();
    }

    public static <T extends Map<?, ?>> boolean isEmptyCollection(T obj) {
        return obj == null || obj.isEmpty();
    }

}
