package spring

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import org.springframework.boot.web.server.WebServer
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.boot.web.servlet.server.ServletWebServerFactory

class WoTServerFactory(val wot: Wot, val servient: Servient) : ServletWebServerFactory {

    override fun getWebServer(vararg initializers: ServletContextInitializer?): WebServer {
         return WoTServer(wot, servient)
    }

}