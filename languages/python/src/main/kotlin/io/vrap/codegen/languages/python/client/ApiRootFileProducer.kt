/**
 *  Copyright 2021 Michael van Tellingen (Lab Digital)
 */
package io.vrap.codegen.languages.python.client

import com.google.inject.Inject
import io.vrap.codegen.languages.python.*
import io.vrap.codegen.languages.python.model.PyObjectTypeExtensions
import io.vrap.rmf.codegen.di.ClientPackageName
import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.codegen.rendring.FileProducer
import io.vrap.rmf.codegen.rendring.utils.keepIndentation
import io.vrap.rmf.codegen.types.VrapTypeProvider
import io.vrap.rmf.raml.model.modules.Api
import io.vrap.rmf.raml.model.resources.ResourceContainer

class ApiRootFileProducer @Inject constructor(
    @ClientPackageName val client_package: String,
    val clientConstants: ClientConstants,
    val api: Api,
    override val vrapTypeProvider: VrapTypeProvider
) : FileProducer, PyObjectTypeExtensions {

    override fun produceFiles(): List<TemplateFile> {

        return listOf(produceApiRoot(api))
    }

    fun produceApiRoot(type: Api): TemplateFile {
        return TemplateFile(
            relativePath = "client/__init__.py",
            content = """|
                |$pyGeneratedComment
                |${type.imports("client")}
                |from commercetools.client import BaseClient
                |
                |
                |class Client(BaseClient):
                |
                |    def __init__(self, *args, **kwargs):
                |        kwargs.setdefault("url", "${api.baseUri?.template}")
                |        super().__init__(self, **kwargs)
                |
                |<${type.subResources("self").prependIndent(" ".repeat(4))}>
                |
            """.trimMargin().keepIndentation()

        )
    }

    fun Api.imports(moduleName: String): String {
        return this.resources
            .map {
                it.tsRequestVrapType(client_package).createPythonVrapType()
            }
            .getImportsForModelVrapTypes(moduleName)
            .joinToString("\n")
    }

    fun ResourceContainer.ClientName(): String {
        return "self"
    }
}
