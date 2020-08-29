package io.github.joke.springfactory;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.apache.commons.io.IOUtils.resourceToURL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpringFactoryProcessorTest {

    @Test
    void checkSpringFactories() throws IOException {
        final Properties springFactories = new Properties();
        springFactories.load(resourceToURL("/META-INF/spring.factories").openStream());

        assertEquals(springFactories.getProperty("java.lang.Integer"), "io.github.joke.springfactory.FactoryWithOneAnnotation,io.github.joke.springfactory.FactoryWithTwoAnnotations");
        assertEquals(springFactories.getProperty("java.lang.String"), "io.github.joke.springfactory.FactoryWithTwoAnnotations");
    }

}