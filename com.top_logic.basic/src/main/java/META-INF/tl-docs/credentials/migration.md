---
description: Read when upgrading an application to the engine version with secret sources, secret stores and credential providers - the short form <api-key name="..."/> as NamedSecretSource resolved through the SecretStoreService (environment by default, @ServiceDependencies of an own service reading a named secret, explicit EnvironmentSecretSource where a variable is meant regardless of the store), the key variables tl_*_api_key and translation_api_key holding the plain key, the %..._API_KEY% / %TRANSLATION_API_KEY% / %BLOB_CONNECT% aliases, removed bearer-token-env-var (HttpMcpTransport) and token-env (A2A remote agent), vector index and search engine keys, and the Java API changes (getters returning PolymorphicConfiguration<? extends SecretSource>, DataSourceConfig#getDriverOptions, ServiceProviderInfo#createFromConfig, mcp-json-jackson3).
order: 50
---

# Migration

Every password, key and client secret of the engine is a [secret source](doc:credentials/secret-sources), named secrets live in the [secret store](doc:credentials/secret-stores) of the installation, and every HTTP consumer takes [credentials](doc:credentials/credential-providers). An attribute value keeps working - it is read as encrypted value or with the prefix `unencrypted:`, as before. What an application changes is collected here.

## Named secrets and the secret store

An element without `class` attribute (`<api-key name="tl_openai_api_key"/>`) is a `NamedSecretSource`: it names the secret, and the secret store service of the installation (`SecretStoreService`) delivers it. The engine configures the environment store, which reads the system property or environment variable of that name - the same variable an element without `class` read before. A configuration using the short form therefore keeps working unchanged.

What changes:

- An installation that configures another store (a secret directory, Azure Key Vault) gets **all** named secrets from that store. A property that must keep reading a variable regardless of the store names its class: `<api-key class="com.top_logic.basic.credential.EnvironmentSecretSource" name="..."/>`, or the installation adds an override for that name.
- A configuration written by the Service Editor keeps the short form for a named secret; an `EnvironmentSecretSource` is written with its `class` attribute.
- A named secret is read through the started `SecretStoreService`. The services of the engine declare the dependency; an own service reading a secret adds `SecretStoreService.Module.class` to its `@ServiceDependencies`, and a test reading a named secret starts the service. Without it, reading the secret fails with "the secret store service is not started".
- Java code creating the configuration of an environment reference in code (`EnvironmentSecretSource.Config`) keeps reading that variable; code meaning a logical secret creates a `NamedSecretSource.Config` instead.

## Key variables hold the plain key

The engine configuration references the keys by secret name (`<api-key name="..."/>`); with the default store, the variable of that name holds the **plain key**, without the prefix `unencrypted:`:

| Variable | Read by |
|---|---|
| `tl_anthropic_api_key`, `tl_openai_api_key`, `tl_mistral_api_key`, `tl_xai_api_key`, `tl_qwen_api_key` | the LLM providers of the application (as configured in `tl-ai-demo`, `tl-ai-eval`), the vector index (`tl_mistral_api_key`) |
| `tl_brave_api_key`, `tl_tavily_api_key` | the search engines of `webSearch` |
| `translation_api_key` | `DeepLTranslationService` (`tl_basic.config.xml`) |

Before:

```bash
export tl_anthropic_api_key="unencrypted:sk-ant-..."
```

After:

```bash
export tl_anthropic_api_key="sk-ant-..."
```

The Eclipse launch `Start TL AI Demo.launch` passes the Eclipse variables `tl_*_api_key` on unchanged; they hold the plain key. An Eclipse variable `translation_api_key` that was set up with the prefix `unencrypted:` loses it.

## Aliases referencing a key

The aliases `%ANTHROPIC_API_KEY%`, `%OPENAI_API_KEY%`, `%MISTRAL_API_KEY%`, `%XAI_API_KEY%` (in `tl-ai-core.conf.xml`) and `%TRANSLATION_API_KEY%` (in `tl_basic.xml`) still stand for the variables above - which now hold the plain key, while an attribute is read as encrypted value. A configuration using such an alias in an attribute either adds the prefix or, better, references the secret by name:

Before:

```xml
<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig" name="anthropic"
	api-key="%ANTHROPIC_API_KEY%"
/>
```

After (recommended - the key never becomes part of the configuration, see [why](doc:credentials#why-secrets-are-not-referenced-by-aliases)):

```xml
<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig" name="anthropic">
	<api-key name="tl_anthropic_api_key"/>
</provider>
```

or, keeping the alias:

```xml
<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig" name="anthropic"
	api-key="unencrypted:%ANTHROPIC_API_KEY%"
/>
```

The same holds for DeepL:

```xml
<!-- Before -->
<instance class="com.top_logic.basic.translation.DeepLTranslationService" api-key="%TRANSLATION_API_KEY%" .../>

<!-- After -->
<instance class="com.top_logic.basic.translation.DeepLTranslationService" ...>
	<api-key name="translation_api_key"/>
</instance>
```

`tl-ai-demo` no longer defines `%BRAVE_API_KEY%`; a configuration copied from its `research.config.xml` uses `<api-key name="tl_brave_api_key"/>`.

An `autoconf` file the Service Editor wrote from such a configuration holds the resolved key, encrypted. Saving the service again after the change writes the reference by name instead; check that the file no longer contains an encrypted key.

## A key given as element content

A value given as element content is read like an attribute value, aliases included, so this form keeps working:

```xml
<secret>%PAC_SECRET%</secret>
```

As with an attribute, the alias must resolve to an encrypted value or to a value with the prefix `unencrypted:`. Referencing the secret by name avoids both:

```xml
<secret name="tl_pac_secret"/>
```

The `api-key` of the search engines is no longer mandatory: a search engine may take `<credentials>` instead.

## Tokens of remote MCP servers and A2A agents

`HttpMcpTransport` no longer has `bearer-token-env-var`, a `<remote-agent>` of `A2AService` no longer has `token-env`. Both take credentials:

```xml
<!-- Before -->
<transport class="com.top_logic.tl.ai.core.mcp.HttpMcpTransport" url="https://mcp.example.com/mcp"
	bearer-token-env-var="MY_MCP_TOKEN"
/>

<!-- After -->
<transport class="com.top_logic.tl.ai.core.mcp.HttpMcpTransport" url="https://mcp.example.com/mcp">
	<credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer">
		<secret name="MY_MCP_TOKEN"/>
	</credentials>
</transport>
```

```xml
<!-- Before -->
<remote-agent name="other" card-url="https://other.example.com/a2a/agent/agent-card.json" token-env="OTHER_TOKEN"/>

<!-- After -->
<remote-agent name="other" card-url="https://other.example.com/a2a/agent/agent-card.json">
	<credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer">
		<secret name="OTHER_TOKEN"/>
	</credentials>
</remote-agent>
```

The MCP client of `tl-ai-core` needs the JSON mapper `io.modelcontextprotocol.sdk:mcp-json-jackson3` on the class path. `tl-ai-mcp-server` brings it; an application connecting to a remote MCP server without that module adds the dependency.

## Vector index

The vector index reads its key from the secret `tl_mistral_api_key`; `aiVectorIndexingConf.xml` no longer defines `%MISTRAL_API_KEY%`:

```xml
<!-- Before -->
<instance class="com.top_logic.ai.vector.index.VectorIndex" mistral-api-key="%MISTRAL_API_KEY%"/>

<!-- After -->
<instance class="com.top_logic.ai.vector.index.VectorIndex">
	<mistral-api-key name="tl_mistral_api_key"/>
</instance>
```

Without a key, the vector index is disabled with a warning instead of aborting the start of the application.

## Azure blob storage

The `connect-string` of `CloudBasedRepository` is a secret property. The alias `%BLOB_CONNECT%` of `azureStorageConf.xml` now carries the prefix `unencrypted:`. An application that overrides `%BLOB_CONNECT%` with a plain connection string, or sets `connect-string` to one, adds the prefix (or encrypts the value), or references a named secret holding the plain connection string:

```xml
<!-- Before -->
<entry name="%BLOB_CONNECT%" value="${env:blob_connect}"/>

<!-- After -->
<entry name="%BLOB_CONNECT%" value="unencrypted:${env:blob_connect}"/>
```

## Database, mail, LDAP and other passwords

The attribute forms are unchanged: `<encrypted-option name="password" value="..."/>` of a data source, the `password` of `MailSenderService` and `MailReceiver`, the `secret` of the pac4j OIDC client, the `pepper` of `Argon2Hashing`, the Kafka SSL and SASL settings, the JMS passwords, the passwords and client secrets of the OpenAPI client and server. Each now also takes the element forms, e.g. `<value name="DB_PASSWORD"/>` in an `encrypted-option`. The LDAP `master-credential` keeps its attribute form encoded with `CryptSupport` (or plain).

## Java API

- A getter of a converted secret property returns `PolymorphicConfiguration<? extends SecretSource>` instead of `String`. Code reading the value instantiates the source once and asks it at each use:

  ```java
  // Before
  String password = config.getPassword();

  // After
  SecretSource password = context.getInstance(config.getPassword());   // null if not configured
  ...
  String value = password.getSecret();
  ```

  A setter takes the configuration of a source; `LiteralSecretSource.Config` with `setValue(...)` holds a value given in code.
- `DataSourceConfig.getDriverOptions()` returns `Map<String, AbstractDriverOption>`; an entry is a `DriverOption` (`getValue()` as `String`) or an `EncryptedDriverOption` (`getValue()` as secret source).
- `ServiceProviderInfo.createFromConfig(Config)` takes no device id any more and holds no credential; `LDAPAccessService` reads the master credential when it opens a context and adds it with `withCredentials(String)`.
- `ReflectiveToolFactory` takes the tool as `<tool class="..."/>` element besides the `tool-class` attribute, so that a tool gets a configuration of its own - e.g. the `<credentials>` of `GiteaIssueTool`.
- The OpenAPI client authenticates through credential providers (`CredentialEnhancer`); `HTTPAuthenticationEnhancer`, `ClientCredentialEnhancer` and `TokenBasedEnhancer` no longer exist.
- An own HTTP consumer takes `credentials` (and an `api-key` shorthand) as described in [credential providers](doc:credentials/credential-providers#a-consumer-of-an-http-service); an own secret property as described in [secret sources](doc:credentials/secret-sources#a-secret-property-in-an-own-service).
