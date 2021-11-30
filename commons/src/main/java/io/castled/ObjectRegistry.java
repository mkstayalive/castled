package io.castled;

import com.google.inject.Injector;
import com.google.inject.Key;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ObjectRegistry {
    @Setter
    private static Injector injector;

    public static <T> T getInstance(Class<T> tClass) {
        return injector.getInstance(tClass);
    }

    public static <T> T getInstance(Key<T> key) {
        return injector.getInstance(key);
    }
}
