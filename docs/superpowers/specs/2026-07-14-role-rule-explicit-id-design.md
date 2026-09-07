# RoleRule: explicit configured `id` instead of a computed id

**Date:** 2026-07-14
**Ticket:** #29221 (TLExpression support for role inheritance rules)

## Problem

A `RoleRule` (role inheritance / role computation rule) needs a stable, unique
`id`. Today that id does not exist in the configuration — it is **computed at
runtime** by `RoleRule.computeId(...)` by concatenating the meta-element name,
source meta-element, `inherit` flag, role / source-role names, each path
element's contribution (`PathElement.appendId`), and the `Type` ordinal.

This is fragile:

- **Expression-based path steps have no stable name.** `PathByExpression`
  (`<script-step>`, the #29221 work) has to fold an `exprHash:<sha1>` fragment
  into the computed id, which is opaque and changes whenever the expression is
  edited.
- The id is derived from the rule's content, so any content change silently
  changes the rule's identity.

## Goal

- The `id` becomes an **explicit, mandatory** property of `RoleRuleConfig`.
- The `id` is the **key** of `RoleRulesConfig#rules`, giving map-like lookup and
  parse-time uniqueness enforcement.
- When a new rule is created **in a UI form**, the `id` field is **prefilled
  with a generated UUID** that the user can keep or override. Loading from XML
  still requires an explicit `id` and never regenerates it.

## Non-goals

- No data migration. The persistency numbers assigned in
  `ElementAccessManager` are recomputed on the next security update, so no
  stored data needs migrating.
- The label of a rule stays as-is (`RoleRuleResourceProvider` keeps using
  `getId()`).
- Building a full rule-editing UI. `RoleRulesComponent` is currently a
  non-functional stub (empty `FormContext`, not wired into any layout). Rules
  are configured through the `ElementAccessManager` service configuration and
  rendered via the generic TypedConfiguration declarative form. The
  UUID-prefill hooks into that generic creation path; no new editor component
  is built here.

## Design

### 1. Config — `RoleRuleConfig`

`com.top_logic.element/.../boundsec/manager/rule/config/RoleRuleConfig.java`

Add:

```java
/** Configuration name of {@link #getId()}. */
String ID = "id";

/**
 * Stable unique identifier of this rule within its {@link RoleRulesConfig}.
 */
@Name(ID)
@Mandatory
@ValueInitializer(UUIDInitializer.class)
String getId();

void setId(String id);
```

- `@Mandatory` — XML must specify an `id`; there is **no** fallback to a
  computed id.
- `@ValueInitializer(UUIDInitializer.class)` — orthogonal to `@Mandatory` (the
  value-annotation conflict check in `PropertyDescriptorImpl` explicitly
  excludes `@ValueInitializer`). On declarative-form creation
  (`InitializerUtil.initProperties`, triggered by `ListEditor` when a new keyed
  entry is added), the field is prefilled with `Utils.getRandomID()` and the
  value is persisted; on later XML load the initializer is skipped because the
  value is already set. Precedent: `ColumnProviderConfig#getColumnId()`.
  - `UUIDInitializer` yields TopLogic's standard random id (`ID_xxxx_...`). If a
    plain `java.util.UUID` string is required instead, a tiny
    `PropertyInitializer` returning `UUID.randomUUID().toString()` can replace
    it. Default choice: the standard `UUIDInitializer`.
- Add `ID` as the first entry of the existing `@DisplayOrder` so the id field
  renders first. The id is **not** `@Hidden` (unlike the columnId precedent) —
  it is meaningful and doubles as the label.

### 2. Keyed rules — `RoleRulesConfig`

`com.top_logic.element/.../boundsec/manager/rule/config/RoleRulesConfig.java`

`getRules()` stays a `List<RoleRuleConfig>` (preserving the `<rules><rule/></rules>`
XML shape and ordering) but gains `@Key`:

```java
@Name(RoleRulesConfig.XML_TAG_RULES)
@EntryTag(RoleRulesConfig.XML_TAG_RULE)
@Key(RoleRuleConfig.ID)
@DefaultContainer
List<RoleRuleConfig> getRules();
```

`@Key` enforces id uniqueness at parse time and provides `getRules().get(id)`
map access. The runtime `duplicateKeyId` check in `ElementAccessManager` stays
as a defensive net (rules ultimately become `RoleProvider`s keyed by id there).

### 3. Runtime — `RoleRule`, subclasses, importer

`com.top_logic.element/.../boundsec/manager/rule/`

**Runtime instance id.** One `RoleRuleConfig` expands in
`RoleRulesImporter.createRules(...)` into several `RoleRule` instances — one per
target-role × source-role combination (the `role` / `source-role` properties are
comma-separated). `ElementAccessManager.setRulesInternal` requires every
`RoleProvider.getId()` to be unique (else `duplicateKeyId`). Therefore the
runtime id is composed from the configured id plus the role(s):

```
runtimeId = configId + "_" + role.getName()
            + (sourceRole != null ? "=" + sourceRole.getName() : "")
```

This keeps per-instance uniqueness while dropping the fragile parts of the old
scheme (meta-element / path / type ordinal / `exprHash`). The config `id`
(section 1) is the stable base; role names are stable too.

- Constructors change from computing the id to receiving the composed id:
  - `RoleRule(BoundRole, List<PathElement>, ResKey, String id)` already takes an
    id — keep it.
  - `DefaultRoleRule(...)` and `SingletonRule(...)` gain a `String id` parameter
    (the composed runtime id) instead of calling `computeId(...)`.
- Remove the static `RoleRule.computeId(...)` and `SingletonRule.computeId(...)`.
- Remove `PathElement.appendId(Appendable)` — it is called **only** from the two
  `computeId` methods (verified). Delete it from the interface and all four
  implementations: `GroupWithName`, `IdentityPathElement`, `PathNavigation`,
  `PathByExpression` (also drop `PathByExpression`'s now-unused `exprHash`/SHA
  helpers). `appendForTooltip(...)` is unrelated and stays.
- `RoleRulesImporter.createRules(...)` composes the runtime id from
  `roleRuleConfig.getId()` + the current `role` (and `sourceRole`) and passes it
  to the constructors. A small private helper `composeRuntimeId(configId, role,
  sourceRole)` keeps this DRY.

### 4. Existing XML configurations

Because `id` is strictly `@Mandatory`, **every** existing `<rule>` /
`<singleton-rule>` in the whole repository must get an explicit `id`, or the
owning module/app fails to load. A repo-wide grep for `<role-rules` /
`RoleRulesConfig` finds 14 files (~40 rules), not just test resources.

**id-derivation rules (must be unique within one `RoleRulesConfig`):**

- Where a `resource-key` is present (the test fixtures): use its last segment
  (e.g. `roleRule.minimal` → `minimal`, `roleRule.mainUsers` → `mainUsers`).
- Where no `resource-key` exists (the shipped/app configs): derive from the
  short (unqualified) meta-element name + target role, e.g. meta-element
  `...:Project`, role `Hauptbenutzer` → `Project_Hauptbenutzer`.
- On collision within one file, append a short numeric suffix (`_2`, `_3`).

**All 14 files are edited in this ticket** (engine, tests, and apps):

Shipped / app configs (no `resource-key` → meta-element+role scheme):
- `com.top_logic.element/src/main/webapp/WEB-INF/conf/mandatorStructure.config.xml` (1)
- `com.top_logic.demo/src/main/webapp/WEB-INF/autoconf/DemoSecurity.model.config.xml` (2)
- `com.top_logic.demo/src/main/webapp/WEB-INF/conf/InitialRoleRules.config.xml` (8)
- `com.top_logic.demo/src/main/webapp/WEB-INF/conf/DemoConf.config.xml` (3)
- `com.top_logic.doc.app/src/main/webapp/WEB-INF/conf/tl-doc-app.conf.config.xml` (3)
- `com.top_logic.contact/src/main/webapp/WEB-INF/conf/orgStructureConf.config.xml` (2)
- `com.top_logic.bpe.app/src/main/webapp/WEB-INF/conf/tl-bpe-app.conf.config.xml` (2)
- `com.top_logic.model.search/deploy/local/webapp/WEB-INF/conf/TestScriptPathElement-test.config.xml` (2)

Test fixtures (`resource-key` → last-segment scheme):
- `com.top_logic.model.search/src/test/webapp/WEB-INF/init/InitialTestMERoleRules.xml` (10)
- `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/ValidRoleRules.xml` (4)
- `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/InvalidAttributeRoleRules.xml` (1)
- `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/InvalidMetaElementRoleRules.xml` (1)
- `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/InvalidPathMetaElementRoleRules.xml` (1)
- `com.top_logic.element/src/test/webapp/WEB-INF/xml/roleRules/InvalidRoleRoleRules.xml` (1)

The `Invalid*RoleRules.xml` fixtures deliberately test load failures; give each
a valid `id` so they still fail for their intended reason (unknown attribute /
meta-element / role / path), not for a missing `id`.

### 5. Label

Unchanged. `RoleRuleResourceProvider.getLabel` continues to use
`RoleProvider.getId()` (raw id, or the short persistency id via
`ElementAccessManager`).

## Testing

- `TestRoleRulesImporter` and `TestElementAccessManager`: update XML resources
  with ids; assert rules resolve by the configured id.
- Add a negative test: a `RoleRulesConfig` with two rules sharing one `id`
  fails to parse (`@Key` uniqueness) — or is rejected by the runtime
  `duplicateKeyId` net, whichever fires first.
- Add a negative test: a `<rule>` without `id` fails to load (`@Mandatory`).
- Manual/where feasible: creating a rule through the generic declarative config
  form prefills the id with a generated UUID.

## Open points / risks

- Deleting the `appendId` chain touches `PathElement`, `PathByExpression`, and
  step configs — confirm no non-id caller depends on it.
- The `Invalid*RoleRules.xml` test fixtures need case-by-case review against the
  new validation.
