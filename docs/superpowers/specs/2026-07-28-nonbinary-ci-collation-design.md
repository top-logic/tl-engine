# Design: Case-insensitive collation for non-binary string columns

- **Date:** 2026-07-28
- **Status:** Approved design, pending implementation plan
- **Goal:** Make a **non-binary** string column behave case-insensitively at the database level on
  **all** supported databases — so that equality, uniqueness (a `UNIQUE` index rejects
  case-insensitively-equal values) and ordering are case-insensitive consistently. Today only
  H2/MySQL/MSSQL do this; PostgreSQL, Oracle and DB2 compare non-binary columns case-sensitively.

## Context

TopLogic renders SQL through an AST layer (`com.top_logic.basic.db.sql`, rendered by
`AbstractStatementBuilder`, a `SQLVisitor`) that delegates dialect fragments to `DBHelper`
subclasses in `com.top_logic.basic.sql`. The `binary` flag of a `DBAttribute` is the switch between
case-sensitive (`binary=true`) and case-insensitive (`binary=false`) semantics
(`CollationHintComputation.getCollationHint`, `.../knowledge/service/db2/CollationHintComputation.java:68-85`:
non-binary string → `NATURAL`, binary → `BINARY`).

### Current per-dialect behavior of a non-binary string column

| DB | non-binary DDL today | case-insensitive? |
|---|---|---|
| H2 | `VARCHAR_IGNORECASE` (`H2Helper.internalGetDBType:101-118`) | yes |
| MySQL | `CHARACTER SET utf8mb4` (default `*_ci`) (`MySQLHelper.appendBinaryModifier:152-160`) | yes |
| MSSQL | `COLLATE Latin1_General_CI_AS` (`MSSQLHelper.appendCollationDefinition:107-113`) | yes |
| PostgreSQL | `VARCHAR(n)` with **no COLLATE** → cluster/db default (`PostgreSQLHelper.appendCollationDefinition:222-226`) | no (usually) |
| Oracle | `NVARCHAR2`, no CI collation | no |
| DB2 | DB-default collation | no (usually) |

### Two facts that shape the design

1. **Collation hints are applied only to `ORDER BY`**, never to equality, `LIKE`, `GROUP BY` or
   `DISTINCT` (`AbstractStatementBuilder.visitSQLOrder:344-366`; equality at
   `AbstractStatementBuilder.java:964-966` emits a bare `=`). Therefore equality and uniqueness
   are governed **entirely by the column's stored collation** — which is why the fix must live at
   the column-DDL level, not in query hints.
2. **`LIKE` has a single AST render choke point** (`AbstractStatementBuilder.visitSQLLike:988-1006`,
   operand rendered at line 992). PostgreSQL nondeterministic collations reject `LIKE`, so a
   deterministic `COLLATE "C"` must be attached to the `LIKE` operand there — for PostgreSQL only.
   (Production SQL-`LIKE` usage is in any case limited: the modern search path's `Matches`
   throws `UnsupportedOperationException` — `.../db2/expr/transform/sql/ExpressionEvaluator.java:379`.)

## Design

### 1. PostgreSQL — CI column collation (`PostgreSQLHelper`)
- Define a nondeterministic ICU collation, locale `und-u-ks-level2` (case-insensitive,
  accent-sensitive), `deterministic=false`, with a fixed name (e.g. `tl_ci`). Create it once during
  schema setup with `CREATE COLLATION IF NOT EXISTS "tl_ci" (provider = icu, locale = 'und-u-ks-level2', deterministic = false)`.
- `appendCollationDefinition` (`:222-226`): add the non-binary branch → `COLLATE "tl_ci"`
  (binary stays `COLLATE "C"`).
- `appendClobType` (`:116-121`, currently emits `TEXT` with no collation): emit the same
  collation rule for non-binary CLOB/`TEXT` columns.
- Requires PostgreSQL 12+ (ICU nondeterministic collations).

### 2. Oracle — CI column collation (`OracleHelper`)
- Non-binary `appendStringType` / `appendCharType` / `appendClobType`: append `COLLATE BINARY_CI`
  (case-insensitive, accent-sensitive). Binary keeps the current behavior.
