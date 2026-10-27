---
description: Read before setting up applications or developer machines so that they hold no LLM or other provider API keys - a central credential-injecting gateway (nginx + oauth2-proxy, docker compose sample in tl-ai-demo/gateway) that validates the caller's Keycloak token (client credentials, Kubernetes workload identity, device login) and adds the provider key; the provider configuration with base-url and credentials pointing at the gateway.
order: 30
---

# Credential gateway

Provider keys (Anthropic, OpenAI, Mistral, DeepL, search engines) are long-lived and grant access on behalf of the whole organization. Copied to every developer machine and every application container, they leak, and rotating one means touching all copies. A credential-injecting gateway keeps them in one place:

```
application ──(Bearer <token of the application or developer>)──► gateway ──(provider key)──► provider API
                                                                    │
                                                         validates the token (Keycloak)
```

- The application's provider configuration points its `base-url` at a route of the gateway and uses `credentials` that authenticate **the application** (or the developer) at the gateway: OAuth client credentials, the Kubernetes service account token, or a developer's device login at Keycloak.
- The gateway validates that token - signature, issuer, audience, expiry, role - removes it and adds the provider key of the route.
- The provider key exists only in the gateway. Revoking an application's or developer's access is a role change in Keycloak; rotating a provider key is one file on the gateway.

## The sample

The engine repository holds a runnable sample in `tl-ai-demo/gateway/`: a Docker Compose file with oauth2-proxy (token validation) and nginx (routes and key injection), the nginx template, a script loading the provider keys from Docker secrets, and an example of the settings. The files are shown below; the Keycloak side is described in [Keycloak setup](doc:credentials/keycloak).

oauth2-proxy runs in its bearer-token mode: `--skip-jwt-bearer-tokens` accepts a request whose `Authorization: Bearer` token is a valid JWT of the configured issuer, with the gateway client (`--client-id`) as audience. `--allowed-role` adds the role check: `<client>:<role>` for a client role of the token's `resource_access`. A request without a valid token or without the role is answered with 401. No cookie session, no browser login is involved - the client secret and cookie secret are required by oauth2-proxy's configuration only.

nginx asks oauth2-proxy for each request (`auth_request`) and forwards an accepted request with the caller's `Authorization` header replaced by the provider key. An alternative to oauth2-proxy is a token introspection (RFC 7662) against Keycloak from nginx, e.g. with the njs module; it adds a round trip to Keycloak per request, which the local JWT validation avoids.

### docker-compose.yml

```yaml
services:

  oauth2-proxy:
    image: quay.io/oauth2-proxy/oauth2-proxy:v7.15.5
    command:
      - --provider=keycloak-oidc
      - --oidc-issuer-url=${OIDC_ISSUER_URL}
      - --client-id=${GATEWAY_CLIENT_ID}
      # Accept requests carrying a bearer token (no browser login, no cookie session).
      - --skip-jwt-bearer-tokens=true
      # The client role a token must carry, "<client>:<role>" for a client role of Keycloak.
      - --allowed-role=${GATEWAY_CLIENT_ID}:${GATEWAY_ROLE}
      # Access is granted by the role. Service account tokens carry no e-mail address, which
      # oauth2-proxy otherwise requires as verified.
      - --email-domain=*
      - --insecure-oidc-allow-unverified-email=true
      - --http-address=0.0.0.0:4180
      - --upstream=static://202
    environment:
      # Required by oauth2-proxy, not used for validating bearer tokens.
      OAUTH2_PROXY_CLIENT_SECRET: ${GATEWAY_CLIENT_SECRET}
      OAUTH2_PROXY_COOKIE_SECRET: ${GATEWAY_COOKIE_SECRET}
    restart: unless-stopped

  gateway:
    image: nginx:1.27-alpine
    depends_on:
      - oauth2-proxy
    ports:
      - "${GATEWAY_PORT}:8080"
    environment:
      OAUTH2_PROXY_URL: ${OAUTH2_PROXY_URL}
      ANTHROPIC_UPSTREAM: ${ANTHROPIC_UPSTREAM}
      OPENAI_UPSTREAM: ${OPENAI_UPSTREAM}
      MISTRAL_UPSTREAM: ${MISTRAL_UPSTREAM}
      # Only these variables are substituted in the templates; nginx variables stay untouched.
      NGINX_ENVSUBST_FILTER: "^(OAUTH2_PROXY_URL|.*_UPSTREAM|.*_API_KEY)$"
    volumes:
      - ./nginx/templates:/etc/nginx/templates:ro
      - ./nginx/entrypoint/10-provider-keys.envsh:/docker-entrypoint.d/10-provider-keys.envsh:ro
    secrets:
      - anthropic_api_key
      - openai_api_key
      - mistral_api_key
    restart: unless-stopped

secrets:
  anthropic_api_key:
    file: ./secrets/anthropic_api_key
  openai_api_key:
    file: ./secrets/openai_api_key
  mistral_api_key:
    file: ./secrets/mistral_api_key
```

