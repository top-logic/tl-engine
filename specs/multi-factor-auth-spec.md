# Specification: Multi-Factor Authentication and Self-Service Account Management

**Status:** Draft
**Created:** 2026-02-12
**Scope:** TopLogic Platform (tl-engine)

---

## 1. Motivation

TopLogic applications increasingly require authentication models beyond single-factor
username/password or external SSO. Specific needs include:

- **Two-factor authentication** (password + TOTP) for user groups that cannot use an
  external identity provider.
- **Passwordless authentication** ("login by email") where a user receives a one-time
  code via email instead of maintaining a password.
- **Self-service account management** (password reset, TOTP reset) without administrator
  intervention.
- **Invitation-based registration**, where existing users can invite external parties
  who then create their own accounts through a guided, token-secured process.
- **Open self-registration**, where users can create their own accounts directly from
  the login page -- useful for development systems, demo instances, and applications
  where user acquisition should be frictionless.
- **Anonymous browsing**, where unauthenticated visitors can interact with an application
  in a restricted way -- for example, browsing releases and tickets on the TopLogic
  development system without logging in, and only authenticating when they want to
  comment or create content.

These are general-purpose platform capabilities that benefit multiple application
scenarios. For example, a production application may use invitation-based registration
with mandatory TOTP for external users, while the TopLogic development system could
enable open self-registration with minimal friction and anonymous read access.

### Architectural Tension: The JSP Boundary

The current login flow creates a hard boundary between two worlds:

- The **unauthenticated "JSP world"** (`login.jsp`, `LoginPageServlet`) -- stateless
  HTML forms with hard-coded logic, no component model, no layout framework.
- The **authenticated "layout component world"** (`TopLogicServlet`, `TLLayoutServlet`)
  -- the full TopLogic component framework with declarative configuration, permissions,
  and model-driven UI.

`TopLogicServlet` enforces this boundary: if no `TLSessionContext` exists, the request
is redirected to `login.jsp`. This means every pre-authentication UI must be
implemented as raw JSPs outside the layout framework.

Adding multi-factor authentication makes this tension acute. After primary authentication
succeeds but before second-factor verification completes, the user is in a **limbo
state** -- neither fully unauthenticated nor authenticated. The current architecture
offers two bad options:

1. **More JSPs** -- inflexible, duplicates UI patterns, hard to customize, no component
   reuse. Each new authentication step (TOTP code entry, enrollment QR code, email OTP
   code, password reset forms) would require another hand-coded JSP page.
2. **Load the layout with a "partially authenticated" session** -- breaks the fundamental
   invariant that `TLSessionContext` implies full authentication. Every security check
   in the platform would need to distinguish "partially authenticated" from "fully
   authenticated", with enormous blast radius.

The same tension applies to self-service flows: a password reset triggered from the
login screen (unauthenticated) and a password change from personal settings
(authenticated) need the same UI, but would require separate implementations -- JSP
for one, layout component for the other.

This specification solves this tension by introducing **anonymous sessions** (section
3.2), which eliminate the unauthenticated JSP world entirely. All authentication,
self-service, and registration UIs become layout components within a unified framework.

---

## 2. Existing Architecture (Summary)

The following is a summary of the relevant existing components. All file paths are relative to
the tl-engine repository root.

### 2.1 Security Device Model

```
SecurityDevice                     (base interface, Config has id + disabled)
  |
  +-- AuthenticationDevice         (authentify, password management)
  |     +-- DBAuthenticationAccessDevice   (database password hashes)
  |     +-- LDAPAuthenticationAccessDevice (LDAP bind)
  |
  +-- PersonDataAccessDevice       (user data retrieval)
```

- **`SecurityDevice`** (`com.top_logic/...security/device/interfaces/SecurityDevice.java`):
  Base interface. Config defines `id` (mandatory) and `isDisabled`.

- **`AuthenticationDevice`** (`com.top_logic/...security/device/interfaces/AuthenticationDevice.java`):
  Extends `SecurityDevice`. Defines `authentify(LoginCredentials)`, `allowPwdChange()`,
  `setPassword(Person, char[])`, `getPasswordValidator()`, `expirePassword(Person)`,
  `isPasswordChangeRequested(Person, char[])`.

- **`TLSecurityDeviceManager`** (`com.top_logic/...security/device/TLSecurityDeviceManager.java`):
  Central registry. Configuration holds a map of `SecurityDevice.Config` keyed by device ID,
  plus `defaultAuthenticationDevice` and `defaultDataAccessDevice` strings.

### 2.2 Person and Device Assignment

- **`Person`** (`com.top_logic/...knowledge/wrap/person/Person.java`):
  Has attribute `authDeviceID` (String). Method `getAuthenticationDevice()` resolves the ID
  through `TLSecurityDeviceManager.getAuthenticationDevice(deviceID)`.

- Each Person has **exactly one** authentication device. There is no concept of a secondary
  factor.

### 2.3 Login Flow

- **`Login`** (`com.top_logic/...base/accesscontrol/Login.java`):
  `login(request, response, LoginCredentials)` resolves the Person's `AuthenticationDevice`,
  calls `authDevice.authentify(credentials)`, checks maintenance mode, creates session via
  `SessionService.loginUser()`.

- **`LoginPageServlet`** (`com.top_logic/...base/accesscontrol/LoginPageServlet.java`):
  Receives `username` and `password` from the login form. Single-step: authentication
  succeeds or fails in one request. After success, checks password expiry and optionally
  redirects to `changePwd.jsp`. Supports a `LoginHook` for additional post-authentication
  checks.

- **`LoginCredentials`** (`com.top_logic/...base/accesscontrol/LoginCredentials.java`):
  Carries `username` (String), `password` (char[]), `person` (Person). Password may be null
  for SSO flows.

- **`ExternalAuthenticationServlet`** (`com.top_logic/...base/accesscontrol/ExternalAuthenticationServlet.java`):
  Base class for external auth (OIDC, NTLM). Subclasses implement
  `retrieveLoginCredentials()`. After credential retrieval, calls
  `Login.loginFromExternalAuth()` which bypasses device-based authentication.

### 2.4 Password Infrastructure

- **`PasswordHashingService`** (`com.top_logic/...security/password/hashing/PasswordHashingService.java`):
  Pluggable hashing with algorithm migration support. Default: Argon2.
  Hash format: `<algorithm>#<hash>`.

- **`PasswordValidator`** (`com.top_logic/...security/password/PasswordValidator.java`):
  Configurable rules: minimum length, content criteria count, expiry period, repeat cycle.

- **`DBAuthenticationAccessDevice`** (`com.top_logic/...security/device/db/DBAuthenticationAccessDevice.java`):
  Stores password hashes in a `Password` table with `account`, `password`, `history`,
  `expired`, `modified` columns.

### 2.5 Email

- **`MailSenderService`** (`tl-mail-smtp/...base/mail/MailSenderService.java`):
  SMTP client. Configurable server, port, authentication, TLS. API: create `Mail` object,
  call `sendMail(Mail)`.

### 2.6 Extension Points

- **`LoginHook`** (`com.top_logic/...base/accesscontrol/LoginHook.java`):
  Interface with `check(request, response) -> ResKey`. Invoked after successful primary
  authentication. Returns a reason to deny login, or null to allow.

- **`ExternalUserMapping`** (`com.top_logic/...base/accesscontrol/ExternalUserMapping.java`):
  Interface with `findAccountForExternalName(String) -> Person`. Used by
  `ExternalAuthenticationServlet` to map external usernames to Person objects.

---

## 3. Proposed Changes

### 3.0 Overview

Six groups of changes are proposed:

| # | Feature | Scope | New Module? |
|---|---------|-------|-------------|
| 3.1 | Authentication Device Types | New security device types (TOTP, Email OTP) | Yes: `com.top_logic.security.auth.totp`; Email OTP in `com.top_logic.security.otp` |
| 3.2 | Anonymous Sessions and Login Transition | Core platform change: anonymous user, layout-based login | No (changes to `com.top_logic` / tl-core) |
| 3.3 | OTP Email Verification Service | New platform service | Yes: `com.top_logic.security.otp` |
| 3.4 | Self-Service Account Management | Password/TOTP reset components | Yes: `com.top_logic.security.selfservice` |
| 3.5 | Registration Framework | Invitation-based and open self-registration | Yes: `com.top_logic.security.register` |

The dependency graph is:

```
                    com.top_logic (tl-core)
                    [3.2: Anonymous sessions,
                     LoginComponent,
                     EnrollableDevice,
                     ChallengeResponseDevice]
                          |
              +-----------+-----------+
              |                       |
  com.top_logic.security.auth.totp   com.top_logic.security.otp
  [3.1: TOTP device]                 [3.1 + 3.3: OTP service,
                                      Email OTP device]
              |                       |
              +-----------+-----------+
                          |
              com.top_logic.security.selfservice
              [3.4: Password/TOTP reset components]
                          |
              com.top_logic.security.register
              [3.5: Registration components]
```

---

### 3.1 Authentication Device Types

#### 3.1.1 Purpose

Introduce new `AuthenticationDevice` implementations that extend the platform's
authentication capabilities beyond passwords and LDAP:

- **TOTP** (Time-Based One-Time Password, RFC 6238): An offline code generator
  (authenticator app). Can serve as a second factor alongside a password device, or
  as a standalone primary device.
