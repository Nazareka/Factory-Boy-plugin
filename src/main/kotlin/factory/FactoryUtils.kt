package factory

import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.stubs.PyClassNameIndexInsensitive

class FactoryUtils {
    data class ResolvedData(val project: Project, val modelClass: PyClass)

    companion object {
        fun getResolvedData(instanceRef: PyReferenceExpression): ResolvedData? {
            val factoryCallExp = PyResolveUtil.fullResolveLocally(instanceRef) ?: return null

            val factoryRef = factoryCallExp.firstChild as? PyReferenceExpression ?: return null

            val factoryClass = factoryRef.reference.resolve() as? PyClass ?: return null

            val factoryClassName = factoryClass.name ?: return null
            if (!factoryClassName.endsWith("Factory")) return null

            val modelName = factoryClassName.replace("Factory", "")
            val project = factoryClass.project
            val modelClass = PyClassNameIndexInsensitive.find(modelName, factoryClass.project).firstOrNull() ?: return null

            return ResolvedData(project, modelClass)
        }
    }
}