package io.vrap.codegen.languages.php.base

import com.damnhandy.uri.template.UriTemplate
import com.google.inject.Inject
import io.vrap.codegen.languages.php.PhpSubTemplates
import io.vrap.codegen.languages.php.extensions.*
import io.vrap.rmf.codegen.di.BasePackageName
import io.vrap.rmf.codegen.io.TemplateFile
import io.vrap.rmf.codegen.rendring.FileProducer
import io.vrap.rmf.codegen.rendring.utils.escapeAll
import io.vrap.rmf.raml.model.modules.Api
import io.vrap.rmf.raml.model.util.StringCaseFormat

class PhpBaseFileProducer @Inject constructor(val api: Api) : FileProducer {

    @Inject
    @BasePackageName
    lateinit var packagePrefix:String

    override fun produceFiles(): List<TemplateFile> = listOf(
            apiClientException(),
            apiRequest(),
            apiResource(),
            apiServerException(),
            authConfig(),
            baseException(),
            baseJsonObject(),
            baseNullable(),
            builder(),
            cachedProvider(),
            clientCredentials(),
            clientCredentialsConfig(),
            clientFactory(),
            collection(),
            composerJson(),
            config(),
            credentialTokenProvider(),
            dateTimeImmutableCollection(),
            invalidArgumentException(),
            jsonObject(),
            jsonObjectModel(),
            jsonObjectCollection(),
            mapperFactory(),
            mapperIterator(),
            mapperInterface(),
            mapperMap(),
            mapperSequence(),
            mapperScalarSequence(),
            middlewareFactory(),
            oauth2Handler(),
            oauthHandlerFactory(),
            PreAuthTokenProvider(),
            psalm(),
            resultMapper(),
            token(),
            tokenModel(),
            tokenProvider()
    )


