package io.castled.exceptions;
import io.castled.functionalinterfaces.ThrowingFunction;

import java.util.function.Function;

public class NonThrowingFunction<T, R> implements Function<T, R> {

    private final ThrowingFunction<T, R> throwingFunction;

    public NonThrowingFunction(ThrowingFunction<T, R> throwingFunction) {
        this.throwingFunction = throwingFunction;
    }

    @Override
    public R apply(T t) {
        try {
            return throwingFunction.apply(t);
        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
    }
}
