---
description: Read when upgrading an application to the engine version with secret sources, secret stores and credential providers - the short form <api-key name="..."/> as NamedSecretSource resolved through the SecretStoreService (environment by default, @ServiceDependencies of an own service reading a named secret, explicit EnvironmentSecretSource where a variable is meant regardless of the store), the key variables tl_*_api_key and translation_api_key holding the plain key, the %..._API_KEY% / %TRANSLATION_API_KEY% aliases, the shipped configurations referencing the database, LDAP, mail, pac4j and blob storage secrets by name (h2_passwd, mysql_passwd, ..., ldap_credential, smtp_pass, imap_pass, tl_pac_secret, blob_connect) with the removed aliases %..._PASSWD%, %LDAP_CREDENTIAL%, %SMTP_PASS%, %IMAP_PASS%, %PAC_SECRET%, %BLOB_CONNECT%, development secrets of an application as files in deploy/local/secrets read by a ChainSecretStore of env-store and directory-store (devel-secrets.config.xml), variables in plain form with the unencrypted: prefix still accepted, removed bearer-token-env-var (HttpMcpTransport) and token-env (A2A remote agent), vector index and search engine keys, and the Java API changes (getters returning PolymorphicConfiguration<? extends SecretSource>, DataSourceConfig#getDriverOptions, ServiceProviderInfo#createFromConfig, mcp-json-jackson3).
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

The engine configuration references the keys by secret name (`<api-key name="..."/>`); with the default store, the variable of that name holds the **plain key**. The form with the prefix `unencrypted:` that a variable read through an alias needed is still accepted - the prefix is stripped - so that existing environments keep working; an encrypted value is not supported in a variable:

| Variable | Read by |
|---|---|
| `tl_anthropic_api_key`, `tl_openai_api_key`, `tl_mistral_api_key`, `tl_xai_api_key`, `tl_qwen_api_key` | the LLM providers of the application (as configured in `tl-ai-demo`, `tl-ai-eval`), the vector index (`tl_mistral_api_key`) |
| `tl_brave_api_key`, `tl_tavily_api_key` | the search engines of `webSearch` |
| `translation_api_key` | `DeepLTranslationService` (`tl_basic.config.xml`) |

```bash
export tl_anthropic_api_key="sk-ant-..."                # documented form
export tl_anthropic_api_key="unencrypted:sk-ant-..."    # still accepted
```

The Eclipse launch `Start TL AI Demo.launch` passes the Eclipse variables `tl_*_api_key` on unchanged.

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
<secret>%MY_PAC_SECRET%</secret>
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

## Secrets of the shipped configurations

The configurations shipped with the engine reference their secrets by name and contain no default for them. The secret names are the names of the variables the aliases read before:

| Configuration | Property | Secret | Removed alias |
|---|---|---|---|
| `db-h2.config.xml`, `db-mysql.config.xml`, `db-postgresql.config.xml`, `db-oracle.config.xml`, `db-mssql.config.xml`, `db-db2.config.xml` | `<encrypted-option name="password">` of the pool `default` | `h2_passwd`, `mysql_passwd`, `postgresql_passwd`, `oracle_passwd`, `mssql_passwd`, `db2_passwd` | `%H2_PASSWD%`, `%MYSQL_PASSWD%`, `%POSTGRESQL_PASSWD%`, `%ORACLE_PASSWD%`, `%MSSQL_PASSWD%`, `%DB2_PASSWD%` |
| `devel-oracle.config.xml`, `devel-oracle-12.config.xml`, `devel-oracle-19.config.xml` | the same | `oracle_passwd`, `oracle12_passwd`, `oracle19_passwd` | `%ORACLE_PASSWD%`, `%ORACLE12_PASSWD%`, `%ORACLE19_PASSWD%` |
| `db-mssql.config.xml` | `<encrypted-option name="trustStorePassword">` | `mssql_trust_store_passwd` | `%MSSQL_TRUST_STORE_PASSWD%` |
| `db-mysql.config.xml` | `<encrypted-option name="clientCertificateKeyStorePassword">`, `<encrypted-option name="trustCertificateKeyStorePassword">` | `mysql_client_certificate_key_store_password`, `mysql_trust_certificate_key_store_password` | `%MYSQL_CLIENT_CERTIFICATE_KEY_STORE_PASSWORD%`, `%MYSQL_TRUST_CERTIFICATE_KEY_STORE_PASSWORD%` |
| `ldapConf.config.xml` | `master-credential` | `ldap_credential` | `%LDAP_CREDENTIAL%` |
| `tl-mail-smtp.config.xml` | `password` of `MailSenderService` | `smtp_pass` | `%SMTP_PASS%` |
| `MailConf.config.xml` | `password` of `MailReceiverService` | `imap_pass` | `%IMAP_PASS%` |
| `pac4j-azure`, `pac4j-generic`, `pac4j-google`, `pac4j-keycloak` (`.config.xml`) | `secret` of the OIDC client | `tl_pac_secret` | `%PAC_SECRET%` |
| `azureStorageConf.config.xml` | `connect-string` of `CloudBasedRepository` | `blob_connect` | `%BLOB_CONNECT%`, `%BLOB_USER%`, `%BLOB_PASS%` |

A missing `tl_pac_secret` does not prevent the start of the application: the OIDC client reads its secret when a login with it starts, and only that login fails.

### The variables hold the plain value

With the default store, each secret is read from the variable (or system property) of its name. The variable holds the **plain** value; the form with the prefix `unencrypted:`, which a variable read through the aliases needed, is still accepted and the prefix is stripped, so that existing environments - containers, start scripts, `.mvn/jvm.config`, Eclipse launch configurations, JNDI entries, the site configuration of a build server - keep working unchanged:

```bash
export h2_passwd="my-password"                # documented form
export h2_passwd="unencrypted:my-password"    # still accepted
```

An **encrypted** value in a variable is not supported: a variable set up with an encrypted value for an alias is replaced by the plain value. Files read by a `FileSecretSource` or `DirectorySecretStore` hold the plain value only.

### No default in a production installation

The aliases had defaults, e.g. `theautomatedappengine` for the H2 password of `db-h2.xml`, `your-password` for the LDAP credential, a placeholder connection string for the blob storage. The secrets have none: an installation that relied on such a default sets the secret (e.g. `h2_passwd=theautomatedappengine` for an H2 database created with the old default), otherwise the database refuses the login, the LDAP bind or the blob storage connection fails. *System > Secrets* lists which secrets are missing.

Secrets that are not set are left out where the consumer can do without them:

- An `encrypted-option` of a data source whose secret source does not hold the secret (`SecretSource#lookup()` answers `null`, e.g. an unset named secret) is left out of the driver options; the driver behaves as without the option. A warning in the log names the option and the secret, e.g. `Driver option 'password' is left out: its secret is not set (h2_passwd).` This makes the trust and key store passwords of `db-mssql.config.xml` and `db-mysql.config.xml` optional, as their empty aliases did before. The database password is left out the same way - the database then refuses the login, and the warning tells why. A secret that is present but cannot be read (an empty secret file, a vault denying access) still fails the connection.
- The SMTP and IMAP passwords: a password the store does not hold is sent empty, for a mail server without login, as the empty default of the alias did before.

### Development secrets of an application

The engine ships no secrets and no defaults. An application that is started locally keeps the secrets of its development deployment - the password of its development database, a demo client secret - as files in its own project, in the folder `deploy/local/secrets`, one file per secret named like the secret and holding the plain value (see [secret stores](doc:credentials/secret-stores#development-secrets)). The local deployment configures the secret store as chain of the environment and this folder in `deploy/local/webapp/WEB-INF/conf/devel-secrets.config.xml`, listed in the `metaConf.txt` of `deploy/local`:

- `tl-demo`, `tl-demo-react`: `h2_passwd` (`password`, the password of `./tmp/h2`) and `tl_pac_secret` (the client secret of the Keycloak demo client).
- `tl-ai-demo`, `tl-kafka-demo`, the migration test applications and every application created by the archetype: `h2_passwd`.

An application created before this version adds the folder and the configuration the same way; without them, its local deployment reads `h2_passwd` from the environment only, and the database `./tmp/h2` created with the password `password` refuses the login. The test configuration of the engine (`testingConnectionPool.config.xml`) holds the passwords of the test databases.

### An application redefining one of the removed aliases

An application that set one of the removed aliases in its own configuration (e.g. `<entry name="%H2_PASSWD%" value="..."/>` in a deployment file) no longer reaches the password, because the shipped configuration does not read the alias any more. It removes the entry and provides the secret instead: as variable holding the plain value, in the store of its installation, or - for a development deployment - as file in the development secrets folder described above. An application that copied one of the shipped configurations with the alias keeps working as long as it defines the alias itself; switching to the named form is recommended.

## Other passwords

The attribute forms are unchanged: `<encrypted-option name="..." value="..."/>` of a data source, the `password` of `MailSenderService` and `MailReceiver`, the `secret` of the pac4j OIDC client, the `pepper` of `Argon2Hashing`, the Kafka SSL and SASL settings, the JMS passwords, the passwords and client secrets of the OpenAPI client and server. Each now also takes the element forms, e.g. `<value name="db_password"/>` in an `encrypted-option`. The LDAP `master-credential` keeps its attribute form encoded with `CryptSupport` (or plain).

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
