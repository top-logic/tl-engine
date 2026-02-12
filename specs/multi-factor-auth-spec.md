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
- **Self-service account management** (password reset, TOTP reset) without administrator
  intervention.
- **Invitation-based self-registration**, where existing users can invite external parties
  who then create their own accounts through a guided, token-secured process.

These are general-purpose platform capabilities. This specification describes additions to the
existing TopLogic authentication model to support them.

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

Five groups of changes are proposed:

| # | Feature | Scope | New Module? |
|---|---------|-------|-------------|
| 3.1 | TOTP Authentication | New security device type | Yes: `com.top_logic.security.auth.totp` |
| 3.2 | Multi-Factor Login Flow | Core login extension | No (changes to `com.top_logic` / tl-core) |
| 3.3 | OTP Email Verification Service | New platform service | Yes: `com.top_logic.security.otp` (or part of tl-core) |
| 3.4 | Self-Service Account Management | Password/TOTP reset flows | Yes: `com.top_logic.security.selfservice` |
| 3.5 | Invitation & Self-Registration | Token-based onboarding | Yes: `com.top_logic.security.invite` |

The dependency graph is:

```
                    com.top_logic (tl-core)
                    [3.2: MFA login flow]
                          |
              +-----------+-----------+
              |                       |
  com.top_logic.security.auth.totp   com.top_logic.security.otp
  [3.1: TOTP device]                 [3.3: OTP email service]
              |                       |
              +-----------+-----------+
                          |
              com.top_logic.security.selfservice
              [3.4: Password/TOTP reset]
                          |
              com.top_logic.security.invite
              [3.5: Invitation & registration]
```

---

### 3.1 TOTP Authentication Module

#### 3.1.1 Purpose

Provide a TOTP (Time-Based One-Time Password, RFC 6238) implementation as a new type of
security device. This device is designed to serve as a **second factor** alongside a primary
authentication device (typically `DBAuthenticationAccessDevice`).

#### 3.1.2 New Interface: `SecondFactorDevice`

The existing `AuthenticationDevice` interface is oriented toward password-based primary
authentication (methods like `allowPwdChange()`, `setPassword()`, `getPasswordValidator()`
are specific to passwords). A second factor has fundamentally different semantics.

A new interface is introduced:

```java
package com.top_logic.base.security.device.interfaces;

/**
 * A security device that provides a second authentication factor.
 *
 * <p>
 * Unlike {@link AuthenticationDevice}, which handles primary authentication (typically
 * password-based), a {@code SecondFactorDevice} provides an additional verification
 * step. It operates on {@link Person} accounts that are already associated with a
 * primary {@link AuthenticationDevice}.
 * </p>
 */
public interface SecondFactorDevice extends SecurityDevice {

    /**
     * Whether the given account has a second factor enrolled.
     *
     * @param account the person to check.
     * @return {@code true} if a second factor is configured for this account.
     */
    boolean isEnrolled(Person account);

    /**
     * Verifies a second-factor code for the given account.
     *
     * @param account the person to verify.
     * @param code    the code entered by the user (e.g. a 6-digit TOTP code).
     * @return {@code true} if the code is valid.
     */
    boolean verify(Person account, String code);

    /**
     * Initiates enrollment for the given account. Returns an {@link Enrollment}
     * object containing the information needed for the user to set up their
     * second factor (e.g. a QR code URI for TOTP).
     *
     * @param account the person to enroll.
     * @return enrollment information; never {@code null}.
     */
    Enrollment beginEnrollment(Person account);

    /**
     * Confirms enrollment by verifying a code produced with the newly enrolled
     * factor. After successful confirmation, the factor is active for the account.
     *
     * @param account    the person being enrolled.
     * @param enrollment the enrollment previously returned by {@link #beginEnrollment}.
     * @param code       the verification code from the user.
     * @return {@code true} if enrollment is confirmed and the factor is now active.
     */
    boolean confirmEnrollment(Person account, Enrollment enrollment, String code);

    /**
     * Removes the second factor for the given account.
     *
     * @param account the person whose second factor is to be removed.
     */
    void unenroll(Person account);
}
```

The `Enrollment` object:

