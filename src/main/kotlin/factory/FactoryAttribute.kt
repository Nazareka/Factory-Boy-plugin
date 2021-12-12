package factory

class FactoryAttribute(var attributeName: String, var modelName: String) {
    fun createCompletionStrings(): Pair<String, String> {
        val typeString = "Factory($modelName)"
        return Pair(attributeName, typeString)
    }
}