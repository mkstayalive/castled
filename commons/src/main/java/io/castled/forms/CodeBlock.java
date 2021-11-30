package io.castled.forms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CodeBlock {

    CodeSnippet[] snippets() default {};

    String[] dependencies() default {};

    String title() default "";
}
