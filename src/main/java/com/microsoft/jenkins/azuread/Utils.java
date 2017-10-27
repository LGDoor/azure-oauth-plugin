package com.microsoft.jenkins.azuread;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by t-wanl on 8/23/2017.
 */
public class Utils {

    public static class UUIDUtil {
        private static final Pattern pattern = Pattern
                .compile("(?i)^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-5][0-9a-f]{3}-?[089ab][0-9a-f]{3}-?[0-9a-f]{12}$");

        public static final boolean isValidUuid(final String uuid) {
            return ((uuid != null) && (uuid.trim().length() > 31)) ? pattern.matcher(uuid).matches() : false;
        }
    }

    public static class JsonUtil {
        private static ObjectMapper mapper = new ObjectMapper();

        static {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        public static <T> T fromJson(String json, Class<T> klazz) {
            try {
                return mapper.readValue(json, klazz);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static <T> String toJson(T obj) {
            try {
                return mapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

