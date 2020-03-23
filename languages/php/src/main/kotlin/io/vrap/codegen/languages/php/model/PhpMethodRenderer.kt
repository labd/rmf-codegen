package io.vrap.codegen.languages.php.model

import com.google.common.net.MediaType
import com.google.inject.Inject
import io.vrap.codegen.languages.php.PhpSubTemplates
import io.vrap.codegen.languages.php.extensions.*
import io.vrap.rmf.codegen.di.BasePackageName
import io.vrap.rmf.codegen.di.ClientPackageName
import io.vrap.rmf.codegen.di.SharedPackageName
import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.codegen.rendring.MethodRenderer
import io.vrap.rmf.codegen.rendring.utils.escapeAll
import io.vrap.rmf.codegen.rendring.utils.keepAngleIndent
import io.vrap.rmf.codegen.types.VrapObjectType
import io.vrap.rmf.codegen.types.VrapTypeProvider
import io.vrap.rmf.raml.model.resources.Method
import io.vrap.rmf.raml.model.responses.Body
import io.vrap.rmf.raml.model.responses.Response
import io.vrap.rmf.raml.model.types.*
import io.vrap.rmf.raml.model.types.impl.TypesFactoryImpl
import org.eclipse.emf.ecore.EObject

class PhpMethodRenderer @Inject constructor(override val vrapTypeProvider: VrapTypeProvider) : MethodRenderer, EObjectTypeExtensions {

    @Inject
    @BasePackageName
    lateinit var basePackagePrefix:String

    @Inject
    @SharedPackageName
    lateinit var sharedPackageName: String

    @Inject
    @ClientPackageName
    lateinit var clientPackageName: String

    private val resourcePackage = "Resource"

