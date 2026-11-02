---
description: Read before registering a TopLogic application, a developer login or the credential gateway at Keycloak (login.top-logic.com or another realm) - the gateway client with its audience and role, confidential application clients with client credentials (client secret, private_key_jwt), the public device-flow client for developers with offline tokens, Kubernetes workload identity (service account token as client assertion), and how to check each with curl.
order: 40
---

# Keycloak setup

The [credential gateway](doc:credentials/gateway) accepts access tokens of one Keycloak realm. This article is the procedure for the realm: one client representing the gateway, one client per application, one client for the device login of developers, and the federation of Kubernetes service accounts. The steps name the admin console of Keycloak 26; the realm is the one the applications log in to (at TopLogic `https://login.top-logic.com/realms/<realm>`, connected to the Active Directory).

The examples use the realm URL `https://login.example.com/realms/my-realm`, the gateway client `llm-gateway` with the role `llm-user`, the application client `my-app` and the developer client `dev-gateway`.

## What the gateway checks

A token passes the gateway if

- it is signed by the realm and not expired (`iss` is the realm URL),
- its audience (`aud`) contains `llm-gateway`,
- it carries the client role `llm-user` of `llm-gateway` (`resource_access.llm-gateway.roles`).

The audience is added by a mapper, the role is assigned to the service account of an application client or to the developers' group. A token of any other client of the realm - e.g. the login token of a web application - lacks the audience and is rejected.

## The gateway client

*Clients → Create client*:

1. Client type *OpenID Connect*, client ID `llm-gateway`.
2. *Client authentication* on; switch off *Standard flow*, *Direct access grants* and *Service accounts roles*. Nobody logs in to this client; it is the audience and holds the role.
3. *Credentials*: copy the client secret into `GATEWAY_CLIENT_SECRET` of the gateway's `.env` (oauth2-proxy requires it in its configuration).
4. *Roles → Create role*: `llm-user`. Further roles allow a finer split, e.g. one per provider, with one oauth2-proxy instance per role in the gateway.

Then a client scope adding the audience, to be reused by every client that calls the gateway: *Client scopes → Create client scope*, name `llm-gateway-audience`, type *None*, protocol *OpenID Connect*; under *Mappers → Configure a new mapper → Audience*: *Included Client Audience* `llm-gateway`, *Add to access token* on, *Add to ID token* off.

## An application client

One client per application (and per stage, if the stages are to be revoked independently), *Clients → Create client*:

