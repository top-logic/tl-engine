# Design: Case-insensitive account usernames (Ticket #29423, part 1)

- **Ticket:** #29423 — "Umgang mit Nutzernamen"
- **Date:** 2026-07-27
- **Status:** Approved design (revised after LDAP review + stakeholder feedback), pending implementation
- **Scope of this spec:** Treat account usernames case-insensitively **without changing their
  spelling**. Names are stored exactly as entered; only lookup/uniqueness are case-insensitive.
  A migration renames pre-existing case-only-differing accounts so the invariant holds.
- **Explicitly out of scope (follow-up specs):** input trimming, allowed-character rules, and
  deriving/normalizing usernames from e-mail during self-provisioning.

## Context

The account is `com.top_logic.knowledge.wrap.person.Person` (`OBJECT_NAME = "Person"`); the login
name is the inherited `name` attribute.

- **Schema:** `com.top_logic/src/main/webapp/WEB-INF/kbase/PersonMeta.xml` — table `PERSON`,
  column `name` (not `binary`), with a **unique index** on `name`. No UID/DN/external-id column
  exists — the name is the only key.
- **Lookup:** `Person.byName(String)` / `Person.byName(KnowledgeBase, String)` resolve through
  `com.top_logic/.../knowledge/util/ItemByNameCache.java`, a `HashMap<String, KnowledgeItem>`
  keyed by the raw stored name → **case-sensitive**. Note `ItemByNameCache.addToCache` throws
  `IllegalStateException` on a key clash.
- **Create:** `Person.create(KnowledgeBase, String, AuthenticationDevice)` stores the name
  verbatim; no case-fold, no duplicate check.

### DB collation is dialect-dependent

For the non-binary `name` column, case-equality of the unique index differs per dialect:

| DB | non-binary column | `Admin` + `ADMIN` can coexist? |
|---|---|---|
| H2 | `VARCHAR_IGNORECASE` | no (case-insensitive) |
| MySQL | `utf8mb4` default `*_ci` collation | no |
| MSSQL | `COLLATE Latin1_General_CI_AS` | no |
| PostgreSQL | no `COLLATE` → DB default (standard = case-sensitive) | yes |
| Oracle | `NVARCHAR2`, no CI collation | yes |
| DB2 | config-dependent | usually yes |

So on PostgreSQL/Oracle/DB2 two case-only-differing accounts can already exist today.
Independently, the **application** lookup (`ItemByNameCache`) is case-sensitive on every dialect,
so a user stored as `Admin` cannot be found as `admin` — this is the core defect.

### LDAP synchronization (critical constraint)

(Module `com.top_logic`; see `.../base/security/device/ldap/LDAPAuthenticationAccessDevice.java`,
`.../base/dsa/ldap/LDAPAccessService.java`, `.../knowledge/wrap/person/RefreshUsersTask.java`.)

- The **only** join key between an LDAP entry and a local `Person` is the login `name`, compared
  **case-sensitively**. There is no stable external id (only `authDeviceID`, the device *name*).
- `synchronizeUsers` (`LDAPAuthenticationAccessDevice.java:223`): reads the LDAP `username`
  verbatim (`:227`), matches via `Person.byName(userName)` (`:236`), and creates unknown entries
  via `Person.create(...)` (`:238`) — name taken verbatim.
- `RefreshUsersTask` (`:72`) **deletes** every LDAP-device person that `synchronizeUsers` did not
  return (gated by `getAccountCleanupLimit()`).
- LDAP login DN resolution: `LDAPAccessService.getFullUserDN(name)` keeps an entry only when
  `memberName.equals(aName)` — **case-sensitive** (`LDAPAccessService.java:554`).

**Consequences that shape this design:**
1. Names must **not** be rewritten to a normalized (e.g. lowercase) form — the LDAP source name
   must stay intact, or the next sync would recreate the original and delete the rewritten copy.
   → confirms "no normalization on store".
2. Making `Person.byName` case-insensitive automatically removes LDAP sync churn (an existing
   account is matched regardless of case).
3. The LDAP DN comparison must also be made case-insensitive, otherwise LDAP login fails to
   resolve a DN when the stored/typed case differs from the directory.

## Goals / non-goals

**Goals**
- Accounts are found regardless of the spelling's case (lookup, login, password reset, external
  auth) — DB-independent.
- Names keep their original spelling on creation (no normalization).
- Case-insensitive uniqueness is enforced going forward (creating a case-variant of an existing
  name is rejected).
- LDAP synchronization and LDAP login work case-insensitively without churn.
- A migration renames pre-existing case-only-differing accounts so at most one account per
  case-insensitive name remains.

**Non-goals (separate follow-up specs):** input trimming, allowed-character rules, e-mail→username
derivation for self-provisioning.

## Design

### 1. No normalization on store
`Person.create(...)` keeps storing the name verbatim. LDAP-created names stay exactly as the
directory provides them.

