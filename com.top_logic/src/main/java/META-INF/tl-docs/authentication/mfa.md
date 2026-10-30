---
description: Read before enabling, requiring or forbidding multi-factor authentication in a TopLogic application, or before adding a second-factor method to the React login - the MfaRequirement values optional / required / disabled, the mfa-requirement option of DBAuthenticationAccessDevice and LDAPAuthenticationAccessDevice, the account attributes tl.accounts:Person#mfaRequirement and #mfaSecret, the pluggable second factor of the React login (SecondFactor, AbstractSecondFactor, SecondFactorsConfig, SecondFactorStep, the choice dialog second-factor-choice.view.xml with <second-factor-choice>, TotpSecondFactor), the TOTP parameters of MFAConfig, the set-up and login dialogs of both UIs (EnableMultiFactorAuthenticationDialog, CheckOTPDialog, EnableMFACommand, <enroll-mfa>, <verify-otp>, <disable-mfa>, <enable-mfa>, MfaSupport), the TL-Script function secondFactorEnrolled and the admin commands that reset a second factor.
order: 10
---

# Multi-factor authentication

A second factor confirms a login after the password, before the session of the account starts.
The classic UI knows one method, a time-based one-time password (TOTP, RFC 6238) from an
authenticator app. The React view UI asks the second-factor methods the application configures
(`SecondFactorsConfig`); `com.top_logic.layout.view` configures TOTP, and a feature module adds
further methods - `tl-security-passkey` adds passkeys, see the passkeys article of this chapter.

The implementation lives in `com.top_logic` (account model, classic dialogs) and
`com.top_logic.layout.view` (React view UI); TOTP generation, verification and QR codes come from
the library `dev.samstevens.totp:totp`, a dependency of `com.top_logic`.

## What an account carries

Two attributes of `tl.accounts:Person` (declared in `tl.accounts.model.xml` of
`com.top_logic.element`) carry the requirement and the TOTP set-up; a further method keeps its
state with the account in its own way (the passkeys in `tl.accounts:Person#passkeys`):

| Attribute | Type | Meaning |
|---|---|---|
| `mfaSecret` | `tl.core:Password` | The TOTP secret (Base32, 32 characters). Hidden, stored encrypted through `CryptSupport` like every `Password` value. Present means: the account has the authenticator app set up. |
| `mfaRequirement` | `tl.accounts:MfaRequirement` (enum `MfaRequirement`) | Whether the account may, must or must not use a second factor. |

Java access: `Person.getMFASecret()`, `getMFARequirement()`, `setMFARequirement(...)`, the constants
`Person.MFA_SECRET_ATTR` / `MFA_REQUIREMENT_ATTR`.

The login decides on the set-up methods first: an account with a second factor set up (a stored
secret, a passkey, ...) is always asked for it, whatever its requirement says. The requirement
decides what happens without one and which set-up and switch-off commands an account gets:

| `MfaRequirement` | Without a second factor at login | User may set up | User may switch off |
|---|---|---|---|
| `optional` | Password alone | yes | yes |
| `required` | Set-up dialog before the session starts; cancelling it cancels the login | yes (forced) | classic UI: no; React UI: one method while another one stays set up |
| `disabled` | Password alone | no | - |

## Enabling MFA for an application

`mfa-requirement` is a mandatory option of `DBAuthenticationAccessDevice` and
`LDAPAuthenticationAccessDevice`. It is the value `Person.create(kb, name, device)` copies into a new
account; an existing account keeps its own value. The core configures (`top-logic.config.xml`):

```xml
<security-device
	id="dbSecurity"
	class="com.top_logic.base.security.device.db.DBAuthenticationAccessDevice"
	mfa-requirement="optional"
>
```

and for LDAP (`ldapConf.config.xml`) `mfa-requirement="%LDAP_MFA_REQUIREMENT%"`, filled from the
environment variable `ldap_mfa_requirement` (default `optional`). A custom `AuthenticationDevice`
implements `getMFARequirement()`.

An administrator changes the requirement of a single account in its form (field
`mfaRequirement`): in the classic UI through the account form of `com.top_logic.layout.formeditor`,
in the React UI in `admin/access-control/accounts.view.xml`. An account created through an
invitation gets the requirement of the invitation instead of the device default.

## Second-factor methods of the React login

In the React view UI the second factor is pluggable. A method is a `SecondFactor`
(`com.top_logic.layout.view.login`):