- **Email OTP** ("login by email"): A one-time code sent via email. Can serve as a
  passwordless primary device, or as a second factor.

Both device types implement the existing `AuthenticationDevice` interface and integrate
with the login flow without requiring changes to the core authentication model.

#### 3.1.2 Design Approach: New Devices as AuthenticationDevice

Both TOTP and Email OTP authentication are conceptually the same as password
authentication: the user provides a credential (a code instead of a memorized password),
and the device verifies it. The existing `AuthenticationDevice` interface already
accommodates devices where the
password-management methods are not applicable -- `LDAPAuthenticationAccessDevice` returns
`false` for `allowPwdChange()` and does not support `setPassword()`. Both new device
types follow the same pattern. For TOTP:

| Method | TOTP behavior |
|--------|---------------|
| `authentify(LoginCredentials)` | Validates the TOTP code (passed via the password field in `LoginCredentials`). |
| `allowPwdChange()` | Returns `false`. |
| `setPassword()` | Throws `UnsupportedOperationException`. |
| `getPasswordValidator()` | Throws `UnsupportedOperationException`. |
| `expirePassword()` | No-op. |
| `isPasswordChangeRequested()` | Returns `false`. |

This means **no new device interface is needed** for the core authentication flow. The
existing `AuthenticationDevice` is sufficient, and the `TLSecurityDeviceManager` already
supports registering any number of `AuthenticationDevice` instances by ID.

#### 3.1.3 Mix-In Interfaces

Two mix-in interfaces are introduced in **tl-core** for authentication devices with
special lifecycle requirements. Code that needs these capabilities checks via
`device instanceof EnrollableDevice` or `device instanceof ChallengeResponseDevice`.

##### EnrollableDevice

Authentication devices like TOTP require an **enrollment** step (generating a secret,
showing a QR code, confirming the first code) that has no equivalent in
`AuthenticationDevice`. This is a provisioning concern, not an authentication concern.

A small mix-in interface is introduced in **tl-core**:

```java
package com.top_logic.base.security.device.interfaces;

/**
 * Optional mix-in for {@link AuthenticationDevice} implementations that require
 * an explicit enrollment step before authentication can succeed.
 *
 * <p>
 * For example, a TOTP device requires generating a shared secret and having
 * the user confirm it via an authenticator app before the device can
 * authenticate the user. A password-based device does not need this -- password
 * setup is handled via {@link AuthenticationDevice#setPassword}.
 * </p>
 *
 * <p>
 * Code that needs enrollment capabilities (registration flows, self-service
 * TOTP reset) checks {@code device instanceof EnrollableDevice}.
 * </p>
 */
public interface EnrollableDevice {

    /**
     * Whether the given account has completed enrollment for this device.
     */
    boolean isEnrolled(Person account);

    /**
     * Initiates enrollment. Returns an {@link Enrollment} containing the
     * information the user needs to set up their factor (e.g. a QR code URI).
     */
    Enrollment beginEnrollment(Person account);

    /**
     * Confirms enrollment by verifying a code produced with the newly enrolled
     * factor. After successful confirmation, the device can authenticate the
     * account.
     *
     * @return {@code true} if the code is valid and enrollment is now active.
     */
    boolean confirmEnrollment(Person account, Enrollment enrollment, String code);

    /**
     * Removes enrollment for the given account. After this call,
     * {@link #isEnrolled} returns {@code false} and authentication will fail
     * until re-enrollment.
     */
    void unenroll(Person account);
}
```

The `Enrollment` object:

```java
package com.top_logic.base.security.device.interfaces;

/**
 * Information needed by the user to complete enrollment for an
 * {@link EnrollableDevice}.
 */
public interface Enrollment {

    /**
     * A URI suitable for encoding as a QR code (e.g. an {@code otpauth://} URI
     * for TOTP). May be {@code null} if the device type does not use QR codes.
     */
    String getProvisioningUri();

    /**
     * A human-readable secret for manual entry (e.g. a Base32-encoded TOTP
     * secret). May be {@code null} if manual entry is not supported.
     */
    String getManualEntryKey();

    /**
     * An opaque token identifying this enrollment session. Used internally to
     * correlate {@link EnrollableDevice#confirmEnrollment} with the
     * corresponding {@link EnrollableDevice#beginEnrollment} call.
     */
    String getEnrollmentToken();
}
```

##### ChallengeResponseDevice

Some authentication devices require the server to **send a challenge** (e.g., an email
with a one-time code) before the user can respond with a credential. This is different
from TOTP, where the code is generated offline by the user's authenticator app.

```java
package com.top_logic.base.security.device.interfaces;

/**
 * Optional mix-in for {@link AuthenticationDevice} implementations where
 * authentication requires a server-initiated challenge before the user can
 * respond.
 *
 * <p>
 * For example, an Email OTP device must send a one-time code to the user's
 * email address before the user can enter it. A TOTP device does not need
 * this -- the code is generated client-side by the authenticator app.
 * </p>
 *
 * <p>
 * The login flow checks {@code device instanceof ChallengeResponseDevice}
 * and calls {@link #sendChallenge} before presenting the code input form.
 * </p>
 */
public interface ChallengeResponseDevice {

    /**
     * Sends a challenge to the user (e.g., an email with a one-time code).
     *
     * <p>
     * Must be called before {@link AuthenticationDevice#authentify} for this
     * device to work. The returned token is stored in the session and passed
     * back during validation.
     * </p>
     *
     * @param account the person to send the challenge to.
     * @return an opaque challenge token identifying this challenge session.
     * @throws ChallengeException if the challenge cannot be sent (e.g.,
     *         email delivery failure, no email address on account).
     */
    String sendChallenge(Person account) throws ChallengeException;

    /**
     * Returns a user-facing message describing where the challenge was sent
     * (e.g., "A code was sent to j***@example.com"). Used by the login
     * component to inform the user.
     *
     * @param account the person.
     * @return an I18N resource key for the message.
     */
    ResKey getChallengeMessage(Person account);
}
```

##### Placement

`EnrollableDevice`, `Enrollment`, and `ChallengeResponseDevice` are added to
`com.top_logic.base.security.device.interfaces` in **tl-core**. They are small
interfaces with no dependencies beyond `Person`.

#### 3.1.4 TOTP Device Implementation

A new module **`com.top_logic.security.auth.totp`** provides:

**`TOTPAuthenticationDevice`** implements both `AuthenticationDevice` and
`EnrollableDevice`:

- **TOTP generation and validation** per RFC 6238 / RFC 4226.
  - Algorithm: HMAC-SHA1 (default, configurable to SHA-256 or SHA-512).
  - Time step: 30 seconds (default, configurable).
  - Code length: 6 digits (default, configurable).
  - Clock skew tolerance: +/- 1 time step (configurable).
- **`authentify(LoginCredentials)`:** Extracts the TOTP code from
  `credentials.getPassword()` (as a char array), converts to String, and validates
  against the stored secret.
- **Secret storage:** TOTP secrets are stored in a new database table (similar to the
  existing `Password` table used by `DBAuthenticationAccessDevice`):

  | Column | Type | Description |
  |--------|------|-------------|
  | `account` | Reference (Person) | The person this secret belongs to |
  | `secret` | String | Encrypted TOTP secret (Base32-encoded, then encrypted at rest) |
  | `confirmed` | Boolean | Whether enrollment has been confirmed |
  | `confirmedAt` | Timestamp | When enrollment was confirmed |
  | `createdAt` | Timestamp | When the secret was created |

- **QR code generation:** The `beginEnrollment()` method generates a standard
  `otpauth://totp/...` provisioning URI. QR code rendering is the responsibility of the
  UI layer (the module provides the URI string; a layout component renders it as a QR
  code image using a client-side library or a server-side renderer).
- **Secret encryption at rest:** TOTP secrets are encrypted before storage using a
  configurable symmetric key (application secret). The key is configured via the
  standard TopLogic alias mechanism (e.g., `%TOTP_ENCRYPTION_KEY%`).

**Configuration:**

```java
public interface Config extends AuthenticationDevice.Config<TOTPAuthenticationDevice> {

    /** HMAC algorithm: HmacSHA1, HmacSHA256, HmacSHA512. */
    @Name("algorithm")
    @StringDefault("HmacSHA1")
    String getAlgorithm();

    /** Time step in seconds. */
    @Name("time-step")
    @IntDefault(30)
    int getTimeStep();

    /** Number of digits in the generated code. */
    @Name("code-length")
    @IntDefault(6)
    int getCodeLength();

    /** Number of time steps to allow before/after the current step (clock skew). */
    @Name("clock-skew-steps")
    @IntDefault(1)
    int getClockSkewSteps();

    /** Issuer name displayed in authenticator apps. */
    @Name("issuer")
    @Mandatory
    String getIssuer();

    /** Alias for the symmetric encryption key for secret storage. */
    @Name("encryption-key")
    @Encrypted
    @Mandatory
    String getEncryptionKey();
}
```

**Registration in `TLSecurityDeviceManager`:**

No changes needed. Since `TOTPAuthenticationDevice` implements `AuthenticationDevice`,
it is registered in the existing `security-devices` map and is accessible via the
existing `getAuthenticationDevice(deviceID)` method.

