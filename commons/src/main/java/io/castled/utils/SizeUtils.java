package io.castled.utils;

public class SizeUtils {

    public static long convertGBToBytes(long sizeInGB) {
        return sizeInGB * 1024 * 1024;
    }

    public static long convertMBToBytes(long sizeInMB) {
        return sizeInMB * 1024 * 1024;
    }
}
