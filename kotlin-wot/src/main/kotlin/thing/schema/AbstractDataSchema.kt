package ai.ancf.lmos.wot.thing.schema



abstract class AbstractDataSchema : DataSchema {
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
        return if (obj !is DataSchema) {
            false
        } else type == obj.type
    }
}
