# Case-insensitive collation for non-binary string columns — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make non-binary string DB columns case-insensitive at the database level on PostgreSQL and Oracle (they already are on H2/MySQL/MSSQL), keep `LIKE` working on PostgreSQL, verify the requirement on DB2 at startup, and provide an opt-in migration tool for existing columns.

**Architecture:** Extend the per-dialect `DBHelper` column-DDL generation to emit a case-insensitive collation for non-binary string columns (PostgreSQL `COLLATE "tl_ci"` via a bootstrapped nondeterministic ICU collation; Oracle `COLLATE BINARY_CI`). A new `DBHelper.prepareDatabase` hook creates the PostgreSQL collation once before tables are created. A new `DBHelper.appendLikeCollation` hook attaches a deterministic `COLLATE "C"` to `LIKE` operands on PostgreSQL. DB2 uses a startup `internalCheck`. A migration processor re-emits existing non-binary string columns to pick up the collation.

**Tech Stack:** Java 17, TopLogic SQL AST (`com.top_logic.basic.db.sql`) + dialect helpers (`com.top_logic.basic.sql`), TopLogic migration framework, JUnit 3-style tests (`junit.framework.TestCase` / `BasicTestCase`).

**Spec:** `docs/superpowers/specs/2026-07-28-nonbinary-ci-collation-design.md` — Ticket #29425.

## Global Constraints

- Member (instance) fields start with `_`. Static/local vars do not.
- New `.java` files need the SPDX header (copy from a sibling, year 2026) and a class JavaDoc; reference symbols with `{@link}` not `{@code}`; avoid bare camelCase words in JavaDoc (wrap or reword) to keep the TLDoclet warning-free.
- Build from the project root with `-pl <module>` (never `cd`): `mvn -B install -pl com.top_logic.basic`.
- Run one test class with the fully-qualified name: `mvn -B test -DskipTests=false -pl <module> -Dtest=test.<FQN>`.
- Commit message format: `Ticket #29425: <description>` — no AI-attribution lines. Commits are GPG-signed; run `git commit` with the sandbox disabled so it reaches the gpg-agent.
- The CI collation name on PostgreSQL is the constant string **`tl_ci`** (referenced verbatim in DDL and tests).
- Driver-free dialect testing: obtain a dialect with `DBHelper.createDefaultInstance(<Helper>.class)` (no DB connection needed) and render DDL by calling `appendDBType(StringBuilder, DBType, name, size, precision, mandatory, binary)`.

---

### Task 1: PostgreSQL — CI collation on non-binary string/CLOB columns

**Files:**
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java` (`appendCollationDefinition` ~line 222; `appendClobType` ~line 116)
- Create: `com.top_logic.basic/src/test/java/test/com/top_logic/basic/sql/TestPostgreSQLCollation.java`

**Interfaces:**
- Produces: `public static final String PostgreSQLHelper.CI_COLLATION = "tl_ci";`

- [ ] **Step 1: Write the failing test**

```java
package test.com.top_logic.basic.sql;

import junit.framework.TestCase;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PostgreSQLHelper;

/** Tests the case-insensitive collation emitted for non-binary string columns on PostgreSQL. */
public class TestPostgreSQLCollation extends TestCase {

    private String columnDDL(DBType type, int size, boolean binary) {
        DBHelper pg = DBHelper.createDefaultInstance(PostgreSQLHelper.class);
        StringBuilder ddl = new StringBuilder();
        pg.appendDBType(ddl, type, "col", size, 0, false, binary);
        return ddl.toString();
    }

    public void testNonBinaryVarcharIsCiCollated() {
        assertTrue(columnDDL(DBType.STRING, 100, false), columnDDL(DBType.STRING, 100, false).contains("COLLATE \"tl_ci\""));
    }

    public void testBinaryVarcharIsCCollated() {
        String ddl = columnDDL(DBType.STRING, 100, true);
        assertTrue(ddl, ddl.contains("COLLATE \"C\""));
        assertFalse(ddl, ddl.contains("tl_ci"));
    }

