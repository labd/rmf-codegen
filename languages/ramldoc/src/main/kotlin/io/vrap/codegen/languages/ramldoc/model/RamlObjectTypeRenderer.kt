package io.vrap.codegen.languages.ramldoc.model

import com.google.inject.Inject
import io.vrap.codegen.languages.extensions.ExtensionsBase
import io.vrap.codegen.languages.extensions.discriminatorProperty
import io.vrap.codegen.languages.ramldoc.extensions.packageDir
import io.vrap.codegen.languages.ramldoc.extensions.renderType
import io.vrap.rmf.codegen.di.ModelPackageName
import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.codegen.rendring.ObjectTypeRenderer
import io.vrap.rmf.codegen.rendring.utils.keepIndentation
import io.vrap.rmf.codegen.types.VrapObjectType
import io.vrap.rmf.codegen.types.VrapType
import io.vrap.rmf.codegen.types.VrapTypeProvider
import io.vrap.rmf.raml.model.types.*
import io.vrap.rmf.raml.model.types.impl.ExampleImpl
import io.vrap.rmf.raml.model.types.impl.StringInstanceImpl

class RamlObjectTypeRenderer @Inject constructor(override val vrapTypeProvider: VrapTypeProvider) : ExtensionsBase, ObjectTypeRenderer {

    @Inject
    @ModelPackageName
    lateinit var modelPackageName: String

    override fun render(type: ObjectType): TemplateFile {
        val vrapType = vrapTypeProvider.doSwitch(type) as VrapObjectType
        val properties = if (type.discriminatorValue != null) {
            if (type.properties.any { property -> property.name == type.discriminatorProperty()?.name })
                type.properties
            else
                listOf<Property>().plus(type.discriminatorProperty()).plus(type.properties).filterNotNull()
        } else {
            type.allProperties
        }

        val postmanExampleAnno = type.getAnnotation("postman-example")
        val postmanExample = if (postmanExampleAnno != null) {
            val example = TypesFactory.eINSTANCE.createExample()
            val boolInstance = TypesFactory.eINSTANCE.createBooleanInstance()
            boolInstance.value = true
            example.name = "postman"
            example.strict = boolInstance
            example
        } else {
            null
        }
        val examples = type.examples.plus(postmanExample).filterNotNull().sortedWith(compareBy { it.name })

        val content = """
            |#%RAML 1.0 DataType
            |displayName: ${type.displayName?.value ?: vrapType.simpleClassName}
            |type: ${type.type?.name ?: "object"}
            |(builtinType): object${if (type.discriminator != null) """
            |discriminator: ${type.discriminatorProperty()?.name}""" else ""}${if (type.discriminatorValue.isNullOrBlank().not()) """
            |discriminatorValue: ${type.discriminatorValue}""" else ""}${if (type.subTypes.filterNot { it.isInlineType }.isNotEmpty()) """
            |(oneOf):
            |${type.subTypes.filterNot { it.isInlineType }.sortedWith(compareBy { it.name }).joinToString("\n") { "- ${it.name}" }}""" else ""}${if (examples.isNotEmpty()) """
            |examples:
            |  <<${examples.joinToString("\n") { renderExample(vrapType, it) }}>>""" else ""}${if (type.description?.value != null) """
            |description: |-
            |  <<${type.description.value.trim()}>>""" else ""}
            |properties:
            |  <<${properties.joinToString("\n") { renderProperty(type, it) }}>>
            """.trimMargin().keepIndentation("<<", ">>")

        return TemplateFile(
                relativePath = "types/" + vrapType.packageDir(modelPackageName) + vrapType.simpleClassName + ".raml",
                content = content
        )
    }

    private fun renderExample(type: VrapObjectType, example: Example): String {
        val t = if (type.packageDir(modelPackageName).isNotEmpty()) "../.." else ".."
        val exampleName = "${t}/examples/" + type.packageDir(modelPackageName) + type.simpleClassName + "-${if (example.name.isNotEmpty()) example.name else "default"}.json"
        return """
            |${if (example.name.isNotEmpty()) example.name else "default"}: !include $exampleName
        """.trimMargin()
    }

//    private fun renderDiscriminatorTypeAsUnion(type: ObjectType): String {
//        val vrapType = vrapTypeProvider.doSwitch(type) as VrapObjectType
//        return """
//            |#%RAML 1.0 DataType
//            |displayName: ${type.displayName?.value ?: vrapType.simpleClassName}
//            |(builtinType): object
//            |type: ${type.subTypes.filterNot { it.isInlineType }.joinToString(" | ") { it.name }}${if (type.discriminatorProperty() != null) """
//            |(oneOf):
//            |${type.subTypes.filterNot { it.isInlineType }.joinToString("\n") { "- ${it.name}" }}""" else ""}
//        """.trimMargin().keepIndentation("<<", ">>")
//    }

    private fun renderProperty(type: ObjectType, property: Property): String {
        val discriminatorProp = type.discriminatorProperty()?.name
        val discriminatorValue = type.discriminatorValue
        return """
            |${property.name}:
            |  <<${property.type.renderType()}>>${if (discriminatorProp == property.name && discriminatorValue.isNullOrBlank().not()) """
            |  enum:
            |  - $discriminatorValue""" else ""}${if (property.type.default != null) """
            |  default: ${property.type.default.value}""" else ""}
            |  required: ${property.required}
        """.trimMargin().keepIndentation("<<", ">>")
    }


}
