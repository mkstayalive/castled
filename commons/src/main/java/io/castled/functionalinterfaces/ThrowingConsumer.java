package io.castled.functionalinterfaces;

@FunctionalInterface
public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;


}