    override fun render(type: Method): TemplateFile {
        val vrapType = vrapTypeProvider.doSwitch(type as EObject) as VrapObjectType

        val resultTypes = type.responses.filter { it.bodies.filter { body -> MediaType.JSON_UTF_8.`is`(body.contentMediaType) }.isNotEmpty() }
        val importTypes = resultTypes.map { response -> "use ${response.bodies.first { body -> MediaType.JSON_UTF_8.`is`(body.contentMediaType) }.returnType().returnTypeModelFullClass().escapeAll()};" }
                .plus(resultTypes.map { response -> "use ${response.bodies.first { body -> MediaType.JSON_UTF_8.`is`(body.contentMediaType) }.returnType().returnTypeFullClass().escapeAll()};" })
                .plus("use ${sharedPackageName.toNamespaceName()}\\Base\\JsonObject;".escapeAll())
                .plus("use ${sharedPackageName.toNamespaceName()}\\Base\\JsonObjectModel;".escapeAll())
                .distinct().sorted()
        val returnTypes = resultTypes.map { response -> response.bodies.first { body -> MediaType.JSON_UTF_8.`is`(body.contentMediaType) }.returnType().returnTypeClass() }
                .plus("JsonObject")
                .distinct().sorted()


        val content = """
            |<?php
            |${PhpSubTemplates.generatorInfo}
            |namespace ${clientPackageName.toNamespaceName().escapeAll()}\\$resourcePackage;
            |
            |use GuzzleHttp\\ClientInterface;
            |use GuzzleHttp\\Exception\\ServerException;
            |use GuzzleHttp\\Exception\\ClientException;
            |use ${sharedPackageName.toNamespaceName().escapeAll()}\\Exception\\InvalidArgumentException;
            |use ${sharedPackageName.toNamespaceName().escapeAll()}\\Exception\\ApiServerException;
            |use ${sharedPackageName.toNamespaceName().escapeAll()}\\Exception\\ApiClientException;
            |use ${sharedPackageName.toNamespaceName().escapeAll()}\\Client\\ApiRequest;
            |${importTypes.joinToString("\n")}
            |${if (type.firstBody()?.type is FileType) "use Psr\\Http\\Message\\UploadedFileInterface;".escapeAll() else ""}
            |use Psr\\Http\\Message\\ResponseInterface;
            |
            |/** @psalm-suppress PropertyNotSetInConstructor */
            |class ${type.toRequestName()} extends ApiRequest
            |{
            |    /**
            |     * @param ${if (type.firstBody()?.type is FileType) "?UploadedFileInterface " else "?object"} $!body
            |     * @psalm-param array<string, scalar|scalar[]> $!headers
            |     */
            |    public function __construct(${type.allParams()?.joinToString(separator = "") { "string $$it, " } ?: ""}${if (type.firstBody()?.type is FileType) "UploadedFileInterface " else ""}$!body = null, array $!headers = [], ClientInterface $!client = null)
            |    {
            |        $!uri = str_replace([${type.allParams()?.joinToString(separator = ", ") { "'{$it}'" } ?: ""}], [${type.allParams()?.joinToString(separator = ", ") { "$$it" } ?: ""}], '${type.apiResource().fullUri.template.trimStart('/')}');
            |        <<${type.firstBody()?.ensureContentType() ?: ""}>>
            |        <<${type.headers.filter { it.type?.default != null }.joinToString("\n\n") { "\$headers = \$this->ensureHeader(\$headers, '${it.name}', '${it.type.default.value}');" }}>>
            |        parent::__construct($!client, '${type.methodName.toUpperCase()}', $!uri, $!headers, ${type.firstBody()?.serialize()?: "!is_null(\$body) ? json_encode(\$body) : null"});
            |    }
            |
            |    /**
            |     * @template T of JsonObject
            |     * @psalm-param ?class-string<T> $!resultType
            |     * @return ${returnTypes.joinToString("|")}|T|null
            |     */
            |    public function mapFromResponse(?ResponseInterface $!response, string $!resultType = null)
            |    {
            |        if (is_null($!response)) {
            |            return null;
            |        }
            |        if (is_null($!resultType)) {
            |            switch ($!response->getStatusCode()) {${resultTypes.joinToString("") { response -> """
            |                case '${response.statusCode}':
            |                    $!resultType = ${response.bodies[0].returnType().returnTypeModelClass()}::class;
            |
            |                    break;""" }}
            |                default:
            |                    $!resultType = JsonObjectModel::class;
            |
            |                    break;
            |            }
            |        }
            |
            |        return $!resultType::of($!this->responseData($!response));
            |    }
            |
            |    /**
            |     * @template T of JsonObject
            |     * @psalm-param ?class-string<T> $!resultType
            |     *
            |     * @return null|${returnTypes.joinToString("|")}
            |     */
            |    public function execute(array $!options = [], string $!resultType = null)
            |    {
            |        try {
            |            $!response = $!this->send($!options);
            |        } catch (ServerException $!e) {
            |            $!result = $!this->mapFromResponse($!e->getResponse());
            |
            |            throw new ApiServerException($!e->getMessage(), $!result, $!this, $!e->getResponse(), $!e, []);
            |        } catch (ClientException $!e) {
            |            $!result = $!this->mapFromResponse($!e->getResponse());
            |
            |            throw new ApiClientException($!e->getMessage(), $!result, $!this, $!e->getResponse(), $!e, []);
            |        }
            |
            |        return $!this->mapFromResponse($!response, $!resultType);
            |    }
            |
            |    <<${type.queryParameters.joinToString("\n\n") { it.withParam(type) }}>>
            |}
        """.trimMargin().keepAngleIndent().forcedLiteralEscape()
        val relativeTypeNamespace = vrapType.`package`.toNamespaceName().replace(basePackagePrefix.toNamespaceName() + "\\", "").replace("\\", "/") + "/$resourcePackage"
        val relativePath = "src/" + relativeTypeNamespace + "/" + type.toRequestName() + ".php"
        return TemplateFile(
                relativePath = relativePath,
                content = content
        )
    }

