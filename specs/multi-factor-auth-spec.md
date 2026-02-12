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
- **Invitation-based registration**, where existing users can invite external parties
  who then create their own accounts through a guided, token-secured process.
- **Open self-registration**, where users can create their own accounts directly from
  the login page -- useful for development systems, demo instances, and applications
  where user acquisition should be frictionless.

These are general-purpose platform capabilities that benefit multiple application
scenarios. For example, a production application may use invitation-based registration
with mandatory TOTP for external users, while the TopLogic development system could
enable open self-registration with minimal friction. This specification describes
additions to the existing TopLogic authentication model to support these use cases
through a unified, configurable framework.

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
| 3.5 | Registration Framework | Invitation-based and open self-registration | Yes: `com.top_logic.security.register` |

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
              com.top_logic.security.register
              [3.5: Registration framework]
```

---

### 3.1 TOTP Authentication Module

#### 3.1.1 Purpose

Provide a TOTP (Time-Based One-Time Password, RFC 6238) implementation as a standard
`AuthenticationDevice`. The TOTP device can serve as:

- A **second authentication factor** alongside a primary device (typically
  `DBAuthenticationAccessDevice`), or
- A **standalone primary device** for accounts that authenticate by TOTP code alone.

#### 3.1.2 Design Approach: TOTP as AuthenticationDevice

TOTP authentication is conceptually the same as password authentication: the user provides
a credential (a time-based code instead of a memorized password), and the device verifies
it. The existing `AuthenticationDevice` interface already accommodates devices where the
password-management methods are not applicable -- `LDAPAuthenticationAccessDevice` returns
`false` for `allowPwdChange()` and does not support `setPassword()`. A TOTP device follows
the same pattern:

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

#### 3.1.3 Enrollment: `EnrollableDevice` Mix-In Interface

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

**Placement:** `EnrollableDevice` and `Enrollment` are added to
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
  UI layer (the module provides the URI string; a JSP or component renders it as a QR
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
- *Password only:* Person has `authDeviceID = "dbSecurity"` and no
  `secondAuthDeviceID`. Existing behavior, unchanged.

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

#### 3.2.3 Login Flow Extension

After successful primary authentication, the login flow checks whether the Person has
a `secondAuthDeviceID` configured. If so, there are two sub-cases depending on whether
the device requires enrollment:

```
After primary authDevice.authentify() succeeds:

secondAuthDeviceID set?
│
├── NO
│   └── Check password expiry → session established (existing behavior)
│
└── YES
    │
    ├── device instanceof EnrollableDevice AND !isEnrolled(person)?
    │   │
    │   YES → Redirect to enrollment page
    │   │     (analogous to forced password change)
    │   │     User completes enrollment → session established
    │   │
    │   NO  → Redirect to second-factor code page
    │         User enters code → device.authentify() → session established
```

This three-way branch mirrors the existing pattern where `LoginPageServlet` redirects
to `changePwd.jsp` when `isPasswordChangeRequested()` returns `true`. The forced
enrollment redirect works the same way -- the user has authenticated their identity
via the primary device, but cannot proceed until the second device is set up.

**Full login sequence (second factor already enrolled):**

```
Browser                    LoginPageServlet / Login
  |                              |
  |  POST username + password    |
  |----------------------------->|
  |                              |  1. Resolve Person
  |                              |  2. authDevice.authentify(credentials)
  |                              |  3. secondAuthDevice set, enrolled
  |                              |  4. Store partial auth in session
  |  <-- redirect to 2FA page --|
  |                              |
  |  POST totp-code              |
  |----------------------------->|
  |                              |  5. secondAuthDevice.authentify(credentials)
  |                              |  6. SessionService.loginUser()
  |  <-- redirect to start --   |
