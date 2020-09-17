package io.github.joke.springfactory

import spock.lang.Specification

import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.tools.FileObject

import static java.nio.charset.StandardCharsets.UTF_8
import static javax.lang.model.SourceVersion.RELEASE_8
import static javax.tools.StandardLocation.CLASS_OUTPUT

class SpringFactoryProcessorTest extends Specification {

    def processor = Spy(new SpringFactoryProcessor())

    def 'environment is initialized'() {
        setup:
        ProcessingEnvironment processingEnvironment = Mock()
        processor.init(processingEnvironment)

        expect:
        processor.processingEnv == processingEnvironment
        processor.initialized
    }

    def 'only our annotations supported'() {
        expect:
        processor.supportedAnnotationTypes == ['io.github.joke.springfactory.SpringFactory'] as Set
    }

    def 'supported version is java'() {
        expect:
        processor.supportedSourceVersion == RELEASE_8
    }

    def 'write output if processing stops'() {
        setup:
        RoundEnvironment roundEnvironment = Mock()

        when:
        processor.process([] as Set, roundEnvironment)


        then:
        1 * roundEnvironment.processingOver() >> processingOver
        numberInvocationsWriteFiles * processor.writeSpringFactoriesFile() >> {}

        where:
        processingOver | numberInvocationsWriteFiles
        false          | 0
        true           | 1
    }

    def 'write output'() {
        setup:
        ProcessingEnvironment processingEnvironment = DeepMock()
        FileObject fileObject = DeepMock()
        processor.@processingEnv = processingEnvironment
        processor.factories.putAll(['Factory1': ['ClassA', 'ClassB'] as Set, 'Factory2': ['ClassC'] as Set])
        Element element = Mock()
        processor.originatingElements.add(element)
        def output = new ByteArrayOutputStream()

        when:
        processor.writeSpringFactoriesFile()

        then:
        1 * processingEnvironment.filer.createResource(CLASS_OUTPUT, '', 'META-INF/spring.factories', [element]) >> fileObject
        1 * fileObject.openOutputStream() >> output

        expect:
        output.toString("$UTF_8") == '''Factory2=\\
                                                    |ClassC
                                                    |Factory1=\\
                                                    |ClassA,\\
                                                    |ClassB
                                                    |'''.stripMargin('|')
    }

}