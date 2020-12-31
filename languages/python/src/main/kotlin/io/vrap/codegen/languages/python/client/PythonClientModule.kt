/**
 *  Copyright 2021 Michael van Tellingen (Lab Digital)
 */
package io.vrap.codegen.languages.python.client

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import io.vrap.rmf.codegen.rendring.FileProducer
import io.vrap.rmf.codegen.rendring.ResourceRenderer

object PythonClientModule : AbstractModule() {
    override fun configure() {
        Multibinder.newSetBinder(binder(), ResourceRenderer::class.java).addBinding().to(RequestBuilder::class.java)
        Multibinder.newSetBinder(binder(), ResourceRenderer::class.java).addBinding().to(RequestBuilderInit::class.java)
        Multibinder.newSetBinder(binder(), FileProducer::class.java).addBinding().to(ApiRootFileProducer::class.java)
        Multibinder.newSetBinder(binder(), FileProducer::class.java).addBinding().to(RootInitFileProducer::class.java)
    }
}