    private fun Body.ensureContentType(): String {
        if (this.type !is FileType) {
            return ""
        }
        return """
            |if (!is_null($!body)) {
            |    $!mediaType = $!body->getClientMediaType();
            |    if (!is_null($!mediaType)) {
            |        $!headers = $!this->ensureHeader($!headers, 'Content-Type', $!mediaType);
            |    }
            |}
        """.trimMargin()
    }
    private fun Body.serialize(): String {
        if (this.type is FileType) {
            return "!is_null(\$body) ? \$body->getStream() : null"
        }
        return "!is_null(\$body) ? json_encode(\$body) : null"
    }

    private fun Response.isSuccessfull(): Boolean = this.statusCode.toInt() in (200..299)

    private fun Body.returnType(): AnyType {
        return this.type
                ?: TypesFactoryImpl.eINSTANCE.createNilType()
    }

    private fun Method.returnType(): AnyType {
        return this.responses
                .filter { it.isSuccessfull() }
                .filter { it.bodies?.isNotEmpty() ?: false }
                .firstOrNull()
                ?.let { it.bodies[0].type }
                ?: TypesFactoryImpl.eINSTANCE.createNilType()
    }

    private fun AnyType.returnTypeClass(): String {
        val vrapType = this.toVrapType()
        if (vrapType.isScalar())
            return "JsonObject"
        return when (vrapType) {
            is VrapObjectType -> vrapType.simpleName()
            else -> "JsonObject"
        }
    }

    private fun AnyType.returnTypeModelClass(): String {
        val vrapType = this.toVrapType()
        if (vrapType.isScalar())
            return "JsonObjectModel"
        return when (vrapType) {
            is VrapObjectType -> vrapType.simpleName() + "Model"
            else -> "JsonObjectModel"
        }
    }

    private fun AnyType.returnTypeFullClass(): String {
        val vrapType = this.toVrapType()
        if (vrapType.isScalar())
            return "${sharedPackageName.toNamespaceName()}\\Base\\JsonObject"
        return when (vrapType) {
            is VrapObjectType -> vrapType.fullClassName()
            else -> "${sharedPackageName.toNamespaceName()}\\Base\\JsonObject"
        }
    }

