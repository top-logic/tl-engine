---
description: Read before offering users a password reset, a reset of their second factor, an invitation to create their own account or an open self-registration without invitation - the UI-independent module tl-security-selfservice-core (InvitationModule with the invitation lifecycle - createInvitation, findOpenInvitation, acceptInvitation, renewInvitation, sendInvitation, resendInvitation -, its options invitation-validity, invitation-link and authentication-device, InvitationLink, the configurable mail texts (invitation-text, verification-text, reset-password-text, reset-mfa-text, MailText) and mail scripts written in the language of the recipient, sendResetPasswordMail, sendResetMFAMail, the TL-Script functions sendInvitation, invitationRenew and invitationResend, the model type tl.security.selfservice:Invitation with its state open / expired / accepted and its language, CurrentUserLanguageDefault, the self-registration SelfRegistration with its option self-registration - mode off / direct / approval, set from the variable tl_self_registration (alias %SELF_REGISTRATION%, default off), captcha, set from the variable tl_self_registration_captcha (alias %SELF_REGISTRATION_CAPTCHA%, default true), callback, registration-text, registration-mail, registration-exists-text, registration-exists-mail, approval-text, approval-mail -, requestCode, register, approve, pendingRegistration, the TL-Script functions registrationEnabled, registrationCaptcha, registrationApprove and registrationPending, the model type tl.security.selfservice:Registration with its state pending / approved, ApplicationAddress), the classic module tl-security-selfservice (the login hook CreateLoginHook, the shared dialogs CheckAccountCode and CreateAccountDialog with the classic AccountRequest (InvitationRequest, RegistrationRequest), the registration dialog RegisterDialog with its login command RegisterDialog$Opener and the captcha field, ForgotPasswordDialog, ResetMFADialog, AbstractVerificationCodeDialog, the admin dialogs "Invite users", "Invitations" and "Registrations" and the command "Approve registration" of the account table) and the React flows of tl-layout-view-selfservice (<request-password-reset>, <apply-password-reset>, forgot-password.view.xml, reset-password.view.xml, the invitation page accept-invitation.view.xml with <request-invitation-code>, the registration dialog register.view.xml with <request-registration-code> and the captcha, the shared dialogs verification-code.view.xml and create-account.view.xml with <check-verification-code>, <send-new-code>, <create-own-account> and the AccountRequest (InvitationRequest, RegistrationRequest), the login command "Register", the administration views admin/access-control/invitations.view.xml with create-invitation.view.xml and admin/access-control/registrations.view.xml, the command "Approve" added to accounts.view.xml), the approval of registered accounts and who may approve, the demo setup, and VerificationCode, the code of all these flows.
order: 20
---

# Self-service: password reset, MFA reset, invitations, registration

Users reset a forgotten password or a lost second factor themselves, and an administrator invites
people by e-mail to create their own account. The identity proof in all three is a verification code
sent by e-mail. In an application with an open user base, visitors also register an account
without an invitation. The flows live in three modules:

| Module (artifact) | UI | Contents |
|---|---|---|
| `tl-security-selfservice-core` | none | `InvitationModule` with the invitation lifecycle and the self-registration, `sendInvitation`, the mail scripts and their texts, the `Invitation` and `Registration` models, their schema and migrations |
| `tl-security-selfservice` | classic | the dialogs for password reset, MFA reset, account creation and registration, the admin dialogs for invitations and registrations; depends on `tl-security-selfservice-core` and on `tl-security-captcha` for the captcha field |
| `com.top_logic.layout.view.selfservice` (`tl-layout-view-selfservice`) | React view UI | password reset, the page accepting an invitation, the registration, the administration of the invitations and registrations; depends on `tl-security-selfservice-core` for `InvitationModule` and on `tl-security-captcha-view` for the captcha |

The second factor itself is described in [Multi-factor authentication](doc:authentication/mfa).

## Prerequisites

- Mail delivery: `tl-security-selfservice-core` depends on `tl-mail-smtp`; the mails are sent by
  the TL-Script function `sendMail(...)`. Without a configured `MailSenderService` the classic "Forgot password" and
  "Reset multi-factor authentication" commands are hidden.
- An e-mail address on the account (`getUser().getEMail()`), and for a password reset an
  `AuthenticationDevice` that allows password changes (`allowPwdChange()`).

## Configuration: `InvitationModule`

`InvitationModule` (`com.top_logic.security.selfservice.invitation`) holds the code parameters, the
options of the invitations and the four mail scripts with their texts. `tl-security-selfservice-core` configures it in
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
		<invitation-text>
			<subject>
				<en>Invitation to {0}</en>
				<de>Einladung zu {0}</de>
			</subject>
			<body>
				<en><![CDATA[<p>Dear Recipient,</p> ... <a href="{1}">{1}</a> ...]]></en>
				<de><![CDATA[<p>Guten Tag,</p> ... <a href="{1}">{1}</a> ...]]></de>
			</body>
		</invitation-text>
		<invitation-mail><![CDATA[invitation -> applicationName -> link -> subject -> body -> { ... sendMail(...) ... }]]></invitation-mail>
		<verification-text>...</verification-text>
		<verification-mail><![CDATA[invitation -> applicationName -> code -> subject -> body -> { ... }]]></verification-mail>
		<reset-password-text>...</reset-password-text>
		<reset-password-mail><![CDATA[email -> applicationName -> code -> subject -> body -> { ... }]]></reset-password-mail>
		<reset-mfa-text>...</reset-mfa-text>
		<reset-mfa-mail><![CDATA[email -> applicationName -> code -> subject -> body -> { ... }]]></reset-mfa-mail>
	</instance>
