package io.castled.exceptions;


import io.castled.functionalinterfaces.ThrowingSupplier;

import java.util.function.Supplier;

public class NonThrowingSupplier<T> implements Supplier<T> {

    private final ThrowingSupplier<T> throwingSupplier;

    public NonThrowingSupplier(ThrowingSupplier<T> throwingSupplier) {
        this.throwingSupplier = throwingSupplier;
    }

    @Override
    public T get() {
        try {
            return throwingSupplier.get();
        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
    }
}