    public void testNonBinaryClobIsCiCollated() {
        assertTrue(columnDDL(DBType.CLOB, 0, false).contains("COLLATE \"tl_ci\""));
    }
}
```

- [ ] **Step 2: Run it, verify failure**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.basic -Dtest=test.com.top_logic.basic.sql.TestPostgreSQLCollation`
Expected: FAIL — non-binary VARCHAR has no `COLLATE` today; CLOB (`TEXT`) has none.

- [ ] **Step 3: Implement**

Add the constant and extend `appendCollationDefinition` to the non-binary branch:

```java
/** Name of the case-insensitive (nondeterministic ICU) collation created by this dialect. */
public static final String CI_COLLATION = "tl_ci";

private void appendCollationDefinition(Appendable result, boolean binary) throws IOException {
    if (binary) {
        result.append(" COLLATE \"C\"");
    } else {
        result.append(" COLLATE \"" + CI_COLLATION + "\"");
    }
}
```

Make `appendClobType` collate too (currently emits only `TEXT`):

```java
@Override
protected void appendClobType(Appendable result, String columnName, long size, boolean mandatory, boolean binary,
        boolean castContext) throws IOException {
    result.append("TEXT");
    if (!castContext) {
        appendCollationDefinition(result, binary);
    }
}
```

- [ ] **Step 4: Run it, verify pass**

Run the same command; Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java \
        com.top_logic.basic/src/test/java/test/com/top_logic/basic/sql/TestPostgreSQLCollation.java
git commit -m "Ticket #29425: Emit case-insensitive collation for non-binary string columns on PostgreSQL."
```

---

### Task 2: PostgreSQL — create the `tl_ci` collation before tables (`prepareDatabase` hook)

The DDL from Task 1 references `COLLATE "tl_ci"`, which must exist before any table is created.

**Files:**
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/DBHelper.java` (add `prepareDatabase`)
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java` (override `prepareDatabase`)
- Modify: `com.top_logic.basic.db.schema/src/main/java/com/top_logic/basic/db/model/util/DBSchemaUtils.java` (`createTables(PooledConnection, DBSchema, boolean)` ~line 750)

**Interfaces:**
- Produces: `public void DBHelper.prepareDatabase(PooledConnection connection) throws SQLException` (no-op default).

- [ ] **Step 1: Add the no-op hook to `DBHelper`**

```java
/**
 * Prepares the database for the schema about to be created (e.g. creating custom collations).
 *
 * <p>
 * Invoked once before the tables are created. The default implementation does nothing.
 * </p>
 */
public void prepareDatabase(PooledConnection connection) throws SQLException {
    // Hook for dialects that need one-time database preparation.
}
```

(Imports `com.top_logic.basic.sql.PooledConnection` — same package, already available — and `java.sql.SQLException`.)

- [ ] **Step 2: Override in `PostgreSQLHelper`**

```java
@Override
public void prepareDatabase(PooledConnection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
        statement.execute(
            "CREATE COLLATION IF NOT EXISTS \"" + CI_COLLATION
                + "\" (provider = icu, locale = 'und-u-ks-level2', deterministic = false)");
    }
}
```

(`java.sql.Statement` is already imported in `PostgreSQLHelper`.)

- [ ] **Step 3: Invoke it at the start of table creation**

In `DBSchemaUtils.createTables(PooledConnection connection, DBSchema schema, boolean checkExistence)` (~line 750), before the loop over tables:

```java
connection.getSQLDialect().prepareDatabase(connection);
```

Wrap/propagate `SQLException` consistent with the surrounding method (it already executes DDL via `SQLLoader`, so it runs in a context that handles `SQLException`).

- [ ] **Step 4: Build**

Run: `mvn -B install -pl com.top_logic.basic,com.top_logic.basic.db.schema 2>&1 | tail -5`
Expected: `BUILD SUCCESS`. (No unit test: this executes DDL against PostgreSQL; verified on a live PostgreSQL during integration/QA. On H2/others `prepareDatabase` is a no-op, so existing table-creation tests must still pass — they run in the module build.)

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.basic/src/main/java/com/top_logic/basic/sql/DBHelper.java \
        com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java \
        com.top_logic.basic.db.schema/src/main/java/com/top_logic/basic/db/model/util/DBSchemaUtils.java
git commit -m "Ticket #29425: Create the case-insensitive collation on PostgreSQL before schema creation."
```

---

