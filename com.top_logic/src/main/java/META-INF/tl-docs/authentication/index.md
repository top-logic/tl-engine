---
description: Read before changing how users log in to a TopLogic application - the login dialog of the classic UI (OpenLoginDialogHook, LoginViewDialog, <login-hooks> on MainLayout$GlobalConfig, the LoginHook interfaces) and of the React view UI (login.view.xml, login-form.view.xml, LoginAction / <login>, <login-commands> fed by LoginCommandsConfig, the single sign-on buttons <login-methods> fed by LoginMethodConfig / LoginMethodProvider / LoginMethod, e.g. Pac4jLoginMethodProvider), finding the account of an external login (ExternalUserMapping, ExternalIdentity, the refusal reason of LoginDeniedException), locked accounts (Person#isLocked, Login#checkMayLogIn, AccountLockedException), the login steps after the authentication (LoginStepsConfig, LoginStep, LoginFlow, the expired password, the second factor through the configured SecondFactor methods, a configured <dialog-step> ended by <continue-login/>), login throttling with LoginFailuresModule, and where multi-factor authentication, passkeys and the self-service flows are described.
order: 25
---

# Authentication

How a user gets from an anonymous session to a session of their own account, and the places where
an application or a feature module hooks into that. The second factor is described in
[Multi-factor authentication](doc:authentication/mfa), the login with passkeys (passwordless and as
second factor) in the passkeys article of this chapter (module `tl-security-passkey`), password reset
and invitations in the self-service article of this chapter (module `tl-security-selfservice`).

## Credentials check

Both UIs check the user name and password through
`Login.getInstance().checkUserPassword(userName, password, request, response)`. The check resolves
the account, asks its `AuthenticationDevice` (`DBAuthenticationAccessDevice`,
`LDAPAuthenticationAccessDevice`, configured as `<security-device>` of `TLSecurityDeviceManager`),
honours the maintenance mode (`InMaintenanceModeException`) and runs the optional
`com.top_logic.base.accesscontrol.LoginHook` configured as `login-hook` on `Login$Config`. That hook
answers a reason to deny the login, or `null`; a denial surfaces as `LoginHookFailedException`.

Whether the password must be changed before the session starts is decided by
`Login.isPasswordValidAndNotExpired(password, account)`.

### Locked accounts

An account whose attribute `tl.accounts:Person#locked` (`Person.isLocked()`) is set cannot log in
through any path. `Login.checkMayLogIn(account)` is the check every login runs after the identity is
established: it refuses a locked account with an `AccountLockedException` (a `LoginDeniedException`
whose reason tells the user that the account is locked) and then applies the maintenance mode
(`checkAllowedGroups`). The password login refuses a locked account only after the password was
verified; a wrong password gets the generic refusal, so the answer does not disclose the lock. The
external logins (`ExternalAuthenticationServlet`), the passkey login and the completion of a React
`LoginFlow` run the same check. `SessionService.loginUser` / `loginUserInWindow` refuse a locked
account as a backstop. The stateless APIs (the OpenAPI server, the MCP and A2A endpoints) answer a
request authenticated for a locked account as an authentication failure (`Login.checkNotLocked`).

Locking an account ends its running sessions: `SessionService` listens for commits and terminates
the sessions of every account that is locked (or deleted) in a commit, also for commits of another
cluster node.

## Login in the classic UI

An unauthenticated request to the `LayoutServlet` gets an anonymous session. The `MainLayout` runs
the login hooks configured on `MainLayout$GlobalConfig` one after another. The core configures
`OpenLoginDialogHook`, which opens `LoginViewDialog` while the session is anonymous and resolves a
bookmark of the request after the login:

```xml
<config config:interface="com.top_logic.mig.html.layout.MainLayout$GlobalConfig">
	<login-hooks>
		<login-hook class="com.top_logic.layout.component.configuration.OpenLoginDialogHook"/>
	</login-hooks>
</config>
```

A hook implements `com.top_logic.mig.html.layout.LoginHook`:
`handleLogin(MainLayout mainLayout, Runnable callback)`. It must call `callback` once it is done,
otherwise the remaining hooks do not run. A hook that opens a dialog calls it when the dialog
closes: `LoginHooks.runOnClose(dialog.getDialogModel(), callback)`. A module that must run before
the login dialog adds its hook with `config:position="begin"` (the invitation hook of
`tl-security-selfservice` does this); `LoginMessagesHook` of `com.top_logic.addons.loginmessages`
is another hook of this kind.

`LoginViewDialog` steps through:

1. Throttling: `LoginFailuresModule` counts failed attempts per user name and client address and
   refuses further attempts for a while (`ERROR_TOO_MANY_LOGIN_ATTEMPS__TIMEOUT`).
2. The credentials check.
3. The second factor: an account with a stored secret gets `CheckOTPDialog`; an account whose
   `MfaRequirement` is `required` but has no secret gets the set-up dialog first.
4. An expired password opens `ChangePasswordDialog`.
5. `LoginViewDialog.loginUserAndReload(context, user)` invalidates the anonymous session, logs the
   user in through `SessionService.loginUser(...)` and reloads the page.

Feature modules add commands to the toolbar of the login dialog through
`LoginViewDialog$Config` (`toolbar-commands`, entries of type `CommandModel`), and to the toolbar of
the one-time-code dialog through `CheckOTPDialog$Config` (`toolbar-commands`, entries are functions
from the `Person` to a `CommandModel`). Size of the login dialog: theme variables
`com.top_logic.layout.component.configuration.Icons.LOGIN_DIALOG_WIDTH` and `...LOGIN_DIALOG_HEIGHT`.

## Login in the React view UI

The view UI of `com.top_logic.layout.view` (served by `ViewServlet`) also starts every browser with
the anonymous account. The login is either the dialog `login.view.xml` (opened from the user menu)
or, for an application that shows a visitor nothing else, the page named by `login-view` on
`ViewConfig` (the framework ships `login-page.view.xml`). Both reference `login-form.view.xml`:
the fields of the transient type `tl.login:Credentials`, the action `<login/>` (`LoginAction`),
the contributed `<login-commands/>` and the SSO buttons `<login-methods/>`. The article
`view-layer/app-shell` of `com.top_logic.layout.view` describes the login page.

`LoginAction` checks the credentials and then runs the [login steps](#login-steps) for the
account; the user is logged in only after the last of them.

### Login steps

Between the authentication of an account and the start of its session, the account runs the steps
of the application configuration `LoginStepsConfig`, in their order, in a `LoginFlow`. Each step
(`LoginStep`) tells whether it applies to the account (`appliesTo(flow)`) and opens a dialog of its
own (`start(context, flow)`); a step that does not apply is skipped. `tl-layout-view` configures:

1. `<change-password-step name="change-password"/>` (`ChangePasswordStep`): an expired password
   replaces the dialog with `change-password.view.xml`, whose `<change-password/>`
   (`ChangePasswordApplyAction`) finishes the step once the new password is stored.
2. `<second-factor-step name="second-factor"/>` (`SecondFactorLoginStep`):
   `SecondFactorStep.decide(account)` asks the methods of `SecondFactorsConfig`. An account with a
   method set up confirms itself with it (the authenticator app in `otp.view.xml` with
   `<verify-otp/>`, a passkey in `passkey-verify.view.xml`); an account with `MfaRequirement`
   `required` and none set up sets one up (e.g. `mfa-enroll.view.xml` with
   `<verify-otp persist="true"/>`). With several methods, `second-factor-choice.view.xml` lets the
   user choose. See [the second factor of the React login](doc:authentication/mfa#second-factor-methods-of-the-react-login).

After the last step, `LoginFlow` re-checks whether the account may log in (lock, maintenance mode
and allowed groups, see [locked accounts](#locked-accounts)) and logs the user in
within the current browser window (`SessionService.loginUserInWindow`): the HTTP session is kept
and only its ID is changed, other open tabs keep their user, and a tab opened later starts with the
user logged in. The window's control tree is discarded and the window reloads
(`ReactWindowRegistry.rebuildWindow`). The logout swaps the session through `PendingSessionAction`
on the reload it triggers.

Every login entry runs the steps: the password `<login/>`, the passwordless passkey login of
`tl-security-passkey`, which begins the flow with the second factor marked as proven
(`SecondFactorLoginStep.PROVEN`), so that only the further steps run, and the account created from
an invitation or a registration (`<create-own-account/>` of `tl-layout-view-selfservice`). A login entry whose page is
not meant for the user logged in sets `LoginFlow.LANDING_PAGE`: after the last step the window
loads that page instead of reloading the one it shows.

**A dialog step from configuration.** `<dialog-step>` (`DialogStep`) shows a dialog view to the
account and needs no Java. A module adds it with a configuration fragment of its own (e.g. in
`WEB-INF/autoconf/`); the list is keyed by `name`, so `config:position` places the step:

```xml
<config config:interface="com.top_logic.layout.view.login.LoginStepsConfig">
    <steps>
        <dialog-step name="terms"
            config:position="after"
            config:reference="second-factor"
            view="terms/accept-terms.view.xml"
            condition="account -> !$account.get(`my.app:Person#termsAccepted`)"
        />
    </steps>
</config>
```

- `view` is the dialog, relative to `WEB-INF/views/`. It declares the channel `account`, which
  carries the account logging in, and derives what it shows from it (e.g. with a
  `<derived-channel>`).
- `condition` is a TL-Script function over the account; without it the step applies to every
  account. A condition that fails refuses the login. It is evaluated without access checks: it is
  application configuration and decides before the account has a session, when the window still
  belongs to the anonymous user. The dialog itself runs for the anonymous user, with its checks: a
  dialog showing model objects the anonymous user may not read receives them from a function that
  renders them on the server, as the login messages of `tl-addons-loginmessages-view` do.
- The dialog ends with the action `<continue-login/>` (`ContinueLoginAction`), typically after the
  actions recording what the user confirmed, which finishes the step and runs the next one. Closing
  the dialog cancels the login.

**Security.** The flow is held by the sub-session of the window, never sent to the client; which
step is due is the flow's own position. A step is finished only by an action of its kind
(`LoginFlow.finish(context, StepClass, account)`): `<change-password/>` finishes the
`ChangePasswordStep`, `<verify-otp/>` and the passkey ceremonies finish the `SecondFactorLoginStep`,
and `<continue-login/>` finishes a `DialogStep` only. Finishing another kind of step than the due
one, e.g. `<continue-login/>` while the second factor is due, refuses and discards the login. The
actions take the account from the flow; the dialog channel `account` is for display. A new login in
the window replaces the flow, and closing the dialog of the due step discards it. The TOTP secret of
the authenticator app travels on the dialog channel `secret`, server side only. The step dialogs do
not close on a backdrop click.

### Contributing a login command

`LoginCommandsConfig` is an application configuration whose `commands` are UI elements (typically a
`<button>`) rendered by `<login-commands/>`. A feature module contributes without the view module
depending on it. The self-service module `com.top_logic.layout.view.selfservice` adds its
"Forgot password?" button this way (`WEB-INF/autoconf/login.selfservice.config.xml`):

```xml
<config config:interface="com.top_logic.layout.view.login.LoginCommandsConfig">
	<commands>
		<button appearance="LINK" size="SMALL">
			<action class="com.top_logic.layout.view.command.GenericViewCommand">
				<label>
					<en>Forgot password?</en>
					<de>Passwort vergessen?</de>
				</label>
				<execute-script function="new(`tl.login:ForgotPassword`, transient: true)">
					<with-result>
						<open-dialog dialog-view="forgot-password.view.xml"
							bind-input-to="model"
							close-on-backdrop="false"
						/>
					</with-result>
				</execute-script>
			</action>
		</button>
	</commands>
</config>
```

The entries are keyed by `id`, so an overlay can address a contributed element.

A further way to log in that belongs beside the single sign-on buttons is added with a same-path
overlay of `login-form.view.xml` instead: an element in `<form config:ref="credentials">`, placed
with `config:position="after" config:reference="login-methods"`. `tl-security-passkey` adds its
button "Sign in with passkey" (`<passkey-login>`) this way; it logs in with a passkey alone and
runs the [login steps](#login-steps) without the password and second-factor steps (see the passkeys
article of this chapter).

### External login methods (single sign-on)

`<login-methods/>` (`LoginMethodsElement`) shows one button per external login method, e.g. "Login
with Keycloak". The methods come from the application configuration `LoginMethodConfig`
(`com.top_logic.base.accesscontrol.loginmethod`), a list of `LoginMethodProvider`s; `LoginMethods.all()`
asks every provider for its `LoginMethod`s and leaves out those that are not `isAvailable()` now.
The interfaces live in this module, so a provider module depends on the core only, not on the view
layer. The pac4j module `tl-security-auth-pac4j` registers its provider in `tlPac4jConf.config.xml`:

```xml
<config config:interface="com.top_logic.base.accesscontrol.loginmethod.LoginMethodConfig">
	<providers>
		<provider class="com.top_logic.security.auth.pac4j.login.Pac4jLoginMethodProvider"/>
	</providers>
</config>
```

`Pac4jLoginMethodProvider` yields one method per configured client of `Pac4jConfigFactory` (nothing
while the pac4j module is not active). The button label is the client's `label`, its `icon` the
optional button icon; without a label the client name is shown. A client is offered only while its
`ClientConfigurator` reports it available - e.g. not while the OIDC client secret is missing.

A `LoginMethod` answers:

- `getInitiationUrl(returnTo, windowName)` - the address the button sends the browser to, a
  full-page redirect. For pac4j:
  `/servlet/openid?force_client=<client>&startPage=<returnTo>&loginWindow=<windowName>`; the
  identity provider authenticates the user, the callback logs the user in through
  `ExternalAuthenticationServlet` and returns to the page the visitor asked for. `<login-methods/>`
  passes the window it is shown in and returns to that window (`/view/<windowName>/<route>`), so the
  external login applies to that window only, like the password login: the servlet finds the
  window's sub-session by `ExternalAuthenticationServlet.WINDOW_PARAM` and calls
  `SessionService.loginUserInWindow`. Without the parameter, or for a window the session does not
  know, the whole session is logged in (`SessionService.loginUser`): an existing HTTP session is
  kept and only its ID is changed, all its windows get the user and show it on their next reload.
- `getReauthenticationUrl(token)` - optional; the address that has the provider confirm the user of
  the running session once more, or `null` when the method cannot. pac4j answers it only for a
  session its own client established.

The buttons are determined each time the element is rendered, so a method that becomes available
(a secret is provided) appears without a restart. An external login that failed returns to the login
page, and `<login-methods/>` shows its message (`ExternalLoginFailure`) above the buttons.

`ExternalAuthenticationServlet` with `reuseSession` reuses only an authenticated session: the
anonymous session the view UI starts every browser with does not suppress the external login.

### Finding the account of an external login

Which account an external login logs in is decided by the `ExternalUserMapping`
(`com.top_logic.base.accesscontrol`) configured as `<user-mapping class="..."/>` of the pac4j
client (`ClientConfigurator$Config`) or of `J2eeRemoteUserAuthenticationServlet$Config`. The default
`DefaultExternalUserMapping` looks up the account by name (`Person.byName`), `LDAPUserMapping`
additionally requires the account to belong to the given LDAP.

The pac4j login hands the mapping the whole verified identity,
`findAccountForIdentity(ExternalIdentity)`: the client name, the subject (`UserProfile.getId()`,
for OpenID Connect the claim `sub`), the login name extracted by the client's `UserNameExtractor`,
and the claims (the profile attributes without the tokens of the login). By default the method
resolves the name through `findAccountForExternalName(String)`, so a mapping that only knows names
behaves the same for every login. The header login knows only a name and calls
`findAccountForExternalName` directly.

An application overrides `findAccountForIdentity` to find an account by the stable subject
instead of a name the user may change, or to link an invited account on the first login by a
verified e-mail claim. The mapping runs in the request of the login servlet, before the session is
logged in and without an open transaction: creating or linking an account needs a transaction of
its own. It also runs when a running session confirms its user once more, so it must find the same
account for the same identity. An `UnknownAccountException` shows the page for unknown accounts
(`unknownAccountPage` of `ApplicationPages$Config`), any other `LoginDeniedException` fails the
login; its reason (`LoginDeniedException.getReason()`), if it has one, is shown on the login page.

The pac4j module ships such a mapping, `OidcProvisioningUserMapping`: it links accounts by the
subject, by a verified e-mail address or by name, creates accounts on the first login, assigns their
groups from a claim and calls `ExternalLoginListener`s of the application. The account provisioning
article of this chapter (module `tl-security-auth-pac4j`) describes it.

## Pitfalls

- The two `LoginHook` interfaces are unrelated: `com.top_logic.base.accesscontrol.LoginHook`
  (`check(request, response)`, one per `Login$Config`) vetoes a login, while
  `com.top_logic.mig.html.layout.LoginHook` (a list on `MainLayout$GlobalConfig`) runs UI steps
  in the classic UI.
- A `MainLayout` login hook that never calls its callback stops all hooks after it, including
  `OpenLoginDialogHook` - the user sees no login dialog.
- `MainLayout` login hooks run in the classic UI only; the React view UI has no counterpart to them.
