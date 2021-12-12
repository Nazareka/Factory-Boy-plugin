package factory

import com.jetbrains.python.psi.AccessDirection
import com.jetbrains.python.psi.PyQualifiedExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.impl.ResolveResultList
import com.jetbrains.python.psi.impl.references.PyQualifiedReference
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.resolve.RatedResolveResult


class FactoryPyReference(
    element: PyQualifiedExpression,
    context: PyResolveContext
) : PyQualifiedReference(element, context) {
    override fun resolveInner(): List<RatedResolveResult> {
        val instanceRef = myElement.qualifier as? PyReferenceExpression ?: return super.resolveInner()

        val (_, modelClass) = FactoryUtils.getResolvedData(instanceRef) ?: return super.resolveInner()

        val modelType = myContext.typeEvalContext.getType(modelClass) ?: return super.resolveInner()
        val ctx = AccessDirection.of(myElement)
        val referencedName = myElement.referencedName ?: return super.resolveInner()
        val modelMembers = modelType.resolveMember(referencedName, null, ctx, myContext) ?: return super.resolveInner()
        val result = ResolveResultList()
        result.addAll(modelMembers)
        return result
      }
}