package ai.ancf.lmos.kotlinsdk.server

import ai.ancf.lmos.sdk.DataSchemaBuilder
import ai.ancf.lmos.sdk.LMOSContext
import ai.ancf.lmos.sdk.LMOSThingTypes
import ai.ancf.lmos.sdk.model.AgentRequest
import ai.ancf.lmos.sdk.model.AgentResult
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.thing.DEFAULT_CONTEXT
import ai.ancf.lmos.wot.thing.schema.Context
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.VersionInfo
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import org.slf4j.LoggerFactory
import kotlin.reflect.full.createType

class LmosRuntime(private val servient: Servient) {

    private val wot = Wot.create(servient)

    suspend fun start() {
        servient.start()
        log.info("LmosRuntime started")
    }

    suspend fun stop() {
        servient.shutdown()
        log.info("LmosRuntime stopped")
    }

    suspend fun add(agent: LmosAgent)  {
        val agentThing = agent.toThing(wot)
        //servient.addThing(agentThing)
        servient.expose(agentThing.getThingDescription().id)
    }

    // TODO: Remove an agent? Currently not possible in kotlin-wot

    companion object {
        private val log = LoggerFactory.getLogger(LmosRuntime::class.java)
    }

}

private fun LmosAgent.toThing(wot: Wot): WoTExposedThing {
    return wot.produce {
        id = this@toThing.id
        title = this@toThing.title
        description = this@toThing.description
        objectType = LMOSThingTypes.AGENT

        val context = Context(DEFAULT_CONTEXT)
        context.addContext(LMOSContext.prefix, LMOSContext.url)
        objectContext = context

        version = VersionInfo("1.0.0", null) // TODO: Is this something specified by the agent developer? Or LMOS specific?

        // TODO: Links to capabilities

        // TODO: Really <Any, Any> ?
        action<Any, Any>("chat") {
            title = "Chat"
            description = "Chat with the agent"
            safe = false
            idempotent = false
            // TODO: Is this unsafe cast needed? How to avoid it?
            input = DataSchemaBuilder.mapTypeToSchema(AgentRequest::class.createType()) as DataSchema<Any>
            output = DataSchemaBuilder.mapTypeToSchema(AgentResult::class.createType()) as DataSchema<Any>
        }

        // TODO: add agent events
    }
}