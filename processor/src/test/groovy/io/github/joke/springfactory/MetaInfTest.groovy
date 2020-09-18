package io.github.joke.springfactory

import spock.lang.Specification

class MetaInfTest extends Specification {

    def 'meta-inf/service is generated'() {
        setup:
        def metaInf = this.class.getResourceAsStream('/META-INF/services/javax.annotation.processing.Processor')

        expect:
        metaInf.readLines().first() == 'io.github.joke.springfactory.SpringFactoryProcessor'
    }

}
