package io.github.joke.springfactory;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

@AutoService(Processor.class)
public class SpringFactoryProcessor extends AbstractProcessor {

    final protected Map<String, Set<String>> factories = new HashMap<>();
    final protected Set<Element> originatingElements = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        processAnnotations(annotations, roundEnv);
        if (roundEnv.processingOver()) {
            try {
                writeSpringFactoriesFile();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to write spring.factories", e);
            }
        }

        return true;
    }

    protected void processAnnotations(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.stream()
                .map(roundEnv::getElementsAnnotatedWith)
                .flatMap(Collection::stream)
                .peek(originatingElements::add)
                .flatMap(SpringFactoryProcessor::streamOfFactoryToClassName)
                .forEach(entry -> factories.computeIfAbsent(entry.getKey(), (ignored) -> new HashSet<>()).add(entry.getValue()));
    }

    protected void writeSpringFactoriesFile() throws IOException {
        final Element[] elements = new Element[originatingElements.size()];
        originatingElements.toArray(elements);
        final FileObject resource = processingEnv.getFiler().createResource(CLASS_OUTPUT, "", "META-INF/spring.factories", elements);
        try (final OutputStream output = resource.openOutputStream(); final PrintStream printStream = new PrintStream(output, false, UTF_8.name())) {
            factories.forEach((k, v) -> {
                printStream.println(k + "=\\");
                printStream.println(join(",\\" + lineSeparator(), v));
            });
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(SpringFactory.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return RELEASE_8;
    }

    private static Stream<SimpleEntry<String, String>> streamOfFactoryToClassName(final Element element) {
        return SpringFactoryProcessor.<MirroredTypesException>getException(element.getAnnotation(SpringFactory.class)::value)
                .getTypeMirrors().stream()
                .map(TypeMirror::toString)
                .map(valueClassName -> new SimpleEntry<>(valueClassName, element.toString()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> T getException(final Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            return (T) e;
        }
        throw new UnsupportedOperationException();
    }
}