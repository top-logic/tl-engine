# Case-insensitive account usernames — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Accounts are found regardless of the case of the entered name (lookup, login, password reset, LDAP), names keep their original spelling, case-insensitive duplicates are rejected on create, and a migration renames pre-existing case-only-differing accounts.

**Architecture:** Names are stored verbatim. `ItemByNameCache` gains an optional key-normalization function; `Person`'s cache lowercases keys so `Person.byName(...)` matches case-insensitively while the stored/returned name keeps its case. `Person.create(...)` rejects a case-insensitive duplicate. The LDAP DN comparison is made case-insensitive. A migration renames case-only collisions (keeper keeps its spelling, others get a suffix); its decision logic is a pure, unit-tested helper.

**Tech Stack:** Java 17, TopLogic KnowledgeBase / TypedConfiguration, JUnit 3-style tests (`BasicTestCase`), Maven, TopLogic migration framework.

**Spec:** `docs/superpowers/specs/2026-07-27-case-insensitive-usernames-design.md`

## Global Constraints

- Member (instance) fields start with `_`. Static/local vars do not.
- Every new `.java` file needs the SPDX header (copy from a sibling, year 2026) and a class JavaDoc.
- JavaDoc references use `{@link}`, not `{@code}`, for resolvable symbols.
- Build from the project root with `-pl <module>` (never `cd` into a module): `mvn -B install -pl com.top_logic`.
- Run one test class with the fully-qualified name: `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.<...>`.
- When adding/renaming `I18NConstants`, do **not** pass `-Dmaven.javadoc.skip=true` (the doclet regenerates `messages_*.properties`). Commit the regenerated `messages_de.properties` too.
- Commit message format: `Ticket #29423: <description>` — no AI-attribution lines. Commits are GPG-signed; run `git commit` with the sandbox disabled so it reaches the gpg-agent.
- LDAP constraint: the LDAP sync matches `Person` by name and would recreate/delete on a name change, so names must **not** be rewritten to a normalized form — only compared case-insensitively.
- Test DB is H2 (`VARCHAR_IGNORECASE`): a test **cannot** seed two case-only-differing accounts; the rename logic is therefore validated by the pure helper (Task 4), not a DB test.

---

### Task 1: Case-insensitive lookup (`ItemByNameCache` + `Person.byName`)

Make `Person` lookups case-insensitive while preserving the stored spelling, by giving `ItemByNameCache` an optional key normalizer and using a lowercasing one for `Person`.

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/knowledge/util/ItemByNameCache.java`
- Modify: `com.top_logic/src/main/java/com/top_logic/knowledge/wrap/person/Person.java` (`getOrInstallByNameCache` ~line 700, `fromCache` ~line 685)
- Create: `com.top_logic/src/test/java/test/com/top_logic/knowledge/wrap/person/TestPersonNameCaseInsensitivity.java`

**Interfaces:**
- Produces on `ItemByNameCache<K>`: new constructor `ItemByNameCache(DBKnowledgeBase, String table, String keyAttribute, Class<K> keyType, Function<? super K, ? extends K> keyMapper)` (the existing 4-arg constructor delegates with `Function.identity()`); new method `public KnowledgeItem lookup(K rawKey)`.

- [ ] **Step 1: Write the failing test**

Create `TestPersonNameCaseInsensitivity`. Copy `suite()`, the `kb` field, `createPerson(String)` and a `deletePerson(Person)` helper from `TestPerson.java` (same package). Add:

```java
public void testLookupIsCaseInsensitivePreservingSpelling() {
    Person admin = createPerson("Admin");
    try {
        assertEquals("Original spelling must be preserved.", "Admin", admin.getName());
        assertSame(admin, Person.byName("admin"));
        assertSame(admin, Person.byName("ADMIN"));
        assertSame(admin, Person.byName("Admin"));
        assertNull(Person.byName("does-not-exist"));
    } finally {
        deletePerson(admin);
    }
}
```

- [ ] **Step 2: Run the test, verify it fails**

Run: `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.knowledge.wrap.person.TestPersonNameCaseInsensitivity`
Expected: FAIL — `Person.byName("admin")` returns `null` for a person stored as `Admin`.

- [ ] **Step 3: Add the key normalizer to `ItemByNameCache`**

Add `import java.util.function.Function;`. Add a field `private final Function<? super K, ? extends K> _keyMapper;`. Replace the constructor:

```java
public ItemByNameCache(DBKnowledgeBase kb, String table, String keyAttribute, Class<K> keyType) {
    this(kb, table, keyAttribute, keyType, Function.identity());
}