**Example configuration:**

```xml
<security-devices>
    <!-- Existing primary auth device -->
    <security-device id="dbSecurity"
        class="com.top_logic.base.security.device.db.DBAuthenticationAccessDevice">
        ...
    </security-device>

    <!-- TOTP device (usable as primary or secondary auth device) -->
    <security-device id="totp"
        class="com.top_logic.security.auth.totp.TOTPAuthenticationDevice"
        issuer="MyApplication"
        encryption-key="%TOTP_ENCRYPTION_KEY%"
    />
</security-devices>
```

**Usage scenarios:**

- *TOTP as second factor:* Person has `authDeviceID = "dbSecurity"` and
  `secondAuthDeviceID = "totp"`. Login requires password, then TOTP code.
- *TOTP as sole factor:* Person has `authDeviceID = "totp"` and no
  `secondAuthDeviceID`. Login requires only a TOTP code.
- *Email OTP as sole factor ("login by email"):* Person has `authDeviceID = "emailOtp"`
  and no `secondAuthDeviceID`. Login component asks for username, system sends code to
  email, user enters code.
- *Email OTP as second factor:* Person has `authDeviceID = "dbSecurity"` and
  `secondAuthDeviceID = "emailOtp"`. Login requires password, then email code.
- *Password only:* Person has `authDeviceID = "dbSecurity"` and no
  `secondAuthDeviceID`. Existing behavior, unchanged.

#### 3.1.5 TOTP External Dependencies

The TOTP module requires a TOTP library. Candidates:

- `com.eatthepath:java-otp` (lightweight, well-maintained)
- `dev.samstevens:totp` (higher-level API, includes QR code helpers)

Alternatively, the TOTP algorithm (RFC 6238) is simple enough to implement directly
using `javax.crypto.Mac`, avoiding an external dependency. This is a design-time decision.

#### 3.1.6 Email OTP Authentication Device

An **Email OTP device** implements `AuthenticationDevice` and `ChallengeResponseDevice`.
It delegates to the `OTPService` (section 3.3) for code generation, delivery, and
validation.

**`EmailOTPAuthenticationDevice`:**

| Method | Behavior |
|--------|----------|
| `authentify(LoginCredentials)` | Validates the OTP code (from `credentials.getPassword()`) against the `OTPService`. |
| `allowPwdChange()` | Returns `false`. |
| `setPassword()` | Throws `UnsupportedOperationException`. |
| `getPasswordValidator()` | Throws `UnsupportedOperationException`. |
| `expirePassword()` | No-op. |
| `isPasswordChangeRequested()` | Returns `false`. |
| `sendChallenge(Person)` | Resolves the person's email address, calls `OTPService.generate(email, "login")`, stores the session token internally. Returns the session token. |
| `getChallengeMessage(Person)` | Returns an I18N message with the masked email address (e.g., "Code sent to j\*\*\*@example.com"). |

**Email resolution:** The device needs the person's email address to send the OTP. This
is resolved via a configurable `AccountResolver` strategy (same interface as section
3.4.6). The default implementation uses the Person's email attribute.

**Configuration:**

```java
public interface Config extends AuthenticationDevice.Config<EmailOTPAuthenticationDevice> {

    /** Purpose string used when calling OTPService. */
    @Name("purpose")
    @StringDefault("login")
    String getPurpose();

    /** Strategy for resolving the email address from a Person. */
    @Name("account-resolver")
    @InstanceDefault(DefaultAccountResolver.class)
    PolymorphicConfiguration<AccountResolver> getAccountResolver();
}
```

The Email OTP device does **not** implement `EnrollableDevice` -- there is no enrollment
step. If the person has a valid email address, the device is ready to use.

**Module placement:** `EmailOTPAuthenticationDevice` is placed in the
`com.top_logic.security.otp` module alongside `OTPService`, since it directly depends
on the OTP infrastructure.

**Example configuration:**

```xml
<security-devices>
    <!-- Existing primary auth device -->
    <security-device id="dbSecurity"
        class="com.top_logic.base.security.device.db.DBAuthenticationAccessDevice">
        ...
    </security-device>

    <!-- TOTP device -->
    <security-device id="totp"
        class="com.top_logic.security.auth.totp.TOTPAuthenticationDevice"
        issuer="MyApplication"
        encryption-key="%TOTP_ENCRYPTION_KEY%"
    />

    <!-- Email OTP device ("login by email") -->
    <security-device id="emailOtp"
        class="com.top_logic.security.otp.EmailOTPAuthenticationDevice"
    />
</security-devices>
```

---

### 3.2 Anonymous Sessions and Login Transition

#### 3.2.1 Purpose

Eliminate the boundary between the unauthenticated JSP world and the authenticated
layout component world. Instead of maintaining a separate login UI stack (`login.jsp`,
`LoginPageServlet`, `changePwd.jsp`, and any new 2FA pages), every visitor immediately
receives a layout session as a technical **anonymous user**. Login, multi-factor
authentication, enrollment, self-service, and registration all become layout components
within the standard TopLogic framework.

This serves two goals:

1. **Unified UI framework.** All user-facing pages -- including login, 2FA, password
   reset, registration -- are layout components. Applications customize them using the
   same tools (layout configuration, component configuration, CSS themes) they use for
   everything else. The `login.jsp` approach is retired.

2. **Anonymous browsing.** Applications can grant anonymous users read access to
   selected content. For example, the TopLogic development system can allow anonymous
   users to browse releases and tickets, requiring login only for write actions like
   commenting or creating tickets. The permission system controls what anonymous users
   can see and do -- no new access control mechanism is needed.

#### 3.2.2 The Anonymous Person

A well-known technical `Person` account named **`anonymous`** is introduced:

- Exists in every TopLogic application by default (created during initial data setup,
  similar to the `root` account).
- Cannot be deleted or disabled.
- Cannot be the target of a direct login (no password, no `authDeviceID`).
- Has **minimal default permissions** -- effectively nothing, unless the application
  explicitly grants permissions to the anonymous user or a role it belongs to.
- The anonymous Person is the inverse of `root`: where `root` can do everything, the
  anonymous user can do almost nothing.

Applications configure what anonymous users can see and do using the standard
role/permission system:

- An application that wants no anonymous access at all configures the anonymous layout
  to show only the login component (equivalent to today's `login.jsp`, but as a
  component).
- An application that wants anonymous browsing grants the anonymous user (or an
  "anonymous" role) read access to specific types/views. The layout shows navigation,
  content areas, and a "Login" button. After login, the layout reloads with the
  authenticated user's permissions, revealing additional functionality.

#### 3.2.3 Session Lifecycle

```
Browser requests any URL
    |
    v
TopLogicServlet: no session exists
    |
    v
SessionService creates anonymous session:
  - New HttpSession
  - TLSessionContext with anonymous Person
  - Anonymous layout loaded
    |
    v
Anonymous user sees the anonymous layout:
  - Login component visible (permission-controlled)
  - Application content per anonymous permissions
    |
    v
User interacts with Login component:
  - Enters username + password
  - If primary device is ChallengeResponseDevice:
    component sends challenge, shows code input
  - If 2FA needed: component shows second-factor step
  - If enrollment needed: component shows enrollment step
    |
    v
All authentication factors verified.
Session upgrade:
  1. Current HttpSession invalidated (session fixation protection)
  2. New HttpSession created
  3. New TLSessionContext with authenticated Person
  4. Full page reload
    |
    v
Authenticated user sees the authenticated layout:
  - Login component hidden
  - Full application per user's permissions
```

**Key property:** The session is *never* in a "partially authenticated" state at the
`TLSessionContext` level. Either the session belongs to the anonymous Person, or it
belongs to a fully authenticated Person. The multi-step login interaction (password,
2FA, enrollment) happens entirely within the anonymous session's layout, managed by
the `LoginComponent`'s internal state. Only when all factors are verified does the
session transition occur.

#### 3.2.4 Anonymous Layout

Applications configure a **separate layout** for anonymous sessions. This allows:

- **Minimal layout:** Just the `LoginComponent` -- equivalent to today's `login.jsp`
  but built with layout components. Application styling, logo, and theme apply
  automatically. This is the default for applications that do not want anonymous
  browsing.
- **Content layout:** Navigation, read-only content areas, and a login button. For
  applications where anonymous users should be able to browse content.

The anonymous layout is loaded when the anonymous session is created. After login
(session upgrade + full page reload), the authenticated user's layout is loaded.

**Configuration:**

```java
// In SessionService.Config or a new AnonymousSessionConfig:

/**
 * Layout definition for anonymous sessions. This layout is loaded when
 * a visitor without an active session accesses the application.
 *
 * <p>
 * The default anonymous layout shows only a login component.
 * Applications can configure a richer layout with read-only content
 * areas for anonymous browsing.
 * </p>
 */
@Name("anonymous-layout")
@Mandatory
String getAnonymousLayout();
```

**Relation to `ApplicationPages`:** The existing `ApplicationPages.getLoginPage()` and
related paths become obsolete. The anonymous layout definition replaces them.

#### 3.2.5 Changes to `TopLogicServlet`

Today, `TopLogicServlet.getSession()` redirects to `login.jsp` when no session exists.
The change:

