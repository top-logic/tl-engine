---
description: Read before offering users a password reset, a reset of their second factor or an invitation to create their own account - the module tl-security-selfservice (InvitationModule and its mail scripts, the model type tl.security.selfservice:Invitation, the TL-Script function sendInvitation, the login hook CreateLoginHoook, CheckInvitationCode, CreateAccountDialog, ForgotPasswordDialog, ResetMFADialog) and the React flows of tl-layout-view-selfservice (<request-password-reset>, <apply-password-reset>, forgot-password.view.xml, reset-password.view.xml).
order: 20
---

# Self-service: password reset, MFA reset, invitations

Users reset a forgotten password or a lost second factor themselves, and an administrator invites
people by e-mail to create their own account. The identity proof in all three is a verification code
sent by e-mail. The flows live in two modules:

| Module (artifact) | UI | Contents |
|---|---|---|
| `tl-security-selfservice` | classic | `InvitationModule`, the `Invitation` model, `sendInvitation`, the dialogs for password reset, MFA reset and account creation, the admin dialogs for invitations |
| `com.top_logic.layout.view.selfservice` (`tl-layout-view-selfservice`) | React view UI | password reset; depends on `tl-security-selfservice` for `InvitationModule` |

The second factor itself is described in [Multi-factor authentication](doc:authentication/mfa).

## Prerequisites

- Mail delivery: the module depends on `tl-mail-smtp`; the mails are sent by the TL-Script function
  `sendMail(...)`. Without a configured `MailSenderService` the classic "Forgot password" and
  "Reset multi-factor authentication" commands are hidden.
- An e-mail address on the account (`getUser().getEMail()`), and for a password reset an
  `AuthenticationDevice` that allows password changes (`allowPwdChange()`).

## Configuration: `InvitationModule`

`InvitationModule` (`com.top_logic.security.selfservice.invitation`) holds the code parameters and
the four mail scripts. The module configures it in `tl-security-selfservice.conf.config.xml`:

```xml
<config service-class="com.top_logic.security.selfservice.invitation.InvitationModule">
	<instance class="com.top_logic.security.selfservice.invitation.InvitationModule"
		code-validity="5min"
		password-reset-timeout="15min"
		verification-code-size="6"
	>
		<invitation-mail><![CDATA[invitation -> applicationName -> link -> { ... sendMail(...) ... }]]></invitation-mail>
		<verification-mail><![CDATA[invitation -> applicationName -> code -> { ... }]]></verification-mail>
		<reset-password-mail><![CDATA[email -> applicationName -> code -> { ... }]]></reset-password-mail>
		<reset-mfa-mail><![CDATA[email -> applicationName -> code -> { ... }]]></reset-mfa-mail>
	</instance>
</config>
```

| Option | Meaning |
|---|---|
| `code-validity` | How long a verification code is accepted after it was created. |
| `verification-code-size` | Number of digits of a code (4 to 18). |
| `invitation-mail` | Called with the `Invitation`, the application title and the invitation link. |
| `verification-mail` | Called with the `Invitation`, the application title and the code that confirms the invitation. |
| `reset-password-mail` | Called with the e-mail address, the application title and the reset code. |
| `reset-mfa-mail` | Called with the e-mail address, the application title and the reset code. |

The shipped mail texts are English. An application that wants its own texts or languages
redefines the scripts in its own configuration of the service. `reset-password-mail` and
`reset-mfa-mail` run before anyone is logged in and are therefore executed without access checks;
the other two run with the access rights of the current user.

## Password reset

Classic UI: `tl-security-selfservice` adds "Forgot password" (`ForgotPasswordDialog$Opener`) to the
toolbar of the login dialog through `LoginViewDialog$Config`. In `ForgotPasswordDialog` the user
enters the user name and requests a code; the code is mailed through `reset-password-mail` only if
the account exists, allows a password change and has an e-mail address, and the dialog shows the
same message in every case, so it does not reveal which user names exist. A correct code opens
`ChangePasswordDialog`; after the change the user logs in with the new password.