- `isEnrolled(account)` - whether the account has the method set up;
- `canEnroll(account)` - whether the account may set it up;
- `verify(context, account)` - opens the dialog in which the account confirms its login with it;
- `enroll(context, account)` - opens the dialog in which the account sets it up during the login;
- `getLabel()`, `getIcon()` - shown when the user chooses among several methods.

The dialogs replace the login dialog (`LoginAction.replaceDialog(context, view, channels)`), get the
account on the dialog channel `account` (`LoginAction.ACCOUNT_CHANNEL`) and end with
`LoginAction.completeLogin(context, userName)` once the account has proven itself; cancelling one
cancels the login. `AbstractSecondFactor` takes label and icon from the configuration
(`SecondFactor.Config`: `name`, `label`, `image`).

### Configuring the methods: `SecondFactorsConfig`

`SecondFactorsConfig` is an application configuration listing the methods, keyed by `name`, in the
order they are offered. `com.top_logic.layout.view` configures the authenticator app
(`tl-layout-view.conf.config.xml`):

```xml
<config config:interface="com.top_logic.layout.view.login.SecondFactorsConfig">
	<methods>
		<method name="totp"
			class="com.top_logic.layout.view.login.TotpSecondFactor"
			image="css:bi bi-phone"
		>
			<label>
				<en>Code from an authenticator app</en>
				<de>Code aus einer Authenticator-App</de>
			</label>
		</method>
	</methods>
</config>
```

A feature module adds its method with a fragment of its own (e.g. in `WEB-INF/autoconf/`), without
the view module depending on it; `tl-security-passkey` contributes `passkey` this way:

```xml
<config config:interface="com.top_logic.layout.view.login.SecondFactorsConfig">
	<methods>
		<method name="passkey"
			class="com.top_logic.security.passkey.view.PasskeySecondFactor"
			image="css:bi bi-key"
		>
			<label>
				<en>Passkey</en>
				<de>Passkey</de>
			</label>
		</method>
	</methods>
</config>
```

An application addresses a contributed method by its `name` in a fragment of its own, e.g. to change
its label.

`TotpSecondFactor` is the authenticator app: set up when the account has a `mfaSecret`, allowed
unless the requirement is `disabled`; it verifies in `otp.view.xml` and sets up in
`mfa-enroll.view.xml`.

### The decision: `SecondFactorStep`

After the password (and a forced password change), `LoginAction` lets
`SecondFactorStep.decide(account)` choose over the configured methods
(`SecondFactorStep.configured()`):

| Situation | `SecondFactorStep.Kind` | What happens |
|---|---|---|
| The account has methods set up | `VERIFY` | It confirms itself with one of them, whatever its requirement. |
| None set up, requirement `required` | `ENROLL` | It sets up one of the methods it may set up; without such a method the login is refused (`ERROR_NO_SECOND_FACTOR__USER`). |
| Otherwise | `COMPLETE` | The password alone completes the login. |

With a single method the step opens its dialog directly. With several, the login dialog is replaced
by `second-factor-choice.view.xml`, whose `<second-factor-choice input="account"/>`
(`SecondFactorChoiceElement`) shows one button per method of the step: an account with an app and a
passkey chooses which one to use, a `required` account without any chooses which one to set up.

## TOTP parameters

`MFAConfig` (`com.top_logic.layout.component.configuration`) is an application configuration; both
UIs read it for generating QR codes and verifying codes. Its defaults match common authenticator
apps, so applications rarely set it:

```xml
<config config:interface="com.top_logic.layout.component.configuration.MFAConfig"
	digits="6"
	period="30"
	allowed-time-period-discrepancy="1"
/>
```

`allowed-time-period-discrepancy` is the number of adjacent periods also accepted, to absorb clock
drift. The hash algorithm is SHA-1. The QR code encodes an `otpauth://` URI with the account name as
label and the application title (`com.top_logic.layout.I18NConstants.APPLICATION_TITLE`) as issuer.
Changing `digits` or `period` invalidates the set-ups of all existing accounts.

## Setting up the authenticator app

A fresh secret is generated from `SecureRandomService`; it is stored only after the user has entered
a valid code generated from it, so an aborted set-up leaves the account unchanged.

- Classic UI: `EnableMultiFactorAuthenticationDialog` shows the QR code and the secret as text for
  manual entry, then `CheckOTPDialog` confirms a code and stores the secret. The user opens it with
  the command "Enable multi-factor authentication" (`EnableMFACommand`, hidden for `disabled`) in
  the personal settings; the login opens it for a `required` account without a secret, after a
  message (`EnableMultiFactorAuthenticationDialog.informMFARequired`).
