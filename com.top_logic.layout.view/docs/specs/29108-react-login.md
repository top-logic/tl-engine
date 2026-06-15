# React UI Login & Self-Service (Ticket #29108)

**Status:** Phase 1 complete (browser-verified). Phase 2 password-expiry sub-flow
complete (browser-verified); Phase 2 OTP + MFA-enrollment and Phases 3–4 outstanding.
**Scope:** Authentication/identity for the new React view layer
(`com.top_logic.layout.view`, served by `ViewServlet` at `/view/*`), parallel to
— and independent of — the legacy `MainLayout`/`LoginViewDialog` UI.

---

## 1. Big picture

The `/view/*` React UI boots every browser as the **anonymous** user
(`ViewServlet.handleNoSession` → `SessionService.loginUser(anonymous)`). Users
must be able to log in to switch the underlying session user (and thus
permissions/settings) to their own account, log out again, run the self-service
flows (forgot/change password, MFA), and — when the pac4j module is present —
choose an SSO provider ("Login with Google") while the built-in account login
remains the default.

### Guiding principles

1. **Compose, don't reimplement.** The view layer is a *declarative composition
   layer*: `.view.xml` assembles existing React controls
   (`TLWindow`/`TLDialog`, `TLForm`/`TLFormField`, `TLTextInput`,
   `TLPasswordInput`, `TLButton`, `TLText`, …) via `UIElement` configs and view
   commands. **Do not** build bespoke monolithic React components for things
   that are just forms/buttons/labels. (Phase 1 originally got this wrong with
   `TLLogin`/`TLUserMenu`; those were removed — see §3.)
2. **Share the headless auth backend, not the UI.** The legacy and React UIs are
   two presentations over the same UI-agnostic services: `Login`,
   `SessionService`, `PersonManager`, `AuthenticationDevice`, `Person` (MFA),
   `LoginFailuresModule`, and (self-service) `InvitationModule`. The React UI
   never depends on `MainLayout` or the legacy dialogs.
3. **Keep session mutation out of the SSE/command pipeline.** A React command
   that invalidates the session would break the command's own post-processing
   and the SSE stream. Instead, commands *record intent* on the session and
   trigger a reload; `ViewServlet` performs the actual `loginUser` swap on the
   reload GET (`PendingSessionAction`).
4. **SSO is injected via a core SPI.** Because `tl-security-auth-pac4j` depends
   on `tl-core` (not on the view module), the extension point for external login
   methods lives in core (`com.top_logic.base.accesscontrol.login`).

---

## 2. Architecture / key decisions

- **Session swap = invalidate + reload.** Login/logout `ViewCommand`/
  `ViewAction`s authenticate, call `PendingSessionAction.requestLogin/Logout`
  (stash on the HTTP session), and push a reload via the existing `JSSnipplet`
  SSE event. `ViewServlet.doGet` calls `PendingSessionAction.apply(...)` **after
  `ensureSubSession`** (a valid subsession context is required because
  `invalidateSession` fires a logout event that opens a KB transaction).
  `SessionService.loginUser` does its own session-cookie-check redirect.
- **Login form = transient model + standard form.** `tl.login:Credentials`
  (transient type, `name` + `password`) is edited by a normal `<form>`. The
  `password` attribute carries an `<input-control>` annotation selecting
  `PasswordInputControlProvider` → the generic `ReactPasswordInputControl`.
- **User identity in the chrome** uses the generic `<text>` element bound to a
  `<derived-channel expr="currentUser()"/>`; Login/Logout buttons are gated by
  `AnonymousOnly`/`AuthenticatedOnly` executability rules.
- **SSO injection SPI** (core, UI-agnostic):
  `LoginMethod` (id, label, icon, `initiationUrl(returnTo)`),
  `LoginMethodProvider`, `LoginMethodConfig` (registry), `LoginMethods.all()`.
  A login UI renders one redirect button per method; pac4j contributes a
  provider. SSO is a full-page redirect to `/servlet/openid?client_name=…&
  startPage=/view/<window>/` → IdP → `/servlet/callback` → `loginUser` →
  back to the view. (SPI defined in Phase 1; no provider yet — Phase 4.)

### Module placement

- **`com.top_logic` (tl-core):** `LoginMethod*` SPI (so pac4j can implement it).
- **`com.top_logic.layout.react`:** generic `ReactPasswordInputControl` +
  `TLPasswordInput` (the only new React control; a plain `type=password`).
- **`com.top_logic.layout.view`:** the login composition — `PasswordInputControl`
  Provider, `<text>` element, `LoginAction`/`LogoutCommand`,
  `AnonymousOnly`/`AuthenticatedOnly`, `PendingSessionAction`,
  `tl.login:Credentials` model + `login.view.xml`/`change-password.view.xml`
  (the reusable dialog views now ship in the view module, see §5).