```java
package com.top_logic.base.security.device.interfaces;

/**
 * Information needed by the user to complete second-factor enrollment.
 */
public interface Enrollment {

    /**
     * A URI suitable for encoding as a QR code (e.g. an {@code otpauth://} URI
     * for TOTP). May be {@code null} if the factor type does not use QR codes.
     */
    String getProvisioningUri();

    /**
     * A human-readable secret for manual entry (e.g. a Base32-encoded TOTP
     * secret). May be {@code null} if manual entry is not supported.
     */
    String getManualEntryKey();

    /**
     * An opaque token identifying this enrollment session. Used internally to
     * correlate {@link SecondFactorDevice#confirmEnrollment} with the
     * corresponding {@link SecondFactorDevice#beginEnrollment} call.
     */
    String getEnrollmentToken();
}
```

**Placement:** Both interfaces are added to the existing package
`com.top_logic.base.security.device.interfaces` in **tl-core**, alongside
`SecurityDevice` and `AuthenticationDevice`. This allows the core login flow (section 3.2)
to reference `SecondFactorDevice` without depending on the TOTP module.

#### 3.1.3 TOTP Device Implementation

A new module **`com.top_logic.security.auth.totp`** provides:

**`TOTPSecondFactorDevice`** implements `SecondFactorDevice`:

- **TOTP generation and validation** per RFC 6238 / RFC 4226.
  - Algorithm: HMAC-SHA1 (default, configurable to SHA-256 or SHA-512).
  - Time step: 30 seconds (default, configurable).
  - Code length: 6 digits (default, configurable).
  - Clock skew tolerance: +/- 1 time step (configurable).
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
  UI layer (the module provides the URI string; a JSP or component renders it as a QR
  code image using a client-side library or a server-side renderer).
- **Secret encryption at rest:** TOTP secrets are encrypted before storage using a
  configurable symmetric key (application secret). The key is configured via the
  standard TopLogic alias mechanism (e.g., `%TOTP_ENCRYPTION_KEY%`).

**Configuration:**

```java
public interface Config extends SecondFactorDevice.Config<TOTPSecondFactorDevice> {

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

The `TLSecurityDeviceManager` configuration is extended to accept `SecondFactorDevice`
instances alongside existing `AuthenticationDevice` and `PersonDataAccessDevice`
instances. Since all three extend `SecurityDevice`, the existing `security-devices` map
already supports this. A new accessor is added:

```java
// In TLSecurityDeviceManager:
public SecondFactorDevice getSecondFactorDevice(String deviceID) { ... }
```

**Example configuration:**

```xml
<security-devices>
    <!-- Existing primary auth device -->
    <security-device id="dbSecurity"
        class="com.top_logic.base.security.device.db.DBAuthenticationAccessDevice">
        ...
    </security-device>

    <!-- New TOTP second factor device -->
    <security-device id="totp"
        class="com.top_logic.security.auth.totp.TOTPSecondFactorDevice"
        issuer="MyApplication"
        encryption-key="%TOTP_ENCRYPTION_KEY%"
    />
</security-devices>
```

#### 3.1.4 External Dependencies

The module requires a TOTP library. Candidates:

- `com.eatthepath:java-otp` (lightweight, well-maintained)
- `dev.samstevens:totp` (higher-level API, includes QR code helpers)

Alternatively, the TOTP algorithm (RFC 6238) is simple enough to implement directly
using `javax.crypto.Mac`, avoiding an external dependency. This is a design-time decision.

---

### 3.2 Multi-Factor Login Flow

#### 3.2.1 Purpose

Extend the core login flow so that a Person may require a second authentication factor in
addition to the primary `AuthenticationDevice`. The current flow is single-step; the new
flow supports an optional second step.

#### 3.2.2 Person Model Extension

A new attribute is added to `Person`:

```java
// In Person.java:
public static final String SECOND_FACTOR_DEVICE_ID = "secondFactorDeviceID";

/**
 * The ID of the {@link SecondFactorDevice} for this account, or {@code null}
 * if no second factor is required.
 */
public String getSecondFactorDeviceID() {
    return tGetDataString(SECOND_FACTOR_DEVICE_ID);
}

/**
 * Resolves the {@link SecondFactorDevice} for this account.
 *
 * @return the second factor device, or {@code null} if none is configured.
 */
