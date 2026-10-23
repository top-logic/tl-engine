---
description: Read before configuring how an application authenticates its requests to an external HTTP service (the <credentials> of LLM providers, web search, MCP client, A2A remote agents, embedder, DeepL, OpenAPI client, Gitea tool), or before writing an own HTTP consumer - HeaderCredentials, BasicCredentials, NoCredentials, ClientCredentialsProvider of tl-credential-oauth on the Nimbus SDK (client_secret, private_key_jwt, Kubernetes service account token), ManagedIdentityCredentials of tl-credential-azure on the Azure SDK (App Service, AKS workload identity, instance metadata service), DeviceLoginProvider for developers, CredentialProvider.create, RequestAuthentication, 401 handling.
order: 20
---

# Credential providers

A `CredentialProvider` adds the credentials to each outgoing HTTP request of a consumer (`authenticate(RequestAuthentication)`). The consumer neither knows nor keeps the credentials, so the administrator decides per service whether it sends a static key, a short-lived token or nothing at all. The providers read their own secrets from [secret sources](doc:credentials/secret-sources).

## Static credentials

### HeaderCredentials

A secret in a request header, optionally preceded by a scheme. The header defaults to `Authorization`:

```xml
<credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer">
	<secret name="tl_mcp_token"/>
</credentials>
```

An API key in a service-specific header, here from a mounted file:

```xml
<credentials class="com.top_logic.basic.credential.HeaderCredentials" header="x-api-key">
	<secret class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/anthropic-api-key"/>
</credentials>
```

Other schemes and headers in use: `scheme="DeepL-Auth-Key"`, `header="X-Subscription-Token"` (Brave), `header="api-key"` (Azure OpenAI).

### BasicCredentials

HTTP basic authentication (RFC 7617) with a user name and a password:

```xml
<credentials class="com.top_logic.basic.credential.BasicCredentials" user="service-user">
	<password name="SERVICE_PASSWORD"/>
</credentials>
```

### NoCredentials

Sends the request without credentials - for a service without authentication, or behind a gateway on the same trusted network that adds them:

```xml
<credentials class="com.top_logic.basic.credential.NoCredentials"/>
```

A consumer with neither `credentials` nor an `api-key` uses `NoCredentials`.

## The api-key shorthand

Consumers whose service expects a static key in a known header accept an `api-key` next to `credentials`: the LLM providers, the search engines, the embedder of `tl-ai-index` and DeepL. The key is sent in the header the service expects (`x-api-key` for Anthropic, `api-key` for Azure OpenAI with `auth-scheme="API_KEY"`, `DeepL-Auth-Key` for DeepL, `Authorization: Bearer` otherwise):

```xml
<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig" name="anthropic">
	<api-key name="tl_anthropic_api_key"/>
</provider>
```

is the same as

```xml
<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig" name="anthropic">
	<credentials class="com.top_logic.basic.credential.HeaderCredentials" header="x-api-key">
		<secret name="tl_anthropic_api_key"/>
	</credentials>
</provider>
```

Configuring both is an error; the consumer does not start.

## OAuth client credentials