```

**Full login sequence (enrollment required):**

```
Browser                    LoginPageServlet / Login
  |                              |
  |  POST username + password    |
  |----------------------------->|
  |                              |  1. Resolve Person
  |                              |  2. authDevice.authentify(credentials)
  |                              |  3. secondAuthDevice set, NOT enrolled
  |                              |  4. Store partial auth in session
  |  <-- redirect to            |
  |     enrollment page    --   |
  |                              |
  |  (enrollment flow:           |
  |   QR code displayed,         |
  |   user scans + enters code)  |
  |  POST totp-code              |
  |----------------------------->|
  |                              |  5. enrollableDevice.confirmEnrollment()
  |                              |  6. SessionService.loginUser()
  |  <-- redirect to start --   |
```

#### 3.2.4 TOTP Enrollment Use Cases

The `EnrollableDevice` interface and the TOTP enrollment flow serve three distinct
use cases. All three use the same `beginEnrollment()` / `confirmEnrollment()` methods
but differ in how the flow is triggered:

**Use case A: Voluntary enrollment from personal settings**

A logged-in user with only a primary auth device decides to add a second factor.

1. User navigates to personal settings (within authenticated session).
2. Clicks "Set up two-factor authentication".
3. System calls `beginEnrollment(person)` → shows QR code.
4. User scans QR code, enters confirmation code.
5. System calls `confirmEnrollment(person, enrollment, code)`.
6. On success, system sets `secondAuthDeviceID = "totp"` on the Person.
7. From the next login onward, the second factor is required.

This is analogous to the existing voluntary password change. It operates within an
authenticated session and is initiated by the user.

**Use case B: Enforced enrollment on first login**

An administrator (or a provisioning system) configures `secondAuthDeviceID = "totp"` on
a Person before the user has enrolled. On the next login:

1. User enters username + password → primary auth succeeds.
2. System detects `secondAuthDeviceID` is set but `isEnrolled()` is `false`.
3. System stores partial auth state and redirects to enrollment page.
4. User completes enrollment (QR code → confirmation code).
5. System calls `confirmEnrollment()` → session established.

This is analogous to the existing forced password change on first login. The
administrator sets the requirement; the user fulfills it during login.

**Use case C: Enrollment during invitation/registration**

A new user going through the registration flow (section 3.5) encounters the
`TOTPEnrollmentStep`:

1. Registration flow reaches the TOTP enrollment step.
2. System calls `beginEnrollment(person)` → shows QR code.
3. User scans QR code, enters confirmation code.
4. System calls `confirmEnrollment()` → step complete, flow continues.

This is the same enrollment logic, just executed within the registration step
pipeline (section 3.5.7) rather than the login flow. The `AccountCreationStep`
sets `secondAuthDeviceID` on the newly created Person.

**Summary:**

| Use case | Trigger | Context | Analog |
|---|---|---|---|
| A: Voluntary | User-initiated from settings | Authenticated session | Voluntary password change |
| B: Enforced | `secondAuthDeviceID` set, not enrolled | Login flow (after primary auth) | Forced password change (expired password) |
| C: Registration | Registration step pipeline | Unauthenticated (registration flow) | Initial password setup during registration |

All three use cases call the same `EnrollableDevice` methods. The only difference is
where and when the flow is triggered.

#### 3.2.5 Changes to `Login`

New methods are added:

```java
/**
 * Verifies the second authentication factor for a partially authenticated
 * user.
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

#### 3.2.6 Changes to `LoginPageServlet`

The servlet is extended to handle two new request parameters:

```java
public static final String SECOND_FACTOR_CODE_PARAMETER = "secondFactorCode";
public static final String ENROLLMENT_CODE_PARAMETER = "enrollmentCode";
```

The `checkRequest()` method is extended with two new branches:

1. If the request contains a `secondFactorCode` parameter (and a valid partial-auth
   session marker exists), the servlet performs second-factor verification.
2. If the request contains an `enrollmentCode` parameter (and a valid partial-auth
   session marker with pending enrollment exists), the servlet performs enrollment
   confirmation via `EnrollableDevice.confirmEnrollment()`.

The decision between showing the second-factor page vs. the enrollment page is made
after primary authentication, based on `requiresSecondFactorEnrollment()`.

#### 3.2.7 JSP Pages

Two new JSP pages are provided:

- **`secondFactor.jsp`** -- Displays a single input field for the TOTP code.
  Used when the second device is already enrolled.
- **`secondFactorEnroll.jsp`** -- Displays a QR code (from `Enrollment.getProvisioningUri()`)
  and a manual-entry key (from `Enrollment.getManualEntryKey()`), plus an input field
  for the confirmation code. Used when enrollment is required.

Both pages carry the original `startPage` and other parameters as hidden fields and
submit to the same `LoginPageServlet`.

The page paths are registered in `ApplicationPages`:

```java
// New methods in ApplicationPages:
String getSecondFactorPage();        // Default: "/jsp/auth/secondFactor.jsp"
String getSecondFactorEnrollPage();  // Default: "/jsp/auth/secondFactorEnroll.jsp"
```

#### 3.2.8 Configuration

New options in `Login.Config`:

```java
/**
 * Maximum time in seconds between successful primary authentication and
 * second-factor submission or enrollment completion. If exceeded, the
 * user must re-authenticate from the beginning.
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
 |                           | 7. secondAuthDevice.authentify(credentials)
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
 |                           | 7. enrollableDevice.unenroll(person)
 |                           | 8. enrollableDevice.beginEnrollment(person)
 |                           |
 |   <-- QR code page --     |
 |                           |
 | 9. Scan QR code,          |
 |    enter TOTP code        |
 |-------------------------->|
 |                           | 10. enrollableDevice.confirmEnrollment()
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

### 3.5 Registration Framework

#### 3.5.1 Purpose

Provide a platform-level registration framework that supports two modes:

- **Invitation-based registration:** An authenticated user invites an external party
  by email. The invited party follows a token-secured link to register an account
  through a configurable multi-step process.
- **Open self-registration:** Anyone can register an account directly from the login
  page, without an invitation. This mode is useful for development systems, demo
  instances, or applications where user acquisition should be frictionless.

Both modes share the same multi-step registration flow engine and the same
`RegistrationStep` plugin architecture. The difference lies in how the flow is
initiated (invitation token vs. direct access) and what verification is required.

This is a *framework* -- the platform provides the core mechanics (token generation,
email delivery, registration flow orchestration), while applications customize the
specific registration steps and the resulting account configuration.

#### 3.5.2 Module

New module: **`com.top_logic.security.register`**
Dependencies: `tl-core`, `com.top_logic.security.otp`, `com.top_logic.security.auth.totp`
(optional, for TOTP enrollment step), `tl-mail-smtp`

#### 3.5.3 Registration Modes

The framework supports two modes, controlled by configuration:

| Mode | Entry Point | Token Required | Typical Use Case |
|------|-------------|----------------|------------------|
| **Invitation** | Email link with token | Yes | Production systems with controlled user onboarding |
| **Open** | "Register" link on login page | No | Development systems, demos, open communities |

Both modes can be enabled simultaneously (an application can allow both open
registration and invitation-based registration), or individually.

**Open registration flow:**

```
User                      System
 |                           |
 | 1. Click "Register"       |
 |    on login page          |
 |-------------------------->|
 |                           |
 |   <-- Registration page   |
 |       (email input)  --   |
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
 |    (/servlet/register     |
 |     ?token=<token>)       |
 |-------------------------->|
 |                           | 2. Validate token
 |                           |    (exists, not expired, not revoked)
 |                           |
 |   Proceed with configured |
 |   registration steps      |
 |   (email verify, password,|
 |   TOTP, etc.)             |
```

In the invitation flow the email address is pre-filled from the invitation token. The
`EmailVerificationStep` still sends an OTP to verify email ownership, but the user does
not need to type the address.

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

The registration flow is a multi-step process orchestrated by a `RegistrationServlet`.
The steps are configurable via a **`RegistrationStep`** plugin interface:

```java
package com.top_logic.security.register;

/**
 * A single step in the registration flow. Steps are executed in configured
 * order. Each step renders a page, processes the form submission, and signals
 * whether to proceed to the next step or re-display with errors.
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

#### 3.5.9 Configuration

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

#### 3.5.10 Registration Servlet

```java
package com.top_logic.security.register;

/**
 * Servlet handling self-registration flows. Operates outside the
 * authenticated session context. Supports two entry points:
 *
 * <ul>
 *   <li><b>Invitation mode:</b>
 *       {@code /servlet/register?token=<invitation-token>}</li>
 *   <li><b>Open mode:</b>
 *       {@code /servlet/register} (no token parameter)</li>
 * </ul>
 *
 * <p>
 * The servlet determines the mode from the presence or absence of the
 * {@code token} parameter, validates the token (in invitation mode),
 * creates a {@link RegistrationContext}, and orchestrates the configured
 * {@link RegistrationStep}s sequentially.
 * </p>
 */