public ItemByNameCache(DBKnowledgeBase kb, String table, String keyAttribute, Class<K> keyType,
        Function<? super K, ? extends K> keyMapper) {
    _kb = kb;
    _table = table;
    _keyAttribute = keyAttribute;
    _keyType = keyType;
    _keyMapper = keyMapper;
}
```

Add a key-deriving helper and a lookup method:

```java
/** Derives the (normalized) cache key from a raw attribute value. */
private K key(Object attributeValue) {
    K value = _keyType.cast(attributeValue);
    if (value == null) {
        return null;
    }
    return _keyType.cast(_keyMapper.apply(value));
}

/** Looks up the cached item for the given raw key, applying the key normalization. */
public KnowledgeItem lookup(K rawKey) {
    K key = key(rawKey);
    if (key == null) {
        return null;
    }
    return getValue().get(key);
}
```

Route every key derivation and every raw `remove`/`put` through `key(...)`:
- `handleEvent`, deletion branch (currently `cacheValue.remove(deletion.getValues().get(_keyAttribute))`) → `cacheValue.remove(key(deletion.getValues().get(_keyAttribute)))`.
- `handleItemUpdate(cacheValue, item, oldName, newName)`: `cacheValue.remove(key(oldName))` and `addToCache(cacheValue, key(newName), item)` (guard `key(newName) != null`).
- `handleChange`: `cacheValue.remove(key(oldValue))` (when `oldValue != null`) and `addToCache(cacheValue, key(newValue), item)` (when `newValue != null`).
- `addToCache(cache, item, newName)` (the 3-arg overload): replace `K keyAttrValue = cast(newName);` with `K keyAttrValue = key(newName);`.

Leave `addToCache(cache, K keyValue, item)` (2-arg) unchanged — its callers now pass an already-normalized key. Remove the now-unused private `cast(...)` only if it has no remaining callers; otherwise keep it.

- [ ] **Step 4: Use the normalizer in `Person`**

In `Person.java` add `import java.util.Locale;` if missing. In `getOrInstallByNameCache(...)` construct the cache with a lowercasing mapper:

```java
byNameCache = new ItemByNameCache<>((DBKnowledgeBase) defaultKB, OBJECT_NAME, NAME_ATTRIBUTE,
    String.class, name -> name.toLowerCase(Locale.ROOT));
```

In `fromCache(...)` resolve via `lookup`:

```java
private static Person fromCache(KnowledgeBase defaultKB, String name) {
    if (StringServices.isEmpty(name)) {
        return null;
    }
    KnowledgeItem cachedPerson = getOrInstallByNameCache(defaultKB).lookup(name);
    if (cachedPerson == null) {
        return null;
    }
    return cachedPerson.getWrapper();
}
```

(`byName(String)` and `byName(KnowledgeBase, String)` need no change: the default-KB path goes through `fromCache`; the non-default-KB `getObjectByAttribute` path is a documented exact-match limitation.)

- [ ] **Step 5: Run the test, verify it passes**

Run: `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.knowledge.wrap.person.TestPersonNameCaseInsensitivity`
Expected: PASS.

- [ ] **Step 6: Compile the module**

Run: `mvn -B install -pl com.top_logic 2>&1 | tee com.top_logic/target/build.log | tail -5`
Expected: `BUILD SUCCESS`.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/knowledge/util/ItemByNameCache.java \
        com.top_logic/src/main/java/com/top_logic/knowledge/wrap/person/Person.java \
        com.top_logic/src/test/java/test/com/top_logic/knowledge/wrap/person/TestPersonNameCaseInsensitivity.java
git commit -m "Ticket #29423: Resolve accounts by name case-insensitively (normalizing name cache)."
```

---

