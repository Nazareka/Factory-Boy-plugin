package factory

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiPolyVariantReference
import com.jetbrains.python.psi.impl.PyReferenceExpressionImpl
import com.jetbrains.python.psi.impl.references.PyImportReference
import com.jetbrains.python.psi.impl.references.PyQualifiedReference
import com.jetbrains.python.psi.impl.references.PyReferenceImpl
import com.jetbrains.python.psi.resolve.PyResolveContext


class FactoryPyReferenceExpression(astNode: ASTNode) : PyReferenceExpressionImpl(astNode) {
    override fun getReference(context: PyResolveContext): PsiPolyVariantReference {
        return when (val reference = super.getReference(context)) {
            is PyImportReference -> reference
            is PyQualifiedReference -> FactoryPyReference(this, context)
            else -> reference
        }
    }
}