```
Today:
  Request arrives -> no TLSessionContext? -> redirect to login.jsp

New:
  Request arrives -> no TLSessionContext? -> create anonymous session
                                          -> load anonymous layout
                                          -> proceed normally
```

The `TopLogicServlet` creates an anonymous session on demand (via `SessionService`)
when it encounters a request without a valid session. From that point on, the request
is handled exactly like any other authenticated request -- the layout framework loads,
components render, permissions are evaluated. The only difference is that the Person in
the session context is the anonymous Person.

#### 3.2.6 Person Model Extension

A new attribute is added to `Person`:

```java
// In Person.java:
public static final String SECOND_AUTH_DEVICE_ID = "secondAuthDeviceID";

/**
 * The ID of the secondary {@link AuthenticationDevice} for this account,
 * or {@code null} if no second factor is required.
 */
public String getSecondAuthDeviceID() {
    return tGetDataString(SECOND_AUTH_DEVICE_ID);
}

/**
 * Resolves the secondary {@link AuthenticationDevice} for this account.
 *
 * @return the second authentication device, or {@code null} if none is
 *         configured.
 */
public AuthenticationDevice getSecondAuthDevice() {
    String deviceId = getSecondAuthDeviceID();
    if (StringServices.isEmpty(deviceId)) {
        return null;
    }
    return TLSecurityDeviceManager.getInstance()
        .getAuthenticationDevice(deviceId);
}
```

This follows the same pattern as the existing `authDeviceID` /
`getAuthenticationDevice()` pair. Both the primary and secondary device are standard
`AuthenticationDevice` instances -- the framework does not distinguish between device
types. The "second factor" semantic is purely a matter of how the Person is configured.

#### 3.2.7 Login Component

A new layout component **`LoginComponent`** replaces `LoginPageServlet` and `login.jsp`.
It is a standard TopLogic `LayoutComponent` that:

- Is visible only to the anonymous Person (controlled via component visibility /
  security configuration).
- Renders the login form and manages the complete multi-step authentication flow
  internally.
- On successful completion of all authentication factors, triggers a session upgrade
  and full page reload.

**Component state machine:**

The `LoginComponent` manages a linear state machine. Each state renders a different
form. The user progresses forward through form submissions; the component validates
each step before advancing.

```
CREDENTIALS
  |
  +--> [if primary is ChallengeResponseDevice] --> PRIMARY_CHALLENGE
  |                                                    |
  |                                                    v
  +--> [if primary is standard device] ----------> PRIMARY_DONE
                                                       |
       +-----------------------------------------------+
       |
       +--> [no secondAuthDeviceID] --> COMPLETE
       |
       +--> [secondAuthDevice, enrolled] --> SECOND_FACTOR --> COMPLETE
       |
       +--> [secondAuthDevice, not enrolled] --> ENROLLMENT --> COMPLETE
       |
       +--> [secondAuthDevice is ChallengeResponseDevice] --> SECOND_CHALLENGE
                                                                  |
                                                                  v
                                                            SECOND_FACTOR --> COMPLETE
```

**States:**

| State | Renders | User Action |
|-------|---------|-------------|
| `CREDENTIALS` | Username + password fields (or username-only if primary device is `ChallengeResponseDevice`). | Submit credentials. |
| `PRIMARY_CHALLENGE` | Message from `ChallengeResponseDevice.getChallengeMessage()` + code input field. | Enter code received via email. |
| `SECOND_FACTOR` | Code input field (for TOTP or other second device). | Enter TOTP code or email code. |
| `SECOND_CHALLENGE` | Message from second device's `getChallengeMessage()` + code input field. | Enter code received via email. |
| `ENROLLMENT` | QR code (from `Enrollment.getProvisioningUri()`), manual entry key, + confirmation code input. | Scan QR code, enter confirmation code. |
| `COMPLETE` | (Not rendered -- triggers session upgrade.) | -- |

The `CREDENTIALS` state adapts its rendering based on the primary device type. If the
application only uses standard password authentication, the form looks identical to
today's `login.jsp` -- username field, password field, submit button.

**Rate limiting:** The component reuses the existing `LoginFailure` mechanism. Failed
authentication attempts (wrong password, wrong TOTP code, wrong email OTP code) are
tracked per username + IP address, with the same exponential backoff as today. For
`ChallengeResponseDevice` primary usage, rate limiting also bounds the number of
challenge emails per username (configurable, see 3.2.12).

**Error display:** Validation errors (wrong code, expired code, rate-limited) are
displayed inline in the component, not via page redirects.

#### 3.2.8 Login Scenarios

**Scenario A -- Password only (existing behavior, no second factor):**

```
Anonymous session active, LoginComponent in CREDENTIALS state.

User enters username + password -> Submit
  LoginComponent:
    1. Resolve Person by username
    2. authDevice.authentify(credentials) -> success
    3. No secondAuthDeviceID
    4. -> COMPLETE -> session upgrade -> reload
```

**Scenario B -- Password + TOTP (most common MFA):**

```
Anonymous session active, LoginComponent in CREDENTIALS state.

User enters username + password -> Submit
  LoginComponent:
    1. Resolve Person
    2. authDevice.authentify(credentials) -> success
    3. secondAuthDevice = TOTP, isEnrolled = true
    4. -> SECOND_FACTOR state (renders TOTP code input)

User enters TOTP code -> Submit
  LoginComponent:
    5. secondAuthDevice.authentify(code) -> success
    6. -> COMPLETE -> session upgrade -> reload
```

**Scenario C -- Password + Email OTP second factor:**

```
Anonymous session active, LoginComponent in CREDENTIALS state.

User enters username + password -> Submit
  LoginComponent:
    1. Resolve Person
    2. authDevice.authentify(credentials) -> success
    3. secondAuthDevice = EmailOTP (ChallengeResponseDevice)
    4. sendChallenge(person) -> email sent
    5. -> SECOND_CHALLENGE state (shows "Code sent to j***@example.com")

User enters email code -> Submit
  LoginComponent:
    6. secondAuthDevice.authentify(code) -> success
    7. -> COMPLETE -> session upgrade -> reload
```

**Scenario D -- Email OTP as sole primary device ("login by email"):**

```
Anonymous session active, LoginComponent in CREDENTIALS state.
(LoginComponent detects primary device is ChallengeResponseDevice
 and renders username-only input.)

User enters username -> Submit
  LoginComponent:
    1. Resolve Person
    2. Primary device is ChallengeResponseDevice
    3. sendChallenge(person) -> email sent
    4. -> PRIMARY_CHALLENGE state (shows "Code sent to j***@example.com")

User enters email code -> Submit
  LoginComponent:
    5. authDevice.authentify(code) -> success
    6. No secondAuthDeviceID
    7. -> COMPLETE -> session upgrade -> reload
```

Note: In scenario D, the user has not yet proven their identity when `sendChallenge()`
is called (the username alone is not a credential). This is acceptable because the
challenge is sent to the registered email -- an attacker who enters a valid username
only triggers an email to the legitimate owner. To prevent abuse, rate limiting applies
(see section 3.2.12).

**Scenario E -- Enrollment required (second factor not yet set up):**

```
Anonymous session active, LoginComponent in CREDENTIALS state.

User enters username + password -> Submit
  LoginComponent:
    1. Resolve Person
    2. authDevice.authentify(credentials) -> success
    3. secondAuthDevice = TOTP, isEnrolled = false
    4. beginEnrollment(person) -> Enrollment with QR code URI
    5. -> ENROLLMENT state (shows QR code + manual key + code input)

User scans QR code, enters confirmation code -> Submit
  LoginComponent:
    6. confirmEnrollment(person, enrollment, code) -> success
    7. -> COMPLETE -> session upgrade -> reload
```

#### 3.2.9 TOTP Enrollment Use Cases

The `EnrollableDevice` interface and the TOTP enrollment flow serve three distinct
use cases. All three use the same `beginEnrollment()` / `confirmEnrollment()` methods
but differ in how the flow is triggered:

**Use case A: Voluntary enrollment from personal settings**

A logged-in user with only a primary auth device decides to add a second factor.

1. User navigates to personal settings (within authenticated session).
2. Clicks "Set up two-factor authentication".
3. System calls `beginEnrollment(person)` -> shows QR code.
4. User scans QR code, enters confirmation code.
5. System calls `confirmEnrollment(person, enrollment, code)`.
6. On success, system sets `secondAuthDeviceID = "totp"` on the Person.
7. From the next login onward, the second factor is required.

This is analogous to the existing voluntary password change. It operates within an
authenticated session and is initiated by the user.

**Use case B: Enforced enrollment on first login**

An administrator (or a provisioning system) configures `secondAuthDeviceID = "totp"` on
a Person before the user has enrolled. On the next login:

1. User enters username + password -> primary auth succeeds.
2. `LoginComponent` detects `secondAuthDeviceID` is set but `isEnrolled()` is `false`.
3. Component transitions to ENROLLMENT state.
4. User completes enrollment (QR code -> confirmation code).
5. `confirmEnrollment()` succeeds -> session upgrade -> reload.

This is analogous to the existing forced password change on first login. The
administrator sets the requirement; the user fulfills it during login.

**Use case C: Enrollment during invitation/registration**

A new user going through the registration flow (section 3.5) encounters the
TOTP enrollment component:

