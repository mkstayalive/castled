package io.castled.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Optional;

public class CastledExceptionUtils {

    public static boolean hasMessage(Throwable e, String searchString) {
        Throwable rootThrowable = ExceptionUtils.getRootCause(e);
        return Optional.ofNullable(rootThrowable.getMessage()).filter(message -> StringUtils.containsIgnoreCase(message, searchString)).isPresent();
    }
}
