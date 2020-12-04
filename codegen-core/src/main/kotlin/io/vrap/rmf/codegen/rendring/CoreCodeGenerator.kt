package io.vrap.rmf.codegen.rendring

import com.google.inject.Inject
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.vrap.rmf.codegen.common.generator.core.ResourceCollection
import io.vrap.rmf.codegen.di.ApiGitHash
import io.vrap.rmf.codegen.di.EnumStringTypes
import io.vrap.rmf.codegen.di.NamedScalarTypes
import io.vrap.rmf.codegen.di.PatternStringTypes
import io.vrap.rmf.codegen.io.DataSink
import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.raml.model.resources.Method
import io.vrap.rmf.raml.model.resources.Resource
import io.vrap.rmf.raml.model.types.ObjectType
import io.vrap.rmf.raml.model.types.StringType
import io.vrap.rmf.raml.model.types.UnionType
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory

class CoreCodeGenerator @Inject constructor(val dataSink: DataSink,
                                            private val allObjectTypes: MutableList<ObjectType>,
                                            private val allUnionTypes: MutableList<UnionType>,
                                            @EnumStringTypes private val allEnumStringTypes : MutableList<StringType>,
                                            @PatternStringTypes private val allPatternStringTypes : MutableList<StringType>,
                                            @NamedScalarTypes private val allNamedScalarTypes: MutableList<StringType>,
                                            private val allResourceCollections: MutableList<ResourceCollection>,
                                            private val allResourceMethods: MutableList<Method>,
                                            private val allResources: MutableList<Resource>
                                            ) {

    private val LOGGER = LoggerFactory.getLogger(CoreCodeGenerator::class.java)
    private val PARALLELISM = 100

    @Inject(optional = true)
    lateinit var generators: MutableSet<CodeGenerator>

    @Inject(optional = true)
    lateinit var objectTypeGenerators: MutableSet<ObjectTypeRenderer>

    @Inject(optional = true)
    lateinit var unionTypeGenerators: MutableSet<UnionTypeRenderer>

    @Inject(optional = true)
    lateinit var enumStringTypeGenerators: MutableSet<StringTypeRenderer>

    @Inject(optional = true)
    lateinit var patternStringTypeGenerators: MutableSet<PatternStringTypeRenderer>

    @Inject(optional = true)
    lateinit var namedScalarTypeGenerators: MutableSet<NamedScalarTypeRenderer>

    @Inject(optional = true)
    lateinit var allResourcesGenerators: MutableSet<ResourceCollectionRenderer>

    @Inject(optional = true)
    lateinit var allResourceMethodGenerators: MutableSet<MethodRenderer>

    @Inject(optional = true)
    lateinit var allResourceGenerators: MutableSet<ResourceRenderer>

    @Inject(optional = true)
    lateinit var fileProducers: MutableSet<FileProducer>

    @Inject(optional = true)
    @ApiGitHash
    lateinit var gitHash: String

    fun generate() {

        if(dataSink.clean()){
            LOGGER.info("data sink cleanup successful")
        } else {
            LOGGER.info("data sink cleanup unsuccessful")
        }

        val templateFiles :MutableList<Publisher<TemplateFile>> = mutableListOf()

        templateFiles.add(Flowable.just(TemplateFile( relativePath = "gen.properties", content = """
            hash=${gitHash}
        """.trimIndent())))

        templateFiles.addAll(generators.flatMap { generator -> generator.generate() })

        Flowable.concat(templateFiles)
                .observeOn(Schedulers.io())
                .parallel(PARALLELISM)
                .map { dataSink.write(it) }
                .sequential()
                .blockingSubscribe(
                        {},
                        { error -> LOGGER.error("Error occured while generating files",error)}
                )

        dataSink.postClean()

        LOGGER.info("files generation ended")
    }

}