1. Registration flow reaches the TOTP enrollment step.
2. System calls `beginEnrollment(person)` -> shows QR code.
3. User scans QR code, enters confirmation code.
4. System calls `confirmEnrollment()` -> step complete, flow continues.

This is the same enrollment logic, just executed within the registration component
rather than the login component.

**Summary:**

| Use case | Trigger | Context | Analog |
|---|---|---|---|
| A: Voluntary | User-initiated from settings | Authenticated session | Voluntary password change |
| B: Enforced | `secondAuthDeviceID` set, not enrolled | Login component (anonymous session) | Forced password change (expired password) |
| C: Registration | Registration flow | Anonymous session (registration) | Initial password setup during registration |

All three use cases call the same `EnrollableDevice` methods. The only difference is
where and when the flow is triggered.

#### 3.2.10 Changes to `Login`

New methods are added:

```java
/**
 * Verifies the second authentication factor for a user in the process
 * of logging in.
 *
 * @param person the person (already authenticated by primary device).
 * @param code   the code entered by the user.
 * @return {@code true} if the code is valid.
 * @throws LoginDeniedException if the person has no second auth device.
 */
public boolean verifySecondFactor(Person person, String code) {
    AuthenticationDevice device = person.getSecondAuthDevice();
    if (device == null) {
        throw new LoginDeniedException("No second auth device configured.");
    }
    try (LoginCredentials credentials =
            LoginCredentials.fromUserAndPassword(person, code.toCharArray())) {
        return device.authentify(credentials);
    }
}

/**
 * Whether the given person requires a second authentication step.
 */
public boolean requiresSecondFactor(Person person) {
    return person.getSecondAuthDevice() != null;
}

/**
 * Whether the given person's second auth device requires enrollment
 * before it can authenticate.
 */
public boolean requiresSecondFactorEnrollment(Person person) {
    AuthenticationDevice device = person.getSecondAuthDevice();
    if (device == null) {
        return false;
    }
    if (device instanceof EnrollableDevice enrollable) {
        return !enrollable.isEnrolled(person);
    }
    return false;
}
```

Note that `verifySecondFactor` uses the standard `authentify(LoginCredentials)`
method -- the second device is just another `AuthenticationDevice` and receives the
code via the password field in `LoginCredentials`.

A new method supports the primary-challenge-response flow:

```java
/**
 * Whether the given person's primary auth device requires a server-sent
 * challenge before authentication (e.g., Email OTP).
 */
public boolean requiresPrimaryChallenge(Person person) {
    return person.getAuthenticationDevice()
        instanceof ChallengeResponseDevice;
}
```

#### 3.2.11 Session Upgrade Mechanics

When the `LoginComponent` reaches the COMPLETE state (all factors verified), it
triggers a session upgrade:

1. **Invalidate the current HttpSession.** This is the anonymous session. Invalidation
   destroys all session-scoped state, including the anonymous `TLSessionContext`. This
   is the standard defense against session fixation attacks.
2. **Create a new HttpSession** via `SessionService.loginUser(request, response, person)`.
   This follows the existing code path: new HttpSession, new `TLSessionContext` with
   the authenticated Person, secure cookie configuration.
3. **Full page reload.** The response directs the browser to reload the application
   entry point. The browser sends a new request with the new session cookie. The
   `TopLogicServlet` finds a valid session for the authenticated Person and loads the
   authenticated layout.

The reload is a clean break. No state carries over from the anonymous session to the
authenticated session. The authenticated layout loads fresh, as if the user had just
arrived. This is equivalent to today's redirect from `LoginPageServlet` to the start
page, but within the layout framework.

**LoginHook compatibility:** The existing `LoginHook` is invoked during the session
upgrade, after all authentication factors have been verified but before the
authenticated session is created. If the hook denies login, the `LoginComponent`
displays the denial reason and remains in the anonymous session.

**No changes for SSO users:** The `ExternalAuthenticationServlet` path
(`loginFromExternalAuth`) is not affected. SSO users bypass the anonymous session
entirely -- the external auth servlet creates the authenticated session directly.

#### 3.2.12 Configuration

New options:

```java
/**
 * Maximum time in seconds the LoginComponent allows between successful
 * primary authentication and second-factor submission or enrollment
 * completion. If exceeded, the component resets to the CREDENTIALS state
 * and the user must re-authenticate from the beginning.
 */
@Name("second-factor-timeout")
@IntDefault(300)
int getSecondFactorTimeout();

/**
 * Maximum number of challenge emails that can be sent for the same
 * username within the rate limit window. Prevents abuse of
 * ChallengeResponseDevice as a primary device (e.g., flooding a user's
 * inbox). Uses the existing LoginFailure rate limiting mechanism.
 */
@Name("max-challenges-per-window")
@IntDefault(5)
int getMaxChallengesPerWindow();
```

---

### 3.3 OTP Email Verification Service

#### 3.3.1 Purpose

Provide a general-purpose service for generating, storing, and validating one-time
passwords (numeric codes) delivered via email. The `OTPService` serves two roles:

1. **Building block** for self-service flows (section 3.4) and registration flows
   (section 3.5), where email verification is a step in a multi-step process.
2. **Backend for `EmailOTPAuthenticationDevice`** (section 3.1.6), where an OTP code
   *is* the authentication credential ("login by email").

#### 3.3.2 Service Interface

```java
package com.top_logic.security.otp;

/**
 * Service for generating and validating one-time passwords delivered via email.
 *
 * <p>
 * OTPs are generated, stored server-side with an expiry, and sent to a
 * specified email address. Validation checks the code, enforces expiry,
 * and limits the number of verification attempts.
 * </p>
 */
public interface OTPService {

    /**
     * Generates a new OTP for the given purpose and email address, and sends
     * it via email.
     *
     * <p>
     * Any previously generated, unconsumed OTP for the same email and purpose
     * is invalidated.
     * </p>
     *
     * @param email   the recipient email address.
     * @param purpose a string identifying the purpose (e.g., "registration",
     *                "password-reset"). Different purposes have independent OTPs.
     * @return an opaque token identifying this OTP session, to be passed to
     *         {@link #validate}.
     * @throws OTPException if email sending fails.
     */
    String generate(String email, String purpose) throws OTPException;

    /**
     * Validates an OTP code.
     *
     * @param sessionToken the token returned by {@link #generate}.
     * @param code         the code entered by the user.
     * @return the result of validation.
     */
    OTPValidationResult validate(String sessionToken, String code);
}
```

**`OTPValidationResult`:**

```java
public enum OTPValidationResult {
    /** Code is correct. The OTP is consumed and cannot be reused. */
    VALID,
    /** Code is incorrect. The attempt is recorded. */
    INVALID,
    /** The OTP has expired. A new one must be generated. */
    EXPIRED,
    /** Too many failed attempts. The OTP is invalidated. */
    MAX_ATTEMPTS_EXCEEDED,
    /** The session token is unknown (already consumed or never existed). */
    UNKNOWN_SESSION
}
```

#### 3.3.3 Storage

OTP state is stored in a database table:

| Column | Type | Description |
|--------|------|-------------|
| `sessionToken` | String (PK) | Opaque session identifier |
| `email` | String | Recipient email |
| `purpose` | String | Purpose identifier |
| `codeHash` | String | Hashed OTP code (not stored in plain text) |
| `createdAt` | Timestamp | Generation time |
| `expiresAt` | Timestamp | Expiry time |
| `attempts` | Integer | Number of failed validation attempts |
| `consumed` | Boolean | Whether the OTP has been successfully validated |

Expired and consumed entries are periodically cleaned up.

#### 3.3.4 Configuration

```java
public interface Config extends ConfiguredManagedClass.Config<OTPServiceImpl> {

    /** Number of digits in the generated code. */
    @Name("code-length")
    @IntDefault(8)
    int getCodeLength();

    /** Validity period in minutes. */
    @Name("validity-minutes")
    @IntDefault(15)
    int getValidityMinutes();

    /** Maximum number of failed validation attempts before invalidation. */
    @Name("max-attempts")
    @IntDefault(5)
    int getMaxAttempts();

    /** Email subject template. Use {purpose} as placeholder. */
    @Name("email-subject")
    @StringDefault("Your verification code")
    String getEmailSubject();

    /** Email body template. Use {code} and {validMinutes} as placeholders. */
    @Name("email-body-template")
    String getEmailBodyTemplate();
}
```

#### 3.3.5 Module

This service can be placed either:
- In a new module `com.top_logic.security.otp`, or
- Directly in tl-core if the dependency on `MailSenderService` is acceptable.

Since `MailSenderService` is in `tl-mail-smtp` (a separate module), a new module
`com.top_logic.security.otp` depending on both `tl-core` and `tl-mail-smtp` is cleaner.

---

### 3.4 Self-Service Account Management

#### 3.4.1 Purpose

Provide platform-level flows for users to reset their own passwords and TOTP enrollment
without administrator intervention, using email verification as the identity proof.

Because all pre-authentication UI now operates within the layout framework (via
anonymous sessions, section 3.2), self-service flows are implemented as **layout
components**. The same components work in both contexts:

- **From the anonymous session:** Accessible via a "Forgot password?" or "Reset
  authenticator" link in the anonymous layout, next to the login component.
- **From an authenticated session:** Accessible via personal settings, where a
  logged-in user can change their own password or re-enroll their TOTP device.