- **New module (Phase 3):** `…layout.view.selfservice` depending on
  `tl-security-selfservice` (mail/invitation deps the core lacks).

---

## 3. Current state — Phase 1 (DONE, browser-verified)

Login / logout / current-user display in the React UI, fully composed.

**Commits (branch `CWS/CWS_29108_react_login_dialog`):**
- `5dbb34768` — first cut (incl. the bespoke `TLLogin`/`TLUserMenu`).
- `44b930de6` — fix session swap (run in subsession context, not system).
- `9c2ac93d2` — **restructure**: removed bespoke components; compose existing
  controls via the config layer. This is the current shape.

**Implemented:**
- Core SPI: `LoginMethod`, `LoginMethodProvider`, `LoginMethodConfig`,
  `LoginMethods` (`com.top_logic.base.accesscontrol.login`).
- `ReactPasswordInputControl` + `TLPasswordInput` (`layout.react`).
- `layout.view`: `form/PasswordInputControlProvider`, `element/TextElement`
  (`<text>`), `login/LoginAction` (`<login>`), `login/LogoutCommand`
  (`<logout-command>`), `login/AnonymousOnly` (`<anonymous-only>`),
  `login/AuthenticatedOnly` (`<authenticated-only>`), `login/PendingSessionAction`,
  `login/I18NConstants`, `ViewServlet` swap hook, `WEB-INF/model/tl.login.model.xml`
  + `WEB-INF/autoconf/model.tl.login.config.xml`.
- View module: `WEB-INF/views/login.view.xml` (reusable feature view).
- Demo: the app-bar composition in `app.view.xml` (`currentPerson` channel +
  `<text>` + Login/Logout commands), which opens `login.view.xml`.

**Verified (Puppeteer, real browser):** anonymous (`"anonymous"` + Login) →
dialog (modal window, Name/Password fields, Login/Cancel) → `root`/`root1234` →
app bar shows `root` + Logout → logout → `anonymous`.

---

## 4. Remaining phases

### Phase 2 — password expiry, OTP, MFA enrollment
Mirror the legacy `LoginViewDialog` follow-up steps, composed as dialog views.
- After `Login.checkUserPassword` succeeds, branch (as `LoginViewDialog.doLogin`
  does): password expired → change-password step; MFA required & no secret →
  enrollment; MFA secret present → OTP verification.
- Services to reuse: `AuthenticationDevice.setPassword` + `PasswordValidator`
  (change password); `Person.getMFASecret/setMFASecret`, `MfaRequirement`,
  `DefaultCodeVerifier.isValidCode`, `MFAConfig` (OTP); TOTP secret + QR
  generation (`ZxingPngQrGenerator`) for enrollment.
- Compose as additional `.view.xml` dialogs (change-password form, OTP entry,
  MFA-enroll with QR image) chained from the login flow; only call
  `PendingSessionAction.requestLogin` once the final step passes.
- **Resolved (open question):** the partially-authenticated **account is carried
  on a dialog channel** (`LoginAction.ACCOUNT_CHANNEL = "account"`), not via a
  transient "pending-auth" holder. The session is not swapped until the final
  step calls `LoginAction.completeLogin(...)`. Each step's *form input* is a small
  transient model; cross-step state that can be re-derived (e.g. expiry) is not
  carried at all.

#### Phase 2a — password expiry → change password (DONE, browser-verified)
- `LoginAction` now branches: after a successful `checkUserPassword`, if
  `!Login.isPasswordValidAndNotExpired(password, account)` it opens
  `change-password.view.xml` (transient `tl.login:PasswordChange` model on the
  `model` channel + the `Person` on the `account` channel) **instead of** swapping
  the session.
- `ChangePasswordApplyAction` (`<change-password>`) reads the account from the
  channel + the new passwords from the form, checks match/non-empty, then reuses
  `AuthenticationDevice.setPassword` (validates the password policy, persists,
  clears the expiry flag, maintains history) and finally `LoginAction.completeLogin`
  → `PendingSessionAction.requestLogin` + reload.
- `OpenDialogAction.openDialog(...)` extracted as a reusable static that seeds
  multiple named channels (so Java actions can chain follow-up dialogs).
- Demo affordance: `ExpirePasswordAction` (`<expire-password>`) wired into the
  Formular-Demo accounts toolbar (`expirePassword` lives in the auth device's
  internal credential store, not a model attribute, so it cannot be set from
  TL-Script). New `tl.login:PasswordChange` transient type + `change-password.view.xml`.
- **Verified (Playwright):** expire `root` → login `root`/`root1234` → forced
  change dialog (app bar stays `anonymous`) → mismatch shows the error and keeps
  the dialog open → matching new password → login completes (app bar shows `root`).

