/**
 *  Copyright 2021 Michael van Tellingen (Lab Digital)
 */
package io.vrap.codegen.languages.python.client

import com.google.inject.Inject
import io.vrap.codegen.languages.extensions.isPatternProperty
import io.vrap.codegen.languages.extensions.resource
import io.vrap.codegen.languages.extensions.returnType
import io.vrap.codegen.languages.python.*
import io.vrap.codegen.languages.python.model.PyObjectTypeExtensions
import io.vrap.codegen.languages.python.model.pyTypeName
import io.vrap.codegen.languages.python.model.simplePyName
import io.vrap.codegen.languages.python.model.snakeCase
import io.vrap.codegen.languages.python.model.toRelativePackageName
import io.vrap.rmf.codegen.di.ClientPackageName
import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.codegen.rendring.ResourceRenderer
import io.vrap.rmf.codegen.rendring.utils.escapeAll
import io.vrap.rmf.codegen.rendring.utils.keepIndentation
import io.vrap.rmf.codegen.types.VrapArrayType
import io.vrap.rmf.codegen.types.VrapEnumType
import io.vrap.rmf.codegen.types.VrapObjectType
import io.vrap.rmf.codegen.types.VrapScalarType
import io.vrap.rmf.codegen.types.VrapType
import io.vrap.rmf.codegen.types.VrapTypeProvider
import io.vrap.rmf.raml.model.modules.Api
import io.vrap.rmf.raml.model.resources.Method
import io.vrap.rmf.raml.model.resources.Resource

