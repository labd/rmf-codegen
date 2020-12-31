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

class RootInitFileProducer @Inject constructor(
    @ClientPackageName val client_package: String,
    val clientConstants: ClientConstants,
    val api: Api,
    override val vrapTypeProvider: VrapTypeProvider
) : FileProducer, PyObjectTypeExtensions {

    override fun produceFiles(): List<TemplateFile> {

        return listOf(produceRootInitFile(api))
    }

    fun produceRootInitFile(type: Api): TemplateFile {
        return TemplateFile(
            relativePath = "__init__.py",
            content = """|
                |$pyGeneratedComment
                |from .client import Client
            """.trimMargin().keepIndentation()
        )
    }
}
