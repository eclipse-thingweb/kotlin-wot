package ai.ancf.lmos.wot.schema


abstract class AbstractDataSchema<T> : DataSchema<T> {
    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        return if (obj !is DataSchema<*>) {
            false
        } else type == obj.type
    }
}
