package ai.ancf.lmos.wot.thing.action


import ai.ancf.lmos.wot.thing.schema.VariableSchema
import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction


class ActionState<I, O>(val handler: BiFunction<I, Map<String, Map<String, VariableSchema>>, CompletableFuture<O>>? = null) {

}

