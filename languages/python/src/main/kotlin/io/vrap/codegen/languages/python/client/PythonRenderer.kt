/**
 *  Copyright 2021 Michael van Tellingen (Lab Digital)
 */
package io.vrap.codegen.languages.python.client
import io.vrap.codegen.languages.extensions.getMethodName
import io.vrap.codegen.languages.python.*
import io.vrap.codegen.languages.python.model.snakeCase
import io.vrap.rmf.raml.model.resources.ResourceContainer

fun ResourceContainer.subResources(clientName: String): String {
    return this.resources
        .map {
            var args = if (!it.relativeUri.variables.isNullOrEmpty()) {
                "self, " +
                    it.relativeUri.variables
                        .map { it.snakeCase() }
                        .map { "$it: str" }
                        .joinToString(separator = ", ")
            } else { "self" }

            val assignments =
                it.relativeUri.variables
                    .map { it.snakeCase() }
                    .map { "$it=$it," }
                    .plus(
                        (it.fullUri.variables.asList() - it.relativeUri.variables.asList())
                            .map { it.snakeCase() }
                            .map { "$it=self._$it," }
                    )
                    .joinToString(separator = "\n")

            """
            |def ${it.getMethodName().snakeCase()}($args) -\> ${it.toRequestBuilderName()}:
            |    <${it.toDocString()}>
            |    return ${it.toRequestBuilderName()}(
                    |${assignments.prependIndent(" ".repeat(8))}
            |        client=$clientName,
            |    )
            |
             """.trimMargin()
        }.joinToString(separator = "\n")
}
