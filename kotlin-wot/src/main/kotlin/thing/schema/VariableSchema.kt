package thing.schema




sealed class VariableSchema {

    data class StringSchema(val value: String) : VariableSchema()


    data class NumberSchema(val value: Float) : VariableSchema()


    data class IntegerSchema(val value: Int) : VariableSchema()


    data class BooleanSchema(val value: Boolean) : VariableSchema()
}