This eliminates the need for a separate servlet with JSP pages. The password reset
component used by an anonymous user is the *same* component used by an authenticated
user in personal settings -- only the initial identity-proof steps differ (anonymous
users must verify via email OTP; authenticated users are already identified).

#### 3.4.2 Module

New module: **`com.top_logic.security.selfservice`**
Dependencies: `tl-core`, `com.top_logic.security.otp`, `com.top_logic.security.auth.totp`

#### 3.4.3 Password Reset Flow

```
User                      System
 |                           |
 | 1. "Forgot password"      |
 |    Enter email/username   |
 |-------------------------->|
 |                           | 2. Resolve Person by username (or email)
 |                           |    Verify Person has a password-based
 |                           |    AuthenticationDevice (allowPwdChange)
 |                           |
 |                           | 3. OTPService.generate(email, "password-reset")
 |   <-- OTP email sent --   |
 |                           |
 | 4. Enter OTP code         |
 |-------------------------->|
 |                           | 5. OTPService.validate(session, code)
 |                           |
 |                           | -- If Person has second factor enrolled --
 | 6. Enter TOTP code        |
 |-------------------------->|
 |                           | 7. secondAuthDevice.authentify(credentials)
 |                           |
 | 8. Enter new password     |
 |    (+ confirmation)       |
 |-------------------------->|
 |                           | 9. PasswordValidator.validatePassword()
 |                           | 10. AuthenticationDevice.setPassword()
 |                           |
 |   <-- Success message --  |
```

**Design notes:**
- Step 6-7 (TOTP verification) is only required if the person has a second factor
  enrolled. This ensures that an attacker who compromises the email account alone
  cannot reset the password of a TOTP-protected account.
- If no second factor is enrolled, the flow proceeds from step 5 directly to step 8.
- The "enter email/username" page must not reveal whether the account exists (to
  prevent user enumeration). If the account is not found, the system still shows a
  "code sent" message but does not actually send an email.

#### 3.4.4 TOTP Reset Flow

```
User                      System
 |                           |
 | 1. "Reset authenticator"  |
 |    Enter username         |
 |    Enter password         |
 |-------------------------->|
 |                           | 2. Resolve Person
 |                           | 3. AuthenticationDevice.authentify()
 |                           |    (verify password)
 |                           |
 |                           | 4. OTPService.generate(email, "totp-reset")
 |   <-- OTP email sent --   |
 |                           |
 | 5. Enter OTP code         |
 |-------------------------->|
 |                           | 6. OTPService.validate(session, code)
 |                           |
 |                           | 7. enrollableDevice.unenroll(person)
 |                           | 8. enrollableDevice.beginEnrollment(person)
 |                           |
 |   <-- QR code display --  |
 |                           |
 | 9. Scan QR code,          |
 |    enter TOTP code        |
 |-------------------------->|
 |                           | 10. enrollableDevice.confirmEnrollment()
 |                           |
 |   <-- Success message --  |
```

**Design notes:**
- Step 1 requires the password (knowledge factor). This ensures that a TOTP reset
  requires both password knowledge and email access. An attacker needs to compromise
  both to reset the TOTP.

#### 3.4.5 Implementation: Layout Components

The self-service flows are implemented as layout components with internal state machines,
following the same pattern as the `LoginComponent` (section 3.2.7):

- **`PasswordResetComponent`** -- Multi-step component managing the password reset
  flow. Each step (email/username input, OTP verification, optional TOTP verification,
  new password entry) is a state in the component's state machine.

- **`TOTPResetComponent`** -- Multi-step component managing the TOTP reset flow.
  Steps: username + password, OTP verification, new enrollment (QR code + confirmation).

Both components operate in the anonymous session context (the anonymous Person has
permission to use them). They do not require an authenticated session because the
identity proof comes from email OTP + optional TOTP verification, not from an existing
session.

When used from an authenticated session (personal settings), the same components can
skip the initial identity-proof steps since the user is already authenticated.

**Layout configuration in the anonymous layout:**

```xml
<layout>
    <!-- Login area -->
    <component class="...LoginComponent" />

    <!-- Self-service links (visible to anonymous) -->
    <component class="...PasswordResetComponent"
        visibility="anonymous-only" />
    <component class="...TOTPResetComponent"
        visibility="anonymous-only" />
</layout>
```

The exact layout structure (tabs, dialogs, inline sections) is an application-level
decision. The components provide the logic; the layout configuration determines the
presentation.

**Enablement configuration:**

```java
interface SelfServiceConfig extends ConfigurationItem {

    /** Whether password reset is available from the anonymous layout. */
    @Name("password-reset-enabled")
    @BooleanDefault(false)
    boolean getPasswordResetEnabled();

    /** Whether TOTP reset is available from the anonymous layout. */
    @Name("totp-reset-enabled")
    @BooleanDefault(false)
    boolean getTotpResetEnabled();
}
```

#### 3.4.6 Application Customization

Applications may need to resolve a Person from an email address (rather than a username)
for the password-reset flow. This is delegated to a configurable strategy interface:

```java
/**
 * Strategy for resolving a {@link Person} from user input in self-service
 * flows. The default implementation resolves by username.
 */
public interface AccountResolver {

    /**
     * Resolves a Person from the user-provided identifier (username or email).
     *
     * @param identifier the value entered by the user.
     * @return the resolved person, or {@code null} if not found.
     */
    Person resolve(String identifier);

    /**
     * Returns the email address for the given person, used for OTP delivery.
     *
     * @param person the person.
     * @return the email address, or {@code null} if unavailable.
     */
    String getEmail(Person person);
}
```

The default implementation resolves by `Person.byName()`. Applications with a contact
module can provide an implementation that also searches by email or queries
`PersonContact`.

---

### 3.5 Registration Framework

#### 3.5.1 Purpose

Provide a platform-level registration framework that supports two modes:

- **Invitation-based registration:** An authenticated user invites an external party
  by email. The invited party follows a token-secured link to register an account
  through a configurable multi-step process.
- **Open self-registration:** Anyone can register an account directly from the
  anonymous layout, without an invitation. This mode is useful for development systems,
  demo instances, or applications where user acquisition should be frictionless.

Both modes share the same multi-step registration flow and the same component
architecture. The difference lies in how the flow is initiated (invitation token vs.
direct access) and what verification is required.

This is a *framework* -- the platform provides the core mechanics (token generation,
email delivery, registration flow orchestration), while applications customize the
specific registration steps and the resulting account configuration.

Because registration operates within the anonymous session's layout framework (section
3.2), all registration UI is built with standard layout components. Applications
customize the registration experience using the same tools they use for any other UI.

#### 3.5.2 Module

New module: **`com.top_logic.security.register`**
Dependencies: `tl-core`, `com.top_logic.security.otp`, `com.top_logic.security.auth.totp`
(optional, for TOTP enrollment step), `tl-mail-smtp`

#### 3.5.3 Registration Modes

The framework supports two modes, controlled by configuration:

| Mode | Entry Point | Token Required | Typical Use Case |
|------|-------------|----------------|------------------|
| **Invitation** | Email link with token | Yes | Production systems with controlled user onboarding |
| **Open** | "Register" button in anonymous layout | No | Development systems, demos, open communities |

Both modes can be enabled simultaneously (an application can allow both open
registration and invitation-based registration), or individually.

**Open registration flow:**

```
User                      System
 |                           |
 | 1. Click "Register"       |
 |    in anonymous layout    |
 |-------------------------->|
 |                           |
 |   <-- Registration        |
 |       component shown --  |
 |                           |
 | 2. Enter email address    |
 |-------------------------->|
 |                           | 3. Check: email already registered?
 |                           |    If yes -> show message (no account details leaked)
 |                           |    If no  -> OTPService.generate(email, "registration")
 |   <-- OTP email sent --   |
 |                           |
 | 4. Enter OTP code         |
 |-------------------------->|
 |                           | 5. OTPService.validate(session, code)
 |                           |    -> email ownership verified
 |                           |
 |   Proceed with remaining  |
 |   registration steps      |
 |   (password, TOTP, etc.)  |
```

**Invitation-based flow:**

```
User                      System
 |                           |
 | 1. Click invitation link  |
 |    (URL with token)       |
 |-------------------------->|
 |                           | 2. Anonymous session created (if none)
 |                           | 3. Validate token
 |                           |    (exists, not expired, not revoked)
 |                           |
 |   Registration component  |
 |   shown with pre-filled   |
 |   email from token.       |
 |                           |
 |   Proceed with configured |
 |   registration steps      |
 |   (email verify, password,|
 |   TOTP, etc.)             |
```

In the invitation flow, the invitation link creates an anonymous session (if none
exists) and opens the registration component with the invitation context. The email
address is pre-filled from the invitation token. The `EmailVerificationStep` still
sends an OTP to verify email ownership, but the user does not need to type the address.

#### 3.5.4 Invitation Token Model

For the invitation mode, the platform manages invitation tokens as persistent objects:

```
InvitationToken
+-- token: String        (cryptographically secure, URL-safe, >= 256 bit entropy)
+-- recipientEmail: String
+-- createdBy: Person    (the inviting user)
+-- createdAt: DateTime
+-- expiresAt: DateTime
+-- status: InvitationStatus (PENDING | COMPLETED | EXPIRED | REVOKED)
+-- revokedBy: Person    (optional)
+-- revokedAt: DateTime  (optional)
+-- context: String      (opaque application-specific context, e.g. JSON)
```