### Task 3: PostgreSQL — keep `LIKE` working (deterministic collation on the operand)

**Files:**
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/DBHelper.java` (add `appendLikeCollation`)
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java` (override)
- Modify: `com.top_logic.basic.db/src/main/java/com/top_logic/basic/db/sql/AbstractStatementBuilder.java` (`visitSQLLike` ~line 992)
- Create: `com.top_logic.basic.db/src/test/java/test/com/top_logic/basic/db/sql/TestLikeCollation.java`

**Interfaces:**
- Produces: `public void DBHelper.appendLikeCollation(Appendable buffer) throws IOException` (no-op default; PostgreSQL appends ` COLLATE "C"`).

- [ ] **Step 1: Write the failing test**

Model on `TestCompiledStatement` (which builds `like(...)` and renders SQL). Build a `SELECT ... WHERE col LIKE 'p'` and assert the PostgreSQL SQL contains `COLLATE "C" LIKE`:

```java
package test.com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import junit.framework.TestCase;

import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PostgreSQLHelper;

/** Tests that LIKE operands get a deterministic collation on PostgreSQL. */
public class TestLikeCollation extends TestCase {

    public void testPostgresLikeGetsDeterministicCollation() {
        DBHelper pg = DBHelper.createDefaultInstance(PostgreSQLHelper.class);
        CompiledStatement statement = query(
            select(columns(columnDef("X")), table("T"), like(column("X"), "pat"))).toSql(pg);
        String sql = statement.toString();
        assertTrue(sql, sql.contains("COLLATE \"C\" LIKE"));
    }
}
```

(If `CompiledStatement.toString()` does not expose the SQL, use the same accessor `TestCompiledStatement` uses to read the generated SQL string — check that test for the exact method.)

- [ ] **Step 2: Run it, verify failure**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.basic.db -Dtest=test.com.top_logic.basic.db.sql.TestLikeCollation`
Expected: FAIL — no `COLLATE "C"` before `LIKE` today.

- [ ] **Step 3: Add the hook and call it**

`DBHelper`:

```java
/**
 * Appends a collation to a {@code LIKE} operand, if the dialect needs a deterministic collation
 * for pattern matching. The default implementation appends nothing.
 */
public void appendLikeCollation(Appendable buffer) throws IOException {
    // Most dialects need no explicit collation for LIKE.
}
```

`PostgreSQLHelper`:

```java
@Override
public void appendLikeCollation(Appendable buffer) throws IOException {
    // Non-binary columns use a nondeterministic collation, which PostgreSQL rejects for LIKE;
    // force a deterministic collation for pattern matching.
    buffer.append(" COLLATE \"C\"");
}
```

`AbstractStatementBuilder.visitSQLLike` — after the operand is rendered (line 992), before `" LIKE '"`:

```java
sql.getExpr().visit(this, buffer);
try {
    buffer.sqlDialect.appendLikeCollation(buffer);
} catch (IOException ex) {
    throw new IOError(ex);
}
buffer.append(" LIKE '");
```

(Match the existing `IOException` handling in this file; if the buffer append pattern here does not throw `IOException`, follow the surrounding style — several visit methods here already wrap `IOException` in `IOError`.)

- [ ] **Step 4: Run it, verify pass**

Run the Task-3 test command; Expected: PASS. Also run the existing `TestCompiledStatement` to confirm non-PostgreSQL LIKE rendering is unchanged:
`mvn -B test -DskipTests=false -pl com.top_logic.basic.db -Dtest=test.com.top_logic.basic.db.sql.TestCompiledStatement`

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.basic/src/main/java/com/top_logic/basic/sql/DBHelper.java \
        com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java \
        com.top_logic.basic.db/src/main/java/com/top_logic/basic/db/sql/AbstractStatementBuilder.java \
        com.top_logic.basic.db/src/test/java/test/com/top_logic/basic/db/sql/TestLikeCollation.java
git commit -m "Ticket #29425: Keep LIKE working on PostgreSQL by collating the operand deterministically."
```

---

### Task 4: PostgreSQL — case-insensitive ORDER BY for non-binary columns

The `NATURAL` collation hint (used for non-binary columns) currently emits `COLLATE "default"`, which would override the column's CI collation and make ordering case-sensitive.

