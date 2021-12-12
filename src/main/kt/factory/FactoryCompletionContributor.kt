package factory

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
        var elementsList: ArrayList<LookupElement> = ArrayList()
        lateinit var typeEvalContext: TypeEvalContext
        lateinit var modelClass: PyClass

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            val prevSibling = position.prevSibling ?: return

            val instanceRef = prevSibling.prevSibling as? PyReferenceExpression ?: return

            val factoryCallExp = PyResolveUtil.fullResolveLocally(instanceRef) ?: return

            val factoryRef = factoryCallExp.firstChild as? PyReferenceExpression ?: return

            val factoryClass = factoryRef.reference.resolve() as? PyClass ?: return

            val factoryClassName = factoryClass.name ?: return
            if (!factoryClassName.endsWith("Factory")) return

            val modelName = factoryClassName.replace("Factory", "")
            val project = factoryClass.project
            val modelClass = PyClassNameIndexInsensitive.find(modelName, factoryClass.project).firstOrNull() ?: return

            typeEvalContext = TypeEvalContext.codeCompletion(project, modelClass.containingFile)

            addClassInheritedAttributes()
            addClassAttributes()
            addInstanceAttributes()
            addMethods()

            result.addAllElements(elementsList)

            result.stopHere()

        }

        private fun addModelClassRelatedThings(elements: Collection<PsiNamedElement>) {
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

        private fun addClassInheritedAttributes() {
            val classAttributesInherited = modelClass.getClassAttributesInherited(typeEvalContext)
            addModelClassRelatedThings(classAttributesInherited)
        }

        private fun addClassAttributes() {
            val classAttributes = modelClass.classAttributes
            addModelClassRelatedThings(classAttributes)
        }

        private fun addInstanceAttributes() {
            val InstanceAttributes = modelClass.instanceAttributes
            addModelClassRelatedThings(InstanceAttributes)
        }

        private fun addMethods() {
            val methods = modelClass.methods.toList()
            addModelClassRelatedThings(methods)
        }

        private fun isBuiltinOrBuiltinFunction(name: String): Boolean {
            return PyNames.UNDERSCORED_ATTRIBUTES.contains(name) || PyNames.INIT == name
        }
    }
}