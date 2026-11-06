---
description: Read before offering users a password reset, a reset of their second factor or an invitation to create their own account - the UI-independent module tl-security-selfservice-core (InvitationModule with the invitation lifecycle - createInvitation, findOpenInvitation, acceptInvitation, renewInvitation, sendInvitation -, its options invitation-validity, invitation-link and authentication-device, InvitationLink, the localized mail scripts, the TL-Script function sendInvitation, the model type tl.security.selfservice:Invitation with its state open / expired / accepted), the classic module tl-security-selfservice (the login hook CreateLoginHoook, CheckInvitationCode, CreateAccountDialog, ForgotPasswordDialog, ResetMFADialog) and the React flows of tl-layout-view-selfservice (<request-password-reset>, <apply-password-reset>, forgot-password.view.xml, reset-password.view.xml).
order: 20
---

# Self-service: password reset, MFA reset, invitations

Users reset a forgotten password or a lost second factor themselves, and an administrator invites
people by e-mail to create their own account. The identity proof in all three is a verification code
sent by e-mail. The flows live in three modules:

| Module (artifact) | UI | Contents |
|---|---|---|
| `tl-security-selfservice-core` | none | `InvitationModule` with the invitation lifecycle, `sendInvitation`, the mail scripts and their texts, the `Invitation` model, its schema and migrations |
| `tl-security-selfservice` | classic | the dialogs for password reset, MFA reset and account creation, the admin dialogs for invitations; depends on `tl-security-selfservice-core` |
| `com.top_logic.layout.view.selfservice` (`tl-layout-view-selfservice`) | React view UI | password reset; depends on `tl-security-selfservice-core` for `InvitationModule` |

The second factor itself is described in [Multi-factor authentication](doc:authentication/mfa).

## Prerequisites

- Mail delivery: `tl-security-selfservice-core` depends on `tl-mail-smtp`; the mails are sent by
  the TL-Script function `sendMail(...)`. Without a configured `MailSenderService` the classic "Forgot password" and
  "Reset multi-factor authentication" commands are hidden.
- An e-mail address on the account (`getUser().getEMail()`), and for a password reset an
  `AuthenticationDevice` that allows password changes (`allowPwdChange()`).

## Configuration: `InvitationModule`

`InvitationModule` (`com.top_logic.security.selfservice.invitation`) holds the code parameters, the
options of the invitations and the four mail scripts. `tl-security-selfservice-core` configures it in
`tl-security-selfservice-core.conf.config.xml`:

```xml
<config service-class="com.top_logic.security.selfservice.invitation.InvitationModule">
	<instance class="com.top_logic.security.selfservice.invitation.InvitationModule"
		code-validity="5min"
		password-reset-timeout="15min"
		verification-code-size="6"
		invitation-validity="7d"
		authentication-device="dbSecurity"
	>
		<invitation-link page="/servlet/LayoutServlet"/>
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
| `invitation-validity` | How long an invitation can be accepted after it was created or renewed, default 7 days. |
| `invitation-link` | The page the invitation link leads to (`InvitationLink`), see [the link](doc:authentication/self-service#the-link). |
| `authentication-device` | The ID of the `AuthenticationDevice` on which an accepted invitation creates the account, default `dbSecurity`. |
| `invitation-mail` | Called with the `Invitation`, the application title and the invitation link. |
| `verification-mail` | Called with the `Invitation`, the application title and the code that confirms the invitation. |
| `reset-password-mail` | Called with the e-mail address, the application title and the reset code. |
| `reset-mfa-mail` | Called with the e-mail address, the application title and the reset code. |

The shipped scripts take subject and text from the resource bundle
`tl-security-selfservice-core.app-messages` (German and English), e.g.
`#"tl.security.selfservice.mail.invitation.body".fill($applicationName, $link).localize()`. The
keys are `tl.security.selfservice.mail.<mail>.subject` and `.body` with `<mail>` one of
`invitation`, `verification`, `resetPassword` and `resetMfa`; `{0}` is the application title, `{1}`
the link or code. The mail is written in the language of the current user - the administrator who
sends an invitation -, or in the default language of the application when nobody is logged in, as
for a password reset requested at the login. An invited person has no account and therefore no
language of its own. An application changes the texts by overriding the keys in its own bundle, or
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
| `id` | Hidden secure random string (`SecureStringDefaultProvider` with `size="32"`, 32 random bytes), the token in the invitation link. |
| `email` | Mandatory. The address the invitation goes to, and the user name of the new account. Its constraints reject an invalid address, an existing account with that name and a second open invitation for the same address (case-insensitive); expired and accepted invitations do not count. |
| `mfaRequirement` | `MfaRequirement` of the new account; overrides the default of the authentication device. |
| `createdAt`, `createdBy` | Derived: when and by whom the invitation was created, taken from the commit that created it. |
| `expires` | Until when the invitation can be accepted; set to the time of creation plus `invitation-validity` (`InvitationExpiryDefault`). |
| `accepted`, `account` | When the invitation was accepted and the account created from it; empty while it is not accepted. |
| `state` | Derived (`<query>`), the enumeration `tl.security.selfservice:InvitationState`: `ACCEPTED` if `accepted` is set, otherwise `EXPIRED` once `expires` has passed, otherwise `OPEN`. |
| `createCallback` | Derived (`tl.model.search:Expr`, a `<storage-algorithm><query>`): a function called with the new account. The base type answers `invitation -> person -> null`. |

