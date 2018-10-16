package io.vrap.rmf.codegen.kt.types

abstract class LanguageBaseTypes(

        val objectType: VrapObjectType,
        val integerType: VrapObjectType,
        val booleanType: VrapObjectType,
        val doubleType : VrapObjectType,
        val dateTimeType: VrapObjectType,
        val dateOnlyType: VrapObjectType,
        val timeOnlyType: VrapObjectType,
        val stringType: VrapObjectType

)