- Requires Oracle 12.2+ with data-bound (column-level) collation enabled.

### 3. PostgreSQL `LIKE` fix
- Add a `DBHelper` hook (default no-op) that wraps/annotates a `LIKE` operand with a deterministic
  collation; `PostgreSQLHelper` overrides it to append `COLLATE "C"`. Apply it in
  `AbstractStatementBuilder.visitSQLLike` around the operand render (line 992), routed through the
  dialect so only PostgreSQL emits it. This keeps `LIKE` working (pattern matching stays
  case-sensitive) despite the column's nondeterministic collation.

### 4. `ORDER BY` alignment
- On PostgreSQL, the `NATURAL` hint currently appends `COLLATE "default"`
  (`PostgreSQLHelper.internalAppendCollatedExpression:207`), which would override the column's CI
  collation and make ordering case-sensitive — inconsistent with H2/MySQL/MSSQL. Adjust the
  `NATURAL` hint on PostgreSQL so non-binary ordering uses the CI collation (append `COLLATE "tl_ci"`
  or omit the override so the column collation is inherited). Verify Oracle ordering is consistent
  with `BINARY_CI`.

### 5. DB2 — startup check
- DB2 has no per-column collation; CI is a database-wide property set at `CREATE DATABASE`. No DDL
  change. Add a startup check that verifies the connected DB2 database uses a case-insensitive
  collation and emits a clear error (or prominent warning) otherwise, plus documentation of the
  requirement.

### 6. H2 / MySQL / MSSQL
- Unchanged — already case-insensitive for non-binary columns.

### 7. Existing databases — migration tool
- New columns get CI collation automatically via the DDL change. For existing databases, provide a
  migration processor that converts existing non-binary string columns to the CI collation on
  Oracle/PostgreSQL. It can be pointed at a specific table/column **or run over all tables** on
  request. This is a potentially long-running, per-column `ALTER` sweep; it is not run
  automatically over everything.

## Risks / edge cases
- **PostgreSQL nondeterministic collation:** breaks `LIKE`/pattern operators and `text_pattern_ops`
  indexes on affected columns. `LIKE` is handled (§3); other pattern operators / functional pattern
  indexes on non-binary columns must be reviewed. PG 12+ required.
- **Oracle 12.2+** column-level collation requirement (data-bound collation must be enabled).
- **Migration cost:** altering the collation of every non-binary string column across all tables is
  expensive and locks tables; hence it is an opt-in tool, not automatic.
- **Semantics change:** on PostgreSQL/Oracle installations, existing non-binary columns become CI
  once migrated — comparisons that previously distinguished case will stop doing so (this is the
  intended, now-consistent behavior).

## Testing
- Per-dialect DDL unit tests: the generated column DDL for a non-binary string/CLOB column contains
  the expected CI collation (PostgreSQL `COLLATE "tl_ci"`, Oracle `COLLATE BINARY_CI`) and binary
  columns keep their case-sensitive collation.
- `LIKE` render test: on PostgreSQL the `LIKE` operand carries `COLLATE "C"`; other dialects
  unchanged.
- `ORDER BY` render test: non-binary ordering on PostgreSQL uses the CI collation, not
  `COLLATE "default"`.
- Where a PostgreSQL/Oracle test database is available, an integration test that a `UNIQUE` index on
  a non-binary column rejects a case-insensitively-equal insert.
- DB2 startup-check unit test (CI vs non-CI database collation → pass/fail).

## Decisions made during brainstorming
- **Full CI semantics** (equality, uniqueness, ordering) for non-binary columns — not just unique
  enforcement.
- **Column-level collation** is the mechanism (query-time hints cannot enforce insert-time
  uniqueness).
- **PostgreSQL** uses a nondeterministic ICU collation, locale `und-u-ks-level2`
  (case-insensitive, accent-sensitive).
- **DB2** handled via a **startup check** (no per-column DDL possible).
- **Existing data:** DDL change for new columns + an **opt-in migration tool** (targeted or all
  tables).

## Out of scope
- Changing the meaning of the `binary` flag or the set of columns that are binary/non-binary.
- Full-text search behavior (Lucene-based) — unaffected.