The OAuth providers `ClientCredentialsProvider` and `DeviceLoginProvider` live in the module `tl-credential-oauth` (`com.top-logic:tl-credential-oauth`, package `com.top_logic.credential.oauth`), which an application adds as dependency. The protocol is implemented by the [Nimbus OAuth 2.0 SDK](https://connect2id.com/products/nimbus-oauth-openid-connect-sdk): token requests, client authentication, signed assertions and discovery are Nimbus classes; the module adds the configuration, the token cache and the refresh token store.

`ClientCredentialsProvider` obtains an access token with the client credentials grant (RFC 6749 §4.4) and sends it as bearer token. The token is cached and renewed `renewal-margin` (default `30s`, at most half the lifetime) before it expires; a token without announced lifetime is assumed to live `default-token-lifetime` (default `5min`).

The authorization server is given by its `issuer` (the token endpoint is read from `<issuer>/.well-known/openid-configuration`) or directly by `token-url`. `scope` and additional `<parameters>` (e.g. `audience`, `resource`) select what the token is for. How the client proves its identity is its `client-authentication`, which the grant requires.

### With a client secret

```xml
<credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
	issuer="https://login.example.com/realms/my-realm"
	client-id="my-app"
>
	<client-authentication class="com.top_logic.credential.oauth.ClientSecretAuthentication" method="basic">
		<secret class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/my-app-client-secret"/>
	</client-authentication>
</credentials>
```

`method="basic"` (default, `client_secret_basic`) sends the secret as HTTP basic authentication, `method="post"` (`client_secret_post`) in the request body.

### With a private key (private_key_jwt)

The client signs a short-lived assertion with its private key (RFC 7523); only the public key is registered at the server, the private key never leaves the application:

```xml
<credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
	issuer="https://login.example.com/realms/my-realm"
	client-id="my-app"
>
	<client-authentication class="com.top_logic.credential.oauth.PrivateKeyJwtAuthentication"
		algorithm="ES256"
		key-id="my-app-2026"
	>
		<private-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/my-app-key.pem"/>
	</client-authentication>
</credentials>
```

The key is PKCS#8 PEM (`-----BEGIN PRIVATE KEY-----`) or a JSON web key (JWK) with its private part. `algorithm` is `RS256` (default) or another JWS algorithm matching the key type (`RS384`, `RS512`, `PS256`, `PS384`, `PS512`, `ES256`, `ES384`, `ES512`); `key-id` lets the server pick the key during a rotation; without it, the `kid` of a JWK is sent. A key pair:

```bash
openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out my-app-key.pem   # ES256
openssl pkey -in my-app-key.pem -pubout                                               # public key to register
```

### With a Kubernetes service account token (workload identity)

The pod authenticates with its projected service account token, which the kubelet renews in a file; the application owns no secret at all. The authorization server trusts the cluster's token issuer for this client (see [Keycloak setup](doc:credentials/keycloak#kubernetes-workload-identity)):

```xml
<credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
	issuer="https://login.example.com/realms/my-realm"
	client-id="my-app"
>
	<client-authentication class="com.top_logic.credential.oauth.ClientAssertionAuthentication">
		<assertion class="com.top_logic.basic.credential.FileSecretSource" path="/var/run/secrets/tokens/keycloak"/>
	</client-authentication>
</credentials>
```

The token file comes from a projected volume whose audience is the realm:

```yaml
spec:
  serviceAccountName: my-app
  containers:
    - name: app
      volumeMounts:
        - name: keycloak-token
          mountPath: /var/run/secrets/tokens
          readOnly: true
  volumes:
    - name: keycloak-token
      projected:
        sources:
          - serviceAccountToken:
              path: keycloak
              audience: https://login.example.com/realms/my-realm
              expirationSeconds: 3600
```

The assertion is read from the file for each token request, so the renewed token is used. `assertion-type` defaults to `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`.

## Azure managed identity

Module `tl-credential-azure`, based on the Azure SDK for Java: `ManagedIdentityCredentials` requests the token with the `ManagedIdentityCredential` of `azure-identity`, which finds the identity of the platform from the environment variables Azure sets:

- App Service, Functions, Container Apps: `IDENTITY_ENDPOINT` / `IDENTITY_HEADER`.
- AKS with workload identity: `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_FEDERATED_TOKEN_FILE`, set by the workload identity webhook in the pod; the pod's service account token is exchanged at Microsoft Entra ID (`AZURE_AUTHORITY_HOST` selects another cloud).
- Otherwise (virtual machines, scale sets): the instance metadata service at `169.254.169.254`, or the host in `AZURE_POD_IDENTITY_AUTHORITY_HOST`.

The SDK reads these as environment variables (the AKS variables also as Java system properties), so the configuration names only the resource:

```xml
<credentials class="com.top_logic.credential.azure.ManagedIdentityCredentials"
	resource="https://cognitiveservices.azure.com"
/>
```

- `resource` is the service the token is for; the token is requested for the scope `<resource>/.default`.
- `client-id` selects a user-assigned identity; in AKS it overrides `AZURE_CLIENT_ID`.
- Access is granted in Azure by a role assignment, e.g. *Cognitive Services OpenAI User* for Azure OpenAI.
- Transient answers of the endpoint (404/410 during updates, 429, 5xx) are retried `max-attempts` times (default 3), starting with `retry-delay` (default `1s`) and doubling; an unreachable endpoint is reported at once. `connect-timeout` and `request-timeout` limit each request.
- The SDK caches the token, for the managed identity shared by all instances in the JVM, and renews it five minutes before it expires. `invalidate()` after a 401 rebuilds the SDK credential, which drops the token of the workload identity; a token of the metadata service or an App Service stays until its renewal, since these endpoints cache it themselves and never revoke it early.
- Error messages name the endpoint, the HTTP status and the error code and description of the response, never other content. The SDK's own log messages (logger `com.microsoft.aad.msal4j`) may quote a failed response.

## Device login for developers

`DeviceLoginProvider` authenticates with a personal access token of the developer, obtained by the device authorization grant (RFC 8628). It is meant for a developer's application calling the [credential gateway](doc:credentials/gateway) instead of holding a provider key:

```xml
<credentials class="com.top_logic.credential.oauth.DeviceLoginProvider"
	issuer="https://login.example.com/realms/my-realm"
	client-id="dev-gateway"
	scope="openid offline_access"
>
	<refresh-token-store class="com.top_logic.credential.oauth.FileRefreshTokenStore"/>
</credentials>
```

- The first request without a usable token starts the login and fails with a message - also logged as warning - naming the URL to open and the code to confirm. After the developer has confirmed in the browser, the next request is authenticated.
- The refresh token is kept in `~/.config/toplogic/oauth/<server>__<client>.json`, readable only by its owner (0600, directories 0700), replaced atomically. All applications of the developer against the same client share it, so one login serves all of them and a restart needs no new login.
- With the scope `offline_access`, the server issues an offline token that survives the end of the browser session; without it, the refresh token ends with the developer's login session at the server.
- A refresh token the server rejects is removed and a new device login starts.
- Neither the device code nor any token appears in a log or exception message.

## A consumer of an HTTP service

An own consumer declares `credentials` and, if its service takes a static key in a known header, the `api-key` shorthand:

```java
/** @see #getApiKey() */
String API_KEY = "api-key";

/** @see #getCredentials() */
String CREDENTIALS = "credentials";

/**
 * The API key of the service, sent as bearer token. Configuring both an API key and
 * credentials is an error.
 */
@Name(API_KEY)
@Format(SecretSourceFormat.class)
PolymorphicConfiguration<? extends SecretSource> getApiKey();

/**
 * How the requests are authenticated, if not with an API key.
 */
@Name(CREDENTIALS)
@Nullable
PolymorphicConfiguration<? extends CredentialProvider> getCredentials();
```

`CredentialProvider.create` resolves the combination: the configured credentials, otherwise `HeaderCredentials` with the API key in the given header and scheme, otherwise `NoCredentials`; both together raise a configuration error naming the consumer:

```java
_credentials = CredentialProvider.create(context, "My service", config.getCredentials(), config.getApiKey(),
	CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER);
```

Each request is authenticated right before it is sent. `RequestAuthentication` adapts the request of the HTTP client: `RequestAuthentication.of(HttpRequest.Builder)` and `RequestAuthentication.of(HttpURLConnection)` for the JDK clients, a method reference such as `request::setHeader` for others. A 401 answer means the credentials were rejected - a cached token may have been revoked before its end - so the consumer invalidates them and repeats the request once:

```java
HttpResponse<String> response = send(uri, body);
if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
	_credentials.invalidate();
	response = send(uri, body);
	if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
		_credentials.invalidate();
	}
}

private HttpResponse<String> send(URI uri, String body) throws IOException, InterruptedException {
	HttpRequest.Builder builder = HttpRequest.newBuilder(uri).POST(BodyPublishers.ofString(body));
	_credentials.authenticate(RequestAuthentication.of(builder));
	return _client.send(builder.build(), BodyHandlers.ofString());
}
```

Repeat only a request that has had no effect yet - a streaming request before anything was streamed, an idempotent request. A provider without cache ignores `invalidate()`.
