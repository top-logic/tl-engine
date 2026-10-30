---
description: Read before enabling, requiring or forbidding multi-factor authentication in a TopLogic application - the MfaRequirement values optional / required / disabled, the mfa-requirement option of DBAuthenticationAccessDevice and LDAPAuthenticationAccessDevice, the account attributes tl.accounts:Person#mfaRequirement and #mfaSecret, the TOTP parameters of MFAConfig, the set-up and login dialogs of both UIs (EnableMultiFactorAuthenticationDialog, CheckOTPDialog, EnableMFACommand, <enroll-mfa>, <verify-otp>, <disable-mfa>, <enable-mfa>, MfaSupport) and the admin commands that reset a second factor.
order: 10
---

# Multi-factor authentication

The second factor of a TopLogic account is a time-based one-time password (TOTP, RFC 6238) from an
authenticator app. It is checked after the password, before the session of the account starts. The
implementation lives in `com.top_logic` (account model, classic dialogs) and
`com.top_logic.layout.view` (React view UI); TOTP generation, verification and QR codes come from
the library `dev.samstevens.totp:totp`, a dependency of `com.top_logic`.

## What an account carries

Two attributes of `tl.accounts:Person` (declared in `tl.accounts.model.xml` of
`com.top_logic.element`):

| Attribute | Type | Meaning |
|---|---|---|
| `mfaSecret` | `tl.core:Password` | The TOTP secret (Base32, 32 characters). Hidden, stored encrypted through `CryptSupport` like every `Password` value. Present means: the account has a second factor. |
| `mfaRequirement` | `tl.accounts:MfaRequirement` (enum `MfaRequirement`) | Whether the account may, must or must not use a second factor. |

Java access: `Person.getMFASecret()`, `getMFARequirement()`, `setMFARequirement(...)`, the constants
`Person.MFA_SECRET_ATTR` / `MFA_REQUIREMENT_ATTR`.

The login decides on the secret first: an account with a stored secret is always asked for a code,
whatever its requirement says. The requirement decides what happens without a secret and which
set-up and switch-off commands an account gets:

| `MfaRequirement` | Without a secret at login | User may set up | User may switch off |
|---|---|---|---|
| `optional` | Password alone | yes | yes |
| `required` | Set-up dialog before the session starts; cancelling it cancels the login | yes (forced) | no |
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

## Setting up a second factor

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
  same dialog for a `required` account without a secret. `EnrollMfaAction` refuses an account with
  `disabled`. `MfaSupport` holds the shared helpers (`generateSecret()`, `createQrCode(...)`,
  `isValidCode(...)`).

## Logging in with the second factor

The step comes after the credentials check, see [the login flow](doc:authentication#login-in-the-classic-ui).

- Classic UI: `CheckOTPDialog` asks for the code. Failed attempts are counted in
  `LoginFailuresModule` per secret; too many lock the check for a while
  (`CHECK_OTP_TOO_MANY_ATTEMPTS__TIMEOUT`). With `tl-security-selfservice` its toolbar offers
  "Reset multi-factor authentication", see the self-service article of this chapter.
- React UI: `otp.view.xml` with `<verify-otp/>` (`VerifyOtpAction`) checks the code against the
  secret on the dialog channel `secret` and completes the login.

## Switching a second factor off and resetting it

| Who | Classic UI | React UI |
|---|---|---|
| The user, own account | "Deactivate multi-factor authentication" in the personal settings: clears `mfaSecret`; hidden for `required` | `<disable-mfa/>` (`DisableMfaAction`) on the "Security" settings tab: clears `mfaSecret` and sets `optional`; refused for `required` |
| An administrator | "Reset multi-factor authentication" in the account details (`detailsForm.layout.xml`): clears `mfaSecret` | `<enable-mfa/>` (`EnableMfaAction`, "Require MFA (enroll)" in `accounts.view.xml`): clears `mfaSecret` and sets `required`, so the next login sets a new factor up |

A cleared secret on a `required` account sends the next login into the set-up dialog. The classic
self-service reset is described in the self-service article of this chapter.

`EnableMfaAction` with a `secret` attribute stores that fixed Base32 secret instead; this exists for
tests of the code check with a known secret.

## Pitfalls

- `disabled` does not remove a stored secret: an account that has one keeps being asked for a code.
  Clear `mfaSecret` when forbidding the second factor for an account that already has one.
- Write the secret with `tUpdateByName(Person.MFA_SECRET_ATTR, password)` and a `Password` value
  (`Password.fromPlainText(...)`), not its encrypted string.
- The secret is the only factor state: there are no recovery codes. A user who loses the
  authenticator app needs an administrator reset or, in the classic UI, the e-mail based
  self-service reset.
- An account without `mfaRequirement` (created before the attribute existed, or by code that does
  not pass a device to `Person.create`) behaves like `optional`.
