package io.castled.utils;

import com.google.api.client.util.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectionUtils {

    public static <A extends Annotation> List<A> getAnnotationsFromType(Class<?> classType, final Class<A> annotationClass) {

        List<A> annotations = Lists.newArrayList();
        while (!classType.getName().equals(Object.class.getName())) {

            annotations.addAll(Arrays.stream(classType.getAnnotationsByType(annotationClass))
                    .collect(Collectors.toList()));
            classType = classType.getSuperclass();
        }
        return annotations;
    }

    public static <A extends Annotation> A getAnnotation(Class<?> classType, final Class<A> annotationClass) {

        List<A> annotations = getAnnotationsFromType(classType, annotationClass);
        if (CollectionUtils.isEmpty(annotations)) {
            return null;
        }
        return annotations.get(0);
    }
}
