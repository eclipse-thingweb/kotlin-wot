package ai.ancf.lmos.wot.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}

@SpringBootApplication
class TestApplication {

}
