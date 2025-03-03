/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.reflection

/*
class ConsumedThingBuilder {
    companion object {
        fun <T : Any> create(clazz: KClass<T>, consumedThing: ConsumedThing): T {
            return Proxy.newProxyInstance(
                clazz.java.classLoader,
                arrayOf(clazz.java)
            ) { _, method, args ->
                handleMethodInvocation(clazz, consumedThing, method, args)
            } as T
        }

        private fun <T : Any> handleMethodInvocation(
            clazz: KClass<T>,
            consumedThing: ConsumedThing,
            method: Method,
            args: Array<out Any>?
        ): Any? {
            // Handle @Action methods
            val actionAnnotation = clazz.declaredMemberFunctions
                .find { it.name == method.name }
                ?.findAnnotation<Action>()

            if (actionAnnotation != null) {
                val actionName = actionAnnotation.name.ifEmpty { method.name }
                return if (method.isSuspend()) {
                    runBlocking {
                        invokeSuspendingAction(consumedThing, actionName, args)
                    }
                } else {
                    runBlocking {
                        consumedThing.invokeAction(
                            actionName,
                            InteractionInput.Value(
                                JsonMapper.instance.valueToTree(
                                    args?.firstOrNull()
                                )
                            )
                        )
                    }
                }
            }

            // Handle @Property methods (getter/setter)
            val propertyAnnotation = clazz.declaredMemberFunctions
                .find { it.name == method.name }
                ?.findAnnotation<Property>()

            if (propertyAnnotation != null) {
                val propertyName = propertyAnnotation.name.ifEmpty { inferPropertyName(method.name) }
                return handleProperty(consumedThing, method, propertyName, args)
            }

            throw UnsupportedOperationException("Unsupported method: ${method.name}")
        }

        private fun inferPropertyName(methodName: String): String {
            return when {
                methodName.startsWith("get") -> methodName.removePrefix("get").decapitalize()
                methodName.startsWith("set") -> methodName.removePrefix("set").decapitalize()
                else -> methodName
            }
        }

        private fun handleProperty(
            consumedThing: ConsumedThing,
            method: Method,
            propertyName: String,
            args: Array<out Any>?
        ): Any? {
            return if (method.name.startsWith("get")) {
                runBlocking { consumedThing.readProperty(propertyName) }
            } else if (method.name.startsWith("set")) {
                runBlocking { consumedThing.writeProperty(propertyName,  InteractionInput.Value(
                    JsonMapper.instance.valueToTree(
                        args?.firstOrNull()
                    )
                )) }
                null
            } else {
                throw UnsupportedOperationException("Property methods must start with get or set")
            }
        }

        private suspend fun invokeSuspendingAction(
            consumedThing: ConsumedThing,
            actionName: String,
            args: Array<out Any>?
        ): Any? {
            val continuation = args?.lastOrNull() as? Continuation<*>
                ?: throw IllegalStateException("Missing Continuation for suspend function")
            return try {
                consumedThing.invokeAction(actionName,  InteractionInput.Value(
                    JsonMapper.instance.valueToTree(
                        args.firstOrNull()
                    )
                ))
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
                null
            }.also { result ->
                /*
                when(val dataSchemaValue = result?.value()) {
                    is JsonNode.StringValue -> continuation.resume(dataSchemaValue.value)
                    is JsonNode.ArrayValue -> continuation.resume(dataSchemaValue.value)
                    is JsonNode.BooleanValue -> continuation.resume(dataSchemaValue.value)
                    is JsonNode.IntegerValue -> continuation.resume(dataSchemaValue.value)
                    is JsonNode.NullValue -> continuation.resume(null)
                    is JsonNode.NumberValue -> continuation.resume(dataSchemaValue.value)
                    is JsonNode.ObjectValue -> continuation.resume(dataSchemaValue.value)
                    null -> continuation.resume()
                }
                 */
            }
        }
    }
}

// Extension to detect if a method is a suspend function
fun Method.isSuspend(): Boolean {
    val params = this.parameterTypes
    return params.isNotEmpty() && Continuation::class.java.isAssignableFrom(params.last())
}

/**
 * Factory interface to create [InvocationHandler] around the original interface / service
 */
interface InterfaceInvocationHandlerFactory {
    fun create(
        originalInterface: Any,
    ): InvocationHandler
}

/*
class InterfaceInvocationHandlerFactoryImpl : InterfaceInvocationHandlerFactory {

    override fun create(
        originalInterface: Any,
    ): InvocationHandler {
        return object : InvocationHandler {

            override fun invoke(
                proxy: Any,
                method: Method,
                args: Array<out Any>?,
            ): Any? {
                val nonNullArgs = args ?: arrayOf()
                val continuation = nonNullArgs.continuation()

                return if (continuation == null) {
                    // non-suspending function, just invoke regularly
                    try {
                        val result = method.invoke(originalInterface, *nonNullArgs)
                        // we could inspect anything that we want on the result at this point
                        result
                    } catch (invocationTargetException: InvocationTargetException) {
                        throw invocationTargetException.cause ?: invocationTargetException
                    }
                } else {
                    // create a wrapper around the original continuation. we want to do this so we can capture the result and
                    // potentially inspect it
                    val wrappedContinuation = object : Continuation<Any?> {
                        override val context: CoroutineContext get() = continuation.context

                        override fun resumeWith(
                            result: Result<Any?>,
                        ) {
                            // here is where we could inspect result for any type of result / error that we'd like.
                            // since we are not doing anything special with it in this example, we can just resume the continuation
                            // with the value
                            continuation.intercepted().resumeWith(result)
                        }
                    }

                    invokeSuspendFunction(continuation) outer@{
                        // we want to invoke the method with our continuation wrapper instead
                        // of the original continuation so we can inspect the results. So we will
                        // grab the original arguments, and replace the last element with our continuation wrapper
                        val argumentsWithoutContinuation = if (nonNullArgs.isNotEmpty()) {
                            nonNullArgs.take(nonNullArgs.size - 1)
                        } else {
                            nonNullArgs.toList()
                        }

                        val newArgs = argumentsWithoutContinuation + wrappedContinuation

                        try {
                            val result =
                                method.invoke(
                                    originalInterface,
                                    *newArgs.toTypedArray(),
                                )

                            if (result == COROUTINE_SUSPENDED) {
                                // this can happen if the method we are proxying is a suspending.
                                // when that is the case, just return result / COROUTINE_SUSPENDED since they are the same thing
                                result
                            } else {
                                // here is where we could inspect result
                                result
                            }
                        } catch (invocationTargetException: InvocationTargetException) {
                            throw invocationTargetException.cause ?: invocationTargetException
                        }
                    }
                }
            }


            @Suppress("UNCHECKED_CAST")
            fun <T> invokeSuspendFunction(
                continuation: Continuation<*>,
                block: suspend () -> T,
            ): T =
                (block as (Continuation<*>) -> T)(continuation)


            @Suppress("UNCHECKED_CAST")
            private fun Array<*>?.continuation(): Continuation<Any?>? {
                return this?.lastOrNull() as? Continuation<Any?>
            }

        }
    }
}
*/