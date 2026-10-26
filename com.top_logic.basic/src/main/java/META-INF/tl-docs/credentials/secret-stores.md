---
description: Read before deciding where the named secrets of an installation live (<api-key name="..."/> = NamedSecretSource) or checking which of them are missing (System > Secrets in the React admin, SecretReferences) - the service SecretStoreService (editable in the Service Editor) with its store (EnvironmentSecretStore default, DirectorySecretStore, LiteralSecretStore, ChainSecretStore, KeyVaultSecretStore of tl-credential-azure), prefix and per-secret overrides, development secrets (devel-secrets.xml / devel-secrets.config.xml of com.top_logic.basic: ChainSecretStore of env-store, directory-store on ~/.top-logic/secrets and literal-store with h2_passwd), putting its configuration into an installation-specific overlay (tl_config, autoconf), the @ServiceDependencies a service reading a named secret declares, and writing an own SecretStore.
order: 15
---

# Secret stores

A secret property of a service names its secret, it does not say where the secret is kept:

```xml
<api-key name="tl_openai_api_key"/>
```

An element without `class` attribute is a `NamedSecretSource`. Where the named secrets live differs between installations - environment variables on a developer machine, mounted files in a container, a vault in production - and is decided **once per installation** in the configuration of the service `SecretStoreService`, not in each secret property. The service configurations are therefore the same in every installation; only the configuration of the secret store service changes. It is edited in the Service Editor like any other service.

Names are written in snake case (`tl_openai_api_key`, `db_password`), the form environment variables use.

## Resolving a named secret

A `NamedSecretSource` asks the running `SecretStoreService` each time the secret is used:

1. If the service has an **override** for the name, its secret source delivers the secret.
2. Otherwise the **store** of the installation delivers it.

The engine configures the service in `tl_basic.config.xml` with the environment store:

```xml
<config service-class="com.top_logic.basic.credential.SecretStoreService">
	<instance class="com.top_logic.basic.credential.SecretStoreService">
		<store class="com.top_logic.basic.credential.EnvironmentSecretStore"/>
	</instance>
</config>
```

## A service reading a named secret

A named secret is read through the started `SecretStoreService`; while it is not started, reading the secret fails with an error naming the secret and the missing service - there is no fallback to the environment. A service whose configuration holds a secret - directly or nested, e.g. the `<credentials>` of an HTTP consumer - therefore declares a dependency on it, so that the secret store is started before and stopped after the service:

```java
@ServiceDependencies({
	ThreadContextManager.Module.class,
	SecretStoreService.Module.class
})
public class MyService extends ConfiguredManagedClass<MyService.Config<?>> {
```

The services of the engine reading secrets declare it: the connection pools, the translation service, the mail services, the LLM providers and the other HTTP consumers of the AI modules, the OpenAPI client and server, JMS, Kafka, pac4j, LDAP, the password hashing and the data access service (blob storage). A test reading a named secret starts the service, e.g. with `AbstractTypedConfigurationTestCase.suite(MyTest.class, SecretStoreService.Module.INSTANCE)`.

A secret that is not available fails its use with a message naming the secret and where it was looked for (the variable, the file, the vault), never the secret. The start of the application is not affected; only the consumer of the secret fails while it is missing.

## The stores

All stores are in `com.top_logic.basic.credential`, the Key Vault store in `tl-credential-azure`. Each store looks the secret up at the moment of use, so a rotated secret takes effect without a restart.

### EnvironmentSecretStore

The default. Reads the system property or environment variable named like the secret - a JNDI value of the application context first, then a system property, then an environment variable, the order of the `${env:...}` aliases. An empty variable counts as not set. The variable holds the plain secret; a value with the prefix `unencrypted:` is accepted as well and the prefix is stripped, an encrypted value is not:

```bash
export tl_openai_api_key="sk-..."
```

### DirectorySecretStore

Reads each secret from the file named like the secret in one directory, e.g. a Kubernetes secret volume or Docker secrets under `/run/secrets`:

```xml
<store class="com.top_logic.basic.credential.DirectorySecretStore" directory="/run/secrets"/>
```