### Task 2: Reject case-insensitive duplicates on create

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/knowledge/wrap/person/Person.java` (`create(...)` ~line 610)
- Modify/Create: `com.top_logic/src/main/java/com/top_logic/knowledge/wrap/person/I18NConstants.java`
- Modify: `com.top_logic/src/test/java/test/com/top_logic/knowledge/wrap/person/TestPersonNameCaseInsensitivity.java`

**Interfaces:**
- Consumes: case-insensitive `Person.byName` (Task 1).
- Produces: `ResKey1 I18NConstants.ERROR_DUPLICATE_ACCOUNT_NAME__NAME`.

- [ ] **Step 1: Write the failing test**

Add to `TestPersonNameCaseInsensitivity`:

```java
public void testCreateRejectsCaseInsensitiveDuplicate() {
    Person admin = createPerson("Admin");
    try {
        Transaction tx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
        try {
            Person.create(kb, "ADMIN", null);
            fail("Creating a case-insensitive duplicate must be rejected.");
        } catch (TopLogicException expected) {
            // Expected.
        } finally {
            tx.rollback();
        }
    } finally {
        deletePerson(admin);
    }
}
```

- [ ] **Step 2: Run it, verify it fails**

Run: `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.knowledge.wrap.person.TestPersonNameCaseInsensitivity#testCreateRejectsCaseInsensitiveDuplicate`
(If method-level selection is unavailable for this suite, run the whole class.)
Expected: FAIL — no exception thrown (a second person is created).

- [ ] **Step 3: Add the error key**

In `com.top_logic.knowledge.wrap.person.I18NConstants` (create the class following `docs/faq/i18n.md` if it does not exist; SPDX header, `extends I18NConstantsBase`, `public static ResKey1 ...`, `static { initConstants(I18NConstants.class); }`):

```java
/**
 * @en An account with the name "{0}" already exists (names are case-insensitive).
 */
public static ResKey1 ERROR_DUPLICATE_ACCOUNT_NAME__NAME;
```

- [ ] **Step 4: Add the check in `Person.create`**

At the top of `create(...)`, before creating the KO:

```java
if (byName(kb, userName) != null) {
    throw new TopLogicException(I18NConstants.ERROR_DUPLICATE_ACCOUNT_NAME__NAME.fill(userName));
}
```

Add imports for `TopLogicException` (`com.top_logic.util.error.TopLogicException`) and the package-local `I18NConstants` if needed.

- [ ] **Step 5: Build (regenerates messages) and run the test**

Run: `mvn -B install -pl com.top_logic 2>&1 | tee com.top_logic/target/build.log | tail -5` (must NOT skip javadoc).
Then: `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.knowledge.wrap.person.TestPersonNameCaseInsensitivity`
Expected: `BUILD SUCCESS` and PASS. Confirm `messages_en.properties` gained the key and add a German text in `messages_de.properties`.

- [ ] **Step 6: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/knowledge/wrap/person/Person.java \
        com.top_logic/src/main/java/com/top_logic/knowledge/wrap/person/I18NConstants.java \
        com.top_logic/src/main/java/META-INF/messages_en.properties \
        com.top_logic/src/main/java/META-INF/messages_de.properties \
        com.top_logic/src/test/java/test/com/top_logic/knowledge/wrap/person/TestPersonNameCaseInsensitivity.java
git commit -m "Ticket #29423: Reject creating an account whose name case-insensitively already exists."
```

---

### Task 3: Case-insensitive LDAP DN resolution

Make LDAP login resolve the DN regardless of case (the sync match is already case-insensitive via Task 1).

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/base/dsa/ldap/LDAPAccessService.java` (`getFullUserDN`, comparison at ~line 554)

- [ ] **Step 1: Read the method**

Read `getFullUserDN(String aName)` around lines 531–560. Locate the comparison `memberName.equals(aName)`.

- [ ] **Step 2: Make the comparison case-insensitive**

