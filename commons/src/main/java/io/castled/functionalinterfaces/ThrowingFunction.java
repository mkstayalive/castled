package io.castled.functionalinterfaces;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;

}