The secret `db_password` is the content of `/run/secrets/db_password`. The file is read like the file of a `FileSecretSource` (see [secret sources](doc:credentials/secret-sources#filesecretsource)): UTF-8, without surrounding white space, cached and read again when the file has been replaced. A missing file means that the store does not hold the secret; an empty or unreadable file is an error. A name that would leave the directory (`../x`) is rejected.

With Kubernetes, the keys of the secret object are the secret names:

```yaml
volumes:
  - name: app-secrets
    secret:
      secretName: app-secrets   # keys: tl_openai_api_key, db_password
```

### LiteralSecretStore

Keeps the secrets in the configuration itself, for a test system without secret infrastructure. Each value is an encrypted configuration value (or a plain value with the prefix `unencrypted:`) and is written encrypted:

```xml
<store class="com.top_logic.basic.credential.LiteralSecretStore">
	<secrets>
		<secret name="db_password" value="unencrypted:test-password"/>
	</secrets>
</store>
```

### ChainSecretStore

Asks several stores in turn; the first store holding the secret delivers it. A store that holds the secret but cannot deliver it - an empty file, a vault denying access - is an error and does not fall through to the next store. If no store holds it, the error names the secret.

```xml
<store class="com.top_logic.basic.credential.ChainSecretStore">
	<stores>
		<directory-store directory="/run/secrets"/>
		<env-store/>
	</stores>
</store>
```

Inside `<stores>` the tag names are `env-store`, `directory-store`, `literal-store`, `chain-store` and `azure-key-vault-store`.

### KeyVaultSecretStore (Azure)

Module `tl-credential-azure`. Keeps all named secrets in one Azure Key Vault:

```xml
<store class="com.top_logic.credential.azure.KeyVaultSecretStore"
	vault-url="https://my-vault.vault.azure.net"
/>
```

- Key Vault names allow only letters, digits and dashes, so the store replaces each underscore by a dash: the secret `tl_openai_api_key` is the vault secret `tl-openai-api-key`. A name that still contains other characters is rejected.
- The requests are authenticated with the managed identity of the machine, container app or pod; the identity needs the role *Key Vault Secrets User* on the vault. `<credentials>` selects another identity, exactly as for the `KeyVaultSecretSource` (see [secret sources](doc:credentials/secret-sources#keyvaultsecretsource-azure)); a request rejected with 401 is repeated once with fresh credentials.
- `cache-time` (default `5min`) is how long a read secret - and the absence of a secret - is kept. A rotated or newly created secret is seen after this time at the latest.
- A secret that does not exist in the vault (HTTP 404) is not held by the store, so a `ChainSecretStore` asks its next store. Any other failure (403, vault unreachable) is an error naming the vault and the secret.
- `connect-timeout` and `request-timeout` limit the requests to the vault.

## Prefix

Every store takes a `prefix` put in front of the name, so that several installations can share one store:

```xml
<store class="com.top_logic.basic.credential.EnvironmentSecretStore" prefix="prod_"/>
```

The secret `db_password` is then read from the variable `prod_db_password` (in Key Vault: `prod-db-password`). The prefix of a `ChainSecretStore` is put in front of the name before its stores (with their own prefixes) are asked.

## Overrides

A single secret that lives apart from the others gets an override with any secret source:

```xml
<config service-class="com.top_logic.basic.credential.SecretStoreService">
	<instance class="com.top_logic.basic.credential.SecretStoreService">
		<store class="com.top_logic.basic.credential.DirectorySecretStore" directory="/run/secrets"/>
		<overrides>
			<override name="db_password">
				<source class="com.top_logic.basic.credential.FileSecretSource" path="/etc/db/password"/>
			</override>
			<override name="translation_api_key" source="unencrypted:test-key"/>
		</overrides>
	</instance>
</config>
```

The `source` takes all forms of a secret property; `<source name="other_secret"/>` makes the name an alias of another named secret. An override referring back to itself is reported as error.

## Development secrets

The configurations shipped with an application contain no secrets, not even defaults, so that an installation without a secret notices it. A local development deployment reads its secrets through `devel-secrets.config.xml` of `com.top_logic.basic`, which the `metaConf.txt` of `deploy/local` lists as `devel-secrets.xml`:

```xml
<config service-class="com.top_logic.basic.credential.SecretStoreService">
	<instance class="com.top_logic.basic.credential.SecretStoreService">
		<store class="com.top_logic.basic.credential.ChainSecretStore">
			<stores>
				<env-store/>
				<directory-store directory="%DEVEL_SECRETS_DIR%"/>
				<literal-store>
					<secrets>
						<secret name="h2_passwd" value="unencrypted:password"/>
					</secrets>
				</literal-store>
			</stores>
		</store>
	</instance>
</config>
```

- A system property or environment variable named like the secret takes precedence.
- Otherwise the file named like the secret in the folder `~/.top-logic/secrets` of the developer delivers it (alias `%DEVEL_SECRETS_DIR%` in `devel-secrets.xml`). The folder is shared by all workspaces and applications of the developer and lies outside every project, so a real key never ends up in a repository:

  ```
  ~/.top-logic/secrets/translation_api_key    <DeepL API key>
  ~/.top-logic/secrets/tl_pac_secret          <client secret of the Keycloak demo client>
  ```

  A missing folder is no error; its secrets are then missing.
- Otherwise the value in the configuration: only the password `password` of the development database `./tmp/h2`, so that a fresh checkout starts without any setup.
- Files named `devel-*` are not packaged into the web application, and a production deployment (`deploy/prod`) does not list them.
- The configuration is the only `SecretStoreService` fragment of the deployment: two fragments both configuring a `ChainSecretStore` would append their stores to one chain.

Applications created by the archetype list `devel-secrets.xml` in the `metaConf.txt` of `deploy/local`.

## The configuration in an installation

The configuration of the secret store service belongs to the installation, not to the application:

- **Service Editor**: the administrator edits the *Secret storage* service like any other service; the editor stores the configuration in the `autoconf` folder of the installation (see below). The service is restarted to apply it; the services depending on it are restarted with it.
- **`tl_config`**: the system property (or JNDI value) `tl_config` names an additional configuration file, loaded last. A file whose name ends with `.config.xml` is a typed configuration and may hold the service configuration directly. A file `local.conf.xml` (aliases, as generated by the application archetype with `-Dtl_config=local.conf.xml` in `.mvn/jvm.config`) brings a typed companion `local.conf.config.xml` from the same directory. The property may also be given per context path as `<context>_tl_config` (`ROOT_tl_config` for the root context).
- **`autoconf`**: every `*.config.xml` of the folder named by `tl_autoconf_dir` (or of `WEB-INF/autoconf`) is loaded.

The fragment is an ordinary typed configuration:

```xml
<?xml version="1.0" encoding="utf-8" ?>

<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<services>
		<config service-class="com.top_logic.basic.credential.SecretStoreService">
			<instance class="com.top_logic.basic.credential.SecretStoreService">
				<store class="com.top_logic.credential.azure.KeyVaultSecretStore"
					vault-url="https://my-vault.vault.azure.net"
				/>
			</instance>
		</config>
	</services>
</application>
```

The `store` of the fragment replaces the environment store of the engine; its overrides are added to those of the engine configuration.

## Checking the secrets of an installation

The administration of the React UI lists all secrets the configurations of the running services refer to under *System > Secrets*: per service the property holding the secret (`providers[anthropic] / credentials / secret`), the kind of source, how it identifies the secret (the name, the variable, the file, the vault secret; a secret kept in the configuration only as such), for a named secret the override or the store delivering it, and whether it is available. A secret is *missing* when its source does not hold it - an undefined variable, a file that does not exist, a name neither an override nor the store knows - and an *error* when it is present but cannot be read, e.g. an empty file. The secret itself is never shown.

`SecretReferences.ofServices()` collects these `SecretReference`s, walking the configuration each service was started with through all nested items, lists and maps; `SecretReferences.of(service, config)` does the same for one configuration. It tells a missing secret from an unreadable one by `SecretSource#lookup()`, which answers `null` for an absent secret (for a named secret through `SecretStoreService#lookup(String)`), and describes the source by `SecretSource#reference()`. An own source overrides both, so that its secrets are reported precisely; without them, any failure counts as error and the reference stays empty.

## Explicit sources as the exception

An element with `class` attribute selects a secret source for this one property, regardless of the store: an `EnvironmentSecretSource` reads a variable on purpose, a `FileSecretSource` a given file, a `KeyVaultSecretSource` one secret of a particular vault, an attribute value is a secret kept in the configuration (see [secret sources](doc:credentials/secret-sources)). Prefer the named form and an override in the secret store service: the service configuration then stays the same in every installation.

## An own store

A store implements `SecretStore`: `lookup(name)` returns the secret, or `null` if the store does not hold it, and throws an `I18NRuntimeException` if it holds the secret but cannot deliver it; `getSecret(name)` turns `null` into an error. Deriving from `AbstractSecretStore` adds the `prefix`: the subclass implements `lookupKey(key)` and `missing(key)` (the error for a secret it does not hold). A store asking a remote service caches the secrets itself, like `KeyVaultSecretStore` does with `KeyVaultClient`.