</config>
```

| Option | Meaning |
|---|---|
| `code-validity` | How long a verification code is accepted after it was created. |
| `verification-code-size` | Number of digits of a code (4 to 18). |
| `invitation-validity` | How long an invitation can be accepted after it was created or renewed, default 7 days. |
| `invitation-link` | The page the invitation link leads to (`InvitationLink`), see [the link](doc:authentication/self-service#the-link). |
| `authentication-device` | The ID of the `AuthenticationDevice` on which an accepted invitation or a registration creates the account, default `dbSecurity`. |
| `invitation-text` | Subject and content of the invitation mail; `{0}` is the application title, `{1}` the invitation link. |
| `invitation-mail` | Called with the `Invitation`, the application title, the invitation link and subject and content of `invitation-text`. |
| `verification-text` | Subject and content of the mail confirming an invitation; `{0}` is the application title, `{1}` the code. |
| `verification-mail` | Called with the `Invitation`, the application title, the code that confirms the invitation and subject and content of `verification-text`. |
| `reset-password-text` | Subject and content of the password reset mail; `{0}` is the application title, `{1}` the reset code. |
| `reset-password-mail` | Called with the e-mail address, the application title, the reset code and subject and content of `reset-password-text`. |
| `reset-mfa-text` | Subject and content of the mail resetting the second factor; `{0}` is the application title, `{1}` the reset code. |
| `reset-mfa-mail` | Called with the e-mail address, the application title, the reset code and subject and content of `reset-mfa-text`. |
| `self-registration` | Registration without invitation, see [Self-registration](doc:authentication/self-service#self-registration). |

### Mail texts

The `*-text` options are configurations of `MailText` (`tl-mail-smtp`, package
`com.top_logic.base.mail`) with an internationalized `subject` and an internationalized HTML `body`.
The shipped texts are German and English and written inline in
`tl-security-selfservice-core.conf.config.xml`; an application changes them in its own
configuration of the service, also in the service editor of the running application:

```xml
<config service-class="com.top_logic.security.selfservice.invitation.InvitationModule">
	<instance>
		<invitation-text>
			<subject>
				<en>Your access to {0}</en>
				<de>Ihr Zugang zu {0}</de>
			</subject>
			<body>
				<en><![CDATA[<p>Welcome!</p><p><a href="{1}">Create your account</a></p>]]></en>
				<de><![CDATA[<p>Willkommen!</p><p><a href="{1}">Zugang anlegen</a></p>]]></de>
			</body>
		</invitation-text>
	</instance>
