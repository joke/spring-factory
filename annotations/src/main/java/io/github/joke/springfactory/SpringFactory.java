package io.github.joke.springfactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation for spring factories.
 * <p>
 * Annotate your spring factories and the annotation processor will
 * generate the {@code META-INF/spring.factories} file.
 */
@Target(TYPE)
@Retention(CLASS)
public @interface SpringFactory {

    /**
     * @return List of interfaces implemented by this spring factory.
     */
    Class<?>[] value();

}