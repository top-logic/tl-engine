---
description: Read before letting MCP, A2A or REST clients sign in to an application through OAuth instead of a personal access token, or when changing the authorization server of tl-oauth-server - the OAuthAuthorizationServer service and its settings (token lifetimes, consent page, dynamic client registration, client ID metadata documents and their fetcher), the endpoints /.well-known/oauth-authorization-server, /oauth/authorize, /oauth/token and /oauth/register, the consent page oauth-consent.view.xml of tl-oauth-server-view with the TL-Script functions oauthRefusal, oauthRequest, oauthApprove and oauthDeny, the authentication scheme oauth-access-token-authentication with its resource, and the stored model tl.oauth.server (OAuthPendingAuthorization, OAuthGrant, OAuthToken, OAuthAuthorizationCode, OAuthClient).
---

# OAuth authorization server

The module `tl-oauth-server` makes the application an OAuth 2.1 authorization server for its own
machine endpoints (MCP, A2A, REST services). A client - Claude Code, an IDE, another agent - signs
its user in through the browser and receives an access token that is accepted at exactly one
endpoint of the application. It follows the authorization profile of the Model Context Protocol:
authorization code grant with PKCE, resource indicators, client ID metadata documents and dynamic
client registration.

The service is `com.top_logic.oauth.server.OAuthAuthorizationServer`; the protocol messages are
parsed and written with the Nimbus OAuth 2.0 SDK.

## Endpoints

All endpoints are public (no login) and answer requests of any origin, because no answer depends
on a cookie. The paths are relative to the context path of the application; the issuer is the
absolute application URL including the context path, built like every URL of the
[agent discovery](doc:agent-discovery) (the configured `%HOST%` alias, else the request).

