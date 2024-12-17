package ai.ancf.lmos.wot.thing

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize


@JsonDeserialize(using = TypeDeserializer::class)
@JsonSerialize(using = TypeSerializer::class)
data class Type(val types: MutableSet<String> = HashSet()) {

    constructor(type: String) : this() {
        addType(type)
    }

    fun addType(type: String): Type {
        types.add(type)
        return this
    }

    val defaultType: String
        get() = types.first()

}
