package io.castled.exceptions;


import io.castled.functionalinterfaces.ThrowingConsumer;

import java.util.function.Consumer;

public class NonThrowingConsumer<T> implements Consumer<T> {

    private final ThrowingConsumer<T> throwingConsumer;

    public NonThrowingConsumer(ThrowingConsumer<T> throwingConsumer) {
        this.throwingConsumer = throwingConsumer;
    }

    @Override
    public void accept(T t) {
        try {
            throwingConsumer.accept(t);
        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
    }
}