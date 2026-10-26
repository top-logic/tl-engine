---
description: Read before deciding where the named secrets of an installation live (<api-key name="..."/> = NamedSecretSource) - the global section SecretStores$Config with its store (EnvironmentSecretStore default, DirectorySecretStore, LiteralSecretStore, ChainSecretStore, KeyVaultSecretStore of tl-credential-azure), prefix and per-secret overrides, putting that section into an installation-specific overlay (tl_config, autoconf), and writing an own SecretStore.
order: 15
---

# Secret stores

A secret property of a service names its secret, it does not say where the secret is kept:

```xml
<api-key name="tl_openai_api_key"/>
```

An element without `class` attribute is a `NamedSecretSource`. Where the named secrets live differs between installations - environment variables on a developer machine, mounted files in a container, a vault in production - and is decided **once per installation** in the global configuration section `SecretStores$Config`, not in each secret property. The service configurations are therefore the same in every installation; only that section changes.

Names are written in snake case (`tl_openai_api_key`, `db_password`), the form environment variables use.

## Resolving a named secret

A `NamedSecretSource` asks `SecretStores.getSecret(name)` each time the secret is used:

1. If the section has an **override** for the name, its secret source delivers the secret.
2. Otherwise the **store** of the installation delivers it.

The section is part of the application configuration, not a service: secrets such as the database password are needed before the services start. The engine ships it in `tl_basic.config.xml` with the environment store:

```xml
<config config:interface="com.top_logic.basic.credential.SecretStores$Config">
	<store class="com.top_logic.basic.credential.EnvironmentSecretStore"/>
</config>
```

A secret that is not available fails its use with a message naming the secret and where it was looked for (the variable, the file, the vault), never the secret. The start of the application is not affected; only the consumer of the secret fails while it is missing.

## The stores

All stores are in `com.top_logic.basic.credential`, the Key Vault store in `tl-credential-azure`. Each store looks the secret up at the moment of use, so a rotated secret takes effect without a restart.

### EnvironmentSecretStore

The default. Reads the system property or environment variable named like the secret - a JNDI value of the application context first, then a system property, then an environment variable, the order of the `${env:...}` aliases. An empty variable counts as not set. The variable holds the plain secret:

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
<config config:interface="com.top_logic.basic.credential.SecretStores$Config">
	<store class="com.top_logic.basic.credential.DirectorySecretStore" directory="/run/secrets"/>
	<overrides>
		<override name="db_password">
			<source class="com.top_logic.basic.credential.FileSecretSource" path="/etc/db/password"/>
		</override>
		<override name="translation_api_key" source="unencrypted:test-key"/>
	</overrides>
</config>
```

The `source` takes all forms of a secret property; `<source name="other_secret"/>` makes the name an alias of another named secret. An override referring back to itself is reported as error.

## The section in an installation

The section belongs to the installation, not to the application: put it into a configuration fragment that only that installation loads.

- **`tl_config`**: the system property (or JNDI value) `tl_config` names an additional configuration file, loaded last. A file whose name ends with `.config.xml` is a typed configuration and may hold the section directly. A file `local.conf.xml` (aliases, as generated by the application archetype with `-Dtl_config=local.conf.xml` in `.mvn/jvm.config`) brings a typed companion `local.conf.config.xml` from the same directory. The property may also be given per context path as `<context>_tl_config` (`ROOT_tl_config` for the root context).
- **`autoconf`**: every `*.config.xml` of the folder named by `tl_autoconf_dir` (or of `WEB-INF/autoconf`) is loaded.

The fragment is an ordinary typed configuration:

```xml
<?xml version="1.0" encoding="utf-8" ?>

<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<configs>
		<config config:interface="com.top_logic.basic.credential.SecretStores$Config">
			<store class="com.top_logic.credential.azure.KeyVaultSecretStore"
				vault-url="https://my-vault.vault.azure.net"
			/>
		</config>
	</configs>
</application>
```

The `store` of the fragment replaces the environment store of the engine; its overrides are added to those of the engine configuration.

## Explicit sources as the exception

An element with `class` attribute selects a secret source for this one property, regardless of the store: an `EnvironmentSecretSource` reads a variable on purpose, a `FileSecretSource` a given file, a `KeyVaultSecretSource` one secret of a particular vault, an attribute value is a secret kept in the configuration (see [secret sources](doc:credentials/secret-sources)). Prefer the named form and an override in the section: the service configuration then stays the same in every installation.

## An own store

A store implements `SecretStore`: `lookup(name)` returns the secret, or `null` if the store does not hold it, and throws an `I18NRuntimeException` if it holds the secret but cannot deliver it; `getSecret(name)` turns `null` into an error. Deriving from `AbstractSecretStore` adds the `prefix`: the subclass implements `lookupKey(key)` and `missing(key)` (the error for a secret it does not hold). A store asking a remote service caches the secrets itself, like `KeyVaultSecretStore` does with `KeyVaultClient`.
