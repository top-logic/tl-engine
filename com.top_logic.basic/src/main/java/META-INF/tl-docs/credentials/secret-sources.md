---
description: Read before configuring a single password, API key or token (NamedSecretSource - the short form <api-key name="..."/> resolved through the secret store of the installation -, EnvironmentSecretSource, FileSecretSource, LiteralSecretSource, KeyVaultSecretSource of tl-credential-azure on the Azure SDK, CryptSupportSecretSource), or before adding a secret property to an own service - SecretSourceFormat, PolymorphicConfiguration<? extends SecretSource>, reading the secret at use, SecretSource#matches for constant-time comparison, TokenEditor for generated tokens.
order: 10
---

# Secret sources

A `SecretSource` delivers one secret at the moment it is used (`getSecret()`). The consumer does not keep the value, so a secret rotated in its source takes effect without a restart. The usual source is a `NamedSecretSource`, which names the secret and leaves its storage to the [secret store](doc:credentials/secret-stores) of the installation. The other sources of `com.top_logic.basic.credential` read one particular variable, file or configuration value; `tl-credential-azure` adds one particular secret of an Azure Key Vault. They are the exception for a secret that lives apart from the others.

A missing secret (an unset variable, an unreadable or empty file) fails the use of the secret with a message naming the secret, variable or file, never the secret. A consumer reading its secret at use therefore does not stop the start of the application; only its requests fail while the secret is missing.

## NamedSecretSource

The short form, an element without `class` attribute, names the secret:

```xml
<api-key name="tl_openai_api_key"/>
```

The secret is fetched by its name from the secret storage of the installation, configured once in the `SecretStoreService` (see [secret stores](doc:credentials/secret-stores)). With the default store, the system property or environment variable of that name holds the plain secret:

```bash
export tl_openai_api_key="sk-..."
```

Inside a list of secret sources (rare), the tag name is `named-secret`.

## EnvironmentSecretSource

Reads one variable on each use, regardless of the secret store of the installation: a JNDI value of the application context first, then a system property, then an environment variable - the same order as the `${env:...}` aliases. It names its class:

```xml
<api-key class="com.top_logic.basic.credential.EnvironmentSecretSource" name="TL_SPECIAL_KEY"/>
```

With the default environment store, `<api-key name="TL_SPECIAL_KEY"/>` reads the same variable; the explicit form keeps reading it when the installation configures another store. Inside a list of secret sources (rare), the tag name is `env-secret`.

## FileSecretSource

Reads the secret from a file, e.g. a Kubernetes secret volume or a Docker secret under `/run/secrets`:

```xml
<api-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/openai-api-key"/>
```

- The file is read as UTF-8; leading and trailing white space, including a final line break, is not part of the secret.
- The content is cached and read again when the file has been replaced. The check is one `stat` per use: the file key (inode), last-modified time and size, read through symbolic links. Kubernetes updates a mounted secret by swapping the `..data` symbolic link to a fresh directory, which this detects.
- The path may contain aliases for non-secret parts, e.g. `path="%SECRETS_DIR%/openai-api-key"` with `<entry name="%SECRETS_DIR%" value="${env:SECRETS_DIR}"/>` in the application configuration.

A Kubernetes deployment mounting a secret:

```yaml
spec:
  containers:
    - name: app
      volumeMounts:
        - name: provider-keys
          mountPath: /run/secrets
          readOnly: true
  volumes:
    - name: provider-keys
      secret:
        secretName: provider-keys   # keys: openai-api-key, anthropic-api-key
```

A Docker Compose service:

```yaml
services:
  app:
    secrets:
      - openai-api-key            # mounted as /run/secrets/openai-api-key
secrets:
  openai-api-key:
    file: ./secrets/openai-api-key
```

## LiteralSecretSource

The secret is part of the configuration. It is written as attribute value of the secret property, either encrypted with the configuration key or in plain text with the prefix `unencrypted:`:

```xml
<instance class="..." api-key="unencrypted:sk-..."/>
```

This is the form every encrypted `String` property had, so existing configurations with an attribute value keep working. As element (rare) it is `<api-key class="com.top_logic.basic.credential.LiteralSecretSource" value="..."/>`.

## KeyVaultSecretSource (Azure)

