---
description: Read before letting users of a React application log in with passkeys (WebAuthn) - the module tl-security-passkey, the relying party taken from the public address %HOST% (variable tl_host) and the PasskeyService options usage (login or second-factor, PasskeyUsage) / rp-id / origins / rp-name / user-verification / timeout, passkeys not set up and the origin check of the page, the Signal API calls removing orphaned passkeys from the authenticator, the refusal categories (PasskeyRefusedException) and what is logged, the model type tl.security.passkey:Passkey and the reference tl.accounts:Person#passkeys, the passwordless login button and the passkey as second factor (PasskeySecondFactor, passkey-verify.view.xml, passkey-enroll.view.xml), the view elements <passkey-login>, <passkey-registration> (account, auto-start, complete-login, <on-success>) and <if-passkey-usage>, the passkeys in the settings (passkey-list.view.xml, the section passkey-login-section or the block passkeys) and the account administration, <delete-passkey/> and its last-factor rule, the TL-Script function passkeyMayUse, the interplay with MfaRequirement, and testing with a virtual authenticator.
order: 15
---

# Passkeys

A passkey is a Web Authentication (WebAuthn) credential: a key pair the authenticator of the user
creates for the application - the platform authenticator of the device (Windows Hello, Touch ID,
Android), a password manager or a security key. The private key never leaves the authenticator; the
application stores the public key and checks a signature over a challenge at every login.

