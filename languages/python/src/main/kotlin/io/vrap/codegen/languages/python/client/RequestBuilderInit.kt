/**
 *  Copyright 2021 Michael van Tellingen (Lab Digital)
 */
package io.vrap.codegen.languages.python.client

import com.google.inject.Inject
import io.vrap.codegen.languages.python.*
import io.vrap.codegen.languages.python.model.PyObjectTypeExtensions
import io.vrap.rmf.codegen.di.ClientPackageName
import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.codegen.rendring.ResourceRenderer
import io.vrap.rmf.codegen.types.VrapObjectType
import io.vrap.rmf.codegen.types.VrapTypeProvider
import io.vrap.rmf.raml.model.modules.Api
import io.vrap.rmf.raml.model.resources.Resource

class RequestBuilderInit @Inject constructor(
    @ClientPackageName val client_package: String,
    private val clientConstants: ClientConstants,
    val api: Api,
    override val vrapTypeProvider: VrapTypeProvider
) : ResourceRenderer, PyObjectTypeExtensions {

    override fun render(type: Resource): TemplateFile {

        val pkg = (type.toPythonVrapType() as VrapObjectType).`package`
        val filename = type
            .pyRequestModuleName(pkg)
            .split('.')
            .dropLast(1)
            .plus("__init__.py")
            .joinToString("/")

        return TemplateFile(relativePath = filename, content = "")
    }
}
