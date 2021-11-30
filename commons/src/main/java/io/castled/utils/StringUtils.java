package io.castled.utils;

public class StringUtils {

    public static String quoteText(String text) {
        return String.format("\"%s\"", text);
    }

    public static String singleQuote(String text) {
        return String.format("'%s'", text);
    }

    public static String nullIfEmpty(String text) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(text) ? text : null;
    }

    public static boolean isEmpty(String text) {
        return org.apache.commons.lang3.StringUtils.isEmpty(text);
    }
}
