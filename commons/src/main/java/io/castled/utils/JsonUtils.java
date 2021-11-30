package io.castled.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import io.castled.exceptions.CastledRuntimeException;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.IOException;
import java.util.Map;

public class JsonUtils {

    public static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .registerModule(new JavaTimeModule());
    }

    public static <T> T byteArrayToObject(byte[] value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new CastledRuntimeException(e);
        }
    }

    public static <T> T jsonStringToTypeReference(String jsonString, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(jsonString, typeReference);
        } catch (JsonProcessingException e) {
            throw new CastledRuntimeException(e);
        }
    }

    public static Map<String, Object> jsonStringToMap(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new CastledRuntimeException(e);
        }
    }

    public static Map<String, Object> jsonObjectToMap(JsonObject jsonObject) {
        Map<String, Object> objectMap = Maps.newHashMap();
        for (String jsonKey : jsonObject.keySet()) {
            JsonValue jsonValue = jsonObject.get(jsonKey);
            objectMap.put(jsonKey, processJsonValue(jsonValue));
        }
        return objectMap;
    }

    private static Object processJsonValue(JsonValue jsonValue) {
        switch (jsonValue.getValueType()) {
            case OBJECT:
                return jsonObjectToMap((JsonObject) jsonValue);
            case STRING:
                return ((JsonString) jsonValue).getString();
            case TRUE:
                return true;
            case FALSE:
                return false;
            case NUMBER:
                if (((JsonNumber) jsonValue).isIntegral()) {
                    return ((JsonNumber) jsonValue).longValue();
                }
            default:
                throw new CastledRuntimeException("Invalid json value type: " + jsonValue.getValueType());
        }

    }

    public static <T> T jsonStringToObject(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new CastledRuntimeException(e);
        }
    }


    public static byte[] objectToByteArray(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new CastledRuntimeException(e.getMessage());
        }
    }

    public static JsonNode jsonParserToJsonNode(JsonParser jsonParser) {
        try {
            return objectMapper.readTree(jsonParser);
        } catch (IOException e) {
            throw new CastledRuntimeException(e.getMessage());
        }
    }

    public static <T> T jsonParserToObject(JsonParser jsonParser, Class<T> clazz) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonParser);
            return jsonNodeToObject(jsonNode, clazz);
        } catch (IOException e) {
            throw new CastledRuntimeException(e.getMessage());
        }
    }

    public static <T> T jsonNodeToObject(JsonNode jsonNode, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            throw new CastledRuntimeException(e.getMessage());
        }
    }

    public static Map<String, Object> jsonNodeToMap(JsonNode jsonNode) {
        return objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
        });

    }

    public static String objectToString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new CastledRuntimeException(e.getMessage());
        }
    }

    public static Map<String, Object> objectToMap(Object object) {
        return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);

    }

    public static JsonNode mapToJsonNode(Map<String, Object> map) {
        return objectMapper.valueToTree(map);
    }
}
