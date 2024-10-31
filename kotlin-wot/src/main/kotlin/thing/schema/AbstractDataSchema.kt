package ai.ancf.lmos.wot.thing.schema


abstract class AbstractDataSchema<T> : DataSchema<T> {

    abstract val classType: Class<*>
}
