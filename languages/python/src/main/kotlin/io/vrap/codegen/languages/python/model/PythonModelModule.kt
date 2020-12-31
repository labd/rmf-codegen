/**
 *  Copyright 2021 Michael van Tellingen (Lab Digital)
 */
package io.vrap.codegen.languages.python.model

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import io.vrap.rmf.codegen.rendring.FileProducer

object PythonModelModule : AbstractModule() {
    override fun configure() {
        val objectTypeBinder = Multibinder.newSetBinder(binder(), FileProducer::class.java)
        objectTypeBinder.addBinding().to(PythonModuleRenderer::class.java)
        objectTypeBinder.addBinding().to(PythonSchemaRenderer::class.java)
        objectTypeBinder.addBinding().to(InitFileProducer::class.java)
        objectTypeBinder.addBinding().to(BaseFileProducer::class.java)
    }
}