**Files:**
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java` (`internalAppendCollatedExpression` ~line 205-208)
- Create: `com.top_logic.basic/src/test/java/test/com/top_logic/basic/sql/TestPostgreSQLNaturalCollation.java`

- [ ] **Step 1: Write the failing test**

```java
package test.com.top_logic.basic.sql;

import junit.framework.TestCase;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PostgreSQLHelper;

/** Tests the collation applied to the NATURAL (non-binary) ordering hint on PostgreSQL. */
public class TestPostgreSQLNaturalCollation extends TestCase {

    public void testNaturalOrderingUsesCiCollation() throws Exception {
        DBHelper pg = DBHelper.createDefaultInstance(PostgreSQLHelper.class);
        StringBuilder sql = new StringBuilder();
        pg.appendCollatedExpression(sql, "COL", CollationHint.NATURAL);
        assertTrue(sql.toString(), sql.toString().contains("COLLATE \"tl_ci\""));
    }
}
```

(`appendCollatedExpression` is the public entry at `DBHelper.java:1341`. Confirm its exact signature `(Appendable, String, CollationHint)` and adjust the call if needed.)

- [ ] **Step 2: Run it, verify failure**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.basic -Dtest=test.com.top_logic.basic.sql.TestPostgreSQLNaturalCollation`
Expected: FAIL — currently emits `COLLATE "default"`.

- [ ] **Step 3: Implement**

In `PostgreSQLHelper.internalAppendCollatedExpression`, change the `NATURAL` branch:

```java
case NATURAL:
    super.internalAppendCollatedExpression(buffer, sqlExpression, collationHint);
    buffer.append(" COLLATE \"" + CI_COLLATION + "\" ");
    break;
```

- [ ] **Step 4: Run it, verify pass** — same command; Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.basic/src/main/java/com/top_logic/basic/sql/PostgreSQLHelper.java \
        com.top_logic.basic/src/test/java/test/com/top_logic/basic/sql/TestPostgreSQLNaturalCollation.java
git commit -m "Ticket #29425: Order non-binary columns case-insensitively on PostgreSQL."
```

---

### Task 5: Oracle — CI collation on non-binary string columns

**Files:**
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/OracleHelper.java` (`appendCharType` ~line 521, `appendStringType` ~line 531; also read `internalAppendCollatedExpression` ~line 222 to confirm ORDER BY consistency)
- Create: `com.top_logic.basic/src/test/java/test/com/top_logic/basic/sql/TestOracleCollation.java`

- [ ] **Step 1: Write the failing test**

```java
package test.com.top_logic.basic.sql;

import junit.framework.TestCase;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.OracleHelper;

/** Tests the case-insensitive collation emitted for non-binary string columns on Oracle. */
public class TestOracleCollation extends TestCase {

    private String columnDDL(DBType type, int size, boolean binary) {
        DBHelper oracle = DBHelper.createDefaultInstance(OracleHelper.class);
        StringBuilder ddl = new StringBuilder();
        oracle.appendDBType(ddl, type, "col", size, 0, false, binary);
        return ddl.toString();
    }

    public void testNonBinaryStringIsBinaryCi() {
        String ddl = columnDDL(DBType.STRING, 100, false);
        assertTrue(ddl, ddl.contains("NVARCHAR2") && ddl.contains("COLLATE BINARY_CI"));
    }

    public void testBinaryStringHasNoCiCollation() {
        assertFalse(columnDDL(DBType.STRING, 100, true).contains("BINARY_CI"));
    }
}
```

- [ ] **Step 2: Run it, verify failure**

Run: `mvn -B test -DskipTests=false -pl com.top_logic.basic -Dtest=test.com.top_logic.basic.sql.TestOracleCollation`
Expected: FAIL — no collation emitted today.

- [ ] **Step 3: Implement**

In `OracleHelper.appendStringType`, non-binary branch (after the `NVARCHAR2(size)` is written):

```java
} else {
    result.append("NVARCHAR2");
    size(result, size);
    result.append(" COLLATE BINARY_CI");
}
```

In `appendCharType`, non-binary branch:

```java
} else {
    result.append("NCHAR(1) COLLATE BINARY_CI");
}
```