    private fun collection(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/Collection.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |interface Collection
                    |{
                    |}
                """.trimMargin()
        )
    }

    private fun resultMapper(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/ResultMapper.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use ${packagePrefix.toNamespaceName()}\Exception\InvalidArgumentException;
                    |use Psr\Http\Message\ResponseInterface;
                    |use stdClass;
                    |use ReflectionClass;
                    |use ReflectionParameter;
                    |use ReflectionException;
                    |
                    |class ResultMapper
                    |{
                    |    /**
                    |     * @template T of JsonObject
                    |     * @psalm-param class-string<T> $!type
                    |     * @psalm-return T
                    |     */
                    |    public function mapResponseToClass(string $!type, ResponseInterface $!response)
                    |    {
                    |        return $!type::of($!this->responseData($!response));
                    |    }
                    |
                    |    /**
                    |     * @psalm-return stdClass
                    |     */
                    |    private function responseData(ResponseInterface $!response)
                    |    {
                    |        $!body = (string)$!response->getBody();
                    |        /** @psalm-var ?stdClass $!data */
                    |        $!data = json_decode($!body);
                    |        if (is_null($!data)) {
                    |           throw new InvalidArgumentException();
                    |        }
                    |        return $!data;
                    |    }
                    |    
                    |    /**
                    |     * @template T
                    |     * @psalm-param class-string<T> $!type
                    |     * @psalm-param array<string, mixed> $!data
                    |     * @psalm-return T
                    |     * @throws InvalidArgumentException
                    |     * @throws ReflectionException
                    |     */
                    |    public function mapToConstructor(string $!type, array $!data) {
                    |        $!typeClass = new ReflectionClass($!type);
                    |        $!constructor = $!typeClass->getConstructor();
                    |        if (is_null($!constructor)) {
                    |            throw new InvalidArgumentException();
                    |        }
                    |        $!params = $!constructor->getParameters();
                    |
                    |        /** @psalm-var array<int, mixed> $!args */
                    |        $!args = array_map(
                    |            function (ReflectionParameter $!param) use ($!data) {
                    |                return ($!data[$!param->name] ?? null);
                    |            },
                    |            $!params
                    |        );
                    |        return $!typeClass->newInstanceArgs($!args);
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun mapperInterface(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/MapperInterface.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use Psr\Http\Message\ResponseInterface;
                    |
                    |interface MapperInterface
                    |{
                    |    /**
                    |     * @template T
                    |     * @psalm-param class-string<T> $!type
                    |     * @psalm-return T
                    |     */
                    |    public function mapResponseToClass(string $!type, ResponseInterface $!response);
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun mapperFactory(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/MapperFactory.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use DateTime;
                    |use DateTimeImmutable;
                    |
                    |class MapperFactory
                    |{
                    |    public const TIME_FORMAT = "H:i:s.u";
                    |    public const DATE_FORMAT = "Y-m-d";
                    |    public const DATETIME_FORMAT = "Y-m-d?H:i:s.uT";
                    |
                    |    /**
                    |     * @psalm-return callable(mixed): ?string
                    |     */
                    |    public static function stringMapper() {
                    |       return
                    |           /** @psalm-param ?mixed $!data */
                    |           function ($!data): ?string {
                    |               if (is_null($!data)) {
                    |                   return null;
                    |               }
                    |               return (string)$!data;
                    |           };
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(?mixed): ?float
                    |     */
                    |    public static function numberMapper() {
                    |       return
                    |           /** @psalm-param ?mixed $!data */
                    |           function ($!data): ?float {
                    |               if (is_null($!data)) {
                    |                   return null;
                    |               }
                    |               return (float)$!data;
                    |           };
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(?mixed): ?int
                    |     */
                    |    public static function integerMapper() {
                    |       return
                    |           /** @psalm-param ?mixed $!data */
                    |           function ($!data): ?int {
                    |               if (is_null($!data)) {
                    |                   return null;
                    |               }
                    |               return (int)$!data;
                    |           };
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(?string): ?DateTimeImmutable
                    |     */
                    |    public static function dateTimeMapper(string $!format = self::DATETIME_FORMAT) {
                    |       return
                    |           /** @psalm-param ?string $!data */
                    |           function ($!data) use ($!format): ?DateTimeImmutable {
                    |               if (is_null($!data)) {
                    |                   return null;
                    |               }
                    |               $!date = DateTimeImmutable::createFromFormat($!format, $!data);
                    |               if ($!date === false) {
                    |                   return null;
                    |               }
                    |               return $!date;
                    |           };
                    |    }
                    |
                    |    /**
                    |     * @template T
                    |     * @psalm-return callable(?mixed): ?T
                    |     * @psalm-param class-string<T> $!className
                    |     */
                    |    public static function classMapper(string $!className) {
                    |       return
                    |           /**
                    |            * @psalm-param ?mixed $!data
                    |            * @psalm-return ?T
                    |            */
                    |           function ($!data) use ($!className): ?object {
                    |               if (is_null($!data)) {
                    |                   return null;
                    |               }
                    |               return new $!className($!data);
                    |           };
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun jsonObjectModel(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/JsonObjectModel.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use stdClass;
                    |
                    |class JsonObjectModel extends BaseJsonObject implements JsonObject
                    |{
                    |    /**
                    |     * @psalm-return scalar|array<int|string, mixed>|JsonObject|JsonObjectCollection|null
                    |     */
                    |    final public function get(string $!field)
                    |    {
                    |        $!data = $!this->raw($!field);
                    |        if ($!data instanceof stdClass) {
                    |            return JsonObjectModel::of($!data);
                    |        }
                    |        if (is_array($!data) && isset($!data[0]) && $!data[0] instanceof stdClass) {
                    |            /** @psalm-var ?array<int, stdClass> $!data */
                    |            return new JsonObjectCollection($!data);
                    |        }
                    |        return $!data;
                    |    }
                    |
                    |    final protected function toArray(): array
                    |    {
                    |        $!data = array_filter(
                    |            get_object_vars($!this),
                    |            /**
                    |             * @psalm-param mixed|null $!value
                    |             * @return bool
                    |             */
                    |            function($!value) {
                    |                return !is_null($!value);
                    |            },
                    |            ARRAY_FILTER_USE_BOTH
                    |        );
                    |        $!data = array_merge($!this->getRawDataArray(), $!data);
                    |        return $!data;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun jsonObject(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/JsonObject.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use stdClass;
                    |
                    |interface JsonObject extends \JsonSerializable
                    |{
                    |    /**
                    |     * @psalm-return scalar|array<int|string, mixed>|JsonObject|JsonObjectCollection|null
                    |     */
                    |    public function get(string $!field);
                    |    
                    |    /**
                    |     * @psalm-param stdClass|array<string, mixed>|null $!data
                    |     * @psalm-return static
                    |     */
                    |    public static function of($!data = null);
                    |
                    |    /**
                    |     * @psalm-param array<string, mixed> $!data
                    |     * @psalm-return static
                    |     */
                    |    public static function fromArray(array $!data = []);
                    |
                    |    /**
                    |     * @psalm-param ?stdClass $!data
                    |     * @psalm-return static
                    |     */
                    |    public static function fromStdClass(stdClass $!data = null);
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun baseJsonObject(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/BaseJsonObject.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use stdClass;
                    |
                    |abstract class BaseJsonObject implements JsonObject
                    |{
                    |    /** @psalm-var ?stdClass */
                    |    private $!rawData;
                    |
                    |    /**
                    |     * @psalm-param ?stdClass|array<string, mixed> $!data
                    |     * @return static
                    |     */
                    |    final public static function of($!data = null)
                    |    {
                    |        if (is_array($!data)) {
                    |            return self::fromArray($!data);
                    |        }
                    |        return self::fromStdClass($!data);
                    |    }
                    |
                    |    /**
                    |     * @psalm-param ?stdClass $!data
                    |     * @return static
                    |     */
                    |    final public static function fromStdClass(stdClass $!data = null)
                    |    {
                    |        $!t = new static();
                    |        $!t->rawData = $!data;
                    |        return $!t;
                    |    }
                    |
                    |    /**
                    |     * @psalm-param array<string, mixed> $!data
                    |     * @return static
                    |     */
                    |    final public static function fromArray(array $!data = [])
                    |    {
                    |        return static::of((object)$!data);
                    |    }
                    |
                    |    /**
                    |     * @psalm-return scalar|array<int, mixed>|array<string, mixed>|stdClass|null
                    |     */
                    |    final protected function raw(string $!field)
                    |    {
                    |        if (isset($!this->rawData->$!field)) {
                    |            /**
                    |             * @psalm-suppress PossiblyNullPropertyFetch
                    |             * @psalm-var scalar|array<int, mixed>|array<string, mixed>|stdClass|null
                    |             */
                    |            return $!this->rawData->$!field;
                    |        }
                    |        return null;
                    |    }
                    |
                    |    public function jsonSerialize()
                    |    {
                    |        return (object)$!this->toArray();
                    |    }
                    |
                    |    /**
                    |     * @return array
                    |     */
                    |    final protected function getRawDataArray(): array
                    |    {
                    |        if (is_null($!this->rawData)) {
                    |            return [];
                    |        }
                    |        return get_object_vars($!this->rawData);
                    |    }
                    |
                    |    /**
                    |     * @return array
                    |     */
                    |    abstract protected function toArray(): array;
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun builder(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/Builder.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |/**
                    | * @template T
                    | */
                    |interface Builder
                    |{
                    |    /**
                    |     * @psalm-return T
                    |     */
                    |    public function build();
                    |}
                """.trimMargin())
    }

    private fun baseNullable(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/Nullable.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |interface Nullable
                    |{
                    |    public function isPresent(): bool;
                    |}
                """.trimMargin())
    }

    private fun clientFactory(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/ClientFactory.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use ${packagePrefix.toNamespaceName()}\Exception\InvalidArgumentException;
                    |use GuzzleHttp\Client as HttpClient;
                    |use GuzzleHttp\ClientInterface;
                    |use GuzzleHttp\HandlerStack;
                    |use Psr\Log\LoggerInterface;
                    |
                    |class ClientFactory
                    |{
                    |    /**
                    |     * @psalm-param array<string, callable> $!middlewares
                    |     * @throws InvalidArgumentException
                    |     */
                    |    public function createGuzzleClient(Config $!config, ?AuthConfig $!authConfig = null, ?LoggerInterface $!logger = null, array $!middlewares = []): ClientInterface
                    |    {
                    |        $!handler = null;
                    |        if (!is_null($!authConfig)) {
                    |            $!handler = OAuthHandlerFactory::ofAuthConfig($!authConfig);
                    |        }
                    |
                    |        return $!this->createGuzzleClientForHandler($!config, $!handler, $!logger, $!middlewares);
                    |    }
                    |
                    |    /**
                    |     * @psalm-param array<string, callable> $!middlewares
                    |     * @throws InvalidArgumentException
                    |     */
                    |    public function createGuzzleClientForHandler(Config $!config, ?OAuth2Handler $!handler = null, ?LoggerInterface $!logger = null, array $!middlewares = []): ClientInterface
                    |    {
                    |        $!middlewares = array_merge(
                    |           MiddlewareFactory::createDefaultMiddlewares($!handler, $!logger, (int) ($!config->getOptions()['maxRetries'] ?? 0)),
                    |           $!middlewares
                    |        );
                    |        return $!this->createGuzzleClientWithOptions($!config->getOptions(), $!middlewares);
                    |    }
                    |
                    |    /**
                    |     * @psalm-param array<string, callable> $!middlewares
                    |     * @throws InvalidArgumentException
                    |     */
                    |    public function createGuzzleClientForMiddlewares(
                    |       Config $!config,
                    |       array $!middlewares = []): ClientInterface
                    |    {
                    |        return $!this->createGuzzleClientWithOptions($!config->getOptions(), $!middlewares);
                    |    }
                    |
                    |    /**
                    |     * @throws InvalidArgumentException
                    |     * @psalm-param array<int|string, callable> $!middlewares
                    |     */
                    |    private function createGuzzleClientWithOptions(array $!options, array $!middlewares = []): ClientInterface
                    |    {
                    |        if (isset($!options['handler']) && $!options['handler'] instanceof HandlerStack) {
                    |            $!stack = $!options['handler'];
                    |        } else {
                    |            $!stack = HandlerStack::create();
                    |            $!options['handler'] = $!stack;
                    |        }
                    |
                    |        $!options = array_replace(
                    |            [
                    |                'allow_redirects' => false,
                    |                'verify' => true,
                    |                'timeout' => 60,
                    |                'connect_timeout' => 10,
                    |                'pool_size' => 25
                    |            ],
                    |            $!options
                    |        );
                    |        foreach ($!middlewares as $!key => $!middleware) {
                    |            if(!is_callable($!middleware)) {
                    |                throw new InvalidArgumentException('Middleware isn\'t callable');
                    |            }
                    |            $!name = is_numeric($!key) ? '' : $!key;
                    |            $!stack->push($!middleware, $!name);
                    |        }
                    |
                    |        $!client = new HttpClient($!options);
                    |
                    |        return $!client;
                    |    }
                    |
                    |    public static function of(): ClientFactory
                    |    {
                    |        return new self();
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun tokenProvider(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/TokenProvider.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |interface TokenProvider
                    |{
                    |    public function getToken(): Token;
                    |
                    |    public function refreshToken(): Token;
                    |}
                """.trimMargin()
        )
    }

    private fun token(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/Token.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |interface Token
                    |{
                    |    public function getValue(): string;
                    |
                    |    public function getExpiresIn(): int;
                    |}
                """.trimMargin()
        )
    }

    private fun composerJson(): TemplateFile {
        return TemplateFile(relativePath = "composer.json",
                content = """
                    |{
                    |  "name": "${packagePrefix.toLowerCase()}/spec-base",
                    |  "license": "MIT",
                    |  "type": "library",
                    |  "description": "",
                    |  "autoload": {
                    |    "psr-4": {
                    |      "${packagePrefix.toNamespaceName().escapeAll()}\\": [
                    |        "src/"
                    |      ]
                    |    }
                    |  },
                    |  "autoload-dev": {
                    |    "psr-4": {
                    |      "${packagePrefix.toNamespaceName().escapeAll()}\\Test\\": [
                    |        "test/unit/${packagePrefix.toNamespaceDir()}"
                    |      ]
                    |    }
                    |  },
                    |  "require": {
                    |    "php": ">=7.2",
                    |    "ext-json": "*",
                    |    "guzzlehttp/psr7": "^1.1",
                    |    "guzzlehttp/guzzle": "^6.0",
                    |    "psr/cache": "^1.0",
                    |    "psr/simple-cache": "^1.0",
                    |    "psr/log": "^1.0",
                    |    "psr/http-client": "^1.0",
                    |    "psr/http-message": "^1.0",
                    |    "cache/filesystem-adapter": "^1.0"
                    |  },
                    |  "require-dev": {
                    |    "monolog/monolog": "^1.3",
                    |    "phpunit/phpunit": "^8.0",
                    |    "vimeo/psalm": "^3.4",
                    |    "cache/array-adapter": "^1.0",
                    |    "squizlabs/php_codesniffer": "^3.0"
                    |  }
                    |}
                """.trimMargin())
    }

    private fun PreAuthTokenProvider(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/PreAuthTokenProvider.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |class PreAuthTokenProvider implements TokenProvider
                    |{
                    |    public const TOKEN = 'token';
                    |
                    |    /** @psalm-var Token */
                    |    private $!token;
                    |
                    |    public function __construct(Token $!token)
                    |    {
                    |        $!this->token = $!token;
                    |    }
                    |
                    |    public function getToken(): Token
                    |    {
                    |        return $!this->token;
                    |    }
                    |
                    |    public function refreshToken(): Token
                    |    {
                    |        return $!this->token;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun tokenModel(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/TokenModel.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |class TokenModel implements Token
                    |{
                    |    /**
                    |     * @psalm-var string
                    |     * @readonly
                    |     */
                    |    private $!value;
                    |
                    |    /**
                    |     * @psalm-var int
                    |     * @readonly
                    |     */
                    |    private $!expiresIn;
                    |
                    |    public function __construct(string $!value, int $!expiresIn = null)
                    |    {
                    |        $!this->value = $!value;
                    |        $!this->expiresIn = $!expiresIn ?? 0;
                    |    }
                    |
                    |    public function getValue(): string
                    |    {
                    |        return $!this->value;
                    |    }
                    |
                    |    public function getExpiresIn(): int
                    |    {
                    |        return $!this->expiresIn;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun oauthHandlerFactory(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/OAuthHandlerFactory.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use ${packagePrefix.toNamespaceName()}\Exception\InvalidArgumentException;
                    |use Cache\Adapter\Filesystem\FilesystemCachePool;
                    |use GuzzleHttp\Client;
                    |use League\Flysystem\Adapter\Local;
                    |use League\Flysystem\Filesystem;
                    |use Psr\Cache\CacheItemPoolInterface;
                    |use Psr\SimpleCache\CacheInterface;
                    |
                    |class OAuthHandlerFactory
                    |{
                    |    /**
                    |     * @psalm-param CacheItemPoolInterface|CacheInterface|null $!cache
                    |     * @psalm-return CacheItemPoolInterface|CacheInterface
                    |     */
                    |    private static function validateCache($!cache = null)
                    |    {
                    |        if ($!cache instanceof CacheItemPoolInterface || $!cache instanceof CacheInterface) {
                    |            return $!cache;
                    |        }
                    |
                    |        $!filesystemAdapter = new Local(getcwd());
                    |        $!filesystem        = new Filesystem($!filesystemAdapter);
                    |        $!cache = new FilesystemCachePool($!filesystem);
                    |        
                    |        return $!cache;
                    |    }
                    |
                    |    /**
                    |     * @psalm-param CacheItemPoolInterface|CacheInterface|null $!cache
                    |     */
                    |    public static function ofAuthConfig(AuthConfig $!authConfig, $!cache = null): OAuth2Handler
                    |    {
                    |        $!cache = self::validateCache($!cache);
                    |        switch(true) {
                    |           case $!authConfig instanceof ClientCredentialsConfig:
                    |               $!provider = new CachedTokenProvider(
                    |                   new ClientCredentialTokenProvider(
                    |                       new Client($!authConfig->getOptions()),
                    |                       $!authConfig->getAuthUri(),
                    |                       $!authConfig->getCredentials()
                    |                   ),
                    |                   $!cache,
                    |                   $!authConfig->getCredentials()->getCacheKey()
                    |               );
                    |               break;
                    |           default:
                    |               throw new InvalidArgumentException('Unknown authorization configuration');
                    |
                    |        }
                    |        return self::ofProvider($!provider);
                    |    }
                    |
                    |    public static function ofProvider(TokenProvider $!provider): OAuth2Handler
                    |    {
                    |        return new OAuth2Handler($!provider);
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun baseException(): TemplateFile {
        return TemplateFile(relativePath = "src/Exception/BaseException.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Exception;
                    |
                    |use Exception;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |abstract class BaseException extends Exception
                    |{
                    |}
                """.trimMargin())
    }

    private fun invalidArgumentException(): TemplateFile {
        return TemplateFile(relativePath = "src/Exception/InvalidArgumentException.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Exception;
                    |
                    |use Exception;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |class InvalidArgumentException extends BaseException
                    |{
                    |}
                """.trimMargin())
    }

    private fun apiServerException(): TemplateFile {
        return TemplateFile(relativePath = "src/Exception/ApiServerException.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Exception;
                    |
                    |use GuzzleHttp\Exception\ServerException;
                    |use Psr\Http\Message\RequestInterface;
                    |use Psr\Http\Message\ResponseInterface;
                    |use ${packagePrefix.toNamespaceName()}\Base\JsonObject;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |class ApiServerException extends ServerException
                    |{
                    |    /**
                    |     * @var ?JsonObject
                    |     * @readonly
                    |     */
                    |    private $!result;
                    |    
                    |    /**
                    |     * @param string $!message
                    |     * @param ?JsonObject $!result
                    |     */
                    |    public function __construct($!message, $!result, RequestInterface $!request, ResponseInterface $!response, \Exception $!previous = null, array $!handlerContext = [])
                    |    {
                    |        $!this->result = $!result;
                    |        parent::__construct($!message, $!request, $!response, $!previous, $!handlerContext);
                    |    }
                    |
                    |    /**
                    |     * @return ?JsonObject
                    |     */
                    |    public function getResult()
                    |    {
                    |        return $!this->result;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun apiClientException(): TemplateFile {
        return TemplateFile(relativePath = "src/Exception/ApiClientException.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Exception;
                    |
                    |use GuzzleHttp\Exception\ClientException;
                    |use Psr\Http\Message\RequestInterface;
                    |use Psr\Http\Message\ResponseInterface;
                    |use ${packagePrefix.toNamespaceName()}\Base\JsonObject;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |class ApiClientException extends ClientException
                    |{
                    |    /**
                    |     * @var ?JsonObject
                    |     * @readonly
                    |     */
                    |    private $!result;
                    |    
                    |    /**
                    |     * @param string $!message
                    |     * @param ?JsonObject $!result
                    |     */
                    |    public function __construct($!message, $!result, RequestInterface $!request, ResponseInterface $!response, \Exception $!previous = null, array $!handlerContext = [])
                    |    {
                    |        $!this->result = $!result;
                    |        parent::__construct($!message, $!request, $!response, $!previous, $!handlerContext);
                    |    }
                    |    
                    |    /**
                    |     * @return ?JsonObject
                    |     */
                    |    public function getResult()
                    |    {
                    |        return $!this->result;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun apiRequest(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/ApiRequest.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use ${packagePrefix.toNamespaceName()}\Exception\InvalidArgumentException;
                    |use GuzzleHttp\ClientInterface;
                    |use GuzzleHttp\Exception\GuzzleException;
                    |use GuzzleHttp\Promise\PromiseInterface;
                    |use GuzzleHttp\Psr7;
                    |use GuzzleHttp\Psr7\Request;
                    |use Psr\Http\Message\ResponseInterface;
                    |
                    |/** @psalm-suppress PropertyNotSetInConstructor */
                    |class ApiRequest extends Request
                    |{
                    |    /** @psalm-var array<string, scalar[]> */
                    |    private $!queryParts;
                    |    /** @psalm-var string */
                    |    private $!query;
                    |
                    |    /**
                    |     * @psalm-var ClientInterface|null
                    |     * @readonly
                    |     */
                    |    private $!client;
                    |
                    |    /**
                    |     * @psalm-param array<string, scalar|scalar[]> $!headers
                    |     * @param string|null|resource|\Psr\Http\Message\StreamInterface $!body
                    |     */
                    |    public function __construct(?ClientInterface $!client, string $!method, string $!uri, array $!headers = [], $!body = null, string $!version = '1.1')
                    |    {
                    |        $!this->client = $!client;
                    |        $!headers = $!this->ensureHeader($!headers, 'Content-Type', 'application/json');
                    |
                    |        parent::__construct($!method, $!uri, $!headers, $!body, $!version);
                    |    }
                    |
                    |    /**
                    |     * @psalm-param array<string, scalar|scalar[]> $!headers
                    |     * @psalm-param string|string[] $!defaultValue
                    |     * @psalm-return array<string, scalar|scalar[]>
                    |     * @param array $!headers
                    |     * @param string $!header
                    |     * @param scalar|array $!defaultValue
                    |     * @return array
                    |     */
                    |    protected function ensureHeader(array $!headers, string $!header, $!defaultValue): array
                    |    {
                    |        $!normalizedHeader = strtolower($!header);
                    |        foreach ($!headers as $!headerName => $!value) {
                    |            $!normalized = strtolower($!headerName);
                    |            if ($!normalized !== $!normalizedHeader) {
                    |                continue;
                    |            }
                    |            return $!headers;
                    |        }
                    |        $!headers[$!header] = $!defaultValue;
                    |
                    |        return $!headers;
                    |    }
                    |
                    |    /**
                    |     * @param string $!parameterName
                    |     * @psalm-param scalar $!value
                    |     * @param mixed $!value
                    |     * @psalm-return static
                    |     */
                    |    public function withQueryParam(string $!parameterName, $!value): ApiRequest
                    |    {
                    |        $!query = $!this->getUri()->getQuery();
                    |        if ($!this->query !== $!query) {
                    |            /** @psalm-var array<string, scalar[]> */
                    |            $!this->queryParts = array_map(
                    |                /**
                    |                 * @psalm-param scalar|scalar[] $!value
                    |                 * @psalm-return scalar[]
                    |                 */
                    |                function($!value): array {
                    |                    if(is_array($!value)) {
                    |                        return $!value;
                    |                    }
                    |                    return [$!value];
                    |                },
                    |                Psr7\parse_query($!query)
                    |            );
                    |        }
                    |        $!this->queryParts[$!parameterName][] = $!value;
                    |        ksort($!this->queryParts);
                    |        $!this->query = Psr7\build_query($!this->queryParts);
                    |
                    |        return $!this->withUri($!this->getUri()->withQuery($!this->query));
                    |    }
                    |    
                    |    /**
                    |     * @param array $!options
                    |     * @throws InvalidArgumentException
                    |     * @throws GuzzleException
                    |     * @psalm-suppress InvalidThrow
                    |     */
                    |    public function send(array $!options = []): ResponseInterface
                    |    {
                    |        if (is_null($!this->client)) {
                    |           throw new InvalidArgumentException();
                    |        }
                    |        return $!this->client->send($!this, $!options);
                    |    }
                    |
                    |    /**
                    |     * @param array $!options
                    |     * @throws InvalidArgumentException
                    |     * @throws GuzzleException
                    |     * @psalm-suppress InvalidThrow
                    |     */
                    |    public function sendAsync(array $!options = []): PromiseInterface
                    |    {
                    |        if (is_null($!this->client)) {
                    |           throw new InvalidArgumentException();
                    |        }
                    |        return $!this->client->sendAsync($!this, $!options);
                    |    }
                    |
                    |    public function getClient(): ?ClientInterface
                    |    {
                    |       return $!this->client;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun mapperIterator(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/MapperIterator.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |class MapperIterator extends \IteratorIterator
                    |{
                    |    /**
                    |     * @var callable
                    |     */
                    |    private $!mapper;
                    |
                    |    public function __construct(\Traversable $!iterator, callable $!mapper)
                    |    {
                    |        parent::__construct($!iterator);
                    |        $!this->mapper = $!mapper;
                    |    }
                    |
                    |    public function current()
                    |    {
                    |        return call_user_func($!this->mapper, parent::current(), parent::key());
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun jsonObjectCollection(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/JsonObjectCollection.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use ${packagePrefix.toNamespaceName()}\Exception\InvalidArgumentException;
                    |
                    |/**
                    | * @extends MapperSequence<JsonObject>
                    | * @method JsonObject current()
                    | * @method JsonObject at($!offset)
                    | */
                    |class JsonObjectCollection extends MapperSequence
                    |{
                    |    /**
                    |     * @psalm-assert JsonObject $!value
                    |     * @psalm-param JsonObject|object $!value
                    |     * @return JsonObjectCollection
                    |     * @throws InvalidArgumentException
                    |     */
                    |    public function add($!value)
                    |    {
                    |        if (!$!value instanceof JsonObject) {
                    |            throw new InvalidArgumentException();
                    |        }
                    |        $!this->store($!value);
                    |
                    |        return $!this;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(int):?JsonObject
                    |     */
                    |    protected function mapper()
                    |    {
                    |        return function(int $!index): ?JsonObject {
                    |            $!data = $!this->get($!index);
                    |            if (!is_null($!data) && !$!data instanceof JsonObject) {
                    |                $!data = JsonObjectModel::of($!data);
                    |                $!this->set($!data, $!index);
                    |            }
                    |            return $!data;
                    |        };
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun dateTimeImmutableCollection(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/DateTimeImmutableCollection.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use DateTimeImmutable;
                    |use ${packagePrefix.toNamespaceName()}\Exception\InvalidArgumentException;
                    |
                    |/**
                    | * @extends MapperScalarSequence<DateTimeImmutable>
                    | * @method DateTimeImmutable current()
                    | * @method DateTimeImmutable at($!offset)
                    | */
                    |class DateTimeImmutableCollection extends MapperScalarSequence
                    |{
                    |    /**
                    |     * @psalm-assert DateTimeImmutable $!value
                    |     * @psalm-param DateTimeImmutable|scalar $!value
                    |     * @return DateTimeImmutableCollection
                    |     * @throws InvalidArgumentException
                    |     */
                    |    public function add($!value)
                    |    {
                    |        if (!$!value instanceof DateTimeImmutable) {
                    |            throw new InvalidArgumentException();
                    |        }
                    |        $!this->store($!value);
                    |
                    |        return $!this;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(int):?DateTimeImmutable
                    |     */
                    |    protected function mapper()
                    |    {
                    |        return function(int $!index): ?DateTimeImmutable {
                    |            $!data = $!this->get($!index);
                    |            if (!is_null($!data) && !$!data instanceof DateTimeImmutable) {
                    |                $!data = new DateTimeImmutable((string)$!data);
                    |                $!this->set($!data, $!index);
                    |            }
                    |            return $!data;
                    |        };
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun mapperSequence(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/MapperSequence.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use stdClass;
                    |
                    |/**
                    | * @template TObject
                    | */
                    |abstract class MapperSequence implements Collection, \ArrayAccess, \JsonSerializable, \IteratorAggregate
                    |{
                    |    /** @psalm-var ?array<int, TObject|stdClass> */
                    |    private $!data;
                    |    /** @psalm-var array<string, array<string, int>> */
                    |    private $!indexes = [];
                    |    /** @psalm-var MapperIterator */
                    |    private $!iterator;
                    |
                    |    /**
                    |     * @psalm-param ?array<int, TObject|stdClass> $!data
                    |     * @param array|null $!data
                    |     */
                    |    public function __construct(array $!data = null)
                    |    {
                    |        if (!is_null($!data)) {
                    |            $!this->index($!data);
                    |        }
                    |        $!this->data = $!data;
                    |        $!this->iterator = $!this->getIterator();
                    |    }
                    |
                    |    public function toArray(): ?array
                    |    {
                    |        return $!this->data;
                    |    }
                    |    
                    |    public function jsonSerialize(): ?array
                    |    {
                    |        return $!this->data;
                    |    }
                    |
                    |    /**
                    |     * @template T
                    |     * @psalm-param array<int, T|stdClass> $!data
                    |     * @return static
                    |     */
                    |    final public static function fromArray(array $!data)
                    |    {
                    |        return new static($!data);
                    |    }
                    |
                    |    /**
                    |     * @param mixed $!data
                    |     */
                    |    protected function index($!data): void
                    |    {
                    |    }
                    |
                    |    /**
                    |     * @psalm-return TObject|stdClass|null
                    |     */
                    |    final protected function get(int $!index)
                    |    {
                    |        if (isset($!this->data[$!index])) {
                    |            return $!this->data[$!index];
                    |        }
                    |        return null;
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TObject|stdClass $!data
                    |     */
                    |    final protected function set($!data, ?int $!index): void
                    |    {
                    |        if (is_null($!index)) {
                    |            $!this->data[] = $!data;
                    |        } else {
                    |            $!this->data[$!index] = $!data;
                    |        }
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TObject|stdClass $!value
                    |     * @param $!value
                    |     * @return Collection
                    |     */
                    |    public function add($!value)
                    |    {
                    |        return $!this->store($!value);
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TObject|stdClass $!value
                    |     * @param $!value
                    |     * @return Collection
                    |     */
                    |    final protected function store($!value)
                    |    {
                    |        $!this->set($!value, null);
                    |        $!this->iterator = $!this->getIterator();
                    |
                    |        return $!this;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return ?TObject
                    |     */
                    |    public function at(int $!index)
                    |    {
                    |        return $!this->mapper()($!index);
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(int): ?TObject
                    |     */
                    |    abstract protected function mapper();
                    |
                    |    /**
                    |     * @psalm-param TObject|object $!value
                    |     */
                    |    final protected function addToIndex(string $!field, string $!key, int $!index): void
                    |    {
                    |        $!this->indexes[$!field][$!key] = $!index;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return ?TObject
                    |     */
                    |    final protected function valueByKey(string $!field, string $!key)
                    |    {
                    |        return isset($!this->indexes[$!field][$!key]) ? $!this->at($!this->indexes[$!field][$!key]) : null;
                    |    }
                    |
                    |    public function getIterator(): MapperIterator
                    |    {
                    |        $!keys = !is_null($!this->data) ? array_keys($!this->data) : [];
                    |        $!keyIterator = new \ArrayIterator(array_combine($!keys, $!keys));
                    |        $!iterator = new MapperIterator(
                    |            $!keyIterator,
                    |            $!this->mapper()
                    |        );
                    |        $!iterator->rewind();
                    |
                    |        return $!iterator;
                    |    }
                    |
                    |    /**
                    |     * @return ?TObject
                    |     */
                    |    public function current()
                    |    {
                    |        /** @psalm-var ?TObject */
                    |        return $!this->iterator->current();
                    |    }
                    |
                    |    /**
                    |     * @return void
                    |     */
                    |    public function next()
                    |    {
                    |        $!this->iterator->next();
                    |    }
                    |
                    |    /**
                    |     * @return int
                    |     */
                    |    public function key()
                    |    {
                    |        /** @psalm-var int */
                    |        return $!this->iterator->key();
                    |    }
                    |
                    |    /**
                    |     * @return bool
                    |     */
                    |    public function valid()
                    |    {
                    |        return $!this->iterator->valid();
                    |    }
                    |
                    |    /**
                    |     * @return void
                    |     */
                    |    public function rewind()
                    |    {
                    |        $!this->iterator->rewind();
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @return bool
                    |     */
                    |    public function offsetExists($!offset)
                    |    {
                    |        return !is_null($!this->data) && array_key_exists($!offset, $!this->data);
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @return ?TObject
                    |     */
                    |    public function offsetGet($!offset)
                    |    {
                    |        return $!this->at($!offset);
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @psalm-param TObject|stdClass $!value
                    |     * @param mixed $!value
                    |     * @return void
                    |     */
                    |    public function offsetSet($!offset, $!value)
                    |    {
                    |        $!this->set($!value, $!offset);
                    |        $!this->iterator = $!this->getIterator();
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @return void
                    |     */
                    |    public function offsetUnset($!offset)
                    |    {
                    |        if ($!this->offsetExists($!offset)) {
                    |            /** @psalm-suppress PossiblyNullArrayAccess */
                    |            unset($!this->data[$!offset]);
                    |            $!this->iterator = $!this->getIterator();
                    |        }
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun mapperScalarSequence(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/MapperScalarSequence.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use stdClass;
                    |
                    |/**
                    | * @template TScalar
                    | */
                    |abstract class MapperScalarSequence implements Collection, \ArrayAccess, \JsonSerializable, \IteratorAggregate
                    |{
                    |    /** @psalm-var ?array<int, TScalar|scalar> */
                    |    private $!data;
                    |    /** @psalm-var array<string, array<string, int>> */
                    |    private $!indexes = [];
                    |    /** @psalm-var MapperIterator */
                    |    private $!iterator;
                    |
                    |    /**
                    |     * @psalm-param ?array<int, TScalar|scalar> $!data
                    |     * @param array|null $!data
                    |     */
                    |    public function __construct(array $!data = null)
                    |    {
                    |        if (!is_null($!data)) {
                    |            $!this->index($!data);
                    |        }
                    |        $!this->data = $!data;
                    |        $!this->iterator = $!this->getIterator();
                    |    }
                    |
                    |    public function toArray(): ?array
                    |    {
                    |        return $!this->data;
                    |    }
                    |    
                    |    public function jsonSerialize(): ?array
                    |    {
                    |        return $!this->data;
                    |    }
                    |
                    |    /**
                    |     * @template T
                    |     * @psalm-param array<int, T|scalar> $!data
                    |     * @return static
                    |     */
                    |    final public static function fromArray(array $!data)
                    |    {
                    |        return new static($!data);
                    |    }
                    |
                    |    /**
                    |     * @param mixed $!data
                    |     */
                    |    protected function index($!data): void
                    |    {
                    |    }
                    |
                    |    /**
                    |     * @psalm-return TScalar|scalar|null
                    |     */
                    |    final protected function get(int $!index)
                    |    {
                    |        if (isset($!this->data[$!index])) {
                    |            return $!this->data[$!index];
                    |        }
                    |        return null;
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TScalar|scalar $!data
                    |     */
                    |    final protected function set($!data, ?int $!index): void
                    |    {
                    |        if (is_null($!index)) {
                    |            $!this->data[] = $!data;
                    |        } else {
                    |            $!this->data[$!index] = $!data;
                    |        }
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TScalar|scalar $!value
                    |     * @param $!value
                    |     * @return Collection
                    |     */
                    |    public function add($!value)
                    |    {
                    |        return $!this->store($!value);
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TScalar|scalar $!value
                    |     * @param $!value
                    |     * @return Collection
                    |     */
                    |    final protected function store($!value)
                    |    {
                    |        $!this->set($!value, null);
                    |        $!this->iterator = $!this->getIterator();
                    |
                    |        return $!this;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return ?TScalar
                    |     */
                    |    public function at(int $!index)
                    |    {
                    |        return $!this->mapper()($!index);
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(int): ?TScalar
                    |     */
                    |    abstract protected function mapper();
                    |
                    |    final protected function addToIndex(string $!field, string $!key, int $!index): void
                    |    {
                    |        $!this->indexes[$!field][$!key] = $!index;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return ?TScalar
                    |     */
                    |    final protected function valueByKey(string $!field, string $!key)
                    |    {
                    |        return isset($!this->indexes[$!field][$!key]) ? $!this->at($!this->indexes[$!field][$!key]) : null;
                    |    }
                    |
                    |    public function getIterator(): MapperIterator
                    |    {
                    |        $!keys = !is_null($!this->data) ? array_keys($!this->data) : [];
                    |        $!keyIterator = new \ArrayIterator(array_combine($!keys, $!keys));
                    |        $!iterator = new MapperIterator(
                    |            $!keyIterator,
                    |            $!this->mapper()
                    |        );
                    |        $!iterator->rewind();
                    |
                    |        return $!iterator;
                    |    }
                    |
                    |    /**
                    |     * @return ?TScalar
                    |     */
                    |    public function current()
                    |    {
                    |        /** @psalm-var ?TScalar */
                    |        return $!this->iterator->current();
                    |    }
                    |
                    |    /**
                    |     * @return void
                    |     */
                    |    public function next()
                    |    {
                    |        $!this->iterator->next();
                    |    }
                    |
                    |    /**
                    |     * @return int
                    |     */
                    |    public function key()
                    |    {
                    |        /** @psalm-var int */
                    |        return $!this->iterator->key();
                    |    }
                    |
                    |    /**
                    |     * @return bool
                    |     */
                    |    public function valid()
                    |    {
                    |        return $!this->iterator->valid();
                    |    }
                    |
                    |    /**
                    |     * @return void
                    |     */
                    |    public function rewind()
                    |    {
                    |        $!this->iterator->rewind();
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @return bool
                    |     */
                    |    public function offsetExists($!offset)
                    |    {
                    |        return !is_null($!this->data) && array_key_exists($!offset, $!this->data);
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @return ?TScalar
                    |     */
                    |    public function offsetGet($!offset)
                    |    {
                    |        return $!this->at($!offset);
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @psalm-param TScalar|scalar $!value
                    |     * @param mixed $!value
                    |     * @return void
                    |     */
                    |    public function offsetSet($!offset, $!value)
                    |    {
                    |        $!this->set($!value, $!offset);
                    |        $!this->iterator = $!this->getIterator();
                    |    }
                    |
                    |    /**
                    |     * @param int $!offset
                    |     * @return void
                    |     */
                    |    public function offsetUnset($!offset)
                    |    {
                    |        if ($!this->offsetExists($!offset)) {
                    |            /** @psalm-suppress PossiblyNullArrayAccess */
                    |            unset($!this->data[$!offset]);
                    |            $!this->iterator = $!this->getIterator();
                    |        }
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun mapperMap(): TemplateFile {
        return TemplateFile(relativePath = "src/Base/MapperMap.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Base;
                    |
                    |use stdClass;
                    |
                    |/**
                    | * @template TObject
                    | */
                    |abstract class MapperMap implements Collection, \ArrayAccess, \JsonSerializable, \IteratorAggregate
                    |{
                    |    /** @psalm-var ?array<string, TObject|stdClass> */
                    |    private $!data;
                    |    /** @psalm-var array<string, array<string, string>> */
                    |    private $!indexes = [];
                    |    /** @psalm-var MapperIterator */
                    |    private $!iterator;
                    |
                    |    /**
                    |     * @psalm-param ?array<string, TObject|stdClass> $!data
                    |     * @param array|null $!data
                    |     */
                    |    public function __construct(array $!data = null)
                    |    {
                    |        if (!is_null($!data)) {
                    |            $!this->index($!data);
                    |        }
                    |        $!this->data = $!data;
                    |        $!this->iterator = $!this->getIterator();
                    |    }
                    |
                    |    /**
                    |     * @template T
                    |     * @psalm-param ?stdClass|array<string, T|stdClass> $!data
                    |     * @return static
                    |     */
                    |    final public static function of($!data = null)
                    |    {
                    |        if (is_array($!data)) {
                    |            return self::fromArray($!data);
                    |        }
                    |        /** @psalm-var stdClass $!data) */
                    |        return self::fromStdClass($!data);
                    |    }
                    |
                    |    /**
                    |     * @psalm-return array<string, stdClass|mixed>
                    |     */
                    |    public function toArray(): ?array
                    |    {
                    |        return $!this->data;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return array<string, stdClass|mixed>
                    |     */
                    |    public function jsonSerialize(): ?array
                    |    {
                    |        return $!this->data;
                    |    }
                    |
                    |    /**
                    |     * @psalm-param ?stdClass $!data
                    |     * @psalm-return static
                    |     */
                    |    final public static function fromStdClass(stdClass $!data = null)
                    |    {
                    |        /** @psalm-var array<string, TObject|stdClass> $!t */
                    |        $!t = (array)$!data;
                    |        return new static($!t);
                    |    }
                    |
                    |    /**
                    |     * @template T
                    |     * @psalm-param array<string, T|stdClass> $!data
                    |     * @return static
                    |     */
                    |    final public static function fromArray(array $!data)
                    |    {
                    |        return new static($!data);
                    |    }
                    |
                    |    /**
                    |     * @param mixed $!data
                    |     */
                    |    protected function index($!data): void
                    |    {
                    |    }
                    |
                    |    /**
                    |     * @psalm-return TObject|stdClass|null
                    |     */
                    |    final protected function get(string $!key)
                    |    {
                    |        if (isset($!this->data[$!key])) {
                    |            return $!this->data[$!key];
                    |        }
                    |        return null;
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TObject|stdClass $!data
                    |     */
                    |    final protected function set($!data, string $!key): void
                    |    {
                    |        $!this->data[$!key] = $!data;
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TObject|stdClass $!value
                    |     * @param $!value
                    |     * @return $!this
                    |     */
                    |    public function put(string $!key, $!value)
                    |    {
                    |        return $!this->store($!key, $!value);
                    |    }
                    |
                    |    /**
                    |     * @psalm-param TObject|stdClass $!value
                    |     * @param string $!key
                    |     * @param $!value
                    |     * @return $!this
                    |     */
                    |    final protected function store(string $!key, $!value)
                    |    {
                    |        $!this->set($!value, $!key);
                    |        $!this->iterator = $!this->getIterator();
                    |
                    |        return $!this;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return ?TObject
                    |     */
                    |    public function at(string $!key)
                    |    {
                    |        return $!this->mapper()($!key);
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable(string): ?TObject
                    |     */
                    |    abstract protected function mapper();
                    |
                    |    /**
                    |     * @psalm-param TObject|stdClass $!value
                    |     */
                    |    final protected function addToIndex(string $!field, string $!key, string $!indexKey): void
                    |    {
                    |        $!this->indexes[$!field][$!key] = $!indexKey;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return ?TObject
                    |     */
                    |    final protected function valueByKey(string $!field, string $!key)
                    |    {
                    |        return isset($!this->indexes[$!field][$!key]) ? $!this->at($!this->indexes[$!field][$!key]) : null;
                    |    }
                    |
                    |    public function getIterator(): MapperIterator
                    |    {
                    |        $!keys = !is_null($!this->data) ? array_keys($!this->data) : [];
                    |        $!keyIterator = new \ArrayIterator(array_combine($!keys, $!keys));
                    |        $!iterator = new MapperIterator(
                    |            $!keyIterator,
                    |            $!this->mapper()
                    |        );
                    |        $!iterator->rewind();
                    |
                    |        return $!iterator;
                    |    }
                    |
                    |    /**
                    |     * @return ?TObject
                    |     */
                    |    public function current()
                    |    {
                    |        /** @psalm-var ?TObject */
                    |        return $!this->iterator->current();
                    |    }
                    |
                    |    /**
                    |     * @return void
                    |     */
                    |    public function next()
                    |    {
                    |        $!this->iterator->next();
                    |    }
                    |
                    |    /**
                    |     * @return string
                    |     */
                    |    public function key()
                    |    {
                    |        /** @psalm-var string */
                    |        return $!this->iterator->key();
                    |    }
                    |
                    |    /**
                    |     * @return bool
                    |     */
                    |    public function valid()
                    |    {
                    |        return $!this->iterator->valid();
                    |    }
                    |
                    |    /**
                    |     * @return void
                    |     */
                    |    public function rewind()
                    |    {
                    |        $!this->iterator->rewind();
                    |    }
                    |
                    |    /**
                    |     * @param string $!offset
                    |     * @return bool
                    |     */
                    |    public function offsetExists($!offset)
                    |    {
                    |        return !is_null($!this->data) && array_key_exists($!offset, $!this->data);
                    |    }
                    |
                    |    /**
                    |     * @param string $!offset
                    |     * @return ?TObject
                    |     */
                    |    public function offsetGet($!offset)
                    |    {
                    |        return $!this->at($!offset);
                    |    }
                    |
                    |    /**
                    |     * @param string $!offset
                    |     * @psalm-param TObject|stdClass $!value
                    |     * @param mixed $!value
                    |     * @return void
                    |     */
                    |    public function offsetSet($!offset, $!value)
                    |    {
                    |        $!this->store($!offset, $!value);
                    |    }
                    |
                    |    /**
                    |     * @param string $!offset
                    |     * @return void
                    |     */
                    |    public function offsetUnset($!offset)
                    |    {
                    |        if ($!this->offsetExists($!offset)) {
                    |            /** @psalm-suppress PossiblyNullArrayAccess */
                    |            unset($!this->data[$!offset]);
                    |            $!this->iterator = $!this->getIterator();
                    |        }
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun psalm(): TemplateFile {
        return TemplateFile(relativePath = "psalm.xml",
                content = """
                    |<?xml version="1.0"?>
                    |<psalm
                    |    totallyTyped="true"
                    |    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    |    xmlns="https://getpsalm.org/schema/config"
                    |    xsi:schemaLocation="https://getpsalm.org/schema/config vendor/vimeo/psalm/config.xsd"
                    |
                    |    strictBinaryOperands="true"
                    |>
                    |    <projectFiles>
                    |        <directory name="src" />
                    |        <ignoreFiles>
                    |            <directory name="vendor" />
                    |        </ignoreFiles>
                    |    </projectFiles>
                    |</psalm>
                """.trimMargin()
        )
    }

    private fun apiResource(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/ApiResource.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use GuzzleHttp\ClientInterface;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |class ApiResource
                    |{
                    |    /**
                    |     * @var string
                    |     */
                    |    private $!uri;
                    |
                    |    /**
                    |     * @psalm-var array<string, scalar>
                    |     */
                    |    private $!args = [];
                    |
                    |    /**
                    |     * @var ?ClientInterface
                    |     */
                    |    private $!client;
                    |
                    |    /**
                    |     * @param string $!uri
                    |     * @psalm-param array<string, scalar> $!args
                    |     */
                    |    public function __construct(string $!uri = '', array $!args = [], ClientInterface $!client = null)
                    |    {
                    |        $!this->uri = $!uri;
                    |        $!this->args = $!args;
                    |        $!this->client = $!client;
                    |    }
                    |
                    |    /**
                    |     * @return string
                    |     */
                    |    final protected function getUri(): string
                    |    {
                    |        return $!this->uri;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return array<string, scalar>
                    |     */
                    |    final protected function getArgs(): array
                    |    {
                    |        return $!this->args;
                    |    }
                    |
                    |    public function getClient(): ?ClientInterface
                    |    {
                    |       return $!this->client;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun authConfig(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/AuthConfig.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |interface AuthConfig
                    |{
                    |    public const OPT_BASE_URI = 'base_uri';
                    |
                    |    public function getGrantType(): string;
                    |
                    |    public function getAuthUri(): string;
                    |
                    |    public function getOptions(): array;
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun clientCredentialsConfig(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/ClientCredentialsConfig.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |interface ClientCredentialsConfig extends AuthConfig
                    |{
                    |    public function getCredentials(): ClientCredentials;
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun clientCredentials(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/ClientCredentials.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |/**
                    | * @psalm-immutable
                    | */
                    |class ClientCredentials
                    |{
                    |    /** @psalm-var string */
                    |    private $!clientId;
                    |
                    |    /** @psalm-var string */
                    |    private $!clientSecret;
                    |
                    |    /** @psalm-var ?string */
                    |    private $!scope;
                    |    
                    |    /** @psalm-var string */
                    |    private $!cacheKey;
                    |
                    |    public function __construct(string $!clientId, string $!clientSecret, string $!scope = null)
                    |    {
                    |        $!this->clientId = $!clientId;
                    |        $!this->clientSecret = $!clientSecret;
                    |        $!this->scope = $!scope;
                    |        $!this->cacheKey = sha1($!clientId . (string)$!scope);
                    |    }
                    |
                    |    public function getClientId(): string
                    |    {
                    |        return $!this->clientId;
                    |    }
                    |
                    |    public function getScope(): ?string
                    |    {
                    |        return $!this->scope;
                    |    }
                    |
                    |    public function getClientSecret(): string
                    |    {
                    |        return $!this->clientSecret;
                    |    }
                    |
                    |    public function getCacheKey(): string
                    |    {
                    |        return $!this->cacheKey;
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }

    private fun config(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/Config.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName().escapeAll()}\Client;
                    |
                    |interface Config
                    |{
                    |    public const OPT_BASE_URI = 'base_uri';
                    |    public const OPT_CLIENT_OPTIONS = 'options';
                    |
                    |    public function getApiUri(): string;
                    |
                    |    public function getOptions(): array;
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun cachedProvider(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/CachedTokenProvider.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use ${packagePrefix.toNamespaceName()}\Exception\InvalidArgumentException;
                    |use Psr\Cache\CacheItemPoolInterface;
                    |use Psr\Cache\CacheItemInterface;
                    |use Psr\SimpleCache\CacheInterface;
                    |
                    |class CachedTokenProvider implements TokenProvider
                    |{
                    |    public const TOKEN_CACHE_KEY = 'access_token';
                    |    
                    |    /** @psalm-var TokenProvider */
                    |    private $!provider;
                    |
                    |    /** @psalm-var CacheItemPoolInterface|CacheInterface */
                    |    private $!cache;
                    |    
                    |    /** @psalm-var string */
                    |    private $!cacheKey;
                    |
                    |    /**
                    |     * @psalm-param CacheItemPoolInterface|CacheInterface|mixed $!cache
                    |     */
                    |    public function __construct(TokenProvider $!provider, $!cache, string $!cacheKey = null)
                    |    {
                    |       $!this->validateCache($!cache);
                    |       $!this->cache = $!cache;
                    |       $!this->provider = $!provider;
                    |       $!this->cacheKey = self::TOKEN_CACHE_KEY . "_" . ($!cacheKey ?? sha1(self::TOKEN_CACHE_KEY));
                    |    }
                    |
                    |    /**
                    |     * @psalm-assert CacheItemPoolInterface|CacheInterface $!cache
                    |     * @psalm-param CacheItemPoolInterface|CacheInterface|mixed $!cache
                    |     */
                    |    private function validateCache($!cache) : void
                    |    {
                    |       if (!$!cache instanceof CacheInterface && !$!cache instanceof CacheItemPoolInterface) {
                    |           throw new InvalidArgumentException();
                    |       }
                    |    }
                    |
                    |    /**
                    |     * @inheritDoc
                    |     */
                    |    public function getToken(): Token
                    |    {
                    |        $!item = null;
                    |
                    |        $!token = $!this->getCacheToken();
                    |        if (!is_null($!token)) {
                    |            return new TokenModel($!token);
                    |        }
                    |
                    |        return $!this->refreshToken();
                    |    }
                    |
                    |    /**
                    |     * @inheritDoc
                    |     */
                    |    public function refreshToken(): Token
                    |    {
                    |        $!token = $!this->provider->refreshToken();
                    |        // ensure token to be invalidated in cache before TTL
                    |        $!ttl = max(1, ($!token->getExpiresIn() - 300));
                    |
                    |        $!this->cache($!token, $!ttl);
                    |
                    |        return $!token;
                    |    }
                    |
                    |    private function getCacheToken(): ?string
                    |    {
                    |        $!cache = $!this->cache;
                    |        if ($!cache instanceof CacheInterface) {
                    |            /** @psalm-var ?string */
                    |            return $!cache->get($!this->cacheKey, null);
                    |        }
                    |        
                    |        $!item = $!cache->getItem($!this->cacheKey);
                    |        if ($!item->isHit()) {
                    |            return (string)$!item->get();
                    |        }
                    |        
                    |        return null;
                    |    }
                    |
                    |    private function cache(Token $!token, int $!ttl): void
                    |    {
                    |        $!cache = $!this->cache;
                    |        if ($!cache instanceof CacheInterface) {
                    |            $!cache->set($!this->cacheKey, $!token->getValue(), $!ttl);
                    |        } else {
                    |            $!item = $!cache->getItem($!this->cacheKey)->set($!token->getValue())->expiresAfter($!ttl);
                    |            $!cache->save($!item);
                    |        }
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun credentialTokenProvider() : TemplateFile {
        return TemplateFile(relativePath = "src/Client/ClientCredentialTokenProvider.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use GuzzleHttp\ClientInterface;
                    |
                    |class ClientCredentialTokenProvider implements TokenProvider
                    |{
                    |    public const GRANT_TYPE_CLIENT_CREDENTIALS = 'client_credentials';
                    |    public const GRANT_TYPE = 'grant_type';
                    |    public const SCOPE = 'scope';
                    |    public const ACCESS_TOKEN = 'access_token';
                    |    public const EXPIRES_IN = 'expires_in';
                    |
                    |    /** @psalm-var ClientInterface */
                    |    private $!client;
                    |
                    |    /** @psalm-var string */
                    |    private $!accessTokenUrl;
                    |
                    |    /** @psalm-var ClientCredentials */
                    |    private $!credentials;
                    |
                    |    public function __construct(ClientInterface $!client, string $!accessTokenUrl, ClientCredentials $!credentials)
                    |    {
                    |        $!this->client = $!client;
                    |        $!this->accessTokenUrl = $!accessTokenUrl;
                    |        $!this->credentials = $!credentials;
                    |    }
                    |
                    |    public function getToken(): Token
                    |    {
                    |        $!data = [
                    |            self::GRANT_TYPE => self::GRANT_TYPE_CLIENT_CREDENTIALS
                    |        ];
                    |        if (!is_null($!this->credentials->getScope())) {
                    |            $!data[self::SCOPE] = $!this->credentials->getScope();
                    |        }
                    |        $!options = [
                    |            'form_params' => $!data,
                    |            'auth' => [$!this->credentials->getClientId(), $!this->credentials->getClientSecret()]
                    |        ];
                    |
                    |        $!result = $!this->client->request("post", $!this->accessTokenUrl, $!options);
                    |
                    |        /** @psalm-var array $!body */
                    |        $!body = json_decode((string)$!result->getBody(), true);
                    |        return new TokenModel((string)$!body[self::ACCESS_TOKEN], (int)$!body[self::EXPIRES_IN]);
                    |    }
                    |
                    |    /**
                    |     * @return Token
                    |     */
                    |    public function refreshToken(): Token
                    |    {
                    |        return $!this->getToken();
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun oauth2Handler(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/OAuth2Handler.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use Psr\Cache\CacheItemInterface;
                    |use Psr\Cache\CacheItemPoolInterface;
                    |use Psr\Http\Message\RequestInterface;
                    |
                    |class OAuth2Handler
                    |{
                    |    /** @psalm-var TokenProvider */
                    |    private $!provider;
                    |
                    |    /**
                    |     * OAuth2Handler constructor.
                    |     * @param TokenProvider $!provider
                    |     */
                    |    public function __construct(TokenProvider $!provider)
                    |    {
                    |        $!this->provider = $!provider;
                    |    }
                    |
                    |    public function __invoke(RequestInterface $!request, array $!options = []): RequestInterface
                    |    {
                    |        return $!request->withHeader('Authorization', $!this->getAuthorizationHeader());
                    |    }
                    |
                    |    public function getAuthorizationHeader(): string
                    |    {
                    |        return 'Bearer ' . $!this->provider->getToken()->getValue();
                    |    }
                    |
                    |    public function refreshToken(): Token
                    |    {
                    |        return $!this->provider->refreshToken();
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape())
    }

    private fun middlewareFactory(): TemplateFile {
        return TemplateFile(relativePath = "src/Client/MiddlewareFactory.php",
                content = """
                    |<?php
                    |${PhpSubTemplates.generatorInfo}
                    |
                    |namespace ${packagePrefix.toNamespaceName()}\Client;
                    |
                    |use GuzzleHttp\Exception\ServerException;
                    |use GuzzleHttp\MessageFormatter;
                    |use GuzzleHttp\Middleware;
                    |use GuzzleHttp\Promise\PromiseInterface;
                    |use Psr\Http\Message\RequestInterface;
                    |use Psr\Http\Message\ResponseInterface;
                    |use Psr\Log\LoggerInterface;
                    |use Psr\Log\LogLevel;
                    |
                    |class MiddlewareFactory
                    |{
                    |    /**
                    |     * @psalm-return array<string, callable>
                    |     */
                    |    public static function createDefaultMiddlewares(
                    |        ?OAuth2Handler $!handler = null,
                    |        ?LoggerInterface $!logger = null,
                    |        int $!maxRetries = 0
                    |    ) {
                    |        $!middlewares = [];
                    |        if (!is_null($!handler)) {
                    |            $!middlewares['oauth'] = self::createMiddlewareForOAuthHandler($!handler);
                    |            $!middlewares['reauth'] = self::createReauthenticateMiddleware($!handler);
                    |        }
                    |        if (!is_null($!logger)) {
                    |            $!middlewares['logger'] = self::createLoggerMiddleware($!logger);
                    |        }
                    |        if ($!maxRetries > 0) {
                    |            $!middlewares['retryNA'] = self::createRetryNAMiddleware($!maxRetries);
                    |        }
                    |        
                    |        return $!middlewares;
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable
                    |     */
                    |    public static function createRetryNAMiddleware(int $!maxRetries)
                    |    {
                    |        return Middleware::retry(
                    |            function (
                    |                int $!retries,
                    |                RequestInterface $!request,
                    |                ResponseInterface $!response = null,
                    |                \Exception $!error = null
                    |            ) use ($!maxRetries) {
                    |                if ($!response instanceof ResponseInterface && $!response->getStatusCode() < 500) {
                    |                    return false;
                    |                }
                    |                if ($!retries < $!maxRetries) {
                    |                    return false;
                    |                }
                    |                if ($!error instanceof ServerException && $!error->getCode() == 503) {
                    |                    return true;
                    |                }
                    |                if ($!response instanceof ResponseInterface && $!response->getStatusCode() == 503) {
                    |                    return true;
                    |                }
                    |                return false;
                    |            }
                    |        );
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable
                    |     */
                    |    public static function createMiddlewareForOAuthHandler(OAuth2Handler $!handler)
                    |    {
                    |        return Middleware::mapRequest($!handler);
                    |    }
                    |
                    |    /**
                    |     * @psalm-return callable
                    |     */
                    |    public static function createLoggerMiddleware(LoggerInterface $!logger, string $!logLevel = LogLevel::INFO, string $!template = MessageFormatter::CLF)
                    |    {
                    |        return Middleware::log($!logger, new MessageFormatter($!template), $!logLevel);
                    |    }
                    |
                    |    /**
                    |     * Middleware that reauthenticates on invalid token error
                    |     *
                    |     * @return callable Returns a function that accepts the next handler.
                    |     */
                    |    public static function createReauthenticateMiddleware(OAuth2Handler $!oauthHandler, int $!maxRetries = 1)
                    |    {
                    |        return
                    |            /**
                    |             * @psalm-param callable(RequestInterface, array{reauth: int}): PromiseInterface $!handler 
                    |             * @psalm-return callable(RequestInterface, array{reauth: int})
                    |             */
                    |            function (callable $!handler) use ($!oauthHandler, $!maxRetries) {
                    |                return
                    |                    /**
                    |                     * @psalm-return PromiseInterface
                    |                     * @psalm-param array{reauth: int} $!options
                    |                     */
                    |                    function (RequestInterface $!request, array $!options) use ($!handler, $!oauthHandler, $!maxRetries): PromiseInterface {
                    |                        return $!handler($!request, $!options)->then(
                    |                            function (ResponseInterface $!response) use (
                    |                                $!request,
                    |                                $!handler,
                    |                                $!oauthHandler,
                    |                                $!options,
                    |                                $!maxRetries
                    |                            ) {
                    |                                if ($!response->getStatusCode() == 401) {
                    |                                    if (!isset($!options['reauth'])) {
                    |                                        $!options['reauth'] = 0;
                    |                                    }
                    |                                    if ($!options['reauth'] < $!maxRetries) {
                    |                                        $!options['reauth']++;
                    |                                        $!token = $!oauthHandler->refreshToken();
                    |                                        $!request = $!request->withHeader(
                    |                                            'Authorization',
                    |                                            'Bearer ' . $!token->getValue()
                    |                                        );
                    |                                        return $!handler($!request, $!options);
                    |                                    }
                    |                                }
                    |                                return $!response;
                    |                            }
                    |                        );
                    |                    };
                    |            };
                    |    }
                    |}
                """.trimMargin().forcedLiteralEscape()
        )
    }
}