1. Client ID `my-app`.
2. *Client authentication* on, *Service accounts roles* on; switch off *Standard flow* and *Direct access grants*. The application obtains tokens only with the client credentials grant.
3. *Client scopes → Add client scope*: `llm-gateway-audience` as *Default*.
4. *Service accounts roles → Assign role → Filter by clients*: `llm-gateway` `llm-user`.
5. *Credentials*, depending on how the application authenticates:
   - **Client secret** (*Client Authenticator* "Client Id and Secret"): the secret goes into the application's secret file or variable, read by `ClientSecretAuthentication`.
   - **Private key** (*Client Authenticator* "Signed JWT", algorithm as configured in `PrivateKeyJwtAuthentication`): under *Keys*, *Use JWKS URL* off, *Import key* with the public key or certificate of the application; with several keys during a rotation, the `kid` selects the key (`key-id` in the application).
   - **Kubernetes service account token**: see [below](#kubernetes-workload-identity).

The application's configuration then names `issuer="https://login.example.com/realms/my-realm"` and `client-id="my-app"`, see [credential providers](doc:credentials/credential-providers#oauth-client-credentials).

Revoking the application's access is removing the role (effective with the next token, after at most the access token lifespan) or disabling the client.

## The developer client

One public client shared by all developers, *Clients → Create client*:

1. Client ID `dev-gateway`.
2. *Client authentication* off (public client: the developer's machine keeps no client secret); *Standard flow* and *Direct access grants* off; *OAuth 2.0 Device Authorization Grant* on.
3. *Client scopes*: `llm-gateway-audience` as *Default*; `offline_access` as *Optional* (it is by default), so that the `DeviceLoginProvider` with `scope="openid offline_access"` receives an offline refresh token.
4. The role: a group, e.g. `llm-developers`, with the client role `llm-gateway` `llm-user` under *Groups → Role mapping*. With the Active Directory federation, a *group-ldap-mapper* of the user federation maps the AD group of the developers to this group; otherwise developers are added to the group by hand.

The users need the realm role `offline_access` (part of the realm's default roles) for an offline token. The lifetime of the offline token is set under *Realm settings → Sessions*: *Offline Session Idle* (e.g. 30 days) ends a token that is not used, *Offline Session Max Limited* caps its total lifetime. A developer's tokens are revoked under *Users → <user> → Sessions*, or by removing the user from the group.

## Kubernetes workload identity

An application pod authenticates with its projected service account token as client assertion (`ClientAssertionAuthentication`, RFC 7521 / RFC 7523 §2.2): the client credentials request carries `client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer` and the service account token as `client_assertion`. Keycloak must trust the cluster as issuer of such assertions. This is the *federated client authentication* of Keycloak, a preview feature of recent Keycloak 26 releases; the exact feature, provider and authenticator names depend on the server version and are checked in its admin console and release notes before the setup.

1. The cluster's issuer and keys: `kubectl get --raw /.well-known/openid-configuration` shows the `issuer` and `jwks_uri`. Keycloak must be able to fetch the keys: managed clusters (AKS, EKS, GKE) publish them; a self-managed cluster exposes `/openid/v1/jwks`, e.g. for anonymous requests through a `ClusterRoleBinding` of the role `system:service-account-issuer-discovery`.
2. In the realm, an identity provider of the type for Kubernetes service accounts (or a generic OIDC/JWT issuer) with the cluster's issuer URL.
3. In the application client `my-app` (set up as above), under *Credentials*, the federated JWT client authenticator: the identity provider of step 2 and the expected subject `system:serviceaccount:<namespace>:<service-account>`.
4. In the pod, the projected token with the realm URL as audience (see [credential providers](doc:credentials/credential-providers#with-a-kubernetes-service-account-token-workload-identity)) and the application's `ClientAssertionAuthentication` reading it from `/var/run/secrets/tokens/keycloak`.

The application then holds no secret; the kubelet renews the token file, and Keycloak issues the gateway token to the client whose federated subject matches.

Where the server version lacks federated client authentication, the application client authenticates with a private key instead (Signed JWT above), the key in a Kubernetes secret mounted into the pod.

## Checking the clients

The token endpoint is `https://login.example.com/realms/my-realm/protocol/openid-connect/token`.

An application client with a secret:

```bash
curl -s -u my-app:$CLIENT_SECRET -d grant_type=client_credentials \
	https://login.example.com/realms/my-realm/protocol/openid-connect/token | jq -r .access_token
```

The payload of the token (the middle part, base64url) shows `"aud": "llm-gateway"` and `"resource_access": {"llm-gateway": {"roles": ["llm-user"]}}`:

```bash
echo "$TOKEN" | jq -R 'split(".")[1] | gsub("-";"+") | gsub("_";"/") | @base64d | fromjson | {iss, aud, azp, resource_access}'
```

The device flow of the developer client:

```bash
curl -s -d client_id=dev-gateway -d "scope=openid offline_access" \
	https://login.example.com/realms/my-realm/protocol/openid-connect/auth/device
# open verification_uri_complete in the browser, confirm, then:
curl -s -d client_id=dev-gateway -d device_code=$DEVICE_CODE \
	-d grant_type=urn:ietf:params:oauth:grant-type:device_code \
	https://login.example.com/realms/my-realm/protocol/openid-connect/token
```

The `DeviceLoginProvider` does the same on the first request of the application and names the URL and code in its error message and in the log.
