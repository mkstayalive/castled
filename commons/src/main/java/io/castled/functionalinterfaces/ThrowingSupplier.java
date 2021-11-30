package io.castled.functionalinterfaces;

@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws Exception;
}