`createCallback` is the hook for an application: a subtype of `Invitation` overrides the attribute
and may add attributes of its own (e.g. what the new user is to get access to). The function returns
`null` to accept the account, or an internationalizable value as the reason to refuse it, which
rolls the creation back.

Invitations are secured below the `SecurityStructure` singleton (access parent rule
`SecRootForInvitation` on `AccessManager`).

### The lifecycle

The operations of `InvitationModule` are independent of a user interface, so the classic dialogs,
React actions and scripts share them:

| Operation | Effect |
|---|---|
| `createInvitation(email, mfaRequirement)` | Creates an open invitation expiring after `invitation-validity`; the constraints of `email`, checked when the invitation is stored, refuse an address with an account or with another open invitation. |
| `findInvitation(token)` | The invitation with the token in any state, or `null`. |
| `findOpenInvitation(token)` | The open invitation with the token; otherwise a `TopLogicException` with `ERROR_UNKNOWN_INVITATION`, `ERROR_INVITATION_EXPIRED` or `ERROR_INVITATION_ACCEPTED` (`com.top_logic.security.selfservice.I18NConstants`), so the user interface can show the reason. |
| `acceptInvitation(invitation, surname, givenName, password)` | In one transaction: creates the account on the device `authentication-device` with the e-mail address as user name, the `mfaRequirement` of the invitation, surname, given name and e-mail address; checks the password against the password rules of the device and sets it, if the device manages passwords; runs `createCallback`, whose answer other than `null` refuses the account (`ERROR_ACCOUNT_CREATION_DENIED` with the answer as details); sets `accepted` and `account`. A refusal leaves neither account nor change. Expired and accepted invitations are refused. |
| `renewInvitation(invitation)` | Gives an open or expired invitation a new token and a new `expires`, so the old link is invalid; refuses an accepted one. |
| `invitationLink(invitation)` | The absolute link of the invitation. |
| `sendInvitation(invitation)` | Runs `invitation-mail` with the invitation, the application title and the link; refuses an invitation that is not open. |

An accepted invitation is kept as the record of the account created from it.

### The link

The link is built by `InvitationLink` from the public address of the application, the alias
`%HOST%` (variable `tl_host`, e.g. `https://app.example.com`), the context path (`%APP_CONTEXT%`),
the configured `page` and the token in the request parameter `invitation`
(`InvitationLink.INVITATION_PARAMETER`):
`https://app.example.com/app/servlet/LayoutServlet?invitation=0123abcd-...`. Without a configured
public address, the scheme, host and port are taken from the current request
(`LayoutUtils.appendHostURL(HttpServletRequest, ...)`), so an invitation sent from a classic or React
command of a development installation leads to the address the administrator works with. Only
without both, e.g. in a scheduled script of an installation without `tl_host`, no link is built
(`ERROR_NO_PUBLIC_ADDRESS`). A production installation sets `tl_host`, so that the link does not
depend on the address under which the sending administrator reached the application. Without `page`, the link leads to the `LayoutServlet`
(`ApplicationPages`) of the classic UI, whose login hook accepts the invitation; a user interface
with an entry point of its own configures its page:

```xml
<config service-class="com.top_logic.security.selfservice.invitation.InvitationModule">
	<instance>
		<invitation-link page="/servlet/MyInvitationPage"/>
	</instance>
</config>
```

### Creating and sending

The classic module `tl-security-selfservice` overlays the account table
(`admin/security/accounts/accountsTable.layout.overlay.xml`) with two dialogs:

- "Invite users" creates an `Invitation` for an e-mail address with `mfaRequirement` `optional` and
  sends it with `sendInvitation`.
- "Open invitations" lists the pending invitations, deletes one, or sends its link again.

From TL-Script, `sendInvitation($invitation)` (`SendInvitation` in `tl-security-selfservice-core`,
registered on `SearchBuilder`) calls `InvitationModule.sendInvitation(...)` and answers the result of
`invitation-mail`, `true` when the shipped script sent the
mail:

```
{
  invitation = new(`tl.security.selfservice:Invitation`);
  set($invitation, `tl.security.selfservice:Invitation#email`, "new.user@example.com");
  sendInvitation($invitation);
}
```

An expired invitation is sent again after `renewInvitation(...)`.

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

- Without the public address of the application (`tl_host`), an invitation can only be sent while a
  request is processed, and its link carries the address of that request.
- An invitation stored without expiry date gets "time of the update plus `invitation-validity`"
  from the migration `Ticket_30041_invitation_lifecycle` (startup action `InitInvitationExpiry`).
- The invitation link and its acceptance are classic UI: an application with the React view UI only
  has no flow that accepts an invitation. The same holds for the MFA reset.
- `CreateAccountDialog` always creates the account on the device with the id `dbSecurity`.
