---
description: Read before letting users of a React application log in with passkeys (WebAuthn) - the module tl-security-passkey, the PasskeyService options rp-id / origins / rp-name / user-verification / timeout, the model type tl.security.passkey:Passkey and the reference tl.accounts:Person#passkeys, the passwordless login button and the passkey as second factor (PasskeySecondFactor, passkey-verify.view.xml, passkey-enroll.view.xml), the view elements <passkey-login> and <passkey-registration> (account, auto-start, complete-login, <on-success>), the passkeys in the settings and the account administration, <delete-passkey/> and its last-factor rule, the TL-Script function passkeyMayUse, the interplay with MfaRequirement, and testing with a virtual authenticator.
order: 15
---

# Passkeys

A passkey is a Web Authentication (WebAuthn) credential: a key pair the authenticator of the user
creates for the application - the platform authenticator of the device (Windows Hello, Touch ID,
Android), a password manager or a security key. The private key never leaves the authenticator; the
application stores the public key and checks a signature over a challenge at every login.

The module `tl-security-passkey` uses passkeys in the React view UI in two ways:

- **Passwordless login**: a button on the login form logs in with a passkey alone. The user picks
  one of the passkeys the authenticator holds for the application, and the authenticator verifies
  the user (PIN, biometrics). Possession of the key and the verification of the user are two
  factors, so this login asks for no further factor.
- **Second factor**: after the password, an account with a passkey confirms the login with it, as
  one of the configured second-factor methods, see
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
| `rp-id` | empty | The relying party ID: the domain the passkeys are bound to, e.g. `example.com`. A passkey works on this domain and its subdomains only. Empty: the host name of the request that starts a ceremony. |
| `origins` | empty | Comma-separated origins a ceremony may run on, e.g. `https://app.example.com`. Empty: scheme, host and port (default ports left out) of the request that starts a ceremony. |
| `rp-name` | application title | The name an authenticator shows when it creates a passkey (`ResKey`). |
| `user-verification` | `REQUIRED` | Whether the authenticator must verify its user when a passkey is created or used as second factor: `REQUIRED`, `PREFERRED`, `DISCOURAGED`. The passwordless login always requires it. |
| `timeout` | `5min` | How long the browser waits for the user, and how long a started ceremony is accepted. |

Without `rp-id` and `origins` a local installation works without configuration. **A production
installation behind a reverse proxy must configure both**: the request the application sees then
names the internal host, not the address in the browser, and every ceremony fails. In the
configuration of the application:

```xml
<config service-class="com.top_logic.security.passkey.PasskeyService">
	<instance class="com.top_logic.security.passkey.PasskeyService"
		rp-id="example.com"
		origins="https://app.example.com"
	/>
</config>
```

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
`mayUsePasskeys(account)`, and the two-step ceremonies `startRegistration` / `finishRegistration`
and `startAuthentication` / `finishAuthentication`.

## The login flows

- **Passwordless**: the module's same-path overlay of `login-form.view.xml` adds
  `<passkey-login id="passkey-login">` (label "Sign in with passkey") to the form `credentials`,
  right after the single sign-on buttons (`<login-methods id="login-methods">`), as wide as they
  are. The form is shared by the login dialog and the login page, so both offer it; see
  [overlays](doc:view-layer/overlays).
  The browser offers the discoverable passkeys of any account; the login is completed with
  `LoginAction.completeLogin(...)` for the account the passkey belongs to, which re-checks
  maintenance mode and allowed groups. The password, its expiry and the second-factor step are not
  involved.