</config>
```

### Mail scripts

Each script gets subject and content of its text after its other arguments - as fourth and fifth
argument, `registration-exists-mail`, which sends no code, as third and fourth -, unfilled, and
fills them itself, so it can add further information:

```
invitation -> applicationName -> link -> subject -> body -> {
  mailBody = $body.fill($applicationName, $link).localize();
  mailSubject = $subject.fill($applicationName).localize();
  ...
}
```

A script declaring only the first three parameters keeps working; it ignores the texts. The shipped
scripts send the HTML content as `text/html` by wrapping it in
`binary(name: "mail.html", contentType: "text/html", data: ...)`, since `sendMail` sends a plain
string as `text/plain`. An application redefines the scripts in its own configuration of the
service. `reset-password-mail`, `reset-mfa-mail` and `verification-mail` run before anyone is
logged in and are therefore executed without access checks; `invitation-mail` runs with the access
rights of the administrator sending it.

### Language of the mails

`InvitationModule` runs each script with the language of the recipient as the locale of the current
sub-session (`SubSessionContext.withLocale(...)`), so `localize()` and the application title are in
this language, also in a redefined script:

- `invitation-mail` and `verification-mail` (also when an invitation is sent again): the `language`
  of the `Invitation` (`InvitationModule.invitationLanguage(...)`).
- `reset-password-mail` and `reset-mfa-mail`: the language of the account the mail is for
  (`InvitationModule.accountLanguage(...)`).
- The mails of the [self-registration](doc:authentication/self-service#self-registration):
  `registration-exists-mail` and `approval-mail` in the language of the account the mail is about;
  `registration-mail` in the default language of the application
  (`InvitationModule.defaultLanguage()`), since the visitor has neither an account nor an invitation.

Without such a language, the mail is written in the default language of the application
(`DefaultLocaleDefault`).

## Password reset

Classic UI: `tl-security-selfservice` adds "Forgot password" (`ForgotPasswordDialog$Opener`) to the
toolbar of the login dialog through `LoginViewDialog$Config`. In `ForgotPasswordDialog` the user
enters the user name and requests a code; the code is mailed through `reset-password-mail`
(`InvitationModule.sendResetPasswordMail(account, code)`) only if
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
mail is configured. `ResetMFADialog` mails a code through `reset-mfa-mail`
(`InvitationModule.sendResetMFAMail(account, code)`); a correct code clears the
secret and logs the session out. At the next login an `optional` account logs in with the password
alone, a `required` account sets a new second factor up.

## Limits on codes

A code is a random number of `verification-code-size` digits (`VerificationCode.newCode()`), kept
by the dialog that sent it and never stored in the database.

- `VerificationCode` of `tl-security-selfservice-core` holds the limits: `check(entered)` refuses
  an expired code and, after `MAX_MISMATCHES` (three) wrong entries, the code altogether;
  `throttleRequest(key)` counts each request in `LoginFailuresModule` and lets each further request
  under the same key wait longer, so nobody can trigger a stream of mails.
- `AbstractVerificationCodeDialog`, the base of the classic code dialogs, keeps such a code and
  throttles code requests per user name, account, invitation or registered address. A correct code
  lifts the throttling of its key.
- The React acceptance of an invitation and the registration in both UIs use `VerificationCode`
  the same way.
- The React password reset has neither limit.

## Invitations

### The model

`tl.security.selfservice:Invitation` (`tl.security.selfservice.model.xml`, wrapper `Invitation`):

| Attribute | Meaning |
|---|---|
| `id` | Hidden secure random string (`SecureStringDefaultProvider` with `size="32"`, 32 random bytes), the token in the invitation link. |
| `email` | Mandatory. The address the invitation goes to, and the user name of the new account. Its constraints reject an invalid address, an existing account with that name and a second open invitation for the same address (case-insensitive); expired and accepted invitations do not count. |
| `mfaRequirement` | `MfaRequirement` of the new account; overrides the default of the authentication device. |
| `language` | The language of the mails of the invitation and of the new account; one of the supported languages of the application (`supportedLocales`). The default is the language of the user creating the invitation (`CurrentUserLanguageDefault` of `com.top_logic`). Without a language, the mails are written in the default language of the application and the account gets the default language. |
| `createdAt`, `createdBy` | Derived: when and by whom the invitation was created, taken from the commit that created it. |
| `expires` | Until when the invitation can be accepted; set to the time of creation plus `invitation-validity` (`InvitationExpiryDefault`). The default is computed in the transaction storing the invitation (`DefaultProvider#isComputedInTransaction()`): a transient draft, such as the one a create dialog edits, has no expiry date, and storing it with `<persist-transient>` or `$draft.copy(transient: false, skipTransactionDefaults: true)` counts the validity from that moment. The token `id` is generated on the draft already and is kept when it is stored. |
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
| `acceptInvitation(invitation, surname, givenName, password)` | In one transaction: creates the account on the device `authentication-device` with the e-mail address as user name, the `mfaRequirement` of the invitation, surname, given name and e-mail address, and the `language` of the invitation, if set; checks the password against the password rules of the device and sets it, if the device manages passwords; runs `createCallback`, whose answer other than `null` refuses the account (`ERROR_ACCOUNT_CREATION_DENIED` with the answer as details); sets `accepted` and `account`. A refusal leaves neither account nor change. Expired and accepted invitations are refused. |
| `renewInvitation(invitation)` | Gives an open or expired invitation a new token and a new `expires`, so the old link is invalid; refuses an accepted one. |
| `invitationLink(invitation)` | The absolute link of the invitation. |
| `sendInvitation(invitation)` | Runs `invitation-mail` with the invitation, the application title, the link and the texts of `invitation-text`, in the language of the invitation; refuses an invitation that is not open. |
| `resendInvitation(invitation)` | Sends an open invitation again with its link; renews an expired invitation first, so it is sent with a new link and a new `expires`; refuses an accepted one. |
| `sendVerificationMail(invitation, code)` | Runs `verification-mail` in the language of the invitation. |
| `sendResetPasswordMail(account, code)`, `sendResetMFAMail(account, code)` | Run `reset-password-mail` or `reset-mfa-mail` with the e-mail address of the account, in the language of the account; answer `false` without running the script if the account has no e-mail address. |

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
(`ApplicationPages`) of the classic UI, whose login hook accepts the invitation. The React module
`tl-layout-view-selfservice` sets `page` to its invitation page `/view/accept-invitation.view.xml`
(`WEB-INF/conf/tl-layout-view-selfservice.conf.config.xml`); a user interface with an entry point of
its own configures its page in the same way:

```xml
<config service-class="com.top_logic.security.selfservice.invitation.InvitationModule">
	<instance>
		<invitation-link page="/servlet/MyInvitationPage"/>
	</instance>
</config>
```

### Creating and sending