Do **not** add a collation to `appendClobType` (Oracle LOB columns cannot be collated). Read `internalAppendCollatedExpression` (~line 222): if the `NATURAL` hint emits an ordering that is not case-insensitive, align it with `BINARY_CI` (e.g. via `NLS_SORT`); add a focused ORDER-BY test analogous to Task 4 if a change is made. If it is already consistent, leave it and note so in the commit.

- [ ] **Step 4: Run it, verify pass** — same command; Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.basic/src/main/java/com/top_logic/basic/sql/OracleHelper.java \
        com.top_logic.basic/src/test/java/test/com/top_logic/basic/sql/TestOracleCollation.java
git commit -m "Ticket #29425: Emit BINARY_CI collation for non-binary string columns on Oracle."
```

---

### Task 6: DB2 — startup check that the database uses a case-insensitive collation

**Files:**
- Modify: `com.top_logic.basic/src/main/java/com/top_logic/basic/sql/DB2Helper.java` (add `internalCheck(Statement)` override)

- [ ] **Step 1: Read the pattern**

Read `PostgreSQLHelper.internalCheck(Statement)` (~line 131) and `DBHelper.internalCheck` (~line 382) and `DBHelper.check` (~line 367) — `internalCheck` runs once per pool at startup (`AbstractConfiguredConnectionPoolBase.java:215`).

- [ ] **Step 2: Implement the check**

Add to `DB2Helper`:

```java
@Override
protected void internalCheck(Statement statement) throws SQLException {
    // The database-wide collation must be case-insensitive; DB2 has no per-column collation.
    try (ResultSet result =
        statement.executeQuery("SELECT VALUE FROM SYSIBMADM.DBCFG WHERE NAME = 'coll_seq'")) {
        String collation = result.next() ? result.getString(1) : null;
        if (collation == null || !isCaseInsensitiveCollation(collation)) {
            Logger.error("DB2 database is not configured with a case-insensitive collation (coll_seq='"
                + collation + "'); non-binary string columns will be case-sensitive.", DB2Helper.class);
        }
    }
}

private boolean isCaseInsensitiveCollation(String collSeq) {
    // A CLDR/UCA locale-sensitive collating sequence (e.g. "SYSTEM_..." with a CLDR locale,
    // or "UCA...") provides case-insensitive comparison; "IDENTITY"/"IDENTITY_16BIT" do not.
    String value = collSeq.toUpperCase();
    return value.contains("CLDR") || value.contains("UCA") || value.contains("NX")
        || value.contains("SYSTEM");
}
```

Imports: `java.sql.ResultSet`, `java.sql.SQLException`, `java.sql.Statement`, `com.top_logic.basic.Logger`.

**Verification note:** the exact DB2 catalog query (`SYSIBMADM.DBCFG` / `coll_seq`) and the CI-detection heuristic MUST be verified against a real DB2 instance — they cannot be tested here. Adjust the query/heuristic during DB2 QA. If unsure at implementation time, keep the check conservative (log a warning, never throw) so a misdetection cannot block startup.

- [ ] **Step 3: Build**

Run: `mvn -B install -pl com.top_logic.basic 2>&1 | tail -5`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.basic/src/main/java/com/top_logic/basic/sql/DB2Helper.java
git commit -m "Ticket #29425: Warn at startup when the DB2 database collation is not case-insensitive."
```

---