| Path | Method | Content |
|------|--------|---------|
| `/.well-known/oauth-authorization-server` | GET | Authorization server metadata (RFC 8414). |
| `/.well-known/openid-configuration` | GET | The same document, for clients that append this path to the issuer. |
| `/oauth/token` | POST | Token endpoint: grants `authorization_code` and `refresh_token`. |
| `/oauth/register` | POST | Dynamic client registration (RFC 7591); `404` when switched off. |
| `/oauth/authorize` | GET | Authorization endpoint: checks the request and sends the browser to the [consent page](#consent-page). |

The metadata announces `code_challenge_methods_supported: ["S256"]`,
`token_endpoint_auth_methods_supported: ["none"]` (public clients only) and
`client_id_metadata_document_supported`. While the service runs, `llms.txt` contains a section
naming the metadata URL.

## Clients

A client identifies itself in one of two ways:

- **Client ID metadata document.** The client ID is an `https` URL with a path. The server fetches
  the JSON document at that URL, which must name the URL itself as `client_id`, contain no client
  secret, and list the redirect URIs. Fetched documents are kept for
  `client-metadata-cache-lifetime`, at most `client-metadata-cache-size` of them.
- **Dynamic registration.** The client posts its metadata to `/oauth/register` and receives a
  generated client ID (`tlocl_...`). Only public clients are registered: authentication method
  `none`, grant types `authorization_code` and `refresh_token`, response type `code`. Registration
  needs no authentication, so the number of registered clients is capped
  (`max-registered-clients`); beyond it, a registration is answered with `503` and the error
  `temporarily_unavailable`. The [clean-up](#clean-up) removes clients nobody uses.

A redirect URI is an `https` URI or an `http` URI of the loopback interface (`localhost`,
`127.0.0.1`, `[::1]`), without fragment. A request matches a registered redirect URI only as the
same string, except that the port of a loopback URI is ignored (RFC 8252, section 7.3), because a
native client listens on an ephemeral port.

## Consent page

The authorization endpoint checks the request with `validateAuthorizationRequest` (see
[below](#api-for-an-authorization-endpoint)):

- An unknown client or a redirect URI the client did not register is shown to the person as a page
  with status `400`; the browser is never sent to an address nobody vouched for.
- Any other refusal - wrong response type, missing PKCE challenge or method other than `S256`, no
  or a foreign resource - goes back to the redirect URI with `error` and the `state` of the client.
- An accepted request is stored as *pending authorization* (`OAuthPendingAuthorization`) under a
  random identifier (`tlopa_...`, only its hash is stored) and the browser is sent to the
  `consent-page` with the identifier in the query parameter `pending`. The pending authorization
  belongs to nobody yet; it lives `pending-authorization-lifetime`.

Without a configured `consent-page`, the authorization endpoint answers `404`. The module
`tl-oauth-server-view` contributes the consent page of the React UI, `oauth-consent.view.xml`, as
an entry point (not anonymous) and sets `consent-page="/view/oauth-consent.view.xml"`. The page

- names why the request cannot be decided: unknown, answered already, or expired;
- offers a visitor who is not logged in the login form; the login keeps the URL, so the page shows
  the request afterwards. In an application with a login view, the login view stands in for the
  page and returns to it the same way, also through an external login (SSO);
- shows a logged-in person the client (and for a client ID metadata document the host publishing
  it), the host the answer goes to, the endpoint (resource) and the account, with the buttons
  *Allow* and *Deny*.

Both buttons are commands of the page, so another site cannot trigger them. *Allow* issues the
code for the logged-in account and sends the browser to the redirect URI with `code` and `state`;
*Deny* sends `error=access_denied` and `state`. Either answers the pending authorization, so it
cannot be decided twice.

The page uses these TL-Script functions (`com.top_logic.oauth.server.OAuthFunctions`), each
taking the identifier of the pending authorization; an application builds its own consent page
from them:

| Function | Result |
|----------|--------|
| `oauthRefusal($pending)` | Why the request cannot be decided (a message), `null` for an open request. |
| `oauthRequest($pending)` | Dictionary with `client`, `clientId`, `clientHost`, `redirectUri`, `redirectHost`, `resource`, `scope`; `null` if it cannot be decided. |
| `oauthApprove($pending)` | Issues the code for the current user, returns the address to send the browser to (e.g. with `<open-url new-window="false"/>`). |
| `oauthDeny($pending)` | Refuses, returns the address with `access_denied`. |

## Tokens

- The authorization code is single use and lives `authorization-code-lifetime`. Its exchange
  deletes it; the grant keeps its hash, so a second exchange of the same code is refused and
  revokes that grant.
- The token request must present the PKCE verifier of the challenge (method `S256` only), the
  client ID the code was issued to, and - if it names them - the redirect URI and the resource of
  the authorization request.
- An access token lives `access-token-lifetime`, a refresh token `refresh-token-lifetime`.
  Exchanging a refresh token issues a new pair and retires the old refresh token; presenting a
  retired refresh token again revokes the grant, the new refresh token included.
- Every token is bound to one resource (RFC 8707), an absolute URI of the application, e.g.
  `https://host/context/mcp`.
- Single use holds for two exchanges at the same time as well: the grant stores the hash of its
  code, and a new refresh token the hash of the refresh token it replaced, each in a column with a
  unique index. The database lets only the first of two such commits through; the other request
  is answered with `invalid_grant`.

Tokens and codes are opaque random strings with a prefix that names their kind (`tloat_` access
token, `tlort_` refresh token, `tloac_` authorization code), see
`com.top_logic.basic.encryption.SecretTokens`. Only their SHA-256 is stored, so a copy of the
database yields no usable token; nothing writes a token to a log.

## Accepting the tokens at an endpoint

An endpoint that is guarded by the authentication framework of the OpenAPI server (MCP and A2A
endpoints, REST services) accepts the access tokens with the scheme
`com.top_logic.oauth.server.OAuthAccessTokenAuthentication`:

```xml
<authentications>
	<oauth-access-token-authentication domain="mcp" resource="/mcp"/>
</authentications>
```

`resource` is the resource the endpoint is: an absolute URI, or a context-relative path that is
resolved against the application URL as the request reaches it. A token is accepted only if it was
issued for this resource, is not expired and its grant is not revoked; the request then acts as the
person who authorized the client. Every refusal is a `401` with a
`WWW-Authenticate: Bearer` challenge (`error="invalid_token"` when a token was presented). A bearer
value that does not have the format of an access token of this server is refused without a
database lookup, so the scheme can be configured beside the personal access token authentication.

## Settings

Configured on the service `com.top_logic.oauth.server.OAuthAuthorizationServer`:

| Setting | Default | Meaning |
|---------|---------|---------|
| `access-token-lifetime` | `1h` | How long an access token is accepted. |
| `refresh-token-lifetime` | `30d` | How long a refresh token can be exchanged; each exchange starts it anew. |
| `authorization-code-lifetime` | `1min` | How long an authorization code can be exchanged. |
| `consent-page` | none | Context-relative path of the page on which a person decides about a request; contributed by `tl-oauth-server-view`. |
| `pending-authorization-lifetime` | `10min` | How long a person has to decide about a request, logging in included. |
| `dynamic-client-registration` | `true` | Whether `/oauth/register` accepts registrations. |
| `client-id-metadata-documents` | `true` | Whether an `https` URL is accepted as client ID. |
| `client-metadata-fetcher` | `HttpClientMetadataFetcher` | How a metadata document is fetched. |
| `client-metadata-cache-size` | `1000` | Number of fetched documents kept in memory. |
| `client-metadata-cache-lifetime` | `1h` | How long a fetched document is used. |
| `max-registered-clients` | `1000` | Number of dynamically registered clients beyond which registration is refused. |
| `unused-client-lifetime` | `1d` | How long a registered client nobody authorized is kept after its registration. |
| `abandoned-client-lifetime` | `90d` | How long a registered client without authorizations is kept after its last use. |

The default fetcher `com.top_logic.oauth.server.HttpClientMetadataFetcher` (Apache HttpClient)
follows no redirect, uses no proxy, gives up after `timeout` (`5s`) and reads at most `max-size`
bytes (`5120`). Unless `allow-private-network` is set, it resolves the host once, refuses it if any
of its addresses is a loopback, link-local, private or other non-public address, and connects to
exactly the checked addresses (`PublicAddressDnsResolver`), so a name that resolves differently
between check and connection (DNS rebinding) cannot reach the internal network. TLS server name
indication and host name verification use the host name as usual.

```xml
<config service-class="com.top_logic.oauth.server.OAuthAuthorizationServer">
	<instance class="com.top_logic.oauth.server.OAuthAuthorizationServer"
		access-token-lifetime="30min"
		dynamic-client-registration="false"
	>
		<client-metadata-fetcher class="com.top_logic.oauth.server.HttpClientMetadataFetcher"
			allow-private-network="true"
		/>
	</instance>
</config>
```

## Clean-up

The scheduler task `com.top_logic.oauth.server.OAuthCleanUpTask` (*Clean up OAuth
authorizations*, contributed by the configuration of the module, hourly, once per cluster) calls
`OAuthAuthorizationServer.cleanUp(Instant)`, which removes:

- pending authorizations and authorization codes whose lifetime ended,
- access and refresh tokens whose lifetime ended; an exchanged refresh token is kept until then,
  so that presenting it again still revokes its grant,
- grants without a valid token: neither the client nor anybody else can use them any more,
- dynamically registered clients without a grant: one that was never used after
  `unused-client-lifetime` from its registration, any other after `abandoned-client-lifetime` from
  its last use (an authorization or a token refresh).

## Stored data

The model `tl.oauth.server` holds:

- `OAuthClient` - a dynamically registered client with its metadata, registration time and last
  use.
- `OAuthGrant` - the permission a person gave a client for one resource: client, resource, scope,
  created, last used, hash of the code it was created from. Deleting it
  (`OAuthAuthorizationServer.revoke`) revokes all its tokens; deleting the person deletes it.
- `OAuthToken` - an access or refresh token of a grant: hash, expiry, whether it is an exchanged
  refresh token, and the hash of the refresh token it replaced.
- `OAuthPendingAuthorization` - an accepted authorization request waiting for the decision on the
  consent page: hash of its identifier, client, redirect URI, PKCE challenge, resource, scope,
  state, expiry.
- `OAuthAuthorizationCode` - an issued, not yet exchanged code: hash, person, client, redirect URI,
  PKCE challenge, resource, expiry.

## API for an authorization endpoint

The authorization endpoint and its consent dialog use the service:

- `validateAuthorizationRequest(AuthorizationRequest, DiscoveryContext)` checks client, redirect
  URI, response type, PKCE challenge and resource, and returns a `ValidatedAuthorization`. A
  refusal is an `OAuthException`; `isRedirectable()` tells whether it may be sent to the redirect
  URI or must be shown to the person.
- `createPendingAuthorization(ValidatedAuthorization)` stores the accepted request and returns the
  identifier for the consent page; `pendingAuthorization(String)` reads it back.
- `approve(String, Person)` issues the code for the logged-in person and `deny(String)` refuses;
  both end the pending authorization and return the URI to send the browser to.
- `issueCode(ValidatedAuthorization, Person)` issues the code once the person agreed.