The classic module `tl-security-selfservice` overlays the account table
(`admin/security/accounts/accountsTable.layout.overlay.xml`) with two dialogs for invitations (and
with the dialog "Registrations", see
[Approving registrations](doc:authentication/self-service#approving-registrations)):

- "Invite users" (`inviteUsers/dialog.layout.xml`) creates an `Invitation` for an e-mail address
  and a `language` (preselected with the language of the inviting user), with `mfaRequirement`
  `optional`, and sends it with `sendInvitation`.
- "Invitations" (`existingInvitations/invitationsGrid.layout.xml`) lists all invitations with the
  columns e-mail address, `language`, `state`, `expires`, `createdAt`, `createdBy`, `accepted`, `account` and
  `mfaRequirement`, and offers two commands for the selected invitation:
  - "Send invitation again" calls `invitationResend($invitation)`: an open invitation is sent with
    its link, an expired one gets a new link and a new expiry date first.
  - "Revoke invitation" deletes the invitation (`GenericDeleteCommandHandler`), so its link no
    longer works.

  Both are disabled for an accepted invitation, which stays as the record of the account.

The messages of these commands are the keys `tl.security.selfservice.invitations.*` of the bundle
`tl-security-selfservice.app-messages`.

React UI: `tl-layout-view-selfservice` adds the view "Invitations" after "Accounts" to the section
"Access control" of the administration, through a same-path overlay of
`WEB-INF/views/admin/admin.view.xml` (see the article `view-layer/overlays` of
`com.top_logic.layout.view`). The view
`admin/access-control/invitations.view.xml` lists all invitations with the same columns, newest
first (`sort="DESC"` on `createdAt`), and offers:

- "Invite": `<create-transient>` opens `create-invitation.view.xml` with a draft `Invitation`, whose
  form takes the e-mail address, the `language` (preselected with the language of the inviting
  user, the default of the draft) and the `mfaRequirement` (empty: the requirement of the
  authentication device). The constraints of `email` show their message at the field and keep the
  command disabled. "Invite" stores the draft with `<persist-transient>`, then sends it with
  `sendInvitation($invitation)`; a mail script answering `false` leaves the invitation and tells
  with a warning that it was not sent.
- "Send again": `invitationResend($selectedInvitation)`, with a warning when the mail script answers
  `false`.
- "Revoke": `<delete-object/>` after a confirmation.

"Send again" and "Revoke" are disabled for an accepted invitation (`<disabled-if>` on `state`), with
the reason as tooltip. The messages are written in the view files. The view is shown to whoever
sees the administration (operation `Administration` on the security root); the commands are further
subject to the access rights on `tl.security.selfservice:Invitation` - create, write and delete
below the `SecurityStructure` singleton -, which an application grants to its administrator roles.

TL-Script offers the operations as functions of `tl-security-selfservice-core`, usable from any
user interface. They open a transaction of their own and are called from a command without
transaction:

| Function | Operation |
|---|---|
| `sendInvitation($invitation)` (`SendInvitation`, registered on `SearchBuilder`) | `InvitationModule.sendInvitation(...)`, answers the result of `invitation-mail`, `true` when the shipped script sent the mail. |
| `invitationRenew($invitation)` (`InvitationFunctions`) | `InvitationModule.renewInvitation(...)`. |
| `invitationResend($invitation)` (`InvitationFunctions`) | `InvitationModule.resendInvitation(...)`, answers the result of `invitation-mail`. |
| `invitationRefusal($token)` (`InvitationFunctions`) | `InvitationModule.refusal(...)`: why the invitation with the token cannot be accepted, `null` for an open one; needs no access rights, the token is the proof. |

```
{
  invitation = new(`tl.security.selfservice:Invitation`);
  set($invitation, `tl.security.selfservice:Invitation#email`, "new.user@example.com");
  sendInvitation($invitation);
}
```

### Accepting

The login hook `CreateLoginHook`, configured at the beginning of `<login-hooks>` of
`MainLayout$GlobalConfig`, reads the parameter `invitation` and looks the invitation up with
`findOpenInvitation(...)`:

1. For an unknown, expired or accepted invitation, a message tells the reason
   (`ERROR_UNKNOWN_INVITATION`, `ERROR_INVITATION_EXPIRED`, `ERROR_INVITATION_ACCEPTED`).
2. The code dialog `CheckAccountCode`, with the invitation as `InvitationRequest`, lets the invitee
   request a code, mailed through `verification-mail` to the invitation's address, and checks it.
3. `CreateAccountDialog` shows the e-mail address as the fixed user name and asks for surname
   (mandatory), given name and, if the device `authentication-device` manages passwords, the
   password twice. It calls `acceptInvitation(...)`, which creates the account and marks the
   invitation accepted; a refusal (password rules, `createCallback`, an invitation that expired in
   the meantime) is shown in the dialog, which stays open.
4. Depending on `mfaRequirement` the new user is offered (`optional`) or required (`required`) to set
   a second factor up, and is then logged in.

React UI: `tl-layout-view-selfservice` contributes the entry point
`<entry-point view="accept-invitation.view.xml" anonymous="true"/>` to `ViewConfig`, so the link
`/view/accept-invitation.view.xml?invitation=<token>` shows the page to a visitor without an account
even in an application with a `login-view` (see the section on the login view in the article
`view-layer/app-shell` of `com.top_logic.layout.view`). Its
`<query-bindings>` put the token on the channel `token`:

1. The derived channel `refusal` is `invitationRefusal($token)` (`InvitationFunctions`, calling
   `InvitationModule.refusal(token)`): the message of `findOpenInvitation(...)` for an unknown,
   expired or accepted invitation, which the page shows instead of the steps, or `null` for an open
   one.
2. "Send verification code" runs `<request-invitation-code/>` (`RequestInvitationCodeAction`) on the
   token: it throttles the request, creates a `VerificationCode`, mails it through
   `InvitationModule.sendVerificationMail(invitation, code)` (`verification-mail`) - without a
   configured `MailSenderService` it refuses the request (`ERROR_MAIL_NOT_CONFIGURED`), the code is
   sent by mail only - and opens the code dialog `verification-code.view.xml` with the invitation
   (as `InvitationRequest`) and the code on its channels `request` and `verification`, which stay
   on the server. "Send new code" (`<send-new-code/>`) mails a new code the same way.
3. `<check-verification-code/>` (`CheckVerificationCodeAction`) checks the entered code
   (`tl.login:OtpEntry`) and replaces the dialog with `create-account.view.xml`: the fixed user name
   (the e-mail address), given name, surname (mandatory) and, if the device `authentication-device`
   manages passwords (channel `passwords`), the password twice (`tl.login:AccountCreation`).
4. `<create-own-account/>` (`CreateOwnAccountAction`) lets the request create the account, here
   `acceptInvitation(...)`; a refusal is shown and the dialog stays open. The new account then runs the
   [login steps](doc:authentication#login-steps) in a `LoginFlow`, as after a password login: an
   account whose `mfaRequirement` is `required` sets a second factor up. After the last step the
   window loads the root of the view application instead of the invitation page (the property
   `LoginFlow.LANDING_PAGE`), with the new user logged in.

## Self-registration

In an application with an open user base, a visitor registers an account without an invitation. The
operations are those of `SelfRegistration` (`com.top_logic.security.selfservice.invitation`,
`InvitationModule.getInstance().getSelfRegistration()` or `SelfRegistration.getInstance()`); they
are independent of a user interface.

### Configuration

The registration is switched on by the variable `tl_self_registration` (environment variable or
system property), which sets the option `mode`:

| Value | Meaning |
|---|---|
| `off` (default) | No registration. |
| `direct` | A registered account can log in right away. |
| `approval` | A registered account is locked (`tl.accounts:Person#locked`) until an administrator approves the registration. |

The shipped configuration of `tl-security-selfservice-core` reads the mode from the alias
`%SELF_REGISTRATION%`, defined in `tl-security-selfservice-core.conf.xml` as
`${env:tl_self_registration:off}`. An application that offers the registration by default
redefines the alias in its own main configuration file (`WEB-INF/conf/<app>.xml`, read after those
of its modules) with another default, which the variable still overrides:

```xml
<alias>
	<entry name="%SELF_REGISTRATION%" value="${env:tl_self_registration:approval}"/>
</alias>
```

The captcha of the registration is switched the same way: the shipped configuration reads the
option `captcha` from the alias `%SELF_REGISTRATION_CAPTCHA%`, defined in
`tl-security-selfservice-core.conf.xml` as `${env:tl_self_registration_captcha:true}`. An
installation that cannot run the captcha widget (plain HTTP from another host than `localhost`) is
started with `tl_self_registration_captcha=false`, see
[Installations served over plain HTTP](doc:authentication/captcha#installations-served-over-plain-http).

The further settings are options of `self-registration` of `InvitationModule`. An application sets
them in its own configuration of the service; the options it does not mention, among them `mode`
and `captcha`, keep the values of the module configuration, since a later configuration file
updates the `<self-registration>` element of an earlier one instead of replacing it:

```xml
<config service-class="com.top_logic.security.selfservice.invitation.InvitationModule">
	<instance>
		<self-registration>
			<callback><![CDATA[account -> {
  $account.set(`tl.accounts:Person#groups`, ...);
  null
}]]></callback>
		</self-registration>
	</instance>