public SecondFactorDevice getSecondFactorDevice() {
    String deviceId = getSecondFactorDeviceID();
    if (StringServices.isEmpty(deviceId)) {
        return null;
    }
    return TLSecurityDeviceManager.getInstance().getSecondFactorDevice(deviceId);
}
```

This follows the same pattern as the existing `authDeviceID` /
`getAuthenticationDevice()` pair.

#### 3.2.3 Login Flow Extension

The modified login sequence in `LoginPageServlet` / `Login` proceeds as follows:

```
Browser                    LoginPageServlet / Login
  |                              |
  |  POST username + password    |
  |----------------------------->|
  |                              |  1. Resolve Person
  |                              |  2. authDevice.authentify(credentials)
  |                              |
  |                              |  3. Check: secondFactorDeviceID set?
  |                              |     AND device.isEnrolled(person)?
  |                              |
  |                              |  --- If NO second factor required ---
  |                              |  4a. SessionService.loginUser()
  |                              |      -> redirect to start page
  |                              |
  |                              |  --- If second factor required ---
  |                              |  4b. Store partial auth state in
  |                              |      HTTP session (not a full login)
  |  <-- redirect to 2FA page --|
  |                              |
  |  POST totp-code              |
  |----------------------------->|
  |                              |  5. Retrieve partial auth state
  |                              |  6. secondFactorDevice.verify(person, code)
  |                              |
  |                              |  --- If valid ---
  |                              |  7. SessionService.loginUser()
  |                              |     -> redirect to start page
  |                              |
  |                              |  --- If invalid ---
  |                              |  8. Increment failure count
  |  <-- redirect to 2FA page --|     (with rate limiting)
  |     (with error message)     |
```

**Key design decisions:**

- **Partial authentication state:** After successful primary authentication but before
  second-factor verification, a temporary marker is stored in the HTTP session. This is
  *not* a full TopLogic session (no `TLSessionContext` is installed). The marker contains:
  - The Person reference (or person name)
  - A timestamp (to enforce a timeout for the second-factor step)
  - The original request parameters (start page, etc.)

  The marker is implemented as a dedicated session attribute (e.g.,
  `"tl.partialAuth"`). It has a configurable timeout (default: 5 minutes). If the
  timeout expires, the user must re-authenticate from the beginning.

- **Rate limiting:** The existing `LoginFailure` mechanism in `LoginPageServlet` is
  reused. Failed second-factor attempts count toward the same failure tracking as
  password failures.

- **No changes for SSO users:** The `ExternalAuthenticationServlet` path
  (`loginFromExternalAuth`) is not affected. Persons authenticating via OpenID Connect
  or other external mechanisms bypass the second-factor check, since their identity
  provider is assumed to handle MFA independently.

- **LoginHook compatibility:** The existing `LoginHook` is invoked *after* successful
  completion of all authentication factors (i.e., after step 6/7 above), preserving
  existing behavior.

#### 3.2.4 Changes to `Login`

A new method is added:

```java
/**
 * Verifies the second factor for a partially authenticated user.
 *
 * @param person the person (already authenticated by primary factor).
 * @param code   the second-factor code entered by the user.
 * @return {@code true} if the code is valid.
 * @throws LoginDeniedException if the person has no second factor enrolled.
 */
public boolean verifySecondFactor(Person person, String code) {
    SecondFactorDevice device = person.getSecondFactorDevice();
    if (device == null || !device.isEnrolled(person)) {
        throw new LoginDeniedException("No second factor enrolled.");
    }
    return device.verify(person, code);
}

/**
 * Whether the given person requires a second authentication factor.
 */
public boolean requiresSecondFactor(Person person) {
    SecondFactorDevice device = person.getSecondFactorDevice();
    return device != null && device.isEnrolled(person);
}
```

#### 3.2.5 Changes to `LoginPageServlet`

The servlet is extended to handle a new request parameter:

```java
public static final String SECOND_FACTOR_CODE_PARAMETER = "secondFactorCode";
```

The `checkRequest()` method is extended with a branch: if the request contains a
`secondFactorCode` parameter (and a valid partial-auth session marker exists), the
servlet performs second-factor verification instead of primary authentication.

#### 3.2.6 Second-Factor JSP Page

A new JSP page (e.g., `secondFactor.jsp`) is provided:

- Displays a single input field for the TOTP code.
- Carries the original `startPage` and other parameters as hidden fields.
- Submits to the same `LoginPageServlet`.

The page path is registered in `ApplicationPages`:

```java
// New method in ApplicationPages:
String getSecondFactorPage();  // Default: "/jsp/auth/secondFactor.jsp"
```

#### 3.2.7 Configuration

The MFA timeout is added to `Login.Config`:

```java
/**
 * Maximum time in seconds between successful primary authentication and
 * second-factor submission. If exceeded, the user must re-authenticate.
 */
