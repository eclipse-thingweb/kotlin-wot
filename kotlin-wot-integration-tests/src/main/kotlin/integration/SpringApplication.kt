package ai.ancf.lmos.wot.integration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


fun main(args: Array<String>) {
    runApplication<ThingAgentApplication>(*args)
}

@SpringBootApplication
class ThingAgentApplication {

}
