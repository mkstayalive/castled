package io.castled.forms;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(GroupActivators.class)
public @interface GroupActivator {

    String condition() default "";

    String group() default "";

    String[] dependencies() default {};
}
