package factory

import com.google.common.collect.ImmutableList
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiNamedElement
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.stubs.PyClassNameIndexInsensitive
import com.jetbrains.python.psi.types.TypeEvalContext

class FactoryCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), FactoryCompletionProvider())
    }

    private class FactoryCompletionProvider: CompletionProvider<CompletionParameters>() {

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            val prevSibling = position.prevSibling ?: return

            val instanceRef = prevSibling.prevSibling as? PyReferenceExpression ?: return

            val (project, modelClass) = FactoryUtils.getResolvedData(instanceRef) ?: return

            val typeEvalContext = TypeEvalContext.codeCompletion(project, modelClass.containingFile)

            val membersList = ImmutableList.of(
                modelClass.getClassAttributesInherited(typeEvalContext),
                modelClass.classAttributes,
                modelClass.instanceAttributes,
                modelClass.methods.toList()
            )

            val elementsList = ArrayList<LookupElement>()
            for (members in membersList) {
                addModelClassMembers(members, modelClass, elementsList)
            }
            result.addAllElements(elementsList)

            result.stopHere()

        }

        private fun addModelClassMembers(elements: Collection<PsiNamedElement>, modelClass: PyClass, elementsList: ArrayList<LookupElement>) {
            for (element in elements) {
                val name = element.name ?: continue
                if (isBuiltinOrBuiltinFunction(name)) continue

                val factoryAttribute = FactoryAttribute(name, modelClass.name ?: "")
                val texts: Pair<String, String> = factoryAttribute.createCompletionStrings()
                val lookupElement: LookupElement = LookupElementBuilder.create(texts.first)
                    .withTypeText(texts.second)
                    .withIcon(element.getIcon(0))
                elementsList.add(lookupElement)
            }
        }

        private fun isBuiltinOrBuiltinFunction(name: String): Boolean {
            return PyNames.UNDERSCORED_ATTRIBUTES.contains(name) || PyNames.INIT == name
        }
    }
}