- **Second factor**: the module adds the method `passkey` (`PasskeySecondFactor`) to
  `SecondFactorsConfig`, after the authenticator app `totp`:
  - An account with a passkey gets `passkey-verify.view.xml`: a `<passkey-login account="account"
    auto-start="true">`, which accepts only the passkeys of that account.
  - An account with `MfaRequirement` `required` and no second factor may set up a passkey:
    `passkey-enroll.view.xml` with `<passkey-registration account="account" complete-login="true">`.
    The new passkey is created with user verification, so it confirms the account and completes
    the login.
  - With several methods set up or available, the login asks which one to use, see
    [the second factor of the React login](doc:authentication/mfa#second-factor-methods-of-the-react-login).

## View elements

Both elements render a button running the ceremony (`PasskeyCeremonyElement`). Common options:

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
the login is completed for the passkey's account.

### `<passkey-registration>`

`PasskeyRegistrationElement` creates a discoverable passkey; the options exclude the passkeys the
account already has, so an authenticator does not create a second one for it.

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

## Managing passkeys

- **Settings**: the overlay of `settings-tabs.view.xml` appends a "Passkeys" section to the
  "Security" tab (`<stack config:ref="security-actions">`): an `<object-list>` of the passkeys of the
  account (`passkey-item.view.xml`: name, creation and last use, rename via
  `passkey-rename.view.xml`, delete) and "Add passkey", visible for `passkeyMayUse($account)`. Adding
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
account. For an account whose `MfaRequirement` is `required`, it refuses the deletion of the last
passkey unless another configured second-factor method is set up for the account (e.g. the
authenticator app). Its intrinsic rule disables the command with the reason, and re-decides on every
change of a passkey or an account (`TypeObservingRule`), so deleting one of two passkeys disables the
button of the other at once. The same applies the other way: `<disable-mfa/>` keeps the
authenticator app of a `required` account that has no passkey.

## TL-Script

- `passkeyMayUse(account)` (`PasskeyFunctions`): whether the account may create passkeys - not the
  anonymous account, not an account with `MfaRequirement` `disabled`. Use it to offer "Add passkey".
- `secondFactorEnrolled(account)` of `com.top_logic.layout.view` lists the passkey among the second
  factors of an account that has one.

## Passkeys and `MfaRequirement`

| `MfaRequirement` | Passkeys |
|---|---|
| `optional` | May be created; a passkey is asked for after the password. |
| `required` | A passkey counts as the second factor; an account without any factor may set up a passkey during the login; the last one cannot be deleted by the account itself. |
| `disabled` | No passkey can be created (`PasskeyService.startRegistration` refuses, the settings hide "Add passkey"). |

## Security notes

- **User verification**: the passwordless login always requires it, since the passkey is its only
  proof. `user-verification` affects only the registration and the passkey as second factor, after
  the password; `REQUIRED` keeps a passkey created there usable for the passwordless login.
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
- Failed passkey logins are logged (`Logger.info`) with the reason; the user sees a generic message.

## Pitfalls

- WebAuthn runs only in a secure context: HTTPS, or `http://localhost`. Use `localhost`, not
  `127.0.0.1` - an IP address is not a valid relying party ID. On a page served by plain `http`
  from another host, browsers hide the WebAuthn API (a passkey browser extension cannot help
  there either): the passkey buttons are shown disabled, with the request to open the application
  via HTTPS as their tooltip.
- A failed ceremony is reported through the messages of the window, with the text of the browser
  where it is not a cancellation; every failure is logged with the error name and message of the
  browser (`Logger.warn` for an unexpected one).
- Changing `rp-id` (or the domain of the application, while `rp-id` is derived from the request)
  makes every registered passkey unusable; the users have to create new ones.
- `disabled` does not remove existing passkeys: an account that has one keeps being asked for it
  after the password and can still log in with it alone. Delete the passkeys when forbidding the
  second factor for such an account.
- The browser asks the authenticator only in answer to a click of the user. A ceremony started by
  something else (`auto-start`, a dialog opened by a click spent on something else) may be refused;
  the button stays as the fallback.
- Testing without hardware: in Chrome, DevTools - More tools - WebAuthn - "Enable virtual
  authenticator environment" and add an authenticator with "Supports resident keys" and "Supports
  user verification". In Playwright the same is available through the Chrome DevTools protocol
  (`WebAuthn.enable`, `WebAuthn.addVirtualAuthenticator` with `hasResidentKey` and
  `hasUserVerification`, `isUserVerified`). Unit tests use the `SoftwareAuthenticator` of the test
  sources of this module.