@Name("second-factor-timeout")
@IntDefault(300)
int getSecondFactorTimeout();
```

---

### 3.3 OTP Email Verification Service

#### 3.3.1 Purpose

Provide a general-purpose service for generating, storing, and validating one-time
passwords (numeric codes) delivered via email. This is a building block used by
self-service flows (section 3.4) and invitation flows (section 3.5).

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
 |                           | 7. SecondFactorDevice.verify(person, code)
 |                           |
 | 8. Enter new password     |
 |    (+ confirmation)       |
 |-------------------------->|
 |                           | 9. PasswordValidator.validatePassword()
 |                           | 10. AuthenticationDevice.setPassword()
 |                           |
 |   <-- Success page --     |
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
 |                           | 7. SecondFactorDevice.unenroll(person)
 |                           | 8. SecondFactorDevice.beginEnrollment(person)
 |                           |
 |   <-- QR code page --     |
 |                           |
 | 9. Scan QR code,          |
 |    enter TOTP code        |
 |-------------------------->|
 |                           | 10. SecondFactorDevice.confirmEnrollment()
 |                           |
 |   <-- Success page --     |
```

**Design notes:**
- Step 1 requires the password (knowledge factor). This ensures that a TOTP reset
  requires both password knowledge and email access. An attacker needs to compromise
  both to reset the TOTP.

#### 3.4.5 Implementation: Servlet-Based

The self-service flows are implemented as a dedicated servlet (or set of servlets) with
corresponding JSP pages, following the same pattern as `LoginPageServlet`:

```java
package com.top_logic.security.selfservice;

/**
 * Servlet handling self-service account management flows (password reset,
 * TOTP reset). Operates outside the authenticated session context.
 */
public class SelfServiceServlet extends NoContextServlet {

    /** Request parameter identifying the flow. */
    public static final String PARAM_FLOW = "flow";

    /** Request parameter for the current step within a flow. */
    public static final String PARAM_STEP = "step";

    // Flow identifiers
    public static final String FLOW_PASSWORD_RESET = "password-reset";
    public static final String FLOW_TOTP_RESET = "totp-reset";

    ...
}
```

Each flow is a state machine. The current state is tracked in the HTTP session (unauthenticated
session, similar to the partial-auth state in section 3.2). State transitions are driven by
form submissions.

**Configuration in `ApplicationPages`:**

```java
/** Page for initiating a password reset. */
String getPasswordResetPage();  // Default: "/jsp/auth/passwordReset.jsp"

/** Page for initiating a TOTP reset. */
String getTotpResetPage();      // Default: "/jsp/auth/totpReset.jsp"
```

**Links on the login page:** The existing `login.jsp` is extended with optional links to
the password-reset and TOTP-reset pages. These links are configurable (can be
enabled/disabled per application):

```java
// In a new SelfServiceConfig, referenced from Login.Config:
interface SelfServiceConfig extends ConfigurationItem {

    /** Whether the password reset link is shown on the login page. */
    @Name("password-reset-enabled")
    @BooleanDefault(false)
    boolean getPasswordResetEnabled();

    /** Whether the TOTP reset link is shown on the login page. */
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

### 3.5 Invitation and Self-Registration Framework

#### 3.5.1 Purpose

Provide a platform-level framework that allows existing (authenticated) users to invite
external parties by email. The invited party follows a token-secured link to register an
account through a configurable multi-step process.

This is a *framework* -- the platform provides the core mechanics (token generation,
email delivery, registration flow orchestration), while applications customize the
specific registration steps and the resulting account configuration.

#### 3.5.2 Module

New module: **`com.top_logic.security.invite`**
Dependencies: `tl-core`, `com.top_logic.security.otp`, `com.top_logic.security.auth.totp`
(optional, for TOTP enrollment step), `tl-mail-smtp`

#### 3.5.3 Invitation Token Model

The platform manages invitation tokens as persistent objects:

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
`RegistrationHandler` (see 3.5.5).

**`InvitationStatus`:**

| Status | Meaning |
|--------|---------|
| `PENDING` | Token generated, email sent, awaiting registration |
| `COMPLETED` | Invited user has successfully registered |
| `EXPIRED` | Token validity has elapsed |
| `REVOKED` | Token manually revoked by an authorized user |

#### 3.5.4 Invitation Service

```java
package com.top_logic.security.invite;

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

