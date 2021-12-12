package factory

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.jetbrains.python.PythonParserDefinition
import com.jetbrains.python.psi.impl.*

class FactoryPythonParserDefinition : PythonParserDefinition() {
    override fun createElement(node: ASTNode): PsiElement {
        var element = super.createElement(node)
        if (element is PyReferenceExpressionImpl) {
            element = FactoryPyReferenceExpression(node)
        }
        return element
    }
}