Module `tl-credential-azure` (`com.top-logic:tl-credential-azure`), based on the Azure SDK for Java (`SecretClient` of `azure-security-keyvault-secrets`, sending with the JDK HTTP client). Reads one secret from Azure Key Vault - an installation keeping all its secrets in one vault uses the `KeyVaultSecretStore` instead (see [secret stores](doc:credentials/secret-stores#keyvaultsecretstore-azure)) - authenticated by default with the managed identity of the machine, container app or pod the application runs on; that identity needs the role *Key Vault Secrets User* on the vault or the secret:

```xml
<api-key class="com.top_logic.credential.azure.KeyVaultSecretSource"
	vault-url="https://my-vault.vault.azure.net"
	secret-name="openai-api-key"
/>
```

- `secret-version` pins a version; without it, the current version is read.
- `cache-time` (default `5min`) is how long a read secret is kept; after it the secret is fetched again, so a rotation in the vault takes effect without a restart.
- `connect-timeout` (default `10s`) and `request-timeout` (default `30s`) limit the requests to the vault.
- `<credentials>` selects another identity, e.g. a user-assigned managed identity by its client id. Any credential provider works here, also a `ClientCredentialsProvider` with the client secret of a service principal for an application outside Azure; a request the vault rejects with 401 is repeated once after the provider has dropped its token:

```xml
<api-key class="com.top_logic.credential.azure.KeyVaultSecretSource"
	vault-url="https://my-vault.vault.azure.net"
	secret-name="openai-api-key"
>
	<credentials class="com.top_logic.credential.azure.ManagedIdentityCredentials"
		resource="https://vault.azure.net"
		client-id="00000000-0000-0000-0000-000000000000"
	/>
</api-key>
```

In AKS with workload identity, the default credentials already use the pod's federated token, see [credential providers](doc:credentials/credential-providers#azure-managed-identity).

## CryptSupportSecretSource (LDAP)

The master credential of an LDAP device (`master-credential` of `ServiceProviderInfo`) uses the `CryptSupportSecretSourceFormat`: an attribute value is decoded with `CryptSupport` (the list of decimal numbers this property always used) or taken as plain text. The element forms are those of all other secret properties, e.g. `<master-credential name="LDAP_CREDENTIAL"/>`.

## A secret property in an own service

A configuration property holding a secret is a `PolymorphicConfiguration` of a `SecretSource` with the `SecretSourceFormat`:

```java
public interface Config<I extends MyService> extends ServiceConfiguration<I> {

	/** @see #getApiKey() */
	String API_KEY = "api-key";

	/**
	 * The key for the requests to the external service.
	 *
	 * <p>
	 * Recommended is a reference to the key by its secret name, e.g.
	 * <code>&lt;api-key name="my_service_api_key"/&gt;</code>. An attribute value is an
	 * encrypted key, or a plain key with the prefix <code>unencrypted:</code>.
	 * </p>
	 */
	@Name(API_KEY)
	@Mandatory
	@Format(SecretSourceFormat.class)
	PolymorphicConfiguration<? extends SecretSource> getApiKey();

	/** @see #getApiKey() */
	void setApiKey(PolymorphicConfiguration<? extends SecretSource> value);
}
```

- The `SecretSourceFormat` makes the attribute form (`api-key="unencrypted:..."` or encrypted) possible and marks the property as encrypted, so its value is never reported in an error message.
- Without `@Mandatory`, an unconfigured property is `null`; `context.getInstance(null)` returns `null`, so the service checks for it.
- The Service Editor preselects the `NamedSecretSource` for a new mandatory secret. `@ImplementationClassDefault(LiteralSecretSource.class)` on the property preselects the literal source instead, e.g. for a generated token.

The service declares a dependency on the `SecretStoreService`, because a named secret is read through it (see [secret stores](doc:credentials/secret-stores#a-service-reading-a-named-secret)):

```java
@ServiceDependencies(SecretStoreService.Module.class)
public class MyService extends ConfiguredManagedClass<MyService.Config<?>> {
```

The service instantiates the source in its constructor and asks it for the secret at each use - never keep the result of `getSecret()` in a field:

```java
private final SecretSource _apiKey;

public MyService(InstantiationContext context, Config<?> config) {
	super(context, config);
	_apiKey = context.getInstance(config.getApiKey());
}

private void call() {
	request.setHeader("X-Api-Key", _apiKey.getSecret());
}
```

A service that **checks** a secret sent by a caller - an API key of an incoming request, a shared token - compares with `matches`, which takes the same time wherever the first differing character is:

```java
if (!_apiKey.matches(request.getHeader("X-Api-Key"))) {
	throw new TopLogicException(I18NConstants.ERROR_INVALID_API_KEY);
}
```

For a calling service the HTTP header is better added by a credential provider than by the service itself: a `credentials` property together with the `api-key` as shorthand gives the administrator all options at once, see [credential providers](doc:credentials/credential-providers#a-consumer-of-an-http-service).

### Generated tokens

A secret the application itself defines - the key a client must present to an API of the application - is edited with the `TokenEditor`. For a literal source configured in place it offers a command generating a random token and, for an optional property, one clearing it; other sources show their reference fields:

```java
@Name(API_KEY)
@Format(SecretSourceFormat.class)
@ImplementationClassDefault(LiteralSecretSource.class)
@PropertyEditor(TokenEditor.class)
PolymorphicConfiguration<? extends SecretSource> getApiKey();
```

`APIKeySecret` of the OpenAPI server is an example.
