# Design: Account name input normalization & self-provisioning (Ticket #29423, part 2)

- **Ticket:** #29423 — "Umgang mit Nutzernamen"
- **Date:** 2026-07-28
- **Status:** Approved design; implementation plan at
  `docs/superpowers/plans/2026-07-28-username-input-normalization.md`
- **Branch:** `CWS/CWS_29423_usernames` — builds on the already-implemented case-insensitivity part
  (CI `Person.byName`/`ItemByNameCache`, CI-duplicate rejection on create, LDAP DN case-insensitive).
- **Scope of this spec:** the remaining ticket points — (1) allowed characters, (2) trimming the
  input at create / login / password reset, (4) self-provisioning username from e-mail.

## Context

- `com.top_logic.knowledge.wrap.person.Person` — login name is the `name` attribute.
  `Person.create(KnowledgeBase, String, AuthenticationDevice)` is the write chokepoint (already
  rejects case-insensitive duplicates). `Person.byName(...)` is the lookup chokepoint (already
  case-insensitive via a lower-casing `ItemByNameCache`), used by login (`LoginAction` →
  `Login.checkUserPassword`), both password-reset dialogs (`RequestPasswordResetAction`,
  `ForgotPasswordDialog`) and external auth.
- `PersonManager` already defines — but nowhere calls — validation:
  `Config.getUserNamePattern()` (default `[a-zA-Z]\w*`), `getPersonNameMaxLength()` (default 128),
  and `validatePersonName(String)` (returns `true` iff non-empty, `length() < max`, matches the
  pattern).
- Only the admin action `CreateAccountAction` trims today; login, password reset and the
  self-service invitation do not. The self-service `CreateAccountDialog` stores the invitation
  **e-mail verbatim** as the login name.

## Design

### 1. Trimming — at the two chokepoints
Add `Person.normalizeName(String)` that strips leading/trailing whitespace (`String.strip()`,
null/empty passed through; case is preserved).
- `Person.create(...)` trims the name first (covers admin create, self-service invitation, and the
  root/anonymous bootstrap).
- `Person.byName(...)` trims its argument before the (case-insensitive) lookup — which transparently
  trims the login mask, both password-reset dialogs, and external auth, with no per-UI change.

### 2. Character & length validation — at create only
In `Person.create(...)`, after trimming and before the existing case-insensitive duplicate check,
validate via `PersonManager.getManager().validatePersonName(name)`; on failure throw a
`TopLogicException` with a new `ResKey` `ERROR_INVALID_ACCOUNT_NAME__NAME`.
- Change `PersonManager.Config.DEFAULT_PATTERN` to **`[\w.@+~-]+`** — letters, digits and `_`, plus
  `. @ + ~ -`; no whitespace, no control characters, non-empty. This admits e-mail addresses as
  usernames (needed for self-provisioning) and the bootstrap names `root` / `anonymous`. The
  pattern stays configurable via `person-name-pattern`; max length stays 128
  (`person-name-max-length`).
- Validation runs **only on create**. Login and password reset only trim (via `byName`), so
  existing accounts whose (legacy) names violate the rules can still authenticate.

### 3. Self-provisioning (point 4)
No new derivation code. `CreateAccountDialog` keeps using the invitation e-mail verbatim as the
username; because it creates through `Person.create`, the name is now trimmed, validated (e-mail
characters are allowed by the new pattern) and subject to the case-insensitive duplicate check.
Two e-mails differing only in case therefore resolve to the same account (case-insensitive lookup)
and a second self-provisioning is rejected as a duplicate — satisfying "e-mails differing only in
case result in the same account". Names are not lower-cased (verbatim, per decision).

## Edge cases / risks
- **Behavior change on create:** callers that create accounts with names containing now-invalid
  characters (e.g. spaces) will be rejected. The new pattern is permissive (admits e-mails), so
  most existing names pass, but the implementation plan must run the affected test suites
  (`TestPerson`, account/self-service tests) to catch regressions and fix any test data that uses
  invalid names.
- Bootstrap accounts `root` / `anonymous` match the new pattern (no special handling needed).
- `validatePersonName` uses `length() < max` (strictly less than 128 → max 127 chars); kept as-is.
- `Login.checkUserPassword` keeps its existing 256-char input cap and empty checks; trimming is
  additive via `byName`.
- Trimming in `byName` also affects programmatic lookups (a no-op for already-clean names).

## Testing
- Create with leading/trailing whitespace → stored name is trimmed.
- Create with an internal space or a disallowed character → rejected (`TopLogicException`).
- Create exceeding the max length → rejected.
- Create a valid e-mail address as the name → accepted.
- Login / password-reset lookup with surrounding whitespace → resolves the account (trim).
- Two accounts from e-mails differing only in case → the second create is rejected as a
  case-insensitive duplicate (extends the existing CI test).

## Decisions made during brainstorming
- Both remaining points (input normalization + self-provisioning) in **one** spec.
- **Trim everywhere** (create/login/reset via the two chokepoints); **validate only on create**.
- Character policy: configurable pattern, e-mail-friendly default **`[\w.@+~-]+`** (incl. `~`).
- Self-provisioning: **verbatim** e-mail + rely on case-insensitive handling (no lower-casing).

## Out of scope
- Re-normalizing / re-validating existing account names (only new/changed names are validated).
- Any change to the case-insensitivity mechanism already implemented on this branch.
