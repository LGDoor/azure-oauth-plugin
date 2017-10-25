package com.microsoft.jenkins.azuread;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.gson.Gson;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by t-wanl on 8/23/2017.
 */
public class Utils {

    public static class TimeUtil {
        private static Date beginDate;
        private static Date endDate;

        public static void setBeginDate() {
//            TimeUtil.beginDate = beginDate;
            TimeUtil.beginDate = new Date();
        }

        public static void setEndDate() {
//            TimeUtil.endDate = endDate;
            TimeUtil.endDate = new Date();
        }

        public static Date getBeginDate() {
            return beginDate;
        }

        public static Date getEndDate() {
            return endDate;
        }

        public static long getTimeDifference() {
            if (beginDate == null || endDate == null) return -1;
            long diff = endDate.getTime() - beginDate.getTime();
            beginDate = null;
            endDate = null;
            return diff;
        }
    }

    public static class UUIDUtil {
        private static final Pattern pattern = Pattern
                .compile("(?i)^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-5][0-9a-f]{3}-?[089ab][0-9a-f]{3}-?[0-9a-f]{12}$");

        public static final boolean isValidUuid(final String uuid) {
            return ((uuid != null) && (uuid.trim().length() > 31)) ? pattern.matcher(uuid).matches() : false;
        }
    }

    public static class JenkinsUtil {
        public static SecurityRealm getSecurityRealm() {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                throw new RuntimeException("Jenkins is not started yet.");
            }
            SecurityRealm realm = jenkins.getSecurityRealm();
            return realm;
        }
    }

    public static class GsonUtil {
        public static Object generateFromJsonString(String jsonStr, Class modelClass) {
            Gson gson = new Gson();
            Object obj = gson.fromJson(jsonStr, modelClass);
            return obj;
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

