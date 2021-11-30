package io.castled.utils;

public class TimeUtils {

    private static final int MINUTES_IN_HOUR = 60;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MILLIS_IN_SECOND = 1000;

    public static long minutesToMillis(long minutes) {
        return minutes * SECONDS_IN_MINUTE * MILLIS_IN_SECOND;
    }

    public static long minutesToSeconds(long minutes) {
        return minutes * SECONDS_IN_MINUTE;
    }

    public static long hoursToSeconds(long hours) {
        return hours * MINUTES_IN_HOUR * SECONDS_IN_MINUTE;
    }

    public static long millisToMinutes(long millis) {
        return millis / (MILLIS_IN_SECOND * SECONDS_IN_MINUTE);
    }

    public static long millisToSeconds(long millis) {
        return millis / (MILLIS_IN_SECOND);
    }

    public static long millisToNanos(long millis) {
        return millis * 1000 * 1000;
    }


    public static long secondsToMillis(long seconds) {
        return seconds * MILLIS_IN_SECOND;
    }

    public static long hoursToMillis(int hours) {
        return minutesToMillis(MINUTES_IN_HOUR);
    }

}
