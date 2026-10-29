---
description: Read before changing how users log in to a TopLogic application - the login dialog of the classic UI (OpenLoginDialogHook, LoginViewDialog, <login-hooks> on MainLayout$GlobalConfig, the LoginHook interfaces) and of the React view UI (login.view.xml, login-form.view.xml, LoginAction / <login>, <login-commands> fed by LoginCommandsConfig), the order of the login steps (password, expired password, second factor), login throttling with LoginFailuresModule, and where multi-factor authentication and the self-service flows are described.
order: 25
---

# Authentication

How a user gets from an anonymous session to a session of their own account, and the places where
an application or a feature module hooks into that. The second factor is described in
[Multi-factor authentication](doc:authentication/mfa), password reset and invitations in the
self-service article of this chapter (module `tl-security-selfservice`).

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

`LoginAction` checks the credentials and then:

1. An expired password replaces the dialog with `change-password.view.xml`
   (`ChangePasswordApplyAction`), which continues with the next step.
2. An account with a stored secret gets `otp.view.xml` (`<verify-otp/>`); an account with
   `MfaRequirement` `required` and no secret gets `mfa-enroll.view.xml` (`<verify-otp persist="true"/>`).
3. The last step re-checks maintenance mode and allowed groups, records the pending login on the
   HTTP session and reloads the page; `PendingSessionAction` performs the session switch on that
   reload.

The partially authenticated account travels on the dialog channel `account` of the step dialogs; the
TOTP secret on the channel `secret`, server side only. The step dialogs do not close on a backdrop
click.

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

## Pitfalls

- The two `LoginHook` interfaces are unrelated: `com.top_logic.base.accesscontrol.LoginHook`
  (`check(request, response)`, one per `Login$Config`) vetoes a login, while
  `com.top_logic.mig.html.layout.LoginHook` (a list on `MainLayout$GlobalConfig`) runs UI steps
  in the classic UI.
- A `MainLayout` login hook that never calls its callback stops all hooks after it, including
  `OpenLoginDialogHook` - the user sees no login dialog.
- `MainLayout` login hooks run in the classic UI only; the React view UI has no counterpart to them.