    private fun AnyType.returnTypeModelFullClass(): String {
        val vrapType = this.toVrapType()
        if (vrapType.isScalar())
            return "${sharedPackageName.toNamespaceName()}\\Base\\JsonObjectModel"
        return when (vrapType) {
            is VrapObjectType -> vrapType.fullClassName() + "Model"
            else -> "${sharedPackageName.toNamespaceName()}\\Base\\JsonObjectModel"
        }
    }

//    fun getAllParamNames(): List<String>? {
//        val params = getAbsoluteUri().getComponents().stream()
//                .filter({ uriTemplatePart -> uriTemplatePart is Expression })
//                .flatMap({ uriTemplatePart -> (uriTemplatePart as Expression).varSpecs.stream().map<String>(Function<VarSpec, String> { it.getVariableName() }) })
//                .collect(Collectors.toList<String>())
//        return if (params.size > 0) {
//            params
//        } else null
//    }
//
//    fun getQueryParameters(): List<QueryParameter> {
//        return method.getQueryParameters().stream().filter({ parameter ->
//            val placeholderParam = parameter.getAnnotation("placeholderParam") == null
//            placeholderParam
//        }).collect(Collectors.toList<QueryParameter>())
//    }
//
//    fun ResourceCollection.methods(): String {
//        return this.resources
//                .flatMap { resource -> resource.methods.map { javaBody(resource, it) } }
//                .joinToString(separator = "\n\n")
//    }
//
//    fun javaBody(resource: Resource, method: Method): String {
//        val methodReturnType = vrapTypeProvider.doSwitch(method.retyurnType())
//        val body = """
//            |${method.toComment().escapeAll()}
//            |@Retryable(
//            |          value = { ConnectException.class },
//            |          maxAttemptsExpression = "#{${'$'}{retry.${method.method.name}.maxAttempts}}",
//            |          backoff = @Backoff(delayExpression = "#{1}", maxDelayExpression = "#{5}", multiplierExpression = "#{2}"))
//            |public ${methodReturnType.fullClassName().escapeAll()} ${method.method.name.toLowerCase()}(${methodParameters(resource, method)}) {
//            |
//            |    final Map\<String, Object\> parameters = new HashMap\<\>();
//            |
//            |    <${resource.allUriParameters.map { "parameters.put(\"${it.name}\",${it.name});" }.joinToString(separator = "\n")}>
//            |
//            |    <${method.mediaType().escapeAll()}>
//            |
//            |    final ParameterizedTypeReference\<${method.retyurnType().toVrapType().fullClassName().escapeAll()}\> type = new ParameterizedTypeReference\<${method.retyurnType().toVrapType().fullClassName().escapeAll()}\>() {};
//            |    final String fullUri = baseUri + "${resource.fullUri.template}";
//            |
//            |    return restTemplate.exchange(fullUri, HttpMethod.${method.method.name.toUpperCase()}, entity, type, parameters).getBody();
//            |
//            |}
//                """.trimMargin()
//        return body
//    }
//
//    fun methodParameters(resource: Resource, method: Method): String {
//
//        val paramList = resource.allUriParameters
//                .map { "final ${it.type.toVrapType().simpleName()} ${it.name}" }
//                .toMutableList()
//
//        if (method.bodies?.isNotEmpty() ?: false) {
//            paramList.add(method.bodies[0].type.toVrapType().fullClassName())
//        }
//        return paramList.joinToString(separator = ", ")
//    }
//
//    fun Method.mediaType(): String {
//        if (this.bodies?.isNotEmpty() ?: false) {
//            val result = """
//                |final HttpHeaders headers = new HttpHeaders();
//                |headers.setContentType(MediaType.parseMediaType(("${this.bodies[0].contentType}")));
//                |final HttpEntity<${this.bodies[0].type.toVrapType().fullClassName()}> entity = new HttpEntity<>(body, headers);
//        """.trimMargin()
//            return result
//
//        }
//        return "final HttpEntity<?> entity = null;"
//
//    }
//
//    fun Method.retyurnType(): AnyType {
//        return this.responses
//                .filter { it.isSuccessfull() }
//                .filter { it.bodies?.isNotEmpty() ?: false }
//                .first()
//                .let { it.bodies[0].type }
//                ?: TypesFactoryImpl.eINSTANCE.createNilType()
//    }
//
//    fun Method.returnTypeClass(): String {
//        return this.returnType().returnTypeClass();
//    }
//
//    fun Method.returnTypeModelClass(): String {
//        val vrapType = this.returnType().toVrapType()
//        if (vrapType.isScalar())
//            return "JsonObjectModel"
//        return when (vrapType) {
//            is VrapObjectType -> vrapType.simpleName() + "Model"
//            else -> "JsonObjectModel"
//        }
//    }
//
//    fun Method.returnTypeFullClass(): String {
//        val vrapType = this.returnType().toVrapType()
//        if (vrapType.isScalar())
//            return "${sharedPackageName.toNamespaceName()}\\Base\\JsonObject"
//        return when (vrapType) {
//            is VrapObjectType -> vrapType.fullClassName()
//            else -> "${sharedPackageName.toNamespaceName()}\\Base\\JsonObject"
//        }
//    }
//
//    fun Method.returnTypeModelFullClass(): String {
//        val vrapType = this.returnType().toVrapType()
//        if (vrapType.isScalar())
//            return "${sharedPackageName.toNamespaceName()}\\Base\\JsonObjectModel"
//        return when (vrapType) {
//            is VrapObjectType -> vrapType.fullClassName() + "Model"
//            else -> "${sharedPackageName.toNamespaceName()}\\Base\\JsonObjectModel"
//        }
//    }
//
//    fun <T> EObject.getParent(parentClass: Class<T>): T? {
//        if (this.eContainer() == null) {
//            return null
//        }
//        return if (parentClass.isInstance(this.eContainer())) {
//            this.eContainer() as T
//        } else this.eContainer().getParent(parentClass)
//    }
}