- React UI: `<enroll-mfa/>` (`EnrollMfaAction`) opens `mfa-enroll.view.xml` for the current user -
  the QR code (`<image input="qr">`) and a code field confirmed by `<verify-otp persist="true"/>`.
  The settings view `settings-tabs.view.xml` offers it on its "Security" tab; the login opens the
  same dialog when `TotpSecondFactor` is the method a `required` account without a second factor
  sets up. `EnrollMfaAction` refuses an account with `disabled`. `MfaSupport` holds the shared helpers (`generateSecret()`, `createQrCode(...)`,
  `isValidCode(...)`).

## Logging in with the second factor

The step comes after the credentials check, see [the login flow](doc:authentication#login-in-the-classic-ui).

- Classic UI: `CheckOTPDialog` asks for the code. Failed attempts are counted in
  `LoginFailuresModule` per secret; too many lock the check for a while
  (`CHECK_OTP_TOO_MANY_ATTEMPTS__TIMEOUT`). With `tl-security-selfservice` its toolbar offers
  "Reset multi-factor authentication", see the self-service article of this chapter.
- React UI: the configured methods the account has set up, see
  [the decision](#the-decision-secondfactorstep). For the authenticator app, `otp.view.xml` with
  `<verify-otp/>` (`VerifyOtpAction`) checks the code against the secret on the dialog channel
  `secret` and completes the login.

## Switching the authenticator app off and resetting it

| Who | Classic UI | React UI |
|---|---|---|
| The user, own account | "Deactivate multi-factor authentication" in the personal settings: clears `mfaSecret`; hidden for `required` | `<disable-mfa/>` (`DisableMfaAction`) on the "Security" settings tab removes the authenticator app: clears `mfaSecret` and sets `optional`. For `required` it clears `mfaSecret` and keeps the requirement, if another configured method stays set up for the account (e.g. a passkey); otherwise it refuses |
| An administrator | "Reset multi-factor authentication" in the account details (`detailsForm.layout.xml`): clears `mfaSecret` | `<enable-mfa/>` (`EnableMfaAction`, "Require MFA (enroll)" in `accounts.view.xml`): clears `mfaSecret` and sets `required`, so the next login sets a new factor up - unless the account keeps another method (a passkey), with which it then confirms its login |

A `required` account left without any second factor is sent into the set-up at its next login. The
classic self-service reset is described in the self-service article of this chapter.

`EnableMfaAction` with a `secret` attribute stores that fixed Base32 secret instead; this exists for
tests of the code check with a known secret.

Other methods have their own management; the passkeys of an account are listed, added and deleted
in the settings and the account administration, see the passkeys article of this chapter.

## Status of the second factor in a view

`secondFactorEnrolled(account)` (`SecondFactorFunctions`, TL-Script) lists the labels of the
configured methods the account has set up, empty for an account logging in with its password alone.
The section "Two-factor authentication" of the "Security" tab of `settings-tabs.view.xml`
(`two-factor-section`) computes its status from it in the derived channel `secondFactors`
(`observed-types="tl.accounts:Person"`); a module whose method keeps its state in objects of its
own adds their type to the `observed-types` of that channel with an overlay, as
`tl-security-passkey` does for `tl.security.passkey:Passkey`. Below the status, the column
`two-factor-methods` holds one block per method - the authenticator app (`authenticator-app`) in
the base view; a module appends the block of its own method with
`<stack config:ref="two-factor-methods">`.

## Pitfalls

- `disabled` does not remove a stored secret: an account that has one keeps being asked for a code.
  Clear `mfaSecret` when forbidding the second factor for an account that already has one.
- Write the secret with `tUpdateByName(Person.MFA_SECRET_ATTR, password)` and a `Password` value
  (`Password.fromPlainText(...)`), not its encrypted string.
- There are no recovery codes. A user who loses the authenticator app and has no other second
  factor needs an administrator reset or, in the classic UI, the e-mail based self-service reset.
  A second method set up (in the React UI, e.g. a passkey) serves as fallback.
- The classic UI knows TOTP only: an account whose only second factor is a passkey is treated
  there as having none - `optional` logs in with the password alone, `required` is sent into the
  TOTP set-up.
- An account without `mfaRequirement` (created before the attribute existed, or by code that does
  not pass a device to `Person.create`) behaves like `optional`.