#### 3.5.5 Registration Flow

The registration flow is a multi-step process orchestrated by a `RegistrationServlet`.
The steps are configurable via a **`RegistrationStep`** plugin interface:

```java
package com.top_logic.security.invite;

/**
 * A single step in the self-registration flow. Steps are executed in
 * configured order. Each step renders a page, processes the form submission,
 * and signals whether to proceed to the next step or re-display with errors.
 */
public interface RegistrationStep extends ConfiguredInstance<RegistrationStep.Config<?>> {

    interface Config<I extends RegistrationStep> extends PolymorphicConfiguration<I> {
        /** Unique name of this step. */
        @Name("name")
        @Mandatory
        String getName();
    }

    /**
     * The JSP page to render for this step.
     */
    String getPagePath();

    /**
     * Processes the form submission for this step.
     *
     * @param context the registration context (accumulates data across steps).
     * @param request the HTTP request.
     * @return {@code null} if the step is complete and the flow should proceed;
     *         otherwise a {@link ResKey} error message to re-display the page.
     */
    ResKey process(RegistrationContext context, HttpServletRequest request);
}
```

**`RegistrationContext`:**

A mutable context object that is built up across steps and used by the final step to
create the account:

```java
public class RegistrationContext {
    private InvitationToken _invitation;
    private String _email;              // verified email
    private String _displayName;
    private char[] _password;           // set during password step
    private Enrollment _totpEnrollment; // set during TOTP step (optional)
    private Map<String, Object> _attributes; // extensible
    ...
}
```

#### 3.5.6 Standard Registration Steps

The platform provides the following standard steps. Applications configure which steps
to include and in which order.

| Step | Name | Description |
|------|------|-------------|
| `EmailVerificationStep` | `email-verify` | Sends OTP to the invitation's email, verifies code. Confirms email ownership. |
| `PasswordSetupStep` | `password-setup` | User chooses a password (with confirmation). Validates against `PasswordValidator`. |
| `TOTPEnrollmentStep` | `totp-enroll` | Generates TOTP secret, displays QR code, verifies initial code. |
| `AccountCreationStep` | `account-create` | Final step: creates Person, sets password, activates TOTP, invokes application callback. |

An application can:
- Omit steps (e.g., skip `totp-enroll` if TOTP is not required).
- Add custom steps (e.g., a step collecting additional profile information, or a
  terms-of-service acceptance step).
- Reorder steps.

#### 3.5.7 Application Callback: `RegistrationHandler`

After the `AccountCreationStep` creates the Person object, the platform invokes a
configurable application callback:

```java
/**
 * Application-specific callback invoked after a new account is created
 * through the invitation flow.
 *
 * <p>
 * Implementations can assign roles, create application-specific objects,
 * link the person to domain entities, etc.
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
     * @param context the registration context (contains invitation, email, etc.).
     */
    void onRegistrationComplete(Person person, RegistrationContext context);
}
```

The default `RegistrationHandler` does nothing. Applications override it to perform
domain-specific setup (e.g., linking the person to a `ContentShare` or assigning
roles).

#### 3.5.8 Invitation Email

The invitation email is rendered from a configurable template. The platform provides
a default template; applications can override it.

**Configuration:**

```java
public interface Config extends ConfiguredManagedClass.Config<InvitationServiceImpl> {

    /** Maximum allowed validity in days (caps user-specified values). */
    @Name("max-validity-days")
    @IntDefault(30)
    int getMaxValidityDays();

    /** Default validity in days (when not specified by the inviter). */
    @Name("default-validity-days")
    @IntDefault(7)
    int getDefaultValidityDays();

    /** URL path for the registration servlet. */
    @Name("registration-path")
    @StringDefault("/servlet/register")
    String getRegistrationPath();

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
}
```

#### 3.5.9 Registration Servlet

