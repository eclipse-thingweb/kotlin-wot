package ai.ancf.lmos.wot.thing

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import kotlinx.serialization.Serializable
import java.util.*


@JsonDeserialize(using = TypeDeserializer::class)
@JsonSerialize(using = TypeSerializer::class)
@Serializable
class Type {
    private val types: MutableSet<String> = HashSet()
    constructor()
    constructor(type: String) {
        addType(type)
    }

    fun addType(type: String): Type {
        types.add(type)
        return this
    }

    fun getTypes(): Set<String> {
        return types
    }

    override fun hashCode(): Int {
        return Objects.hash(types)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val type = o as Type
        return types == type.types
    }

    override fun toString(): String {
        return "Type{" +
                "types=" + types +
                '}'
    }
}
