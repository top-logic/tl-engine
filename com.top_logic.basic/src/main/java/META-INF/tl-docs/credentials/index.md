---
description: Read before configuring a password, API key, client secret or token of a TopLogic service, or before letting an application call an external HTTP service with credentials - secret sources (NamedSecretSource resolved through the secret store of the installation, EnvironmentSecretSource, FileSecretSource, LiteralSecretSource, Azure Key Vault) and secret stores (SecretStoreService: environment, directory, literal, chain, Key Vault) vs. credential providers (HeaderCredentials, ClientCredentialsProvider, DeviceLoginProvider, ManagedIdentityCredentials), the configuration forms, what the Service Editor writes, and why ${env:...} aliases for secrets are discouraged.
order: 40
---

# Secrets and credentials

An application holds secrets - database passwords, API keys of LLM and translation services, OAuth client secrets, mail passwords - and authenticates requests to external services with them. The engine keeps both apart in two concepts:

- A **secret source** (`com.top_logic.basic.credential.SecretSource`) delivers one secret. Any configuration property holding a password or key is a secret source. Usually it only names the secret (`<api-key name="tl_openai_api_key"/>`).
- A **secret store** (`com.top_logic.basic.credential.SecretStore`) holds the named secrets of an installation: environment variables, the files of a mounted directory, an Azure Key Vault. It is configured once per installation in the service `SecretStoreService` (also in the Service Editor), so the other service configurations are the same everywhere.
- A **credential provider** (`com.top_logic.basic.credential.CredentialProvider`) authenticates an outgoing HTTP request: with a static key from a secret source in a header, with HTTP basic authentication, with a short-lived OAuth access token, with the token of an Azure managed identity, or not at all when a gateway adds the credentials. Every HTTP consumer of the engine (LLM providers, web search, MCP client, A2A client, embedder, DeepL, OpenAPI client, Gitea tool) takes a `<credentials>` element.

A credential provider usually reads its own secrets from secret sources - the key of a `HeaderCredentials`, the client secret of a `ClientCredentialsProvider` - so one mechanism decides where every secret is kept.

## When a secret is read

A secret source is asked for the secret each time the consumer uses it; the consumer does not keep the value. The environment store looks the variable up on each call, a `FileSecretSource` re-reads its file when the file has been replaced, a Key Vault secret is fetched again after its cache time. A rotated secret therefore takes effect without a restart wherever the consumer reads it per use:

- HTTP consumers read their credentials for each request.
- The database pool reads a password for each new connection; the JMS clients and the mail receiver for each new connection to their server.
- The OIDC client secret of pac4j is read when a login with the client starts and before each token request, not at startup: the application starts without it. While it is missing, the login page does not offer the client, and a login started through its URL returns to the login page; once the secret is provided, the client is offered again without a restart. pac4j derives the authentication of its token requests from the secret and keeps it, so a changed secret makes the client load the provider metadata once more.
- A few consumers read the secret once when they start: the password pepper of `Argon2Hashing`, the Azure blob connection string and the key of the vector index. The documentation of each property says when it is read.

A credential provider that obtains tokens caches them until shortly before they expire. A request rejected with HTTP 401 makes the consumer drop the cached credentials and repeat the request once with fresh ones.

## Configuration forms

A secret property (declared with the `SecretSourceFormat`, see [secret sources](doc:credentials/secret-sources)) accepts these forms. The examples use the `api-key` of an LLM provider; every secret property works the same way.

| Form | Example | Secret kept |
|---|---|---|
| Named secret, short form | `<api-key name="tl_openai_api_key"/>` | in the [secret store](doc:credentials/secret-stores) of the installation, by default the variable of that name (system property, environment variable or JNDI value), plain |
| Environment variable, explicit class | `<api-key class="com.top_logic.basic.credential.EnvironmentSecretSource" name="tl_openai_api_key"/>` | in that variable, whatever the store of the installation |
| File | `<api-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/openai-api-key"/>` | in the file, e.g. a Kubernetes or Docker secret |
| Azure Key Vault | `<api-key class="com.top_logic.credential.azure.KeyVaultSecretSource" vault-url="https://my-vault.vault.azure.net" secret-name="openai-api-key"/>` | in the key vault (module `tl-credential-azure`) |
| Attribute, encrypted | `api-key="<encrypted value>"` | in the configuration file, encrypted with the configuration key |
| Attribute, plain | `api-key="unencrypted:sk-..."` | in the configuration file, in plain text |

An element without `class` attribute is a `NamedSecretSource`. An attribute value is a `LiteralSecretSource`: like any `@Encrypted` property it is either encrypted or carries the prefix `unencrypted:` (no space after the colon - it would become part of the secret).

The recommended form is the named secret: the configuration holds the name, never the secret, and the installation decides once where its secrets live. An explicit source is the exception for a secret that lives apart from the others.

## The content of a variable or file

A variable or file read by a secret source or secret store holds the **plain secret**, without encryption. A variable may also hold the secret with the prefix `unencrypted:`, as written for an alias read into an attribute; the prefix is stripped. A secret file may end with a line break; leading and trailing white space is not part of the secret.

Some engine aliases still exist for configurations referencing a secret through an alias in an attribute (e.g. `%OPENAI_API_KEY%`); they are read as encrypted values, so such a configuration writes `api-key="unencrypted:%OPENAI_API_KEY%"`. The [migration](doc:credentials/migration) article lists them.

## The Service Editor

The Service Editor stores a service configuration in a file of the application's `autoconf` folder. For a secret property it writes:

- a named secret, an environment or file source as element with the name or path - the secret is not part of the file;
- a literal secret as encrypted attribute value.

The editor preselects the named secret for a new mandatory secret and shows its name, not its value. The editor never resolves a secret into the file.

## Why secrets are not referenced by aliases

An alias defined as `<entry name="%MY_KEY%" value="${env:my_key}"/>` and used as `api-key="%MY_KEY%"` looks equivalent to `<api-key name="my_key"/>` with the default environment store, but it is not:

- The alias is resolved once, when the configuration is loaded at startup. The secret becomes part of the effective configuration, and a rotated key needs a restart.
- When the service is saved in the Service Editor, the effective configuration is written: the resolved key ends up (encrypted) in the `autoconf` file, which is often part of a deployment or of the source tree.

Aliases remain the right tool for non-secret values around a secret - the path of a secret file (`path="%SECRETS_DIR%/my-key"`), a token URL, a tenant id.

## The chapter

- [Secret sources](doc:credentials/secret-sources): each source, file reload, Key Vault, and how to declare a secret property in an own service.
- [Secret stores](doc:credentials/secret-stores): where the named secrets of an installation live, and how an installation configures it.
- [Credential providers](doc:credentials/credential-providers): each provider, the HTTP consumer pattern, OAuth client credentials, Azure managed identity, device login.
- [Credential gateway](doc:credentials/gateway): a central gateway holding the provider keys, so that neither developer machines nor application containers do.
- [Keycloak setup](doc:credentials/keycloak): the clients, roles and flows the gateway and the applications use.
- [Migration](doc:credentials/migration): what an application changes when it moves to secret sources and credential providers.