</config>
```

| Option | Meaning |
|---|---|
| `mode` | `off`, `direct` or `approval`, see above; set in the shipped configuration from the variable `tl_self_registration`. |
| `captcha` | Whether a code request needs a solved captcha, checked by `CaptchaService` of `tl-security-captcha` (see [Captcha](doc:authentication/captcha)); set in the shipped configuration from the variable `tl_self_registration_captcha`, default `true`. |
| `callback` | Function called with each registered account in the transaction creating it, e.g. to assign roles or groups. It answers `null` to accept the account, or an internationalizable value as the reason to refuse it (`ERROR_ACCOUNT_CREATION_DENIED` with the answer as details), which rolls the registration back. |
| `registration-text` | Subject and content of the mail with the verification code; `{0}` is the application title, `{1}` the code. |
| `registration-mail` | Called with the e-mail address, the application title, the verification code and subject and content of `registration-text`. |
| `registration-exists-text` | Subject and content of the mail sent instead of the code when an account with the address exists; `{0}` is the application title. The shipped text points to "Forgot password". |
| `registration-exists-mail` | Called with the e-mail address, the application title and subject and content of `registration-exists-text`. |
| `approval-text` | Subject and content of the mail telling that a registration has been approved; `{0}` is the application title, `{1}` the address of the login page. |
| `approval-mail` | Called with the e-mail address of the registration, the application title, the address of the login page (`ApplicationAddress.loginUrl()`, built like the [invitation link](doc:authentication/self-service#the-link) from `tl_host`, the context path and `loginPage` of `ApplicationPages`) and subject and content of `approval-text`; without a value, no mail is sent. |

`tl-security-selfservice-core` ships the three mail scripts and their German and English texts,
which are [mail texts](doc:authentication/self-service#mail-texts) and
[mail scripts](doc:authentication/self-service#mail-scripts) like those of the invitations; the
[language](doc:authentication/self-service#language-of-the-mails) of each mail is that of its
recipient as far as known. `registration-mail`, `registration-exists-mail` and
`callback` run before anyone is logged in, without access checks; `approval-mail` also runs without
access checks, since its recipient is the address recorded in the registration. Registration is enabled (`isEnabled()`) if `mode` is
not `off`, the two scripts `registration-mail` and `registration-exists-mail` are configured and the
application can send mails (`MailSenderService.isConfigured()`).

### Operations

| Operation | Effect |
|---|---|
| `getMode()`, `isEnabled()`, `isCaptchaRequired()` | The configured mode; whether visitors can register; whether a code request needs a solved captcha. |
| `requestCode(email, captchaPayload, clientAddress)` | Refuses when registration is not enabled (`ERROR_SELF_REGISTRATION_DISABLED`); checks the captcha if required; checks the syntax of the address (`ERROR_INVALID_EMAIL__ADDRESS`, the same check as the constraint of `Invitation#email`); throttles; sends `registration-mail` with a new `VerificationCode`, or `registration-exists-mail` if an account with the address exists, and answers a code in both cases, so the caller behaves the same and does not reveal which addresses have an account. A script answering `false` fails with `ERROR_VERIFICATION_CODE_NOT_SENT`. Nothing is stored. |
| `register(email, surname, givenName, password)` | Refuses when registration is not enabled. In one transaction: creates the account like an accepted invitation (`InvitationModule.createAccount(...)`: device `authentication-device`, the address as user name, surname, given name, the password checked against the password rules of the device), locks it in mode `approval`, runs `callback`, and stores a `Registration`. A refusal leaves neither account nor record. The caller has checked the code sent to the address before. |
| `approve(registration)` | Refuses an approved registration (`ERROR_REGISTRATION_NOT_PENDING`). In one transaction: unlocks the account and sets `approved` and `approvedBy` (the current user, the approving administrator); then sends `approval-mail` and answers its result. Without a public address and request, nothing is changed (`ERROR_NO_PUBLIC_ADDRESS`). |
| `pendingRegistration(account)`, `isPending(registration)` | The registration of an account that waits for its approval - not approved, account locked -, `null` otherwise. |