Change `memberName.equals(aName)` to `memberName.equalsIgnoreCase(aName)` (guarding for `memberName != null`). Add a brief comment: account names are matched case-insensitively (Ticket #29423).

- [ ] **Step 3: Compile**

Run: `mvn -B install -pl com.top_logic 2>&1 | tee com.top_logic/target/build.log | tail -5`
Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Verify**

There is no LDAP test harness in this module; verify by inspection that `getFullUserDN` now matches case-insensitively, and note in the commit that LDAP login was verified manually (or defer to an integration environment). No automated test.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/base/dsa/ldap/LDAPAccessService.java
git commit -m "Ticket #29423: Resolve LDAP user DN case-insensitively so login works regardless of case."
```

---

### Task 4: Pure rename-plan helper (case-preserving)

Decide which case-only-colliding accounts to rename, without lowercasing. Pure and DB-independent (H2 cannot seed collisions).

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/_29423/PersonNameCollisions.java`
- Create: `com.top_logic/src/test/java/test/com/top_logic/knowledge/service/migration/_29423/TestPersonNameCollisions.java`

**Interfaces:**
- Produces:
  - `PersonNameCollisions.Account` — `(TLID id, String name, long ordering, boolean externallyManaged)`.
  - `PersonNameCollisions.Rename` — `(TLID id, String oldName, String newName)`.
  - `static List<Rename> PersonNameCollisions.computeRenames(List<Account> accounts)` — one `Rename` per account that must be renamed (keepers never appear). Deterministic; idempotent (no collisions ⇒ empty list); preserves the spelling of both keeper and renamed names.

- [ ] **Step 1: Write the failing tests**

Create `TestPersonNameCollisions extends junit.framework.TestCase`. Use `com.top_logic.basic.LongID.valueOf(long)` for ids. Helpers: `account(long id, String name)` → `new Account(LongID.valueOf(id), name, id, false)`; `managed(long id, String name)` → same with `externallyManaged = true`; `index(List<Rename>)` → `Map<Long,String>` of `id.longValue()`→`newName`.

```java
public void testNoCollisionNoRenames() {
    assertTrue(PersonNameCollisions.computeRenames(
        list(account(1, "Alice"), account(2, "bob"))).isEmpty());
}

public void testKeeperKeepsSpellingOthersSuffixed() {
    // No externally managed member -> oldest (id 1) keeps its spelling.
    Map<Long, String> r = index(PersonNameCollisions.computeRenames(
        list(account(1, "Admin"), account(2, "admin"), account(3, "ADMIN"))));
    assertNull("Keeper (id 1) keeps 'Admin'.", r.get(1L));
    assertEquals("admin2", r.get(2L));
    assertEquals("ADMIN2", r.get(3L));
}

public void testExternallyManagedIsKept() {
    // The LDAP-managed account (id 2) keeps its name; the local one is renamed.
    Map<Long, String> r = index(PersonNameCollisions.computeRenames(
        list(account(1, "john"), managed(2, "John"))));
    assertNull("Managed keeper (id 2) keeps 'John'.", r.get(2L));
    assertEquals("john2", r.get(1L));
}

public void testSuffixSkipsTakenNamesCaseInsensitively() {
    // "john2" already exists -> the renamed collision member must become "John3".
    Map<Long, String> r = index(PersonNameCollisions.computeRenames(
        list(account(1, "john"), account(2, "John"), account(3, "john2"))));
    assertNull(r.get(1L));   // keeper
    assertNull(r.get(3L));   // "john2" is a different name, no collision
    assertEquals("John3", r.get(2L));
}
```

Add `list(...)` = `java.util.Arrays.asList`.

- [ ] **Step 2: Run, verify failure**

Run: `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.knowledge.service.migration._29423.TestPersonNameCollisions`
Expected: FAIL (class does not exist).

- [ ] **Step 3: Implement the helper**

```java
public final class PersonNameCollisions {

    public static final class Account {
        private final TLID _id;
        private final String _name;
        private final long _ordering;
        private final boolean _externallyManaged;

        public Account(TLID id, String name, long ordering, boolean externallyManaged) {
            _id = id;
            _name = name;
            _ordering = ordering;
            _externallyManaged = externallyManaged;
        }

        public TLID getId() { return _id; }
        public String getName() { return _name; }
        public long getOrdering() { return _ordering; }
        public boolean isExternallyManaged() { return _externallyManaged; }
    }

    public static final class Rename {
        private final TLID _id;
        private final String _oldName;
        private final String _newName;

        public Rename(TLID id, String oldName, String newName) {
            _id = id;
            _oldName = oldName;
            _newName = newName;
        }

        public TLID getId() { return _id; }
        public String getOldName() { return _oldName; }
        public String getNewName() { return _newName; }
    }

    /**
     * Computes the renames that make all account names unique case-insensitively while keeping
     * their original spelling. Within a group of case-insensitively equal names the keeper is the
     * externally managed account (else the one with the smallest {@link Account#getOrdering()});
     * the keeper is never renamed. Each other member keeps its own spelling plus the smallest
     * integer suffix that is free (compared case-insensitively).
     */
    public static List<Rename> computeRenames(List<Account> accounts) {
        Map<String, List<Account>> groups = new LinkedHashMap<>();
        for (Account account : accounts) {
            groups.computeIfAbsent(lower(account.getName()), x -> new ArrayList<>()).add(account);
        }

        Set<String> used = new HashSet<>(groups.keySet()); // lower-cased names already claimed

        List<Rename> renames = new ArrayList<>();
        for (List<Account> group : groups.values()) {
            if (group.size() == 1) {
                continue;
            }
            Account keeper = chooseKeeper(group);
            for (Account account : group) {
                if (account == keeper) {
                    continue;
                }
                String target = nextFreeName(account.getName(), used);
                used.add(lower(target));
                renames.add(new Rename(account.getId(), account.getName(), target));
            }
        }
        return renames;
    }

    private static Account chooseKeeper(List<Account> group) {
        Account managed = null;
        Account oldest = null;
        for (Account account : group) {
            if (account.isExternallyManaged()
                    && (managed == null || account.getOrdering() < managed.getOrdering())) {
                managed = account;
            }
            if (oldest == null || account.getOrdering() < oldest.getOrdering()) {
                oldest = account;
            }
        }
        return managed != null ? managed : oldest;
    }

    private static String nextFreeName(String base, Set<String> used) {
        int suffix = 2;
        String candidate = base + suffix;
        while (used.contains(lower(candidate))) {
            suffix++;
            candidate = base + suffix;
        }
        return candidate;
    }

    private static String lower(String s) {
        return s.toLowerCase(java.util.Locale.ROOT);
    }

    private PersonNameCollisions() {
        // Utility class.
    }
}
```

Imports: `com.top_logic.basic.TLID` and `java.util.*`.

- [ ] **Step 4: Run, verify pass**

Run: `mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.knowledge.service.migration._29423.TestPersonNameCollisions`
Expected: PASS (all four cases).

- [ ] **Step 5: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/_29423/PersonNameCollisions.java \
        com.top_logic/src/test/java/test/com/top_logic/knowledge/service/migration/_29423/TestPersonNameCollisions.java
git commit -m "Ticket #29423: Add case-insensitive account-name collision resolver (rename plan)."
```

---

### Task 5: Migration processor + registration

Read the `PERSON` table, apply the renames from Task 4, log them, and register the migration.

**Files:**
- Create: `com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/_29423/ResolvePersonNameCollisionsProcessor.java`
- Create: `com.top_logic/src/main/webapp/WEB-INF/kbase/migration/tl/Ticket_29423_resolve_person_name_collisions.migration.xml`
- Reference (read first): `com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/_27517/Ticket27517UpdatePersonTable.java`

**Interfaces:**
- Consumes: `PersonNameCollisions` (Task 4), `SQLProcessor`, `SQLFactory`.

- [ ] **Step 1: Read the precedent**

Read `Ticket27517UpdatePersonTable.java`: how it resolves the `PERSON` table + columns, iterates current-revision rows (branch / `BasicTypes.IDENTIFIER_DB_NAME` / `REV_MAX_DB_NAME`), and executes updates.

- [ ] **Step 2: Implement the processor**

Resolve names from `context.getPersistentRepository()` (do not hard-code): `MOClass person = (MOClass) repo.getMetaObject(Person.OBJECT_NAME)`; table `person.getDBMapping().getDBName()`; name column `person.getAttribute(AbstractWrapper.NAME_ATTRIBUTE).getDbMapping()[0].getDBName()`; id column `BasicTypes.IDENTIFIER_DB_NAME`; also read the `authDeviceID` column (`Person.AUTHENTICATION_DEVICE_ID`, resolve its DB name the same way). Select current-revision rows (mirror the `REV_MAX_DB_NAME` filter from the precedent), build `PersonNameCollisions.Account` per row (id as both id and ordering; `externallyManaged` = the row's `authDeviceID` names a data-access device — resolve via `TLSecurityDeviceManager` if available at migration time, else `false`), call `PersonNameCollisions.computeRenames(...)`, and for each rename execute an `UPDATE` and log it. Skeleton:

```java
/**
 * {@link MigrationProcessor} that renames accounts whose names collide case-insensitively so at
 * most one account per case-insensitive name remains (Ticket #29423). Spellings are preserved.
 */
public class ResolvePersonNameCollisionsProcessor
        extends AbstractConfiguredInstance<ResolvePersonNameCollisionsProcessor.Config<?>>
        implements MigrationProcessor {

    /** Configuration options for {@link ResolvePersonNameCollisionsProcessor}. */
    @TagName("resolve-person-name-collisions")
    public interface Config<I extends ResolvePersonNameCollisionsProcessor> extends PolymorphicConfiguration<I> {
        // No options.
    }

    @CalledByReflection
    public ResolvePersonNameCollisionsProcessor(InstantiationContext context, Config<?> config) {
        super(context, config);
    }

    @Override
    public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
        // 1. Resolve PERSON table + NAME/ID/authDeviceID column DB names from getPersistentRepository().
        // 2. SELECT id, name, authDeviceID for current-revision rows (mirror Ticket27517UpdatePersonTable).
        // 3. Build List<PersonNameCollisions.Account> (row id as id AND ordering).
        // 4. renames = PersonNameCollisions.computeRenames(accounts).
        // 5. For each rename: SQLProcessor.execute(update(table(PERSON),
        //        eqSQL(column(idCol), literal(DBType.ID, id)),
        //        columnNames(nameCol), expressions(literal(DBType.STRING, newName))));
        //    log.info("Renaming account '" + r.getOldName() + "' to '" + r.getNewName() + "'.").
        // 6. If renames is empty, log.info("No case-insensitive account-name collisions found.").
    }
}
```

Fill the numbered steps with concrete `SQLFactory`/`SQLProcessor` calls adapted from the precedent (branch/revision column handling exactly as there).

- [ ] **Step 3: Register the migration**

Create the migration XML; set the dependency to the current `tl` tip — check with `ls -t com.top_logic/src/main/webapp/WEB-INF/kbase/migration/tl/*.migration.xml | head -1` and confirm nothing depends on it (at plan time: `Ticket_29221_Remove_security_domain`).

```xml
<?xml version="1.0" encoding="utf-8" ?>

<migration config:interface="com.top_logic.knowledge.service.migration.MigrationConfig"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<version name="Ticket_29423_resolve_person_name_collisions"
		module="tl"
	/>
	<dependencies>
		<dependency name="Ticket_29221_Remove_security_domain"
			module="tl"
		/>
	</dependencies>
	<processors>
		<processor class="com.top_logic.knowledge.service.migration._29423.ResolvePersonNameCollisionsProcessor" />
	</processors>
	<post-processors/>
</migration>
```

- [ ] **Step 4: Compile**

Run: `mvn -B install -pl com.top_logic 2>&1 | tee com.top_logic/target/build.log | tail -5`
Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Verify wiring**

Confirm the migration is discovered (version chained to the `tl` tip) and the processor class name matches the XML. The rename logic itself is covered by Task 4's unit tests (H2 cannot seed collisions). Re-run Task 4's tests to confirm nothing regressed:
`mvn -B test -DskipTests=false -pl com.top_logic -Dtest=test.com.top_logic.knowledge.service.migration._29423.TestPersonNameCollisions` → PASS.

- [ ] **Step 6: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/knowledge/service/migration/_29423/ResolvePersonNameCollisionsProcessor.java \
        com.top_logic/src/main/webapp/WEB-INF/kbase/migration/tl/Ticket_29423_resolve_person_name_collisions.migration.xml
git commit -m "Ticket #29423: Add migration renaming case-insensitively colliding account names."
```

---

## Self-Review

**Spec coverage:**
- Found regardless of case (lookup/login/reset) → Task 1 (all route through `byName`). ✓
- Original spelling preserved → Task 1 (no store normalization); asserted in the Task 1 test. ✓
- CI uniqueness on create (decision A) → Task 2. ✓
- LDAP sync no churn → Task 1 (CI `byName`); LDAP login CI → Task 3. ✓
- Migration renames collisions, keeper preserved/others suffixed, spelling kept → Task 4 (logic) + Task 5 (apply). ✓
- No `binary`/DDL change → confirmed (migration only rewrites colliding row values). ✓
- Out of scope (trim, char rules, self-provisioning) → absent. ✓

**Placeholder scan:** Task 5 Step 2 leaves the versioned-`UPDATE` body as numbered instructions bound to the `Ticket27517UpdatePersonTable` precedent (branch/revision columns must be mirrored, not guessed); every other step has full code.

**Type consistency:** `ItemByNameCache.lookup(K)` + 5-arg constructor; `Account(TLID,String,long,boolean)`, `Rename(TLID,String,String)`, `computeRenames(List<Account>):List<Rename>`, `ERROR_DUPLICATE_ACCOUNT_NAME__NAME` used consistently across tasks.

## Notes / risks
- `ItemByNameCache`'s clash detection with lowercased keys turns a residual collision into a fail-fast `IllegalStateException` at cache build — the migration (Task 5) must run before the cache is first built (it does: migrations run at boot).
- The rename branch cannot be integration-tested on H2 (CI collation prevents seeding collisions); Task 4's unit tests are authoritative. Consider a manual check on PostgreSQL/Oracle before release.
- Confirm no other `ItemByNameCache` users are affected — the 4-arg constructor keeps identity behavior, so they are not.
- Audit for direct `getObjectByAttribute("Person", "name", …)` callers outside `Person.byName` (exploration found none).