class RequestBuilder @Inject constructor(
    @ClientPackageName val client_package: String,
    private val clientConstants: ClientConstants,
    val api: Api,
    override val vrapTypeProvider: VrapTypeProvider
) : ResourceRenderer, PyObjectTypeExtensions {

    override fun render(type: Resource): TemplateFile {

        val pkg = (type.toPythonVrapType() as VrapObjectType).`package`
        val module = type.pyRequestModuleName(pkg)
        val filename = module.replace(".", "/")

        val clientPath = "base_client".toRelativePackageName(module)

        return TemplateFile(
            relativePath = "$filename.py",
            content = """|
                |$pyGeneratedComment
                |${type.imports(type.pyRequestModuleName(pkg))}
                |
                |if typing.TYPE_CHECKING:
                |    from $clientPath import BaseClient
                |
                |
                |class ${type.toRequestBuilderName()}:
                |
                |    <${type.constructor()}>
                |    <${type.subResources("self._client")}>
                |    <${type.methods()}>
                |
            """.trimMargin().keepIndentation()
        )
    }

    protected fun Resource.constructor(): String {
        val pathArgs = if (!this.fullUri.variables.isEmpty()) {
            this
                .fullUri
                .variables
                .map { it.snakeCase() }
                .map { "$it: str" }
                .joinToString(separator = ", ")
        } else { "" }

        val pathTypes = if (!this.fullUri.variables.isEmpty()) {
            this
                .fullUri
                .variables
                .map { it.snakeCase() }
                .map { "_$it: str" }
                .joinToString(separator = "\n")
        } else { "" }

        val assignments = this.fullUri.variables
            .map { it.snakeCase() }
            .map { "self._$it = $it" }
            .joinToString(separator = "\n")

        return """
            |_client: "BaseClient"
            |$pathTypes
            |
            |def __init__(
            |    self,
            |    $pathArgs,
            |    client: "BaseClient",
            |):
            |${assignments.prependIndent("    ")}
            |    self._client = client
            |
            """.trimMargin()
    }

    protected fun Resource.methods(): String {
        return this.methods
            .map { renderMethod(it) }
            .joinToString(separator = "\n\n")
    }

    private fun renderMethod(method: Method): String {
        val methodKwargs = listOf("self")
            .plus(
                {
                    val bodyVrapType = method.vrapType()
                    if (bodyVrapType != null) "body: ${method.vrapType()?.pyTypeName()}" else ""
                }()
            )
            .plus("*")
            .plus(
                method.queryParameters
                    .map {
                        if (it.required)
                            if (it.isPatternProperty())
                                "${it.paramName()}: typing.Dict[str, ${it.type.toPythonVrapType().pyTypeName()}]"
                            else
                                "${it.name.snakeCase()}: ${it.type.toPythonVrapType().pyTypeName()}"
                        else if (it.isPatternProperty())
                            "${it.paramName()}: typing.Dict[str, ${it.type.toPythonVrapType().pyTypeName()}] = None"
                        else
                            "${it.name.snakeCase()}: ${it.type.toPythonVrapType().pyTypeName()} = None"
                    }
            )
            .plus("headers: typing.Dict[str, str] = None")
            .plus("options: typing.Dict[str, typing.Any] = None")
            .filter {
                it != ""
            }
            .joinToString(separator = ", ")

        val paramsKwarg = method.queryParameters
            .filter { !it.isPatternProperty() }
            .map {
                """"${it.name}": ${it.name.snakeCase()}"""
            }
            .joinToString(", ", prefix = "{", postfix = "}")

        val paramsPatternDicts = method.queryParameters
            .filter { it.isPatternProperty() }
            .map {
                "${it.paramName()} and params.update(${it.template()})"
            }
            .joinToString("\n")

        data class Key(val className: String, val success: Boolean)
        var idx = 0
        var returnTypeOptional = false
        val responseHandler = method.responses
            .map {
                val statusCode = it.statusCode
                if (it.bodies.isNotEmpty()) {
                    val vrap = it.bodies[0].type.toPythonVrapType()
                    vrap.simplePyName() to statusCode
                } else {
                    "None" to statusCode
                }
            }
            .groupBy {
                Key(it.first, (it.second.toInt() in (200..299)))
            }
            .mapValues {
                entry ->
                entry.value.map { it.second }
            }
            .map {
                var condition = if (it.value.size > 1)
                    it.value.joinToString(prefix = "in (", separator = ",", postfix = ")")
                else
                    "== ${it.value[0]}"
                val stmt = "${if (idx == 0) "if" else "elif"} response.status_code $condition"
                idx++ // Should use mapIndexed

                if (it.key.success) {
                    if (it.key.className == "None") {
                        returnTypeOptional = true
                        """
                            |$stmt:
                            |    return None
                            """.trimMargin()
                    } else {
                        """
                            |$stmt:
                            |    return ${it.key.className}.deserialize(response.json())
                            """.trimMargin()
                    }
                } else {
                    if (it.key.className == "None") {
                        returnTypeOptional = true
                        """
                            |$stmt:
                            |    return None
                            """.trimMargin()
                    } else {
                        """
                            |$stmt:
                            |    obj = ${it.key.className}.deserialize(response.json())
                            |    raise self._client._create_exception(obj, response)
                            """.trimMargin()
                    }
                }
            }.joinToString("\n")

        var methodReturn = "${method.returnType().toPythonVrapType().pyTypeName()}"
        if (returnTypeOptional) {
            methodReturn = "typing.Optional[$methodReturn]"
        }
        val endpoint = transformUriTemplate(method.resource().fullUri.template)

        var bodyExpr = ""
        val bodyVrapType = method.vrapType()
        if (bodyVrapType is VrapScalarType && bodyVrapType.scalarType == "typing.BinaryIO") {
            bodyExpr = "data=body.read(),"
        } else if (bodyVrapType is VrapObjectType) {
            bodyExpr = "json=body.serialize(),"
        }

        return """
        |def ${method.methodName}(<$methodKwargs>) -\> $methodReturn:
        |    <${method.toDocString().escapeAll()}>
        |    <${if (paramsPatternDicts.isNotEmpty()) "params = $paramsKwarg" else ""}>
        |<${paramsPatternDicts.prependIndent(" ".repeat(4))}>
        |    headers = {} if headers is None else headers
        |    response = self._client._${method.methodName.toLowerCase()}(
        |        endpoint=f'$endpoint',
        |        <${if (paramsPatternDicts.isEmpty()) "params=$paramsKwarg" else "params=params"}>,
        |        <$bodyExpr>
        |        headers=<${if (method.pyMediaType().isNotEmpty()) "{${method.pyMediaType()}, **headers}" else "headers"}>,
        |        options=options
        |    )
        |${responseHandler.prependIndent("    ")}
        |    raise ValueError("Unhandled status code %s", response.status_code)
        """.trimMargin()
    }

    fun transformUriTemplate(template: String): String {
        val regex = "\\{([^}]+)}".toRegex()
        val matches = regex.findAll(template)

        var result = template
        matches.map { it.groupValues[1] }.forEach {
            result = result.replace("{$it}", "{self._${it.snakeCase()}}")
        }
        return result
    }

    fun Method.vrapType(): VrapType? {
        if (bodies.isNotEmpty()) {
            return bodies[0].type.toPythonVrapType()
        }
        return null
    }

    fun Resource.imports(moduleName: String): String {
        return this.resources
            .map {
                it.tsRequestVrapType(client_package).createPythonVrapType()
            }.plus(
                this.methods
                    .flatMap { method ->
                        method.bodies
                            .plus(
                                method.queryParameters
                            )
                    }
                    .filter { it.type != null }
                    .map { it.type.toPythonVrapType() }
                    .filter { it is VrapEnumType || it is VrapObjectType || (it is VrapArrayType && it.itemType is VrapObjectType) }
            )
            .plus(
                this.methods
                    .flatMap {
                        it.responses
                    }
                    .filter {
                        it.bodies.isNotEmpty()
                    }
                    .map {
                        it.bodies[0].type.toPythonVrapType()
                    }
            )
            .getImportsForModelVrapTypes(moduleName)
            .plus("import typing")
            .joinToString("\n")
    }
}
