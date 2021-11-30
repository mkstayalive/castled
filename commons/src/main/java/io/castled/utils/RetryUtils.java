package io.castled.utils;

import io.castled.core.WaitTimeAndRetry;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.functionalinterfaces.ThrowingSupplier;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {

    public static <T> T retrySupplier(ThrowingSupplier<T> supplier, int maxRetries,
                                      List<Class<? extends Throwable>> whitelistedExceptions, BiFunction<Throwable, Integer,
            WaitTimeAndRetry> actionOnError) throws Exception {
        for (int attempts = 0; attempts <= maxRetries; attempts++) {
            try {
                return supplier.get();
            } catch (Throwable e) {
                WaitTimeAndRetry waitTimeAndRetry = actionOnError.apply(e, attempts);
                if (!waitTimeAndRetry.isShouldRetry()) {
                    throw e;
                }
                if (attempts == maxRetries || !isExceptionWhitelisted(whitelistedExceptions, e)) {
                    throw e;
                }
                ThreadUtils.interruptIgnoredSleep(waitTimeAndRetry.getWaitTimeMs());
            }
        }
        throw new CastledRuntimeException("cannot reach here");
    }

    public static boolean isExceptionWhitelisted(List<Class<? extends Throwable>> whitelistedExceptions, Throwable exception) {
        for (final Class<? extends Throwable> type : whitelistedExceptions) {
            if (type.isAssignableFrom(exception.getClass())) {
                return true;
            }
        }
        return false;
    }
}