The `context` field allows applications to attach arbitrary data to an invitation
(e.g., which resources the invited user should have access to, which roles to assign).
The platform does not interpret this field; it passes it to the application-provided
`RegistrationHandler` (see 3.5.8).

**`InvitationStatus`:**

| Status | Meaning |
|--------|---------|
| `PENDING` | Token generated, email sent, awaiting registration |
| `COMPLETED` | Invited user has successfully registered |
| `EXPIRED` | Token validity has elapsed |
| `REVOKED` | Token manually revoked by an authorized user |

#### 3.5.5 Invitation Service

```java
package com.top_logic.security.register;

/**
 * Service for creating and managing invitation tokens.
 */
public interface InvitationService {

    /**
     * Creates an invitation and sends the invitation email.
     *
     * @param recipientEmail the email of the person being invited.
     * @param invitedBy      the person creating the invitation.
     * @param context        application-specific context (opaque string, may be null).
     * @param validityDays   number of days the invitation is valid
     *                       (capped by system maximum).
     * @return the created invitation token object.
     * @throws InvitationException if creation or email sending fails.
     */
    InvitationToken createInvitation(String recipientEmail, Person invitedBy,
                                     String context, int validityDays)
        throws InvitationException;

    /**
     * Validates an invitation token string.
     *
     * @param token the token from the invitation URL.
     * @return the validation result.
     */
    InvitationValidationResult validateToken(String token);

    /**
     * Revokes an invitation.
     *
     * @param token     the token to revoke.
     * @param revokedBy the person revoking the invitation.
     */
    void revoke(String token, Person revokedBy);

    /**
     * Marks an invitation as completed after successful registration.
     *
     * @param token  the token to complete.
     * @param person the newly created Person.
     */
    void complete(String token, Person person);
}
```

**`InvitationValidationResult`:**

```java
public class InvitationValidationResult {
    public enum Status { VALID, EXPIRED, REVOKED, UNKNOWN }

    public Status getStatus();
    public InvitationToken getToken();  // non-null only if VALID
}
```

#### 3.5.6 Registration Flow Engine

The registration flow is a multi-step process orchestrated by a
**`RegistrationComponent`** -- a layout component that manages a configurable pipeline
of registration steps. The steps are configurable via a **`RegistrationStep`** plugin
interface:

```java
package com.top_logic.security.register;

/**
 * A single step in the registration flow. Steps are executed in configured
 * order. Each step provides a form model for the component to render and
 * processes the user's input.
 */
public interface RegistrationStep extends ConfiguredInstance<RegistrationStep.Config<?>> {

    interface Config<I extends RegistrationStep> extends PolymorphicConfiguration<I> {
        /** Unique name of this step. */
        @Name("name")
        @Mandatory
        String getName();
    }

    /**
     * Whether this step applies to the current registration context.
     *
     * <p>
     * Steps can be conditionally skipped based on the registration mode
     * or other context properties. For example, a CAPTCHA step might only
     * apply in open registration mode, while an invitation-context step
     * might only apply in invitation mode.
     * </p>
     *
     * @param context the current registration context.
     * @return {@code true} if this step should be executed; {@code false} to skip.
     */
    default boolean appliesTo(RegistrationContext context) {
        return true;
    }

    /**
     * Creates the form model for this step. The {@link RegistrationComponent}
     * renders this model using the standard form rendering mechanism.
     *
     * @param context the registration context (accumulates data across steps).
     * @return the form model for this step.
     */
    FormContext createFormContext(RegistrationContext context);

    /**
     * Processes the submitted form for this step.
     *
     * @param context the registration context (accumulates data across steps).
     * @param formContext the form context with user input.
     * @return {@code null} if the step is complete and the flow should proceed;
     *         otherwise a {@link ResKey} error message to re-display the form.
     */
    ResKey process(RegistrationContext context, FormContext formContext);
}
```

**`RegistrationContext`:**

A mutable context object that is built up across steps and used by the final step to
create the account:

```java
public class RegistrationContext {
    private RegistrationMode _mode;     // INVITATION or OPEN
    private InvitationToken _invitation; // non-null only in INVITATION mode
    private String _email;              // verified email
    private String _displayName;
    private char[] _password;           // set during password step
    private Enrollment _totpEnrollment; // set during TOTP step (optional)
    private Map<String, Object> _attributes; // extensible

    public enum RegistrationMode { INVITATION, OPEN }
    ...
}
```

The `_mode` field allows registration steps to behave differently depending on how
the flow was initiated. For example, the `EmailVerificationStep` pre-fills the email
in invitation mode but asks the user to enter it in open mode.

#### 3.5.7 Standard Registration Steps

The platform provides the following standard steps. Applications configure which steps
to include and in which order.

| Step | Name | Description | Mode Behavior |
|------|------|-------------|---------------|
| `EmailInputStep` | `email-input` | User enters email address. | Skipped in invitation mode (email comes from token). |
| `EmailVerificationStep` | `email-verify` | Sends OTP to email, verifies code. Confirms email ownership. | In invitation mode, uses the token's email. In open mode, uses the email from `EmailInputStep`. |
| `ProfileStep` | `profile` | User enters display name and optional profile fields. | Same in both modes. |
| `PasswordSetupStep` | `password-setup` | User chooses a password (with confirmation). Validates against `PasswordValidator`. | Same in both modes. |
| `TOTPEnrollmentStep` | `totp-enroll` | Generates TOTP secret, displays QR code, verifies initial code. | Same in both modes. |
| `AccountCreationStep` | `account-create` | Final step: creates Person, sets password, activates TOTP, invokes application callback. | Same in both modes. |

An application can:
- Omit steps (e.g., skip `totp-enroll` for open registration on a dev system).
- Add custom steps (e.g., a CAPTCHA step for open registration, a terms-of-service
  acceptance step, or a step collecting application-specific profile data).
- Reorder steps.
- Use `appliesTo(RegistrationContext)` to conditionally include steps based on mode.

**Example configurations:**

*Production system (invitation only, with TOTP):*
```xml
<registration-steps>
    <step name="email-verify"    class="...EmailVerificationStep" />
    <step name="password-setup"  class="...PasswordSetupStep" />
    <step name="totp-enroll"     class="...TOTPEnrollmentStep" />
    <step name="account-create"  class="...AccountCreationStep" />
</registration-steps>
```

*Development system (open registration, no TOTP):*
```xml
<registration-steps>
    <step name="email-input"     class="...EmailInputStep" />
    <step name="email-verify"    class="...EmailVerificationStep" />
    <step name="password-setup"  class="...PasswordSetupStep" />
    <step name="account-create"  class="...AccountCreationStep" />
</registration-steps>
```

*Minimal development system (open registration, no email verification):*
```xml
<registration-steps>
    <step name="profile"         class="...ProfileStep" />
    <step name="password-setup"  class="...PasswordSetupStep" />
    <step name="account-create"  class="...AccountCreationStep" />
</registration-steps>
```

#### 3.5.8 Application Callback: `RegistrationHandler`

After the `AccountCreationStep` creates the Person object, the platform invokes a
configurable application callback:

```java
/**
 * Application-specific callback invoked after a new account is created
 * through the registration flow.
 *
 * <p>
 * Implementations can assign roles, create application-specific objects,
 * link the person to domain entities, etc. The callback receives the
 * {@link RegistrationContext} which includes the registration mode and,
 * in invitation mode, the invitation token with its application-specific
 * context.
 * </p>
 */
public interface RegistrationHandler
        extends ConfiguredInstance<RegistrationHandler.Config<?>> {

    interface Config<I extends RegistrationHandler>
            extends PolymorphicConfiguration<I> {
    }

    /**
     * Called after the Person has been created and persisted.
     *
     * @param person  the newly created Person.
     * @param context the registration context (contains mode, invitation, email, etc.).
     */
    void onRegistrationComplete(Person person, RegistrationContext context);
}
```

The default `RegistrationHandler` does nothing. Applications override it to perform
domain-specific setup. The handler can distinguish between invitation-based and open
registrations via `context.getMode()`:

- **Invitation mode:** The handler can read `context.getInvitation().getContext()`
  to retrieve application-specific data (e.g., which resources to grant access to).
- **Open mode:** The handler can assign default roles, send a welcome notification,
  or perform other onboarding logic.

#### 3.5.9 Registration Completes with Login

When account creation completes and the `RegistrationHandler` has run, the registration
component triggers a session upgrade: the anonymous session is invalidated, a new
authenticated session is created for the newly registered Person, and a full page
reload loads the authenticated layout. The user is immediately logged in without
needing to go through the login flow again.

#### 3.5.10 Configuration