#### Phase 2b — OTP verification + MFA enrollment (TODO)
- OTP-entry dialog: text field + verify action reusing `MFAConfig` +
  `DefaultCodeVerifier.isValidCode(secret, code)`; reuse the `account` channel.
- MFA-enrollment dialog: needs a generic **image display** in the view layer for
  the QR PNG — feasible by wrapping the existing `ReactPhotoViewerControl`
  (`TLPhotoViewer`, renders a `BinaryDataValue` as `<img>`) behind a new
  `<image>`/`<photo>` `UIElement`. Generate the secret (`Base32` + `SecureRandomService`)
  and QR (`ZxingPngQrGenerator`); on OTP confirm, persist via `Person.setMFASecret`,
  then `completeLogin`. Browser-verifying this needs a demo account configured
  with MFA (add a demo affordance similar to `<expire-password>`).

### Phase 3 — self-service module (forgot password)
- New module `com.top_logic.layout.view.selfservice` depending on
  `tl-security-selfservice`.
- React forgot-password flow over `InvitationModule`
  (`getResetPasswordMail`, verification-code generation/checking in
  `AbstractVerificationCodeDialog`), then reuse the Phase-2 change-password view.
- Contribute a "Forgot password?" affordance into `login.view.xml` (parallel to
  how the legacy dialog gets `ForgotPasswordDialog$Opener` via `toolbar-commands`).
- Requires `MailSenderService` configured; guard the affordance on that.

### Phase 4 — SSO providers ("Login with Google") via pac4j
- Implement `LoginMethodProvider` in `com.top_logic.security.auth.pac4j`,
  enumerating clients via `Pac4jConfigFactory.getInstance().getPac4jConfig()
  .getClients()`; one `LoginMethod` per client.
- Add display label/icon to `ClientConfigurator.Config` (clients currently have
  no presentation metadata).
- Render the methods in `login.view.xml` as redirect buttons:
  `window.location.assign(method.initiationUrl("/<ctx>/view/<window>/"))`. A
  small generic mechanism is needed to surface `LoginMethods.all()` into the
  view (e.g. a `<login-methods>` element or a channel) — design when starting.
- Register the provider in the pac4j module's config (`LoginMethodConfig`).
- Optional: retrofit the legacy `LoginViewDialog` to the same SPI so both UIs
  share one contract.

---

## 5. Known follow-ups / cleanups

- **`login.view.xml` location.** ✅ Done. The reusable feature views
  (`login.view.xml`, `change-password.view.xml`) now live in
  `com.top_logic.layout.view/src/main/webapp/WEB-INF/views/` alongside
  `designer.view.xml`. `ViewLoader` resolves them via the aggregated
  `FileManager`, so any app depending on the view module gets them for free; the
  demo only keeps `app.view.xml` (its app-bar composition) and the
  `<expire-password>` demo affordance. References are unchanged (paths are
  relative to `/WEB-INF/views/`).
- **German model labels.** `tl.login:Credentials` field labels fall back to
  English defaults ("Name"/"Password"); add a model messages bundle if German
  labels are wanted.
- **`currentUser()` for anonymous** shows the literal `"anonymous"` account
  label. Fine for validation; consider a "not logged in" presentation if desired.
- **Build/worktree note.** This worktree needed `clean` rebuilds of stale
  modules (e.g. `tl-security-auth-pac4j` was compiled against the old
  `pac4j-oidc` 6.3.3 while the branch upgraded to 6.5.2 → boot
  `NoSuchMethodError`). `.claude/scripts/rebuild-stale.sh` silently no-op'd
  (its `mvn exec:exec` enumeration tripped `set -e`); prefer explicit
  `mvn clean install -pl <module>` when a stale-jar `NoSuchMethodError` appears.

---

## 6. Key references

- Legacy login (to mirror): `com.top_logic/.../layout/component/configuration/`
  `LoginViewDialog`, `ChangePasswordDialog`, `CheckOTPDialog`,
  `EnableMultiFactorAuthenticationDialog`; `OpenLoginDialogHook`, `LogoutView`.
- Headless services: `base.accesscontrol.{Login,SessionService}`,
  `knowledge.wrap.person.{Person,PersonManager}`.
- View layer composition: `ViewServlet`, `element/*Element`, `command/*`,
  `channel/*`, `form/FieldControlService`; see also
  `docs/specs/29108-dynamic-slot-content.md` and root `react-integration.md`.
- pac4j: `com.top_logic.security.auth.pac4j` (`Pac4jConfigFactory`,
  `Pac4jAuthenticationServlet`, `/servlet/openid` + `/servlet/callback`,
  `startPage` return param).
