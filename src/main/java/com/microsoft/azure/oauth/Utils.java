package com.microsoft.azure.oauth;

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
}