### Task 7: Migration tool — convert existing non-binary string columns to the CI collation

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/processors/RecollateStringColumnsProcessor.java`

**Interfaces:**
- Consumes: `SQLFactory.modifyColumnType` / `SQLModifyColumn` (`setBinary`/`setSize`/`setPrecision`), `MigrationContext.getPersistentRepository()`, `DBHelper.prepareDatabase` (Task 2).

- [ ] **Step 1: Read the precedent**

Read `com.top_logic/src/main/java/com/top_logic/knowledge/service/db2/migration/AlterColumnProcessor.java` — specifically `adjustTable` (~line 206) and the `modifyColumnType(...)` usage (~line 259-268), and how it iterates types/columns (`getPersistentRepository()` ~line 144, `getMetaObjects()`, `MOStructure.getAttributes()`, `getDbMapping()`).

- [ ] **Step 2: Implement the processor**

Config: optional `getTable()` and `getColumn()` (both `@Nullable`); when both empty, process **all** tables. Logic:

1. `Util` not needed; get `MORepository repo = context.getPersistentRepository();`.
2. First, ensure the PostgreSQL collation exists: `connection.getSQLDialect().prepareDatabase(connection);`.
3. Iterate `repo.getMetaObjects()`; keep `MOStructure` that are concrete tables (skip abstract `MOClass`; if `getTable()` is set, only that one).
4. For each, iterate `getAttributes()`; for the configured `getColumn()` (or all), get `attr.getDbMapping()`; for each `DBAttribute col` with `col.getSQLType() == DBType.STRING && !col.isBinary()`:
   - Re-emit the column type to apply the (new) collation, forcing the MODIFY even though type/size/binary are unchanged:
     ```java
     SQLModifyColumn modification = modifyColumnType(table(table.getDBMapping().getDBName()),
         col.getDBName(), col.getSQLType());
     modification.setBinary(col.isBinary());
     modification.setSize(col.getSQLSize());
     modification.setPrecision(col.getSQLPrecision());
     query(modification).toSql(connection.getSQLDialect()).executeUpdate(connection);
     log.info("Re-collated column '" + col.getDBName() + "' of table '" + table.getName() + "'.");
     ```
   (Unlike `AlterColumnProcessor`, always emit — do not diff, because the collation change is not visible in type/size/binary.)
5. Wrap in `try/catch (SQLException)` → `log.error(...)`, following `AlterColumnProcessor`.

Class skeleton mirrors `AlterColumnProcessor` (`@TagName("recollate-string-columns")`, `AbstractConfiguredInstance`, `@CalledByReflection` ctor). Fill the numbered steps with concrete `SQLFactory` calls copied from `AlterColumnProcessor`.

- [ ] **Step 3: Build**

Run: `mvn -B install -pl com.top_logic 2>&1 | tail -5`
Expected: `BUILD SUCCESS`. (No automated test: the MODIFY executes against Oracle/PostgreSQL and only changes collation, which H2 does not model; verified during DB QA. The DDL correctness is covered by Tasks 1/5.)

- [ ] **Step 4: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/processors/RecollateStringColumnsProcessor.java
git commit -m "Ticket #29425: Add migration processor re-collating existing non-binary string columns."
```

---

## Self-Review

**Spec coverage:**
- PostgreSQL CI column collation → Task 1 (+ bootstrap Task 2). ✓
- Oracle CI column collation → Task 5. ✓
- PostgreSQL LIKE fix → Task 3. ✓
- ORDER BY alignment → Task 4 (PostgreSQL) + Task 5 step 3 (Oracle check). ✓
- DB2 startup check → Task 6. ✓
- H2/MySQL/MSSQL unchanged → no task touches them. ✓
- Existing-data migration tool (targeted or all) → Task 7. ✓

**Placeholder scan:** Tasks 6 and 7 carry explicit verification notes (DB2 catalog query; migration executes only against Oracle/PostgreSQL) because those cannot be unit-tested in this environment — the code is concrete, the verification is deferred to DB QA, which is stated, not hidden. All testable tasks (1,3,4,5) have full test + implementation code.

**Type consistency:** `PostgreSQLHelper.CI_COLLATION = "tl_ci"` is referenced in Tasks 1/2/4 consistently. `DBHelper.prepareDatabase(PooledConnection)` (Task 2) and `DBHelper.appendLikeCollation(Appendable)` (Task 3) are defined before use; Task 7 consumes `prepareDatabase`.

## Notes / risks
- Tasks 1, 3, 4, 5 are unit-testable driver-free via `DBHelper.createDefaultInstance(...)`. Tasks 2, 6, 7 need a live PostgreSQL/Oracle/DB2 and are verified in DB QA — they are built and inspected here.
- PostgreSQL requires version 12+ (ICU nondeterministic collations); Oracle requires 12.2+ with data-bound collation. Document these as deployment requirements.
- Verify `CompiledStatement`'s SQL accessor for the Task-3 test (mirror `TestCompiledStatement`).
- The migration (Task 7) is a long, locking, per-column `ALTER` sweep when run over all tables — intended as an explicitly-invoked tool.