```java
public interface Config extends ConfiguredManagedClass.Config<RegistrationServiceImpl> {

    /** Whether open self-registration (without invitation) is enabled. */
    @Name("open-registration-enabled")
    @BooleanDefault(false)
    boolean getOpenRegistrationEnabled();

    /** Whether invitation-based registration is enabled. */
    @Name("invitation-registration-enabled")
    @BooleanDefault(false)
    boolean getInvitationRegistrationEnabled();

    /** Maximum allowed invitation validity in days (caps user-specified values). */
    @Name("max-invitation-validity-days")
    @IntDefault(30)
    int getMaxInvitationValidityDays();

    /** Default invitation validity in days (when not specified by the inviter). */
    @Name("default-invitation-validity-days")
    @IntDefault(7)
    int getDefaultInvitationValidityDays();

    /** Ordered list of registration steps. */
    @Name("registration-steps")
    @Key(RegistrationStep.Config.NAME)
    List<RegistrationStep.Config<?>> getRegistrationSteps();

    /** Application callback after registration. */
    @Name("registration-handler")
    @InstanceDefault(NoOpRegistrationHandler.class)
    PolymorphicConfiguration<RegistrationHandler> getRegistrationHandler();

    /** Email subject for invitation emails. */
    @Name("invitation-email-subject")
    String getInvitationEmailSubject();

    /** Email body template for invitation emails. */
    @Name("invitation-email-body-template")
    String getInvitationEmailBodyTemplate();

    /**
     * Authentication device ID assigned to newly registered accounts.
     * Defaults to the system's default authentication device.
     */
    @Name("auth-device-id")
    @Nullable
    String getAuthDeviceId();

    /**
     * Second-factor device ID assigned to newly registered accounts.
     * If null, no second factor is configured (unless the registration
     * steps include TOTP enrollment, in which case the TOTP device ID
     * is used automatically).
     */
    @Name("second-factor-device-id")
    @Nullable
    String getSecondAuthDeviceId();
}
```

#### 3.5.11 Repeated Invitation (Known Email)

If the `recipientEmail` already corresponds to an existing Person (or an existing
registered external user), the `InvitationService.createInvitation()` method can detect
this. The behavior is configurable:

- **Default:** Proceed normally (create invitation token, but the registration flow
  recognizes the existing account and skips account creation, only invoking the
  `RegistrationHandler` callback to grant additional access).
- **Alternative:** Skip the registration flow entirely and directly invoke the
  `RegistrationHandler` (for applications where no user interaction is needed for
  repeated invitations).

#### 3.5.12 Open Registration: Abuse Prevention

Open registration introduces the risk of automated account creation (spam, bots). The
framework provides the following mitigations:

- **Email verification (required):** In open mode, the `EmailVerificationStep` is
  strongly recommended (though technically optional for minimal dev setups). It
  ensures that each registration corresponds to a real, accessible email address.
- **Rate limiting:** The `RegistrationComponent` enforces rate limits on registration
  attempts per IP address (configurable, default: 5 registrations per hour per IP).
- **CAPTCHA step (optional):** Applications can add a `CaptchaStep` to the
  registration flow. The platform does not provide a built-in CAPTCHA implementation
  (this depends on external services like reCAPTCHA or hCaptcha), but the
  `RegistrationStep` interface makes it straightforward to integrate one.
- **Email domain restrictions (optional):** Applications can configure an allowed list
  or blocked list of email domains for open registration:

```java
// Optional addition to Config:
/** Allowed email domains for open registration (empty = all allowed). */
@Name("allowed-email-domains")
@Format(CommaSeparatedStrings.class)
List<String> getAllowedEmailDomains();

/** Blocked email domains for open registration. */
@Name("blocked-email-domains")
@Format(CommaSeparatedStrings.class)
List<String> getBlockedEmailDomains();
```

---

## 4. Summary of Changes to Existing Code

| File / Component | Change |
|---|---|
| `SecurityDevice.java` | No change |
| `AuthenticationDevice.java` | No change |
| **New:** `EnrollableDevice.java` | Mix-in interface for devices requiring enrollment (e.g. TOTP) |
| **New:** `Enrollment.java` | Enrollment information interface (QR code URI, manual key, session token) |
| **New:** `ChallengeResponseDevice.java` | Mix-in interface for devices that send a server-side challenge before authentication (e.g. Email OTP) |
| `TLSecurityDeviceManager.java` | No change (new devices registered as standard `AuthenticationDevice`) |
| `Person.java` | Add `secondAuthDeviceID` attribute and accessor methods. Add well-known `anonymous` Person. |
| `Login.java` | Add `verifySecondFactor()`, `requiresSecondFactor()`, `requiresSecondFactorEnrollment()`, `requiresPrimaryChallenge()` methods |
| `TopLogicServlet.java` | Create anonymous session on demand when no session exists (instead of redirecting to `login.jsp`) |
| `SessionService.java` | Support anonymous session creation. Add `anonymous-layout` configuration. Session upgrade (invalidate anonymous + create authenticated). |
| `LoginCredentials.java` | No change |
| **New:** `LoginComponent` | Layout component replacing `LoginPageServlet` + `login.jsp`. Manages multi-step login flow (credentials, challenge, 2FA, enrollment). |
| `LoginPageServlet.java` | Retired. Functionality moved to `LoginComponent`. |
| `login.jsp` | Retired. Replaced by anonymous layout with `LoginComponent`. |
| `ApplicationPages` | `getLoginPage()` and related paths become obsolete, replaced by anonymous layout configuration. |

## 5. New Modules Summary

| Module | Artifact ID | Key Contents |
|---|---|---|
| `com.top_logic.security.auth.totp` | `tl-security-auth-totp` | `TOTPAuthenticationDevice` (implements `AuthenticationDevice` + `EnrollableDevice`), TOTP generation/validation, secret storage |
| `com.top_logic.security.otp` | `tl-security-otp` | `OTPService`, OTP generation/validation, email delivery; `EmailOTPAuthenticationDevice` (implements `AuthenticationDevice` + `ChallengeResponseDevice`) |
| `com.top_logic.security.selfservice` | `tl-security-selfservice` | `PasswordResetComponent`, `TOTPResetComponent`, `AccountResolver` |
| `com.top_logic.security.register` | `tl-security-register` | `RegistrationComponent`, `RegistrationStep` framework, `RegistrationHandler`, `InvitationService` (invitation + open self-registration) |

---

## 6. Security Considerations

| Concern | Mitigation |
|---|---|
| **Anonymous session cost** | Anonymous sessions are lightweight (minimal layout, no heavy component tree). The anonymous layout should be configured to load only essential components. Session timeouts apply normally. |
| **Anonymous session abuse** | Anonymous sessions have no write access by default. Rate limiting applies to login attempts, challenge sends, and registration. The anonymous Person's permissions are minimal. |
| **TOTP secret exposure** | Secrets encrypted at rest with configurable symmetric key. Never exposed in logs or API responses. |
| **OTP brute force** | OTPs invalidated after configurable max attempts (default: 5). Rate limiting on the component level. |
| **Invitation token guessing** | Tokens generated with >= 256 bits of cryptographic randomness (e.g., `SecureRandom`). URL-safe encoding (Base64url). |
| **User enumeration** | Password-reset flow does not reveal whether an account exists. Always shows "code sent" message. Primary ChallengeResponseDevice login also does not reveal account existence (challenge is "sent" regardless). |
| **Challenge flooding** | When Email OTP is used as primary device, an attacker could trigger emails to arbitrary users. Mitigated by rate limiting (`max-challenges-per-window`, default: 5) tied to the existing `LoginFailure` mechanism. |
| **Session fixation** | Session upgrade invalidates the anonymous HttpSession and creates a new one. No state carries over. |
| **TOTP replay** | Each TOTP code is valid for only one time step (30s). Server tracks last accepted time step per account to prevent replay within the tolerance window. |
| **Password storage** | Existing Argon2 hashing (via `PasswordHashingService`) is used. No changes needed. |
| **OTP code storage** | OTP codes are hashed before storage (using `PasswordHashingService`). Never stored in plain text. |
| **Open registration abuse** | Rate limiting per IP (default: 5/hour). Email verification required. Optional CAPTCHA step and email domain restrictions. Open registration disabled by default. |

---

## 7. Open Questions

1. **TOTP library choice:** Use an external library (`java-otp`, `totp-java`) or implement
   RFC 6238 directly? Direct implementation avoids a dependency but requires more testing.

2. **QR code rendering:** Server-side (e.g., ZXing library generating a PNG) or client-side
   (JavaScript library rendering from the `otpauth://` URI)? Server-side is simpler but
   adds a dependency; client-side requires no server dependency.

3. **Module granularity:** The four new modules could be consolidated into fewer (e.g.,
   merging `otp` + `selfservice`, or merging all into one `tl-security-mfa` module).
   More modules give finer dependency control; fewer modules reduce complexity.

4. **Person vs. separate external user entity:** The current design creates standard
   `Person` objects for self-registered users (with appropriate `authDeviceID` and
   `secondAuthDeviceID`). An alternative would be a separate `ExternalUser` entity type.
   Using `Person` is simpler and reuses existing infrastructure; a separate type provides
   clearer separation but requires duplicating authorization hooks.

5. **Anonymous session timeout:** Should anonymous sessions have a shorter idle timeout
   than authenticated sessions? A shorter timeout reduces server resource consumption
   from abandoned anonymous sessions (e.g., search engine crawlers), but too short a
   timeout could frustrate users filling out registration forms.

6. **LoginComponent rendering:** Should the `LoginComponent` render as an inline form
   within the layout, as a modal dialog overlay, or should this be configurable per
   application? The component provides the logic; the rendering mode is a presentation
   concern.