oauth2-proxy is not published; only nginx reaches it. The `--insecure-oidc-allow-unverified-email` flag only lifts oauth2-proxy's requirement of a verified e-mail address, which a service account token has not; the token is still validated and the role still checked.

### nginx/templates/default.conf.template

The nginx image renders the templates at container start into `/etc/nginx/conf.d/`, substituting the variables the `NGINX_ENVSUBST_FILTER` admits:

```nginx
server {
	listen 8080;

	# Requests of LLM calls carry whole conversations, images and documents.
	client_max_body_size 50m;

	# Streaming responses (server-sent events) are passed on as they arrive.
	proxy_buffering off;
	proxy_request_buffering off;
	proxy_read_timeout 600s;
	proxy_send_timeout 600s;
	proxy_http_version 1.1;
	proxy_ssl_server_name on;

	# The token check of every request: oauth2-proxy validates signature, issuer, audience,
	# expiry and role of the bearer token and answers 202 (accepted) or 401.
	location = /_auth {
		internal;
		proxy_pass ${OAUTH2_PROXY_URL}/oauth2/auth;
		proxy_pass_request_body off;
		proxy_set_header Content-Length "";
		proxy_set_header X-Original-URI $request_uri;
	}

	# Anthropic Messages API: key in x-api-key.
	location /anthropic/ {
		auth_request /_auth;
		proxy_set_header Host $proxy_host;
		proxy_set_header Connection "";
		proxy_set_header Authorization "";
		proxy_set_header x-api-key "${ANTHROPIC_API_KEY}";
		proxy_pass ${ANTHROPIC_UPSTREAM}/;
	}

	# OpenAI API: key as bearer token, replacing the caller's token.
	location /openai/ {
		auth_request /_auth;
		proxy_set_header Host $proxy_host;
		proxy_set_header Connection "";
		proxy_set_header Authorization "Bearer ${OPENAI_API_KEY}";
		proxy_pass ${OPENAI_UPSTREAM}/;
	}

	# Mistral API: key as bearer token, replacing the caller's token.
	location /mistral/ {
		auth_request /_auth;
		proxy_set_header Host $proxy_host;
		proxy_set_header Connection "";
		proxy_set_header Authorization "Bearer ${MISTRAL_API_KEY}";
		proxy_pass ${MISTRAL_UPSTREAM}/;
	}

	location / {
		return 404;
	}
}
```

A further provider is one more `location` with its header; a provider taking the key in a header other than `Authorization` (like Anthropic) clears `Authorization`, so that the caller's token does not travel on. The `proxy_set_header` directives are written per location, since a location that sets one inherits none from the server level.

### nginx/entrypoint/10-provider-keys.envsh

The nginx image sources executable `*.envsh` scripts of `/docker-entrypoint.d/` before it renders the templates. This one exports the keys from the Docker secrets, so they appear neither in the compose file nor in the container environment shown by `docker inspect` (the file must be executable):

```sh
for name in anthropic openai mistral; do
	file="/run/secrets/${name}_api_key"
	if [ -r "$file" ]; then
		var="$(echo "$name" | tr '[:lower:]' '[:upper:]')_API_KEY"
		export "$var=$(tr -d '\r\n' < "$file")"
	fi
done
```

The rendered configuration in the container holds the keys; the gateway container is the one place that does. A key rotation is a new secret file and a restart of the `gateway` service.

### Settings (.env)

