package ai.ancf.lmos.wot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonMapper {
    val instance: ObjectMapper = jacksonObjectMapper().apply {
        registerKotlinModule()
    }
}