public class RegistrationServlet extends NoContextServlet {
    ...
}
```

**Login page integration:** When open registration is enabled, the login page shows a
"Register" link pointing to the registration servlet (without a token). This link is
rendered conditionally based on `Config.getOpenRegistrationEnabled()`.

```java
// Addition to SelfServiceConfig (from section 3.4):
/** Whether the "Register" link is shown on the login page. */
@Name("open-registration-enabled")
@BooleanDefault(false)
boolean getOpenRegistrationEnabled();
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
- **Rate limiting:** The `RegistrationServlet` enforces rate limits on registration
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
| `TLSecurityDeviceManager.java` | No change (TOTP device registered as standard `AuthenticationDevice`) |
| `Person.java` | Add `secondAuthDeviceID` attribute and accessor methods |
| `Login.java` | Add `verifySecondFactor()`, `requiresSecondFactor()` methods |
| `Login.Config` | Add `second-factor-timeout` option, `self-service` sub-config |
| `LoginPageServlet.java` | Extend `checkRequest()` for second-factor flow branch |
| `LoginCredentials.java` | No change |
| `ApplicationPages` | Add `getSecondFactorPage()`, `getPasswordResetPage()`, `getTotpResetPage()` |
| `login.jsp` | Add optional links for password reset, TOTP reset, and open registration |
| **New:** `secondFactor.jsp` | Second-factor code entry page |