```bash
OIDC_ISSUER_URL=https://login.example.com/realms/my-realm
GATEWAY_CLIENT_ID=llm-gateway
GATEWAY_ROLE=llm-user
GATEWAY_CLIENT_SECRET=<secret of the llm-gateway client>
GATEWAY_COOKIE_SECRET=<openssl rand -base64 32 | tr -- '+/' '-_'>
OAUTH2_PROXY_URL=http://oauth2-proxy:4180
GATEWAY_PORT=8080
ANTHROPIC_UPSTREAM=https://api.anthropic.com
OPENAI_UPSTREAM=https://api.openai.com
MISTRAL_UPSTREAM=https://api.mistral.ai
```

Start it with the keys in `secrets/` (one file per provider, plain key, directory 0700):

```bash
cd tl-ai-demo/gateway
cp gateway.env.example .env        # and fill in
mkdir -p secrets && chmod 700 secrets
printf %s "$ANTHROPIC_KEY" > secrets/anthropic_api_key
printf %s "$OPENAI_KEY" > secrets/openai_api_key
printf %s "$MISTRAL_KEY" > secrets/mistral_api_key
docker compose up -d
```

The gateway speaks plain HTTP; in production it runs behind a TLS-terminating ingress or load balancer, since every request carries an access token. In Kubernetes, the same two containers run as one pod (nginx reaching oauth2-proxy at `http://127.0.0.1:4180`) with the keys from a Kubernetes secret mounted at `/run/secrets`.

## The application side

The provider of the application gets the gateway route as `base-url` and credentials for the gateway instead of an `api-key`. The provider appends its API path (`/v1/messages`, `/v1/responses`, ...) to the `base-url`, so the route prefix is all the gateway needs.

The OAuth credential providers come from the module `tl-credential-oauth`, which the application adds as dependency. An application in Kubernetes authenticates with its service account token (no secret in the application at all):

```xml
<provider class="com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig"
	name="anthropic"
	base-url="https://llm-gateway.example.com/anthropic"
	models="claude-sonnet-4-6"
>
	<credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
		issuer="https://login.example.com/realms/my-realm"
		client-id="my-app"
	>
		<client-authentication class="com.top_logic.credential.oauth.ClientAssertionAuthentication">
			<assertion class="com.top_logic.basic.credential.FileSecretSource" path="/var/run/secrets/tokens/keycloak"/>
		</client-authentication>
	</credentials>
</provider>
```

An application outside Kubernetes uses a client secret instead (`ClientSecretAuthentication`, the secret in a mounted file or variable) or a private key (`PrivateKeyJwtAuthentication`), see [credential providers](doc:credentials/credential-providers#oauth-client-credentials). Its client secret is a credential for the gateway only: it grants no direct access to any provider, and it is revoked in Keycloak without touching a provider key.

A developer's application uses the device login; the developer confirms one login in the browser, and every application of the developer shares the refresh token:

```xml
<provider class="com.top_logic.tl.ai.core.provider.openai.OpenAIProviderConfig"
	name="openai"
	base-url="https://llm-gateway.example.com/openai"
>
	<credentials class="com.top_logic.credential.oauth.DeviceLoginProvider"
		issuer="https://login.example.com/realms/my-realm"
		client-id="dev-gateway"
		scope="openid offline_access"
	>
		<refresh-token-store class="com.top_logic.credential.oauth.FileRefreshTokenStore"/>
	</credentials>
</provider>
```

All providers of an application may share the same credentials; each gets the route of its provider. The token is cached and renewed by the credential provider, and a request the gateway rejects with 401 is repeated once with a fresh token.

Other HTTP consumers work the same way: DeepL with `api-host` pointing at a `/deepl/` route, a search engine with its endpoint at a `/brave/` route, each with `<credentials>` for the gateway - provided the gateway has a route adding the header that service expects.

## Checking a gateway

Without the application, a token from Keycloak and `curl` show each step:

```bash
TOKEN=$(curl -s -u my-app:$CLIENT_SECRET -d grant_type=client_credentials \
	https://login.example.com/realms/my-realm/protocol/openid-connect/token | jq -r .access_token)

curl -i https://llm-gateway.example.com/anthropic/v1/models                                # 401: no token
curl -i -H "Authorization: Bearer $TOKEN" https://llm-gateway.example.com/anthropic/v1/models   # 200 from the provider
```

A 401 with a token usually means the audience mapper is missing (`aud` lacks the gateway client) or the token lacks the role; oauth2-proxy logs the reason for each rejected request.