### 2. Case-insensitive lookup (`ItemByNameCache` + `Person.byName`)
Give `ItemByNameCache` an optional key-normalization function (default identity) applied wherever
a cache key is derived (initial fill, create/update/delete events, and lookup), plus a
`lookup(K)` method that normalizes the query key. `Person` constructs its cache with a lowercasing
mapper (`s -> s.toLowerCase(Locale.ROOT)`) and resolves via `cache.lookup(name)`.

- The stored/returned `Person.name` keeps its original case; only the cache *key* and the query
  are lowercased for comparison.
- Because keys are lowercased, `addToCache`'s clash detection now enforces case-insensitive
  uniqueness at cache-build time. After the migration (below) no collisions remain, so the cache
  builds cleanly; a future stray collision would fail fast rather than silently mis-resolve.
- `Person.byName(KnowledgeBase, String)` on a **non-default** KB uses `getObjectByAttribute`
  (exact match) — kept as-is (case-insensitive matching there is a documented limitation; the
  default-KB path via the cache is the case-insensitive one used everywhere in practice).

### 3. Case-insensitive uniqueness on create
`Person.create(...)` rejects a name that already exists case-insensitively (checked via the now
case-insensitive `Person.byName`), throwing a `TopLogicException` with a new `ResKey`
`ERROR_DUPLICATE_ACCOUNT_NAME__NAME`. This makes uniqueness DB-independent (the DB unique index
alone does not enforce it on PostgreSQL/Oracle/DB2). LDAP sync and the admin create action already
call `byName` before creating, so they get a clean check; the low-level guard covers the rest.
(Limitation: two brand-new case-variant creates within a single uncommitted transaction cannot see
each other; this is an accepted edge case.)

### 4. LDAP case-insensitivity
- `synchronizeUsers` needs no change beyond #2: `Person.byName` is now case-insensitive, so an
  existing account is matched and neither recreated nor deleted.
- `LDAPAccessService.getFullUserDN(name)` changes its `memberName.equals(aName)` comparison to a
  case-insensitive comparison (`equalsIgnoreCase`) so LDAP login resolves the DN regardless of
  case.

### 5. Migration (module `tl`, table `PERSON`): rename case-only collisions
Rename pre-existing accounts whose names are equal case-insensitively so only one per
case-insensitive name remains. **Names are not lowercased** — the keeper keeps its exact spelling.

- Group accounts by the lowercased name.
- **Keeper rule:** prefer the externally managed account (an `authDeviceID` belonging to a
  data-access/LDAP device) so its name keeps matching the directory; otherwise the oldest
  (smallest id). The keeper's name is unchanged.
- **Renamed accounts:** each non-keeper gets `<its own name>` + the smallest integer suffix
  (`2`, `3`, …) that is free case-insensitively (its original case is preserved, e.g. `john` →
  `john2`, `JOHN` → `JOHN2`).
- Log every rename (`old → new`) at INFO/WARN so admins can inform affected users.
- No DDL, no `binary` change, no lowercasing.

The rename *decision logic* is a pure, DB-independent helper (unit-tested); the SQL apply mirrors
the `Ticket27517UpdatePersonTable` precedent.

## Edge cases
- **Cache clash = fail-fast invariant.** With lowercased keys, a residual collision throws at
  cache build — acceptable because the migration removes collisions and create rejects new ones.
- **Renaming an LDAP account is avoided** by the keeper rule (keep the externally managed one); if
  two collision members are *both* externally managed (a directory anomaly), keep the oldest and
  log a warning that the next sync may re-touch it.
- **Non-default KB lookup** stays exact-match (documented limitation).
- **null/empty** names pass through unchanged.

## Testing
- **CI lookup:** create `Admin`; assert `byName("admin")`/`byName("ADMIN")`/`byName("Admin")` all
  return it; assert the stored name is still `Admin` (case preserved).
- **CI uniqueness on create:** creating `ADMIN` when `Admin` exists is rejected with the new error.
- **Rename-plan helper (pure unit tests):** keeper prefers externally managed, else oldest;
  non-keepers get suffixed, case preserved, CI-unique (`john` kept, `John`→`John2`, `JOHN`→`JOHN3`
  when `john2` taken); idempotent on clean data.
- **Migration:** on H2 the CI collation prevents seeding a collision, so the pure helper is the
  authoritative test; the migration integration test verifies a no-op on collision-free data.
- **LDAP DN comparison** change is covered by a focused test of the comparison (or manual
  verification against a directory); full LDAP sync is not automatically testable here.

## Decisions made during brainstorming
- **No normalization on store** — names keep their original spelling (revised from the earlier
  "lowercase-on-store"; required by LDAP sync, which round-trips the directory name).
- **Case-insensitive lookup** via a normalizing `ItemByNameCache`.
- **Reject case-insensitive duplicates on create** (decision A = yes).
- **Migration renames** case-only collisions (keeper preserved, others suffixed) — it does not
  merely report.
- DB scope: all supported databases.

## Follow-up specs (Ticket #29423, later parts)
1. Input trimming + allowed-character validation at create / login / password reset (wiring up
   `PersonManager.validatePersonName`).
2. Self-provisioning: derive the username from the e-mail so e-mails differing only in case yield
   the same account.
