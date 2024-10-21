package ai.ancf.lmos.wot.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Defines the type of data. Used e.g. to define which input and output values a [ThingAction]
 * has or of which type a [ThingProperty] is.<br></br> See also: https://www.w3.org/TR/wot-thing-description/#sec-data-schema-vocabulary-definition
 *
 * @param <T>
</T> */
@JsonIgnoreProperties(ignoreUnknown = true)
interface DataSchema<T> {
    val type: String?

    @get:JsonIgnore
    val classType: Class<T>
}