A user interface keeps the `VerificationCode` and the address it was sent to on the server side of
its dialog, checks the entered code (`VerificationCode.check(entered)`) and then calls `register`
with that address.

TL-Script functions (`RegistrationFunctions`):

| Function | Operation |
|---|---|
| `registrationEnabled()` | `isEnabled()`. |
| `registrationCaptcha()` | `isCaptchaRequired()`, the option `captcha`. |
| `registrationApprove($registration)` | `approve(...)`, in a transaction of its own; answers the result of `approval-mail`. |
| `registrationPending($account)` | `pendingRegistration(...)`. |

### Classic UI

`tl-security-selfservice` offers the registration in the login dialog of the classic UI. It shares
the code dialog `CheckAccountCode` and the account dialog `CreateAccountDialog` with the acceptance
of an invitation (see [Accepting](doc:authentication/self-service#accepting)); what differs is the
classic `AccountRequest` the dialogs are opened for (`InvitationRequest`, `RegistrationRequest`),
which provides the text of the code dialog, sends a new code, creates the account and decides what
follows.

1. The command "Register" (`RegisterDialog$Opener`) is added beside "Forgot password" to the
   toolbar of the login dialog through `LoginViewDialog$Config`. It is shown while
   `SelfRegistration.isEnabled()` holds.
2. `RegisterDialog` asks for the e-mail address and, if `isCaptchaRequired()` holds, shows the
   captcha field of `tl-security-captcha` (see
   [Captcha](doc:authentication/captcha#the-captcha-in-the-classic-ui)).
3. "Send code" calls `requestCode(email, captchaPayload, clientAddress)` with the network address of
   the client and resets the captcha field afterwards, so the captcha gets a new challenge for a
   further attempt. A refusal (captcha not solved, invalid address, throttling) is shown and the
   dialog stays open. Otherwise `CheckAccountCode` opens for a `RegistrationRequest` of the address
   with the message "If there is no account for ... yet, we have sent a verification code to this
   address." - the same whether the address has an account or not.
4. "Request verification code" in the code dialog leads back to `RegisterDialog` with the address
   filled in, since a new code needs a new captcha.
5. `CreateAccountDialog` follows as for an invitation and calls
   `register(email, surname, givenName, password)` for the verified address. In mode `direct` the
   new account is logged in like after accepting an invitation (`LoginViewDialog.loginUserAndReload`,
   after setting up a second factor if required). In mode `approval` the account is locked: the
   dialog closes with the message "Your account has been created and awaits the approval of an
   administrator.", nobody is logged in.

### React UI

`tl-layout-view-selfservice` offers the registration in the login dialog of the React view UI. It
shares the code dialog and the account dialog with the acceptance of an invitation (see
[Accepting](doc:authentication/self-service#accepting)); what differs is the `AccountRequest` the
dialogs carry on their channel `request` (`InvitationRequest`, `RegistrationRequest`), which sends
a new code, creates the account and decides what follows.

1. The login command "Register" (`WEB-INF/autoconf/login.selfservice.config.xml`, beside "Forgot
   password?") is shown while `registrationEnabled()` holds (`<visible-if>` in the
   `<executability>` of its command). It opens `register.view.xml` on a transient
   `tl.login:RegistrationRequest` (`email`, mandatory).
2. The dialog asks for the e-mail address and, if `registrationCaptcha()` holds, shows the captcha
   `<captcha payload="captcha"/>` of `tl-security-captcha-view` (see
   [Captcha](doc:authentication/captcha#the-captcha-in-the-react-view-ui)), which writes its
   solution into the channel `captcha`.
3. "Send code" runs `<request-registration-code/>` (`RequestRegistrationCodeAction`, option
   `captcha`: the name of the channel holding the solution, default `captcha`). It calls
   `requestCode(email, captchaPayload, clientAddress)` with the network address of the client
   (`ServletRequest.getRemoteAddr()`, as the login counts failed attempts) and clears the channel
   `captcha` afterwards, so the captcha gets a new challenge for a further attempt. A refusal
   (captcha not solved, invalid address, throttling) is shown and the dialog stays open. Otherwise
   the code dialog opens with a `RegistrationRequest` for the address, and the message "If there is
   no account for ... yet, we have sent a verification code to this address." - the same whether
   the address has an account or not.
4. "Send new code" in the code dialog leads back to `register.view.xml` with the address filled in,
   since a new code needs a new captcha.
5. `<check-verification-code/>` and `create-account.view.xml` follow as for an invitation;
   `<create-own-account/>` calls `register(email, surname, givenName, password)` for the verified
   address. In mode `direct` the new account then runs the
   [login steps](doc:authentication#login-steps) in a `LoginFlow` and the window loads the root of
   the view application. In mode `approval` the account is locked: the dialog closes with the
   message "Your account has been created and awaits the approval of an administrator.", nobody is
   logged in.

### The record

`tl.security.selfservice:Registration` (wrapper `Registration`, table `Registration`) records each
registration:

| Attribute | Meaning |
|---|---|
| `email` | The registered address, the user name of the account. |
| `account` | The registered account. Deleting the account deletes the registration (`deletion-policy="delete-referer"`), so an administrator rejects a pending registration by deleting its account. |
| `createdAt` | Derived: the time of the commit that created the registration. |
| `approved`, `approvedBy` | When and by whom the registration was approved. In mode `direct`, `approved` is the time of the registration and `approvedBy` stays empty. |
| `state` | Derived, the enumeration `tl.security.selfservice:RegistrationState`: `PENDING` while `approved` is empty and the account is locked, otherwise `APPROVED` - also for an account unlocked in the account administration. |

Registrations are secured below the `SecurityStructure` singleton (access parent rule
`SecRootForRegistration` on `AccessManager`), like invitations.

### Abuse protection

- Verification code: the address is proven before an account exists; `check(entered)` accepts
  three wrong entries per code (see [Limits on codes](doc:authentication/self-service#limits-on-codes)).
- Throttling: each code request counts in `LoginFailuresModule` under the address (key
  `registration:` and the address in lower case) and under the client (`registration-client:` and
  the client address), so that a further request for the same address, or from the same client for
  any address, waits longer each time.
- Captcha: with `captcha="true"`, a request without a solved proof-of-work captcha is refused before
  it is counted, so requests of bots do not delay the requests of others.
- No address enumeration: an address with an account gets `registration-exists-mail` instead of a
  code, and the caller cannot tell the difference.
- Approval: in mode `approval`, a registered account cannot log in before an administrator approves
  it (see [Approving registrations](doc:authentication/self-service#approving-registrations)).

### Approving registrations

In mode `approval` the registered account is locked (`tl.accounts:Person#locked`, see
[locked accounts](doc:authentication#locked-accounts)): every login is refused until an administrator
approves the registration. Approving calls `registrationApprove(...)`, which unlocks the account,
sets `approved` and `approvedBy` and sends `approval-mail` to the registered address. Rejecting a
registration is deleting its account in the account administration; the `Registration` is deleted
with it. A registration is pending only while it is not approved and its account is locked: an
administrator who unlocks the account in its form instead lets it log in as well, and the
registration counts as approved from then on (`state` `APPROVED`, with `approved` and `approvedBy`
empty); no approval mail is sent then.

Classic UI (`tl-security-selfservice`, through the overlay
`admin/security/accounts/accountsTable.layout.overlay.xml` of the account table):

- "Approve registration" in the toolbar of the account table approves the pending registration of
  the account of the selected person (target `registrationPending(...)` of the account,
  `TransformLinkingByExpression`); it is disabled with the reason for a person whose account does
  not wait for an approval.
- The dialog "Registrations" (`existingRegistrations/registrationsGrid.layout.xml`, beside
  "Invitations") lists all registrations with the columns e-mail address, `state`, `createdAt`,
  `approved`, `approvedBy` and `account`, and offers "Approve registration" for the selected
  registration, disabled for an approved one.

The messages of these commands are the keys `tl.security.selfservice.registrations.*` of the bundle
`tl-security-selfservice.app-messages`.

React UI (`tl-layout-view-selfservice`):

- The view "Registrations" (`admin/access-control/registrations.view.xml`, a tab after
  "Invitations" in the section "Access control", added by the same overlay of
  `admin/admin.view.xml`) lists all registrations with the same columns, newest first, and offers
  "Approve" for the selected registration, disabled for an approved one.
- "Approve" is also added to the list panel of the account administration by a same-path overlay of
  `admin/access-control/accounts.view.xml` (command `approve-registration` in the panel
  `list-panel`). It works on the channel `selectedAccount`, so it also works in the administration
  of people of `tl-layout-view-contact`, and is disabled with the reason while
  `registrationPending($selectedAccount)` is `null`.

Both UIs tell the result: "The account has been approved, the person has been notified by e-mail.",
a warning if the mail script answered `false` (the account is approved nonetheless), and "The account
has been approved." if no `approval-mail` is configured.

Registrations are listed by both UIs in every mode: in mode `direct` they are the approved record
of each registered account.

### Who may approve

The administration is shown to whoever holds the operation `Administration` on the security root
(see the article `view-layer/access-control` of `com.top_logic.layout.view`). The commands
approving a registration need the operation `Write` on the `Registration` (React: `<access>` of
the commands; classic: the command group of `CommandHandlerByExpression`). Registrations are
secured below the `SecurityStructure` singleton (`SecRootForRegistration`), so an application
grants the operations on the module `tl.security.selfservice` to the role its administrators hold
on the security root, together with those for the invitations:

```xml
<config service-class="com.top_logic.model.security.SecurityConfigurationService">
	<instance>
		<security-config>
			<module name="tl.security.selfservice">
				<grant inherit="true" operation="Read" roles="my.app.Administrator"/>
				<grant inherit="true" operation="Write" roles="my.app.Administrator"/>
				<grant inherit="true" operation="Create" roles="my.app.Administrator"/>
				<grant inherit="true" operation="Delete" roles="my.app.Administrator"/>
			</module>
		</security-config>
	</instance>
</config>
```

The approval itself unlocks the account without a further check, so `Write` on the account is not
needed. Rejecting by deleting the account needs the right to delete the account. An account with
`admin` set (such as `root`) needs no grant.

### The demo applications

Both demo applications offer the registration, with the captcha, and need a mail server (variables
`smtp_active`, `smtp_server`, ... of `tl-mail-smtp`):

- `tl-demo` (classic UI) registers in mode `direct`: the new account is logged in right away.
- `tl-demo-react` (React view UI) registers in mode `approval`: the new account is locked until an
  administrator approves it under Administration → Access control → People ("Approve") or
  → Registrations. Its role `demo.react.Administrator` holds the grants on
  `tl.security.selfservice` shown above; in `tl-demo` the role `SecurityStructure.superuser` holds
  them.

These are the defaults of `tl_self_registration` in `DemoConf.xml` and `demoReactConf.xml` (alias
`%SELF_REGISTRATION%`); a demo started with `tl_self_registration=off` offers no registration.

The captcha needs HTTPS or `localhost`. A demo reached over plain HTTP from another host is started
with the variable `tl_self_registration_captcha=false` of `tl-security-selfservice-core`, see
[Installations served over plain HTTP](doc:authentication/captcha#installations-served-over-plain-http).

## Pitfalls

- Without the public address of the application (`tl_host`), an invitation can only be sent while a
  request is processed, and its link carries the address of that request.
- An invitation stored without expiry date gets "time of the update plus `invitation-validity`"
  from the migration `Ticket_30041_invitation_lifecycle` (startup action `InitInvitationExpiry`).
- The reset of the second factor is classic UI only.
- An application with both `tl-security-selfservice` and `tl-layout-view-selfservice` gets the
  invitation link of the module configured last; it sets `page` in its own configuration of
  `InvitationModule` to the UI that is to accept invitations.
- An accepted invitation cannot be revoked in the "Invitations" dialog or view; it is the record of
  the account and refers to it in `account`.