## 5. New Modules Summary

| Module | Artifact ID | Key Contents |
|---|---|---|
| `com.top_logic.security.auth.totp` | `tl-security-auth-totp` | `TOTPAuthenticationDevice` (implements `AuthenticationDevice` + `EnrollableDevice`), TOTP generation/validation, secret storage |
| `com.top_logic.security.otp` | `tl-security-otp` | `OTPService`, OTP generation/validation, email delivery |
| `com.top_logic.security.selfservice` | `tl-security-selfservice` | `SelfServiceServlet`, password-reset flow, TOTP-reset flow, `AccountResolver` |
| `com.top_logic.security.register` | `tl-security-register` | `RegistrationServlet`, `RegistrationStep` framework, `RegistrationHandler`, `InvitationService` (invitation + open self-registration) |

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

4. **JSP vs. layout components:** The self-service and registration pages operate outside
   the authenticated TopLogic layout framework. JSP pages (like the existing `login.jsp`)
   are the natural choice. Should there be a lightweight template mechanism for
   application-specific styling, or is JSP customization sufficient?

5. **Person vs. separate external user entity:** The current design creates standard
   `Person` objects for self-registered users (with appropriate `authDeviceID` and
   `secondAuthDeviceID`). An alternative would be a separate `ExternalUser` entity type.
   Using `Person` is simpler and reuses existing infrastructure; a separate type provides
   clearer separation but requires duplicating authorization hooks.
