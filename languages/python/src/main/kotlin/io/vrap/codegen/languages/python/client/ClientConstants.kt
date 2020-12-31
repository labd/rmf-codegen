/**
 *  Copyright 2021 Michael van Tellingen (Lab Digital)
 */
package io.vrap.codegen.languages.python.client

import com.google.inject.Inject
import io.vrap.rmf.codegen.di.BasePackageName
import io.vrap.rmf.codegen.di.ClientPackageName
import io.vrap.rmf.codegen.di.SharedPackageName
import io.vrap.rmf.raml.model.util.StringCaseFormat

class ClientConstants @Inject constructor(
    @SharedPackageName
    val sharedPackage: String,
    @ClientPackageName
    val clientPackage: String,
    @BasePackageName
    val basePackageName: String
) {

    val indexFile = "index"
}

private fun String.lowerCasePackage(): String {
    return this.split("/").map { StringCaseFormat.LOWER_HYPHEN_CASE.apply(it) }.joinToString(separator = "/")
}
