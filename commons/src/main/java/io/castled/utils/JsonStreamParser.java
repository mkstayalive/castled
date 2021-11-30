package io.castled.utils;

import com.google.inject.Singleton;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.functionalinterfaces.ThrowingConsumer;
import lombok.extern.slf4j.Slf4j;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Singleton
public class JsonStreamParser {

    public void parseJsonStream(InputStream inputStream, ThrowingConsumer<Map<String, Object>> recordsConsumer) {
        try {
            JsonParser jsonParser = Json.createParser(inputStream);
            JsonParser.Event event = jsonParser.next();
            if (event != JsonParser.Event.START_ARRAY) {
                throw new JsonParsingException("Result Json should start with START_ARRAY tag", jsonParser.getLocation());
            }
            event = jsonParser.next();
            if (event == JsonParser.Event.END_ARRAY) {
                return;
            }
            if (event != JsonParser.Event.START_OBJECT) {
                throw new JsonParsingException("Invalid result json: expected START_OBJECT", jsonParser.getLocation());
            }
            while (event != JsonParser.Event.END_ARRAY) {
                JsonObject record = jsonParser.getObject();
                recordsConsumer.accept(JsonUtils.jsonObjectToMap(record));
                event = jsonParser.next();
            }
        } catch (Exception e) {
            log.error("Json stream parse failed", e);
            throw new CastledRuntimeException(e);
        }
    }
}