```java
package com.top_logic.security.invite;

/**
 * Servlet handling the self-registration flow for invited users.
 * Operates outside the authenticated session context.
 *
 * <p>
 * URL pattern: {@code /servlet/register?token=<invitation-token>}
 * </p>
 *
 * <p>
 * The servlet validates the token, then orchestrates the configured
 * registration steps sequentially.
 * </p>
 */
public class RegistrationServlet extends NoContextServlet {
    ...
}
```

#### 3.5.10 Repeated Invitation (Known Email)

If the `recipientEmail` already corresponds to an existing Person (or an existing
registered external user), the `InvitationService.createInvitation()` method can detect
this. The behavior is configurable:

- **Default:** Proceed normally (create invitation token, but the registration flow
  recognizes the existing account and skips account creation, only invoking the
  `RegistrationHandler` callback to grant additional access).
- **Alternative:** Skip the registration flow entirely and directly invoke the
  `RegistrationHandler` (for applications where no user interaction is needed for
  repeated invitations).

---

## 4. Summary of Changes to Existing Code

| File / Component | Change |
|---|---|
| `SecurityDevice.java` | No change (already generic enough) |
| `AuthenticationDevice.java` | No change |
| **New:** `SecondFactorDevice.java` | New interface in same package |
| **New:** `Enrollment.java` | New interface in same package |
| `TLSecurityDeviceManager.java` | Add `getSecondFactorDevice(String)` method |
| `Person.java` | Add `secondFactorDeviceID` attribute and accessor methods |
| `Login.java` | Add `verifySecondFactor()`, `requiresSecondFactor()` methods |
| `Login.Config` | Add `second-factor-timeout` option, `self-service` sub-config |
| `LoginPageServlet.java` | Extend `checkRequest()` for second-factor flow branch |
| `LoginCredentials.java` | No change |
| `ApplicationPages` | Add `getSecondFactorPage()`, `getPasswordResetPage()`, `getTotpResetPage()` |
| `login.jsp` | Add optional links for password reset and TOTP reset |
| **New:** `secondFactor.jsp` | Second-factor code entry page |

## 5. New Modules Summary

| Module | Artifact ID | Key Contents |
|---|---|---|
| `com.top_logic.security.auth.totp` | `tl-security-auth-totp` | `TOTPSecondFactorDevice`, TOTP generation/validation, secret storage |
| `com.top_logic.security.otp` | `tl-security-otp` | `OTPService`, OTP generation/validation, email delivery |
| `com.top_logic.security.selfservice` | `tl-security-selfservice` | `SelfServiceServlet`, password-reset flow, TOTP-reset flow, `AccountResolver` |
| `com.top_logic.security.invite` | `tl-security-invite` | `InvitationService`, `RegistrationServlet`, `RegistrationStep` framework, `RegistrationHandler` callback |

---

## 6. Security Considerations

| Concern | Mitigation |
|---|---|
| **TOTP secret exposure** | Secrets encrypted at rest with configurable symmetric key. Never exposed in logs or API responses. |
| **OTP brute force** | OTPs invalidated after configurable max attempts (default: 5). Rate limiting on the servlet level. |
| **Invitation token guessing** | Tokens generated with >= 256 bits of cryptographic randomness (e.g., `SecureRandom`). URL-safe encoding (Base64url). |
| **User enumeration** | Password-reset flow does not reveal whether an account exists. Always shows "code sent" message. |
| **Partial auth session hijacking** | Partial-auth markers have short timeout (default: 5 minutes). Bound to the HTTP session (not transferable). Cleared on use. |
| **TOTP replay** | Each TOTP code is valid for only one time step (30s). Server tracks last accepted time step per account to prevent replay within the tolerance window. |
| **Password storage** | Existing Argon2 hashing (via `PasswordHashingService`) is used. No changes needed. |
| **OTP code storage** | OTP codes are hashed before storage (using `PasswordHashingService`). Never stored in plain text. |

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

4. **JSP vs. layout components:** The self-service and registration pages operate outside
   the authenticated TopLogic layout framework. JSP pages (like the existing `login.jsp`)
   are the natural choice. Should there be a lightweight template mechanism for
   application-specific styling, or is JSP customization sufficient?

5. **Person vs. separate external user entity:** The current design creates standard
   `Person` objects for self-registered users (with appropriate `authDeviceID` and
   `secondFactorDeviceID`). An alternative would be a separate `ExternalUser` entity type.
   Using `Person` is simpler and reuses existing infrastructure; a separate type provides
   clearer separation but requires duplicating authorization hooks.
