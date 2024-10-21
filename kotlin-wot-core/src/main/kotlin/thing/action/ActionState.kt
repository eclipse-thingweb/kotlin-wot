package ai.ancf.lmos.wot.thing.action

import java.util.concurrent.CompletableFuture
import java.util.function.BiFunction


class ActionState<I, O> internal constructor(var handler: BiFunction<I, Map<String, Map<String, Any>>, CompletableFuture<O>>?) {
    constructor() : this(null)
}

