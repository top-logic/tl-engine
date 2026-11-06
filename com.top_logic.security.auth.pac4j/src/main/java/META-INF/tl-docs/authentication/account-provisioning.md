---
description: Read before letting an external login (pac4j / OpenID Connect client of tl-security-auth-pac4j) find accounts by the subject of the identity provider, link existing accounts by verified e-mail or login name, create accounts on the first login, or assign their groups from a claim - the user mapping OidcProvisioningUserMapping configured as user-mapping of a client and its options (link-by-email, link-by-name, create-accounts, name-conflict refuse/rename, require-verified-email, allowed-email-domains, authentication-device, the claim names, group-claim, group-mapping, groups-by-name, default-groups, listeners), the model type tl.security.pac4j:OidcIdentity with the reference tl.accounts:Person#oidcIdentities and its grantedGroups, the Java interface ExternalLoginListener (onCreate, onLogin), the refusal reasons shown on the login page (LoginDeniedException with a reason), and why the e-mail address needs tl-contact.
order: 30
---

# Accounts of external logins

By default an external login only finds an existing account by its login name (see
[Finding the account of an external login](doc:authentication#finding-the-account-of-an-external-login)).
The user mapping `OidcProvisioningUserMapping` of the module `tl-security-auth-pac4j` links the
accounts to the identities of the identity provider instead, creates accounts on demand and keeps
their group memberships in sync with a claim. It is the `user-mapping` of a pac4j client:

```xml
<config service-class="com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory">
	<instance class="com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory">
		<clients>
			<client name="keycloak"
				class="com.top_logic.security.auth.pac4j.config.keycloak.KeycloakClientConfigurator"
				...
			>
				<user-mapping class="com.top_logic.security.auth.pac4j.provisioning.OidcProvisioningUserMapping"
					allowed-email-domains="example.com"
					create-accounts="true"
					default-groups="Guests"
					group-claim="groups"
					name-conflict="rename"
					require-verified-email="true"
				>
					<group-mapping>
						<group claim-value="/developers" groups="Developers"/>
						<group claim-value="/admins" groups="Administrators, Developers"/>
					</group-mapping>
					<listeners>
						<listener class="com.example.app.login.ProjectMembershipListener"/>
					</listeners>
				</user-mapping>
			</client>
		</clients>
	</instance>
</config>
```

Each client has a mapping of its own, so the options apply per identity provider.

## Identity of an account

An account remembers the identities that log it in as parts of type `tl.security.pac4j:OidcIdentity`
in the read-only composition `tl.accounts:Person#oidcIdentities`: the name of the client
(`client`), the identifier of the user at the identity provider (`subject`, for OpenID Connect the
claim `sub`), and the groups the identity has granted (`grantedGroups`, see [Groups](#groups)). The
subject is unique and stable within a client, while the login name may change at the identity
provider. An account can carry identities of several clients, at most one per client. Deleting the
account deletes its identities. `OidcIdentities` (Java) looks an identity up by client and subject
through an indexed query.

## Finding the account

The account of a login is the first one found in this order:

1. The account with an identity of the client and subject of the login.
2. With `link-by-email="true"` (default `false`): the single account with the e-mail address of
   the login (claim `email-claim`, default `email`), if the claim `email-verified-claim` (default
   `email_verified`) is `true`. Without an account of this address, or with several, the search
   goes on. Switch it on only for an identity provider whose confirmation of e-mail addresses is
   trusted: whoever controls an address there gets the account with that address.
3. With `link-by-name="true"` (the default): the account with the login name of the login. The
   login name is the claim `login-name-claim` (default `preferred_username`), without it the name
   the `user-name-extractor` of the client extracts.
4. With `create-accounts="true"` (default `false`): a new account, see [Creating
   accounts](#creating-accounts).

Otherwise the login is refused as unknown account: the user sees the page for unknown accounts.
An account found in step 2 or 3 gets the identity attached; if it already carries another identity
of the same client, the login is refused (a second person must not take over an account through
the same identity provider).

**E-mail addresses need `tl-contact`.** An account has no e-mail address of its own: the address
is that of its contact (`Person.getUser()`), which exists only in an application with the module
`tl-contact`. Without it, `link-by-email` finds no account and a created account gets no e-mail
address (its first and last name are stored at the account).

## Creating accounts

A created account gets its login name, its first name (claim `first-name-claim`, default
`given_name`), last name (`last-name-claim`, default `family_name`) and e-mail address from the
login. It has no password: without `authentication-device` it has no authentication device and logs
in only through an external login; with the ID of a device (e.g. the default database device) it
gets that device, and a password can be set later.

Creation can be restricted:

- `require-verified-email="true"`: only for a login with an e-mail address that the identity
  provider has confirmed.
- `allowed-email-domains="example.com, example.org"`: only for an e-mail address of one of the
  domains (ignoring case). A login without e-mail address is refused as well.

**Name conflict.** If the login name of the account to create is taken by an account the identity
is not linked to (e.g. with `link-by-name="false"`), `name-conflict` decides: `refuse` (the
default) refuses the login with a message naming the conflict; `rename` creates the account under
`<name>@<client>`, if that is taken as well under `<name>@<client>-2`, `<name>@<client>-3`, and so
on. A conflicting account is never linked silently.

## Groups

`group-claim` names the claim with the groups of the user at the identity provider, e.g. `groups`
(Keycloak with a group mapper, Entra ID) or `roles`. The claim holds a list of values or a single
value. `group-mapping` translates the values to groups of the application: an entry
`<group claim-value="/admins" groups="Administrators, Developers"/>` makes an account whose claim
contains `/admins` a member of both groups. Values without entry are ignored; with
`groups-by-name="true"` a value without entry names the existing group of the same name.
`default-groups` are the groups of an account while the login has no group claim at all (an empty
claim yields no groups, not the default groups). Groups that do not exist are ignored, a configured
one with a warning in the log.

At every login the memberships are synchronized:

- The account becomes a direct member of every group the login yields. The identity records the
  membership as granted.
- A granted group the login no longer yields is removed, including a default group once the claim
  is present.
- A membership the account had before the login yielded the group, e.g. one assigned by hand, is
  not recorded and therefore never removed. Groups the mapping does not yield are never touched.
- The identity provider is authoritative for the groups it yields: a granted membership that an
  administrator removed by hand is assigned again at the next login.
- If two identities of an account (two clients) yield the same group, both record it; the
  membership stays as long as one of them still yields it.

## Listeners

`listeners` is a list of `ExternalLoginListener`s (Java interface in `com.top_logic.base.accesscontrol`,
configured by `class`). After the account is found or created and its groups are synchronized, the
mapping calls in the transaction of the login:

- `onCreate(Person account, ExternalIdentity identity)` once, when the login has created the
  account, e.g. to create objects of the application and link the account to them, so that role
  rules give it its roles right away;
- `onLogin(Person account, ExternalIdentity identity)` at every login, after `onCreate`.

The `ExternalIdentity` carries the client name, the subject, the login name and all claims. An
exception of a listener rolls the whole login transaction back (no account is created, no identity
attached, no group changed) and refuses the login; a `LoginDeniedException` with a reason tells the
user why.

```java
public class ProjectMembershipListener implements ExternalLoginListener {
	@Override
	public void onCreate(Person account, ExternalIdentity identity) {
		Object project = identity.getClaim("project");
		if (project == null) {
			throw new LoginDeniedException(I18NConstants.ERROR_NO_PROJECT, "No project claim: " + identity);
		}
		// ... link the account to the project
	}
}
```

A login by subject that changes nothing (no new account, no new link, no group change) and has no
listeners opens no transaction.

## Refusals

A refused login is a `LoginDeniedException` carrying a reason (`getReason()`, a `ResKey`), which
`ExternalAuthenticationServlet` shows on the login page of the application (`ExternalLoginFailure`):
e.g. that the e-mail address is not confirmed, the domain is not allowed, the login name is taken,
or the account is already connected with another identity of the client. A `LoginDeniedException`
without reason shows the general message that the login failed. A reason must not disclose
anything an attacker could use; details belong into the message of the exception, which is logged.

Deleting accounts the identity provider no longer knows is not part of the mapping: a login only
happens for users the provider knows.