React UI: `tl-layout-view-selfservice` contributes "Forgot password?" to `<login-commands/>`
(`WEB-INF/autoconf/login.selfservice.config.xml`, see
[contributing a login command](doc:authentication#contributing-a-login-command)).
`forgot-password.view.xml` takes the user name (`tl.login:ForgotPassword`) and runs
`<request-password-reset/>` (`RequestPasswordResetAction`), which generates a code, mails it under the
same conditions and opens `reset-password.view.xml`. That dialog takes the code and the new password
twice (`tl.login:PasswordReset`) and runs `<apply-password-reset/>` (`ApplyPasswordResetAction`): it
checks the code and its age against `code-validity` and sets the password through
`AuthenticationDevice.setPassword(...)`, which applies the password policy of the device. The user
is not logged in by the reset.

## Resetting the second factor

Classic UI only. `tl-security-selfservice` adds "Reset multi-factor authentication"
(`ResetMFADialog$Opener`) to the toolbar of `CheckOTPDialog` through `CheckOTPDialog$Config`, so it
is offered after the password was accepted, when the account has a secret and an e-mail address and
mail is configured. `ResetMFADialog` mails a code through `reset-mfa-mail`; a correct code clears the
secret and logs the session out. At the next login an `optional` account logs in with the password
alone, a `required` account sets a new second factor up.

## Limits on codes (classic dialogs)

`AbstractVerificationCodeDialog`, the base of the classic code dialogs, keeps the code in the dialog
(it is never stored in the database), accepts three wrong entries per code and throttles code
requests per user name, account or invitation through `LoginFailuresModule`, so nobody can trigger
a stream of mails. The React password reset has neither limit.

## Invitations

### The model

`tl.security.selfservice:Invitation` (`tl.security.selfservice.model.xml`, wrapper `Invitation`):

| Attribute | Meaning |
|---|---|
| `id` | Hidden secure random string (`SecureStringDefaultProvider`, 16 random bytes), part of the invitation link. |
| `email` | Mandatory. The address the invitation goes to, and the user name of the new account. Its constraints reject an invalid address, an existing account with that name and a second invitation for the same address (case-insensitive). |
| `mfaRequirement` | `MfaRequirement` of the new account; overrides the default of the authentication device. |
| `createCallback` | Derived (`tl.model.search:Expr`, a `<storage-algorithm><query>`): a function called with the new account. The base type answers `invitation -> person -> null`. |

`createCallback` is the hook for an application: a subtype of `Invitation` overrides the attribute
and may add attributes of its own (e.g. what the new user is to get access to). The function returns
`null` to accept the account, or an internationalizable value as the reason to refuse it, which
rolls the creation back.

Invitations are secured below the `SecurityStructure` singleton (access parent rule
`SecRootForInvitation` on `AccessManager`).

### Creating and sending

The module overlays the classic account table (`admin/security/accounts/accountsTable.layout.overlay.xml`)
with two dialogs:

- "Invite users" creates an `Invitation` for an e-mail address with `mfaRequirement` `optional` and
  sends it with `sendInvitation`.
- "Open invitations" lists the pending invitations, deletes one, or sends its link again.

From TL-Script, `sendInvitation($invitation)` (`SendInvitation`, registered on `SearchBuilder`) runs
`invitation-mail` with the link and answers its result, `true` when the shipped script sent the
mail:

```
{
  invitation = new(`tl.security.selfservice:Invitation`);
  set($invitation, `tl.security.selfservice:Invitation#email`, "new.user@example.com");
  sendInvitation($invitation);
}
```

The link is the `LayoutServlet` URL of the host of the current request with the parameter
`invitation=<id>`.

### Accepting

The login hook `CreateLoginHoook`, configured at the beginning of `<login-hooks>` of
`MainLayout$GlobalConfig`, reads the parameter `invitation`:

1. An unknown id shows an error message.
2. `CheckInvitationCode` lets the invitee request a code, mailed through `verification-mail` to the
   invitation's address, and checks it.
3. `CreateAccountDialog` shows the e-mail address as the fixed user name and asks for surname
   (mandatory), given name and, if the device allows it, a password checked against the device's
   policy. It creates the account on the authentication device `dbSecurity`, sets the invitation's
   `mfaRequirement` and the e-mail address, runs `createCallback` and deletes the invitation.
4. Depending on `mfaRequirement` the new user is offered (`optional`) or required (`required`) to set
   a second factor up, and is then logged in.

## Pitfalls

- Invitations do not expire; an unused invitation stays valid until it is deleted.
- `sendInvitation` builds the link from the current request, so it needs an interactive request
  (a command, not a background task).
- The invitation link and its acceptance are classic UI: an application with the React view UI only
  has no flow that accepts an invitation. The same holds for the MFA reset.
- `CreateAccountDialog` always creates the account on the device with the id `dbSecurity`.
