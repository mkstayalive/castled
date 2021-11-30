package io.castled.interceptors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Retry {
    /**
     * No. of attempts to be made
     */
    int attempts() default 1;
    /**
     * Wait this much time before retrying again (in milliseconds)
     */
    long waitTime() default 0L;
    /**
     * Retry on these exceptions
     */
    Class<? extends Exception>[] types() default {Exception.class};
    /**
     * Don't retry on these exceptions
     */
    Class<? extends Exception>[] ignore() default {};
}