The module `tl-security-passkey` uses passkeys in the React view UI in one of two ways, chosen
per application by the option `usage` of `PasskeyService` (see
[Usage of the passkeys](#usage-of-the-passkeys)):

- **Passwordless login** (`login`, the default): a button on the login form logs in with a passkey
  alone. The user picks one of the passkeys the authenticator holds for the application, and the
  authenticator verifies the user (PIN, biometrics). Possession of the key and the verification of
  the user are two factors, so this login asks for no further factor.
- **Second factor** (`second-factor`): after the password, an account with a passkey confirms the
  login with it, as one of the configured second-factor methods, see
  [Multi-factor authentication](doc:authentication/mfa).

The relying party (challenges, client data, attestation and signature checks) is the library
`com.yubico:webauthn-server-core`, its version is managed in `tl-bom`. The browser side is the
generic `ReactWebAuthnControl` of `com.top_logic.layout.react` (React component `TLWebAuthn`),
which runs `navigator.credentials.create()` / `get()` in the click handler of its button.

## Enabling passkeys

Add the dependency to the application:

```xml
<dependency>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-security-passkey</artifactId>
</dependency>
```

The module brings everything else: it activates `PasskeyService`, extends the account model,
contributes the login button and the second-factor method (`WEB-INF/autoconf/login.passkey.config.xml`)
and overlays the settings and the account administration. The demo `tl-demo-react` includes it.

### Service configuration

`PasskeyService` (`com.top_logic.security.passkey`) is the relying party. The module configures it
in `tl-security-passkey.conf.config.xml`:

| Option | Default | Meaning |
|---|---|---|
| `usage` | `login` | What the passkeys are for: `login` (a passkey alone logs in) or `second-factor` (a passkey confirms the login after the password), see [Usage of the passkeys](#usage-of-the-passkeys). |
| `rp-id` | empty | Override of the relying party ID: the domain the passkeys are bound to, e.g. `example.com`. A passkey works on this domain and its subdomains only. It must be the host of every accepted origin or a parent domain of it. Empty: the host of `%HOST%`. |
| `origins` | empty | Override of the accepted origins: comma-separated origins a ceremony may run on, e.g. `https://app.example.com`. Set, they **replace** the origin of `%HOST%` (list it too if passkeys are to work there). Empty: the origin of `%HOST%`. |
| `rp-name` | application title | The name an authenticator shows when it creates a passkey (`ResKey`, in the system language). |
| `user-verification` | `REQUIRED` | Whether the authenticator must verify its user when a passkey is created or used, for the usage `second-factor`: `REQUIRED`, `PREFERRED`, `DISCOURAGED`. The usage `login` always requires it. |
| `timeout` | `5min` | How long the browser waits for the user, and how long a started ceremony is accepted. |

### Usage of the passkeys

The passkeys of an application serve one purpose, never both at once: a passkey that logs in by
itself would also be what is asked for after the password, and the second factor would add no
second proof. The option `usage` (enum `PasskeyUsage`, `PasskeyService.getUsage()`) chooses:

| | `login` (default) | `second-factor` |
|---|---|---|
| Login form | "Sign in with passkey" (`<passkey-login>` without `account`) | no passkey button |
| After the password | no passkey asked for; the configured second factors (e.g. the authenticator app) as usual | the method `passkey` (`PasskeySecondFactor`): verify with a passkey, or set one up for a `required` account |
| A passkey created | discoverable (the user picks it without a user name), user verification always required | non-discoverable where the authenticator allows it, user verification as `user-verification` says |
| Refused | a passkey as second factor | a passkey login without password |
| Settings, "Security" tab | the section "Sign in with passkey" after "Password" | the block "Passkeys" in "Two-factor authentication" |
| Last-factor rule of `<delete-passkey/>` | none, a passkey is no second factor | applies |
| Account with `MfaRequirement` `disabled` | may create and use passkeys | may create none |

```xml
<config service-class="com.top_logic.security.passkey.PasskeyService">
	<instance class="com.top_logic.security.passkey.PasskeyService"
		usage="second-factor"
	/>
</config>
```

Switching the usage keeps the registered passkeys as they are:

- From `second-factor` to `login`: passkeys created as second factor are non-discoverable, the
  authenticator cannot offer them without a user name, so they cannot log in alone. The users
  create new passkeys in the settings; the old ones are listed there and can be deleted.
- From `login` to `second-factor`: the existing passkeys confirm the login after the password; an
  account with a passkey is asked for it from its next login on.

The views follow the usage without reading it: `<passkey-login>` hides itself when the usage does
not allow its kind of login, and content belonging to one usage is wrapped in
[`<if-passkey-usage>`](#if-passkey-usage).

### The relying party: the public address of the application

The relying party ID and the accepted origin come from the **public address of the application**,
the alias `%HOST%` (`AliasManager.HOST`), which `top-logic.xml` sets from the variable `tl_host`
(environment variable or system property), e.g. `https://apps.example.com`. The development start
sets it to `http://localhost:<port>`; a production installation sets `tl_host` to the address the
users open in the browser. `PasskeyService` resolves the address once, when it starts
(`RelyingPartyAddress.resolve`):

- the **origin** is `%HOST%` normalized to what a browser reports: `scheme://host[:port]` with the
  scheme and host in lower case, without path or trailing slash, and without the default port of
  the scheme (`https://app.example.com:443/tl/` becomes `https://app.example.com`);
- the **relying party ID** is the host of that origin;
- `rp-id` and `origins` override them, e.g. to bind the passkeys to a parent domain
  (`rp-id="example.com"`) or to accept further addresses. Without `%HOST%`, `origins` alone
  suffice; the relying party ID is then the host of the first origin.

Nothing is taken from the HTTP request: behind a reverse proxy the request names the internal
address, and the relying party ID binds every passkey for good. The service logs the resolved
relying party ID and origins at startup (`INFO`).

```xml
<config service-class="com.top_logic.security.passkey.PasskeyService">
	<instance class="com.top_logic.security.passkey.PasskeyService"
		rp-id="example.com"
		origins="https://app.example.com,https://login.example.com"
	/>
</config>
```

**Passkeys not set up.** If neither `%HOST%` nor `origins` gives an origin, a value is malformed
(only `http` and `https` addresses), or `rp-id` does not fit every origin, the service logs an
`ERROR` at startup naming `tl_host` / `%HOST%` and the option `origins`, and stays up without
passkeys: every ceremony start is refused with "Passkeys are not set up for this installation", the
tooltip of the disabled passkey buttons. The rest of the login is not affected.

### The address of the page

When a passkey button is displayed, the client reports the origin of its page
(`window.location.origin`, `ReactWebAuthnControl.CMD_ORIGIN`). `PasskeyCeremony` compares it with
the accepted origins: on a page the relying party does not accept (e.g. the application opened by
its IP address or an internal host name), a ceremony would be refused - a registration only after
the authenticator has stored the key. So no ceremony is offered there: the button is disabled, its
tooltip names the address of the page and the addresses where passkeys work, and a `WARN` log names
the page origin, the accepted origins and the settings to fix. Options given before the report
arrives are withdrawn; the reason stays for every later offer.

### Cleaning up the authenticator

The application tells the browser (and through it the password manager or platform authenticator)
which passkeys it does not know, with the Web Authentication Signal API of
`ReactWebAuthnControl`; a browser without that API ignores the signals:

| Event | Signal |
|---|---|
| A registration is refused after the browser created the credential (wrong address, expired, not allowed, user not verified, refused by the library), or a credential answers a withdrawn registration | `signalUnknownCredential(rpId, credentialId)` - the authenticator drops the key it just stored |
| A login with a passkey the application does not know (no account has it, its account is deleted, or the user handle does not match) | `signalUnknownCredential(rpId, credentialId)` |
| A successful passkey login | `signalAllAcceptedCredentials(rpId, userHandle, ids)` with the IDs of all passkeys of the account, before the login completes - removes or hides passkeys deleted in the application |

A registration refused because the passkey is already registered sends no signal.

## The model

A passkey is a `tl.security.passkey:Passkey` (wrapper `com.top_logic.security.passkey.model.Passkey`),
held by the composite reference `tl.accounts:Person#passkeys` (read-only in forms): deleting an
account deletes its passkeys.

| Attribute | Meaning |
|---|---|
| `label` | Name shown to the user; a new passkey is named "Passkey" and the date of its creation, the user renames it. |
| `credentialId` | The credential ID (base64url), by which a login finds the passkey. Hidden. |
| `publicKey` | The COSE-encoded public key (base64url). Hidden. |
| `signatureCount` | The signature counter of the last login; zero for an authenticator without a counter. Hidden. |
| `transports` | Transport hints of the authenticator, e.g. `internal,hybrid`. |
| `backupEligible`, `backedUp` | Whether the passkey may be, respectively is, synchronized to other devices (a passkey of a password manager is). |
| `createdAt`, `lastUsed` | Time of creation and of the last login. |

Access goes through the account: `tl.security.passkey:Passkey` has the account as
`<access-parent>` (`<container reference="tl.accounts:Person#passkeys"/>`), so reading a passkey is
reading its account, renaming and deleting it is editing its account. The account holder (role
`PersonalProfile`) and an administrator manage the passkeys; nobody else sees them.

Java: `PasskeyService.getInstance()` with `getPasskeys(account)`, `isEnrolled(account)`,
`mayUsePasskeys(account)`, `isAvailable()` / `getAddress()` / `getRpId()` (the resolved relying
party, `null` while passkeys are not set up), and the two-step ceremonies `startRegistration` /
`finishRegistration` and `startAuthentication` / `finishAuthentication`. A `finish` method refuses
with a `PasskeyRefusedException` carrying the category of the refusal, the base64url credential ID
and whether the relying party does not know the credential (`isCredentialUnknown()`).

## The login flows

- **Passwordless** (usage `login`): the module's same-path overlay of `login-form.view.xml` adds
  `<passkey-login id="passkey-login">` (label "Sign in with passkey") to the form `credentials`,
  right after the single sign-on buttons (`<login-methods id="login-methods">`), as wide as they
  are. The form is shared by the login dialog and the login page, so both offer it; see
  [overlays](doc:view-layer/overlays).
  The browser offers the discoverable passkeys of any account; the login is completed with
  `LoginAction.completeLogin(...)` for the account the passkey belongs to, which re-checks
  maintenance mode and allowed groups. The password, its expiry and the second-factor step are not
  involved. With the usage `second-factor`, the button is hidden.
- **Second factor** (usage `second-factor`): the module adds the method `passkey`
  (`PasskeySecondFactor`) to `SecondFactorsConfig`, after the authenticator app `totp`. With the
  usage `login`, no account has the method set up or may set it up, so the second-factor step
  never offers it:
  - An account with a passkey gets `passkey-verify.view.xml`: a `<passkey-login account="account"
    auto-start="true">`, which accepts only the passkeys of that account.
  - An account with `MfaRequirement` `required` and no second factor may set up a passkey:
    `passkey-enroll.view.xml` with `<passkey-registration account="account" complete-login="true">`.
    The new passkey is created with user verification, so it confirms the account and completes
    the login.
  - With several methods set up or available, the login asks which one to use, see
    [the second factor of the React login](doc:authentication/mfa#second-factor-methods-of-the-react-login).

## View elements

`<passkey-login>` and `<passkey-registration>` render a button running the ceremony
(`PasskeyCeremonyElement`). Common options:

| Option | Meaning |
|---|---|
| `label` (mandatory), `image`, `appearance`, `css-class` | The button. |
| `account` | Channel with the account the ceremony runs for. It must be filled by the server (the login puts the account whose password it checked there), never by user input. While it holds no account, the button is disabled. |
| `auto-start` | Ask the authenticator as soon as the element is displayed. A browser that accepts the request only after a click refuses silently; the button stays available. |

A started ceremony lives on the server, so the challenge never leaves it. Every answer of the
browser consumes it and a fresh one is started for the next attempt. Shortly before the started
ceremony expires (`timeout`), the client asks for a fresh one (`ReactWebAuthnControl.CMD_REFRESH`),
so a button left alone works on its first click. A refused credential shows a message below the
button; a cancelled browser dialog only a short hint.

### `<passkey-login>`

`PasskeyLoginElement`. Without `account`, a passwordless login with any account's passkey. With
`account`, only the passkeys of that account are accepted - the passkey as second factor. On success
the login is completed for the passkey's account. The element without `account` is offered only for
the usage `login`, the element with `account` only for `second-factor`; otherwise it is hidden
(`ReactControl#setHidden`) and offers no ceremony, so a view places it unconditionally.

### `<passkey-registration>`

`PasskeyRegistrationElement` creates a passkey as the usage asks for it (discoverable for `login`,
non-discoverable for `second-factor`); the options exclude the passkeys the account already has, so
an authenticator does not create a second one for it.

- Without `complete-login`: the account is the logged-in user, or the account on `account`, which
  must then be the logged-in user (otherwise the button is disabled and says why). The actions in
  `<on-success>` run afterwards with the stored passkey as argument `passkey`:

  ```xml
  <passkey-registration appearance="PRIMARY" image="css:bi bi-key">
  	<label>
  		<en>Create passkey</en>
  	</label>
  	<on-success>
  		<close-dialog/>
  	</on-success>
  </passkey-registration>
  ```

- `complete-login="true"`: the set-up of a second factor during the login. Requires `account` and
  excludes `<on-success>` (both are configuration errors). The button is offered only while
  `SecondFactorStep.decide(account)` decides to set up a factor, i.e. for a `required` account
  without any second factor; an account that has one must confirm itself with it instead.

### `<if-passkey-usage>`

`PasskeyUsageElement` shows its content only if the passkeys of the application have the given
`usage` (mandatory, `login` or `second-factor`), and otherwise an empty, hidden control that takes
no place in its container - no gap of the enclosing stack around it. The usage is configuration of
the service, so the element decides once, when its control is created. Views use it instead of
asking for the usage in TL-Script:

```xml
<if-passkey-usage usage="login">
	<text>
		<label>
			<en>A passkey signs you in without user name and password.</en>
		</label>
	</text>
	<view-ref view="passkey-list.view.xml">
		<bind channel="account" to="account"/>
	</view-ref>
</if-passkey-usage>
```

The element has no control of its own: a sole content element stands in its place, several are
combined in a column (`ContentControls.combine`). It carries an `id` like any element, so an overlay
can address and place it.

## Managing passkeys

- **Settings**: the overlay of `settings-tabs.view.xml` places the passkeys on the "Security" tab
  where the usage puts them, each place in an `<if-passkey-usage>`:
  - usage `login`: a section "Sign in with passkey" of its own (`passkey-login-section`), right
    after the section "Password" (`<stack config:ref="security-sections">`,
    `config:position="after" config:reference="password-section"`);
  - usage `second-factor`: a block "Passkeys" (`passkeys`) in the section "Two-factor
    authentication", after the authenticator app (`<stack config:ref="two-factor-methods">`).

  Both show a line saying what a passkey does there and the shared `passkey-list.view.xml` (bound
  to the channel `account`): an `<object-list>` of the passkeys of the account
  (`passkey-item.view.xml`: name, creation and last use, rename via `passkey-rename.view.xml`,
  delete) and "Add passkey", visible for `passkeyMayUse($account)`. Adding
  and deleting ask for the password first (`<verify-identity/>`); adding then opens
  `passkey-add.view.xml`, because the browser asks the authenticator only in answer to a click and
  the click that opened the dialog was spent on the password. The overlay adds
  `tl.security.passkey:Passkey` to the `observed-types` of the channel `secondFactors`, so the
  status line of the tab follows added and deleted passkeys.
- **Account administration**: the overlay of `admin/access-control/accounts.view.xml` adds a panel
  "Passkeys" below the account form (`<panel config:ref="detail-panel">`): a table of the passkeys
  of the selected account and a delete command with `<delete-object/>`. An administrator may delete
  every passkey; a `required` account that loses its last second factor sets a new one up at its
  next login.

### The last-factor rule: `<delete-passkey/>`

`<delete-passkey/>` (`DeletePasskeyAction`, a `<delete-object/>`) deletes a passkey on behalf of its
account. With the usage `second-factor`, for an account whose `MfaRequirement` is `required`, it
refuses the deletion of the last
passkey unless another configured second-factor method is set up for the account (e.g. the
authenticator app). Its intrinsic rule disables the command with the reason, and re-decides on every
change of a passkey or an account (`TypeObservingRule`), so deleting one of two passkeys disables the
button of the other at once. The same applies the other way: `<disable-mfa/>` keeps the
authenticator app of a `required` account that has no passkey. With the usage `login`, a passkey
is no second factor, and every passkey may be deleted.

## TL-Script

- `passkeyMayUse(account)` (`PasskeyFunctions`): whether the account may create passkeys - never
  the anonymous account; for the usage `second-factor` not an account with `MfaRequirement`
  `disabled` either. Use it to offer "Add passkey".
- `secondFactorEnrolled(account)` of `com.top_logic.layout.view` lists the passkey among the second
  factors of an account that has one, for the usage `second-factor`.

## Passkeys and `MfaRequirement`

| `MfaRequirement` | Passkeys |
|---|---|
| `optional` | May be created. Usage `second-factor`: a passkey is asked for after the password. |
| `required` | Usage `second-factor`: a passkey counts as the second factor; an account without any factor may set up a passkey during the login; the last one cannot be deleted by the account itself. Usage `login`: the passkey login needs no further factor, the password login asks for the other configured methods. |
| `disabled` | Usage `login`: passkeys may be created and log in like for any other account - a passkey is a way to sign in like the password, not a second factor. Usage `second-factor`: no passkey can be created (`PasskeyService.startRegistration` refuses, the settings hide "Add passkey"). |

## Security notes

- **User verification**: the usage `login` always requires it, since the passkey is the only proof
  of the login. `user-verification` applies to the usage `second-factor` only, where the password
  is the other proof.
- **Signature counter**: a login whose counter did not increase is refused (a sign of a cloned
  authenticator). Authenticators without a counter (always zero, as synchronized passkeys) pass.
- **User handle**: the external form of the account's object ID - stable, unique, independent of
  the login name, which may change and which authenticators would show in their diagnostics. A
  passkey presented for another account than the one of the login is refused.
- **No attestation check**: attestation conveyance is `none` and untrusted attestation is accepted,
  so any authenticator model is allowed. An application that must restrict the authenticator models
  needs an attestation trust source in `PasskeyService`.
- Signature algorithms: ES256, EdDSA, ES384, ES512, RS256 - those the JVM verifies without an extra
  security provider.
- Every refused registration and login is logged (`Logger.warn`) with its category, the reason
  (the message of the library with its causes, where the library refused), the base64url credential
  ID, the accounts involved (registration: the account; login: the account of the user handle, the
  owner of the passkey and, for a second factor, the expected account) and the origin of the page
  from the client data. The user sees the message of the category: wrong address, unknown passkey,
  passkey of another account, user not verified, signature counter, request expired or answered,
  or the generic message with the reason of the library. The categories are decided by checks
  before the library runs, not by its messages.

## Pitfalls

- WebAuthn runs only in a secure context: HTTPS, or `http://localhost`. Use `localhost`, not
  `127.0.0.1` - an IP address is not a valid relying party ID. On a page served by plain `http`
  from another host, browsers hide the WebAuthn API (a passkey browser extension cannot help
  there either): the passkey buttons are shown disabled, with the request to open the application
  via HTTPS as their tooltip.
- A failed ceremony is reported through the messages of the window, with the text of the browser
  where it is not a cancellation; every failure is logged with the error name and message of the
  browser (`Logger.warn` for an unexpected one).
- Changing `rp-id` (or the host of `tl_host`, while `rp-id` is not set) makes every registered
  passkey unusable; the users have to create new ones.
- `tl_host` must be the address in the browser of the users, including `https` behind a
  TLS-terminating reverse proxy; a mismatch disables the passkey buttons with a tooltip naming the
  page address, see "The address of the page".
- With the usage `second-factor`, `disabled` does not remove existing passkeys: an account that
  has one keeps being asked for it after the password. Delete the passkeys when forbidding the
  second factor for such an account. With the usage `login`, `disabled` does not concern passkeys
  at all.
- Switching the usage from `second-factor` to `login` leaves the users with passkeys that cannot
  log in alone (they are not discoverable); they create new ones, see
  [Usage of the passkeys](#usage-of-the-passkeys).
- The browser asks the authenticator only in answer to a click of the user. A ceremony started by
  something else (`auto-start`, a dialog opened by a click spent on something else) may be refused;
  the button stays as the fallback.
- Testing without hardware: in Chrome, DevTools - More tools - WebAuthn - "Enable virtual
  authenticator environment" and add an authenticator with "Supports resident keys" and "Supports
  user verification". In Playwright the same is available through the Chrome DevTools protocol
  (`WebAuthn.enable`, `WebAuthn.addVirtualAuthenticator` with `hasResidentKey` and
  `hasUserVerification`, `isUserVerified`). Unit tests use the `SoftwareAuthenticator` of the test
  sources of this module.
