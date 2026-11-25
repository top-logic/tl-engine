---
description: Read before designing or implementing the object security for historic object versions - the specification of the `history` attribute of role rules and role parent rules (current / historic / anchored), `anchors`, anchor references and anchor steps over historic references, gates, checking historic object versions, and the release example.
order: 24
---

# Specification: object security for historic versions

**Status:** implemented by ticket #30432, with the exceptions listed in section 8. How to configure
it is described in the [access configuration](doc:access-configuration#historic-versions-of-objects).

The requirements are numbered **H1**, **H2**, ... so that tests and tickets can refer to them.
"Must" is binding. The running example (section 7) is at the same time the reference test case.

## 1. Purpose

This specification defines how the object security checks **historic versions** of an object. In
particular, a historic version can be bound through a historic reference to a **current** object
whose security takes part in the decision about the version. A user then sees exactly certain
versions of an object and no other, not even the current one, and this set changes with the
current data. Business example: releases pointing to released versions of a document.

## 2. Starting point

- Roles are attached to the **identity** of an object (`BoundObject#getID`, stored in the
  `SecurityStorage`), not to a revision.
- A historic version `Y@r` is checked with
  - the roles the user holds on Y **today**, from role rules and direct assignments (determined
    from the current data, stored by the identity of Y), and
  - today's roles of the **role parents of Y@r**: the path of the role parent rule is navigated
    from the historic object, that is as of r (`ElementAccessManager#getSecurityParents`), the
    roles of the parents found are read through their identity. A step `<singleton>` always
    delivers the current singleton.
- Consequence: whoever may read an old version through role rules may read all versions, including
  the current one. Whoever holds no role today loses the old versions as well. If an object is
  moved below another role parent, its old versions keep inheriting from the old parent.
- This holds for the way through the `SecurityStorage`. In fallback paths, role rules are navigated
  from the historic object as well, that is as of r (`PathNavigation`): with the rebuild strategy
  `COMPUTE`, for objects marked as invalid during an update, with the automatic update switched
  off, and in a pure `ElementAccessManager` without storage. The default `current` (H5) unifies
  this to the way through the storage.

## 3. Scope

Not subject of this specification:

- **Writing on historic versions.** A historic version is technically immutable; the knowledge
  base ensures that, not the access check. Writing operations checked on a version are decided by
  H4 through the current object.
- **History view, comparison and other ways to a version.** They get no special rule. Every
  version is checked in the same way, no matter how the user reaches it. The precondition is that
  every source of historic versions filters (H36, H37).
- **Grants.** The rights on the types (`<security-config>`) and their evaluation do not change.
- **Persistent storage of roles on historic versions** and the invalidation it would need.
- **Role assignments to a single revision.** Roles have no revision (H1).
- **Historic groups and memberships.** They are always current (H2).
- **The [Discover specification](doc:access-discover-spec).** It applies unchanged, also to
  historic versions.
- **Branches.** The specification applies within one branch: a version, its anchors and all paths
  are evaluated in the branch of the checked version. An anchor step delivers only anchors of the
  same branch, and a value of an anchor reference covers a version only with the same branch, the
  same identity and the same revision. Rights across branch boundaries are not provided, because
  branches play no role in applications.

## 4. Terms

**Historic version `Y@r`:** the object Y with the navigation context r
(`KnowledgeItem#getHistoryContext`). From `Y@r`, every reference is resolved in r: from D@r11 one
reaches S1@r11, not S1@r10. `Y@r10` and `Y@r11` are different versions, even if Y was not changed
in between. There is no notion of "the same version". It could not be determined reliably either:
a change of a reference stored outside the object table changes the object without its row
changing. Conversely, a commit without a change of content still creates a new row.

**Anchor reference:** a historic reference over which a role rule or role parent rule navigates
backwards, or which an `anchored` rule names in `anchors`. There is no declaration of its own (H10).

**Anchor:** a current object pointing to a historic version through an anchor reference.

**Anchor step:** a path step `inverse="true"` over a historic reference (`HISTORIC`). A backward
step over a reference of type `MIXED` is not an anchor step.

**Covered by an anchor:** see H15.

**Gate groups of an anchor:** the groups that may read the value of the anchor reference on the
anchor (H18).

**Anchored for a group (through certain anchor references):** `Y@r` is covered by an anchor
through one of these anchor references, and the group is a gate group of this anchor (H18).

**Contained (groups):** a group g is contained in a group h if g = h or g is a member of h,
directly or through nested groups. The representative group of a person is contained in every
group the person is a member of, directly or nested (H19).

**Gate:** the precondition of a rule with `history="anchored"` for a historic version: `Y@r` must
be anchored through one of the anchor references **of the rule** (attribute `anchors`) (H20). The
rule gives a role only to groups for which path and gate hang on the same group, or on a group and
a group contained in it (H19). The gate is the AND condition of the rule: the path says to whom
the version belongs in business terms, the gate whether it was released to this group.

## 5. Requirements

### 5.1 General

- **H1 Roles have no revision.** A role is only a name. Historic or current is only **from which
  version** it is determined whether a user holds it on an object: from the current data or from
  the data as of a revision r.
- **H2 Today's groups.** Roles are computed for groups. A user holds a role if one of their groups
  of today holds it: their representative group or a group they are a member of, directly or
  nested. The comparison uses the ID of the group. Where this document says that a user holds a
  role or may read something, this is what is meant.
- **H3 Default.** Without new configuration the engine behaves as today. Existing applications
  need not be migrated, with these exceptions (appendix B):
  1. **Anchor step.** A rule with a backward step over a historic reference follows H13 and H22:
     from a current object the anchor step delivers nothing any more, and on historic versions the
     rule is evaluated as of their revision. On current objects the step delivers something today
     only as long as the target is unchanged since the value was set; it is unlikely that an
     application relies on this.
  2. **Writing operations on a historic version** are decided through the current object (H4).
     Today the role parents and the access parent of the historic version count for them; after
     the object was moved, those of the current object will count.
  3. **Fallback paths.** Where role rules are today exceptionally navigated from the historic
     object (section 2), the default `current` will apply uniformly.
  4. **Storage.** Rules with a forward step over a historic reference keep their meaning, but are
     computed during the check (H30.2); the `SecurityStorage` is rebuilt once on update (H30.6).
- **H4 Operations.** The type of the command group (`CommandGroupType`) decides on which version
  the check is made:
  - Type `read` (e.g. `Read`, `Discover`, `Export`, application groups such as `Show`): on `Y@r`
    itself, according to this specification.
  - Type `write` or `delete` (e.g. `Write`, `Create`, `Delete`, application groups with
    `type="write"`): like the same operation on the **current** object Y, with its grants, roles,
    role parents and access parent.
  - If Y is deleted, an operation of type `write` or `delete` on `Y@r` is decided as today: with
    the role parents and the access parent of the version, but without roles from rules with an
    explicit `historic`, with `anchored` or with an anchor step. A command such as "restore
    deleted object" thereby stays possible.

  Commands such as "restore version" or "copy as new document" are checked on the displayed
  version, but write the current object or a new one. They stay possible for everyone who may
  write the current object. Roles given only by a gate or an anchor never give a right of a
  command group of type `write` or `delete`: P gets no `Write` on S1@r11, because P holds no role
  on the current S1. The type is decisive, not the name: the built-in group `restrictedWrite` has
  the type `read` and is decided on the version.

### 5.2 The attribute `history`

- **H5 Values.** Role rules and role parent rules get the attribute `history`. If it is missing,
  today's behaviour of the kind of rule applies (H3): `current` for role rules, `historic` for
  role parent rules.

  | Value | current version | historic version `Y@r` |
  |---|---|---|
  | `current` | path in the current state | path in the **current** state (the role of the identity applies to every version) |
  | `historic` | path in the current state | path **as of r** |
  | `anchored` | **no role** | path as of r, but only with an open gate through the anchor references of the rule (H20) |

- **H6 Effect.** The `history` value of a rule decides only two things: for which groups the rule
  counts on the checked version (with `anchored` the gate, H19) and in which state its own path
  is navigated. Which groups hold a role on the object at the end of the path (inheritance rule,
  role parents) is decided by the rules of that object with their own `history` values. At the
  end of the path of an inheritance rule, as today only the roles from the role rules and direct
  assignments of the target object count, not the roles the target object inherits from its role
  parents. This also holds for rules with an anchor step.
- **H7 Kinds of rules.**

  | Kind of rule | `history` | Meaning |
  |---|---|---|
  | `<rule>` with a path to persons or groups | yes | according to H5 |
  | `<rule>` without a path (the object itself is the role holder) | yes, `anchored` forbidden | the comparison uses the ID, which is the same in every version; `current` and `historic` therefore differ only for a deleted object (H27). `anchored` would take the role from the object in the current version and is an error (H33) |
  | `<inheritance-rule>` without an anchor step | yes | path according to H5; which groups hold the source role at the target is decided by the target's rules (H6) |
  | rule with an anchor step | must not be given | according to H22 |
  | role parent rule (`<security-parents>`) without an anchor step | yes, default `historic` | like an inheritance rule for all roles; in the transitive collection, the `history` value of the rule applying at each chain element holds there. With `anchored` the gate applies to the chain element on which the rule is evaluated, not to the originally checked object |
  | role parent rule with an anchor step | must not be given | according to H22 |
  | `<singleton-rule>` | not provided | behaves like `current` |

- **H8 Path elements.**
  - `<singleton module="…"/>` always delivers the **current** singleton, independent of the
    `history` value, as today (`SingletonPathElement` ignores the start object). Following steps
    read in the current state. Thereby the shipped role parent rules to the security root
    (`<singleton module="SecurityStructure"/>`) keep their behaviour although role parent rules
    have the default `historic`; a singleton that did not yet exist in r would otherwise not be
    there.
  - `<group-with-name name="…"/>` always delivers today's group.
- **H9 Forward step.** A forward step delivers the value of the reference as the start object
  delivers it in its version (`tValue`), and the path continues at the delivered object in that
  object's version. Over a historic reference it therefore leads into the version the reference
  points to, over a reference of type `MIXED` per value into the stored version or into the
  version of the start object. This holds for every `history` value.

### 5.3 Anchor references and anchor step

- **H10 No declaration.** Every historic reference can serve as an anchor reference. There is no
  list of anchor references of its own; which references open a gate is named by the rule itself
  (H20).
- **H11 Storage.** A historic reference over which a role rule or role parent rule navigates
  backwards, or which is named in `anchors`, should be stored such that the backward search is
  possible and runs over an index:
  - in a link table (`hasHistoricValue`, `hasHistoricValueBag` ...). These tables have the generic
    index `dest` on (target ID, attribute) for all references in them; an index per reference is
    not necessary.
  - in a column of its own. For that the engine must create the index for historic references as
    well when `use-default-index` is set; today it is generally omitted for historic references
    in columns of their own (`AbstractMOReference#getIndex`).

  A reference in flex attributes (`HistoricStorage`) cannot be navigated backwards at all;
  `tReferers` throws an exception for it today. A rule using such a reference as anchor would
  silently never grant a role; its configuration is therefore rejected at startup (H33.8). A
  column without an index results in the warning H34.6; every search would then scan
  the whole table.
- **H12 Anchor step.** An anchor step from `Y@r` delivers all **current** objects whose reference
  points exactly to (Y, r). The comparison is for equality of identity and revision, not for an
  interval; for a multi-valued reference each value counts individually. This is today's behaviour
  of `tReferers` for historic references. Verified for the link table, single-valued
  (`SingletonLinkStorage`) and multi-valued (`ListStorage`): from T@7 exactly the objects are
  found that (also) point to T@7, from T@8 none, although T was unchanged between commit 8 and 9;
  an object with the values T@7 and T@9 is found from both versions. For the storage in a column
  of its own this is still to be checked.
- **H13 No anchor step from a current object.** From a current object, today's search delivers the
  objects whose reference points to a version since the last change of the object. For the role
  computation an anchor step from a current object must therefore not be executed; it delivers
  nothing (H22). Otherwise rule E1 would give the roles of R2 and R3 on the current D as long as D
  was not changed since r20.

### 5.4 Covered, anchored, gate

- **H14 Revision of the anchor.** Every value of the anchor reference of an anchor A points to a
  version Z@r1. r1 is arbitrary: if the reference is filled with the current Z, r1 is the revision
  of the commit in which the value is set. But it can also point to an older version. A
  multi-valued anchor reference can point to several objects and also to several versions of the
  same object (D@r11 and D@r15).
- **H15 Covered.** `Y@r2` is covered by A if a value Z@r1 of the anchor reference with r2 = r1
  exists and Y, as of r1, is this Z or belongs to it through composition. "Covered" therefore
  holds per value.
- **H16 Composition.** The composition applies transitively (parts of parts). The composition tree
  as of r1 counts. A part added later is not covered by older anchors; a part moved later stays
  covered for older anchors.
- **H17 Composition only.** Objects reached through normal references are not covered.
- **H18 Anchored and gate groups.** `Y@r` is anchored for a group g through a set M of anchor
  references if an anchor A covers `Y@r` through an anchor reference from M and g is a gate group
  of A for this reference. Gate groups are the groups that the normal check "read the value of
  this attribute on A" allows to read:
  1. **If the type of A decides itself:** the groups holding a role on A that is granted reading.
     If there is an attribute right (`<part>`) for the anchor reference, its roles count,
     otherwise the roles with `Read` on the type of A.
  2. **If the type of A delegates** (`<access-parent>` or composition default): the chain of the
     access parents of A is resolved in the current state. The groups count that hold a relevant
     role on the object E at the end of the chain. With an attribute right on the anchor reference
     these are its roles (as today with `<part>` grants), without an attribute right the roles
     with `Read` on the type of E, because a delegating type has no grants of its own. If the
     chain leads nowhere or runs in a cycle, A has no gate groups.
  3. **`without-security`:** every group is a gate group.
  4. In all cases the normal check also applies in detail: a role also counts if it is granted an
     operation that implies `Read`; on a module singleton its own grants apply; which groups hold
     a role on A or E includes, as in every check, the roles of the role parents. The mark
     `internal` does not change the check.

  `Discover` is not sufficient. Decisive is only the read right on the attribute, not the value
  visible to the group: whether the target version itself is discoverable plays no role for the
  gate (it would otherwise be circular, because the discoverability of the version can depend on
  the gate). Whether a user can *navigate* from the anchor to the version, in contrast, is decided
  as for every reference by the Discover filtering (R4 of the
  [Discover specification](doc:access-discover-spec)). The gate is part of the role computation
  and is checked through roles and grants, not through `isAllowed`. The bypass (administrator,
  `withoutSecurityChecks`) stays unchanged for the decision: `isAllowed` returns `true` there as
  today without any check. It only does not flow into the roles, so that e.g.
  `getRoles(person, Y@r)` delivers the same roles in a bypass context as outside of it.
- **H19 Gate and role from the same group.** For an `anchored` rule on `Y@r`, let
  - the **path groups** be the groups its path delivers as of r: for a reference rule the groups
    at the end of the path (for a person their representative group), for an inheritance rule the
    groups holding the source role at the end of the path, for a role parent rule per role the
    groups holding it at the role parent;
  - the **gate groups** be the gate groups of all anchors that cover `Y@r` through an anchor
    reference of the rule (H18). A single matching anchor is sufficient, independent of why the
    group may read its anchor reference.

  The rule gives the role to
  - every path group contained in a gate group, and
  - every gate group contained in a path group.

  "Contained" means: equal, or a member directly or nested; the representative group of a person
  is contained in every group the person is a member of. Of two groups one of which contains the
  other, the smaller one therefore gets the role (V10, V11). Otherwise nobody gets the role, not
  even a common subgroup or a common member: if neither the path group contains the gate group nor
  vice versa, a user satisfies gate and path only through two different groups, and does not hold
  the role (V7). For a role parent rule this is determined per role.

  Thereby every result of the role computation stays a set of groups: inheritance rules,
  `getGroups`, `getAllowedBusinessObjects` and the explanation of access deliver exact results for
  historic versions as well (H30.7).
- **H20 Gate.** A rule with `anchored` names in the attribute `anchors` the anchor references that
  open its gate (one or more, separated by commas). Its path is evaluated as of r; which groups
  get the role is determined by H19 from path groups and gate groups. For a current version the
  gate is always closed. The gate itself gives no role.

  ```xml
  <rule id="A" meta-element="m:Section" role="PARTNER_READER" history="anchored"
  	anchors="m:Release#snapshot">
  	…
  </rule>
  ```
- **H21 Direction of the check (implementation requirement).** The check whether `Y@r` is covered
  goes bottom-up: over the container chain of Y as of r, at each chain element an anchor step over
  every anchor reference of the rule on (element, r). The basic question is "Which groups are gate
  groups of an anchor that points through reference a exactly to element@r?", with the key
  (element@r, a). The gate groups of a rule for `Y@r` are the union over the chain elements and
  the references in `anchors`.

### 5.5 Passing roles from the anchor into the version

- **H22 Rules with an anchor step.** A rule whose path contains an anchor step always starts in
  the version of the checked object. A `history` attribute must not be given on it. For the path
  the following holds:
  1. Each step is evaluated in the version of its start object; a forward step over a historic
     reference switches into the version it points to (H9).
  2. An anchor step from Z@r' delivers the current objects whose anchor reference points exactly
     to (Z, r') (H12), even if r' was only reached through a forward step. After that the
     navigation continues in the current state.
  3. An anchor step from a current object is not executed and delivers nothing (H13). A second
     anchor step therefore only makes sense if a forward step into a historic version lies in
     between.
  4. On a current object the rule is evaluated like any other; an anchor step delivers something
     there according to item 3 only if it is preceded by a forward step into a historic version.
     E1 and E1' therefore never give a role on the current D. A rule on `Release` with the path
     `snapshot` (forward), then `Release#snapshot` (backward) - "whoever may read a release may
     read all releases of the same version" - in contrast also takes effect on the current
     release.
  5. This also holds for role parent rules. A role parent rule with an anchor step gives the
     version all roles the groups hold on the anchor, like the E rules, but without one rule per
     role.
- **H23 No rule kind of its own.** Roles from the anchor are passed on with a normal inheritance
  rule (`<inheritance-rule>`); which groups hold the source role on the anchor results from the
  current data. An anchor step may stand at any position of the path, also in a rule of type
  `reference`, and the path up to the anchor step may also lead over normal references.

### 5.6 Direct role assignments, access parents, deleted objects

- **H24 Direct role assignments** (`BoundedRole#assignRole`) act like a rule with `current`: the
  assignments of today count, for every version of the object. `anchored` does not restrict them.
  For a historic version the assignments are therefore looked up through the identity of the
  object, not through the historic version itself (see appendix A).
- **H25 No assignment to a historic version.** A historic version is not an admissible context of
  a role assignment. `BoundedRole#assignRole` and the removal of an assignment with a historic
  version as context reject the call as a programming error (`IllegalArgumentException`): roles
  have no revision (H1), and an assignment applies to all versions of the identity (H24). Every
  user interface that assigns roles offers only current objects as context. Whoever wants to give
  a role for all versions assigns it to the current object, whoever wants to give it only for
  certain versions, to the anchor (V4).
- **H26 Access parents** get no `history` attribute. They always navigate in the context of the
  checked object (from S1@r11 to D@r11), the composition default as well. There the rules of the
  access parent decide with their own `history` values. If a computed access parent delivers an
  object in another version than that of the checked object (e.g. the current object), the
  decision is delegated to exactly this object in its version.
- **H27 Deleted objects.** For the historic versions of a deleted object the following holds:
  - `current`: no role (there is no current object any more), as today. This also holds for a
    rule without a path.
  - `historic`: the path is navigated as of r, the role is kept.
  - `anchored`: the role stays as long as an anchor anchors the version for the group. If the
    anchor is deleted, the access through it ends.
  - Rules with an anchor step depend only on the current anchor.

### 5.7 Cycles

- **H28 Meaning.** A group holds a role only if it can be derived without recourse to itself
  (least fixed point). A cycle alone never gives a role.
- **H29 Evaluation.** The evaluation keeps a recursion guard of its own over two kinds of
  questions: "Which groups hold role X on object@revision?" (key: object@revision, role) and the
  basic question of the gate according to H21 (key: element@revision, anchor reference). If a
  question is asked again while it is still open, it counts as "nobody" on this way. A result is
  provisional as long as a question is open on whose assumption it is based; provisional results
  must be stored neither in the `AccessDecisionCache` nor in the memo according to H31, and may be
  reused only according to H40. Once the outermost of these questions is answered, its result is
  final (this is the least fixed point according to H28).
  - The guard of the `AccessDecisionCache` is not sufficient for this: it is keyed by operation,
    stores results from cycles and is bypassed for attribute rights (`<part>`).
  - A cycle is not an error of the configuration and is not logged as an error.

### 5.8 Caching

- **H30 No persistent storage.**
  1. Roles from rules with `historic`, `anchored` or an anchor step are computed during the check
     for historic versions.
  2. A rule whose path contains a forward step over a historic reference (H9) is computed during
     the check on **every** object, also a current one, and is never stored in the
     `SecurityStorage`. Its result depends on a historic version whose roles change with the
     current data (anchors, today's roles); a stored entry could not be kept up to date reliably.
     Example: rule F on the release R1.
  3. **Rules not stored.** A rule is *not stored* if its path contains a forward step over a
     historic reference (item 2), or if, as an inheritance rule or role parent rule, it inherits a
     role from a type U on whose current objects this role is (also) given by a rule not stored.
     This is determined statically from the configuration, transitively over the inheritance and
     role parent edges of the graph from H34.3 (gate edges do not count). Rules with `historic` or
     `anchored` and rules whose anchor step is not preceded by a forward step are not affected:
     for current objects they are stored as before, or deliver nothing there.
  4. A rule not stored is computed during the check on every object as in item 2. Otherwise it
     would never see the inherited role (a stored inheritance rule reads the roles at the target
     from the `SecurityStorage`) and would not be kept up to date on changes.
  5. **`hasRoleOnAnyObject`.** A role is *version-dependent* on a type if a role rule with an
     explicit `historic`, a rule with `anchored` or a rule with an anchor step gives it there, or
     a rule that inherits from such a role on historic versions. A role parent rule with
     `historic` does not count, no matter whether the value is set explicitly or applies as the
     default: whoever holds a role through a role parent also holds it on the parent itself. The
     question whether a user holds a role on any object delivers `true` as soon as one of the
     roles asked for is version-dependent on any type or is given there by a rule not stored. The
     answer is cautious: `ModelAccessPolicy` uses it to hide views and commands and guarantees not
     to hide any operation the user may execute on any object. Without this rule a P user would
     not see the section view although they may read S1@r11. The price is that a view may appear
     that stays empty for the user; the answer applies to the role as a whole, because the
     question names no type (in the example for EDITOR because of rule F). The implementation may
     answer more precisely as long as it never wrongly delivers `false`, for instance by asking
     for the source role for an inheritance rule.
  6. **One-time rebuild on update.** With the implementation the engine ships an automatic
     migration that lets the `SecurityStorage` be rebuilt completely once at the first start (e.g.
     by resetting the stored version of the rule configuration by which the start detects a
     necessary rebuild). Without it, entries of rules would remain that are no longer stored
     according to items 2 and 4 or no longer deliver anything according to H13: the rebuild
     depends today only on a changed rule configuration, and these entries would no longer be kept
     up to date. A former editor would thereby e.g. keep their role from rule F on a release.
  7. **All entry points.** `hasRole`, `getRoles`, `getGroups` and `getAllowedBusinessObjects`
     deliver for every object the union of the stored part (for historic versions according to
     H31.3) and the part computed during the check. `getAllowedBusinessObjects` may decide through
     object IDs from the `SecurityStorage` alone only if no checked object is a historic version
     and none of the roles asked for is version-dependent on the type of a checked object or is
     given by a rule not stored; otherwise the decision is made per object.
- **H31 Caches.**
  1. For these roles, only that may be cached which lives for one interaction, is discarded with a
     newer revision of the knowledge base and has the `ObjectKey` including the revision in its
     key: the `AccessDecisionCache` for decisions and a memo following the same pattern for
     intermediate results (results of anchor steps, gate, role sets per version).
  2. Caches whose key contains only the identity must not be used for this: the caches of the
     cache mode in `RoleRule#getGroupsFor` and `ElementAccessManager#getGroups` as well as
     `PersonRoleCache` in its present form (see item 4).
  3. The security storage (`SecurityStorage`) stores per object ID the roles of all stored rules
     (that is, not of the rules according to H30.3), evaluated for the current version, with the
     generating rule as reason (column `REASON`). For a historic version, only entries may be read
     from it whose reason is a rule with `current` or a direct assignment. An entry of a
     `historic` rule belongs to the current version: Bernd holds EDITOR on the current D through
     EditorsA (`historic`, section 6), that must not make D@r11 readable for him.
  4. A cache for the roles of a person per object (`PersonRoleCache`) is keyed by the `ObjectKey`
     including the revision, not by the identity. For historic versions it contains only the part
     from the `SecurityStorage` according to item 3 (rules with `current`, direct assignments,
     stored roles of the role parents); roles computed during the check belong into the memo
     according to item 1. Today the cache is keyed by the identity although the role parents are
     navigated from the requested version: if D was moved from project P1 to P2, within one
     session the current D gets the roles of P1 if D@r11 was checked first, or vice versa. The new
     key fixes that. The cache is only active if `use-person-role-cache` is set; the shipped
     configuration does not set it.

- **H32 Running transaction.** For historic versions the same holds as today for current objects:
  1. Decisions are stored in the `AccessDecisionCache` per interaction and revision, also during a
     running transaction. A decision made before a change of one's own can remain until the
     commit.
  2. Roles computed during the check (H30) read through the transaction and see its own changes,
     as today the paths of the role parents and access parents. Roles from the `SecurityStorage`
     show the committed state until the commit.
  3. If an anchor reference is filled with a current object in the transaction, the value only
     takes effect after the commit, because only there it is fixed to a version (H14). A value
     pointing to an already existing version (e.g. D@r11) takes effect immediately according to
     item 2.

  The mixture of committed and uncommitted data within a transaction is an existing, general
  behaviour of the access check and not subject of this specification (#30526).

### 5.9 Configuration errors and warnings

- **H33 Errors at startup** (the configuration is rejected):
  1. A rule or role parent rule with an anchor step has a `history` attribute.
  2. A `<singleton-rule>` has a `history` attribute.
  3. `history` has an unknown value.
  4. A rule without a path has `history="anchored"`.
  5. A rule with `history="anchored"` has no attribute `anchors`.
  6. `anchors` names no attribute, no reference, or a reference that is not historic
     (`HistoryType` `CURRENT` or `MIXED`).
  7. A rule without `history="anchored"` has an attribute `anchors`.
  8. An anchor reference (the reference of an anchor step of a role rule or a role parent rule, or
     a reference from `anchors`) is stored such that it cannot be navigated backwards (flex
     attributes, `HistoricStorage`). The rule would never find an anchor (H11).
- **H34 Warnings of the coverage check:**
  1. A rule with `anchored` applies to a type whose objects none of its anchor references can
     cover: the type has no common instances with the target type of the reference (neither equal
     nor specialization or generalization) and is not a composition part of such a type on any
     level either.
  2. (Dropped: an anchor reference that cannot be navigated backwards is the error H33.8.)
  3. Rules form a cycle in which this specification takes part (H28). The basis is a dependency
     graph with the nodes (type, role) and an edge if the role on the one type depends on the role
     on the other:
     - inheritance rule on T with role R and source role Q whose path ends at U:
       (T, R) → (U, Q);
     - role parent rule on T whose path ends at U: (T, R) → (U, R) for every role R;
     - **gate:** `anchored` rule on T with role R: (T, R) → (A, R') for the owner type A of every
       reference in `anchors` and every role R' that is granted reading the anchor reference there
       (if A delegates, the type at the end of the access parent chain).

     A warning is given only for a cycle that contains at least one gate edge or an edge from a
     role rule with an explicitly set `historic`, from a rule with `anchored` or from a rule with
     an anchor step. A role parent rule with `historic` (explicit or as the default) counts as a
     kind of edge of today. Cycles consisting only of today's kinds of edges (e.g. self-referring
     folder or project hierarchies whose data is a tree) are not reported; existing applications
     thereby get no cycle warnings. Neither is a self-referring edge (T, R) → (T, R) of an
     inheritance rule or a role parent rule reported as a cycle of its own, whatever its kind: it
     describes a hierarchy within one type (a level inherits the role from its parent level),
     whose data is a tree. Such an edge still counts as part of a longer cycle, and a gate edge
     leading back to its own node is reported. The message names the cycle with its rules and the hint that a
     cycle never gives a role according to H28.
  4. An anchor step can only start from a current object (it follows an anchor step, `<singleton>`
     or `<group-with-name>` without a forward step over a historic reference in between) and
     therefore never delivers anything (H13, H22.3).
  5. A type has role sources, but all of them are `anchored` or contain an anchor step. They
     switch off the composition default, but never give a role on current objects: the current
     objects of the type are readable for nobody. The remedy is a role source for the current
     version (a rule with `current` or `historic`, a role parent), like rule B' in the example.
  6. An anchor reference (from a role rule, a role parent rule or from `anchors`) is stored in a
     column of its own without an index; the backward search scans the whole table (H11).
  7. A rule is not stored according to H30.3 because it inherits from a rule not stored, and is
     therefore computed during every check. The message names the rule it inherits from, so that
     the cost is visible when configuring.
  8. A backward step over a reference of type `MIXED` stands in a rule that is evaluated on
     historic versions (explicitly `historic` or `anchored`, rule with an anchor step, role parent
     rule with the default `historic`). Such a step is not an anchor step and behaves as today
     (`tReferers` on the start object); what it delivers from a historic version is not guaranteed
     for the security. Remedy: use a historic reference. An error would be too strict, because
     existing role parent rules would otherwise be rejected without new configuration (H3).
  The warnings 7 and 8 can also appear in an existing application if it has such rules already
  today.
- **H35 Coverage check as role source.** The coverage check must recognize rules with an anchor
  step as a role source. Otherwise it reports grants for roles that are given only this way as
  dead.

### 5.10 Sources of historic versions

These requirements belong to the implementation of #30432; without them the security of historic
versions can be circumvented through the affected functions.

- **H36 `inRevision`.** In a secured evaluation, `inRevision` delivers a version `Y@r` only if the
  user may discover it according to this specification (`DiscoverRight`); other versions are
  omitted, as for every object source according to R4 of the
  [Discover specification](doc:access-discover-spec). This applies to every delivered object, also
  to the current version. Unsecured, `inRevision` behaves as before. A reservation for
  administrators is not necessary, because `inRevision` delivers the versions themselves and these
  can be filtered directly.

  Example: if a script in the context of a P user receives the object S1 (e.g. as a section of
  D@r11, reached through R1), `inRevision(S1, 11)` delivers the version S1@r11 and
  `inRevision(S1, 15)` nothing.
- **H37 `changeLog`.** The basis is #30436: secured, `changeLog()` is reserved for administrators;
  for other users it is called privileged and every commit is restricted to the rights of the user
  with `changeSetRestrict` (`ChangeSetRestriction`). There a change is checked against
  `Change#object`, the version Y@r in the revision of the commit; this follows this specification
  without adaptation. In addition, `ChangeSetRestriction` must:
  1. take over an old value (`Modification#oldValue`) only if the attribute is also readable on
     `Change#oldObject`, the version Y@r−1; otherwise it stays empty and the commit is marked as
     incomplete;
  2. take over `Change#oldObject` only if the user may discover it.

  Today this has no effect, because all versions of an object have the same rights. With rights
  per version it is necessary. Example (deviating from the timeline in 7.4): S1 is edited in r11,
  and R1 is created in the same commit. P may read S1@r11, but not S1@r10; without item 1 the old
  value would show the never released content from S1@r10.

  For a deletion, `Change#object` is the last version before the deletion, Y@r−1
  (`ChangeSetAnalyzer`). A deletion is therefore visible to whoever may read this last version.
  For users who see an object only through anchors it is mostly invisible, because the last
  version is rarely anchored. This is intended.

### 5.11 Runtime

Roles on historic versions are recomputed in every interaction (H30): per version the container
chain, per chain element and reference in `anchors` an anchor step (database query) and the groups
with the read right on every anchor found.

- **H38 Reuse within the interaction.** The memo according to H31.1 remembers anchor steps per
  (version, reference) and the gate groups per (element@revision, anchor reference) according to
  H21, not per rule. All `anchored` rules on the same version and all parts of the same container
  use the same results; 500 sections of D@r11 ask D@r11 for its anchors only once.
- **H39 Batching.** If many versions of the same revision are checked (tables, lists), their
  anchor steps are batched. This can be verified by the number of database queries: for a list of
  objects of one type in one revision, the number of queries for anchor steps does not depend on
  the number of objects, but at most on the number of chain levels times the number of anchor
  references.
- **H40 Cycles.** The implementation may avoid repeated computation in cyclic configurations, for
  instance by computing the questions of a cycle together up to the fixed point. H28 stays
  decisive: the result of every question is the least fixed point and does not depend on the order
  in which rules, paths and gate are evaluated (V12). A provisional result may therefore only be
  reused as long as every question is still open on whose assumption it is based; after that it is
  discarded and recomputed if needed.
- **H41 `changeLog`.** The pattern from #30436 stays: call privileged, limit with `maxEntries`,
  restrict afterwards. Every entry concerns another version; there are no savings beyond H38
  there. The documentation of `changeLog` points this out.

## 6. Rationale

**Why `history` per rule:** Whether a former editor may still see the versions from her time
depends on the situation. Two model parts built alike must be able to regulate that differently:

```xml
<!-- Part 1: former editors keep seeing the versions from their time -->
<rule id="EditorsA" meta-element="partA:Document" role="EDITOR" history="historic">
	<step attribute="partA:Document#editors"/>
</rule>

<!-- Part 2: only today's editors see the history, and all of it -->
<rule id="EditorsB" meta-element="partB:Document" role="EDITOR" history="current">
	<step attribute="partB:Document#editors"/>
</rule>
```

| | Part 1 (`historic`) | Part 2 (`current`) |
|---|---|---|
| Anna (editor until r19) sees the version of r11 | yes | no |
| Bernd (editor from r20) sees the version of r11 | no | yes |
| current version | only Bernd | only Bernd |

If rules always navigated in the context (that is, always `historic`), part 2 could not be
expressed.

**Why the default depends on the kind of rule (H5):** Today role rules are evaluated for a
historic version from the current data, whereas role parent rules are navigated from the historic
object. A uniform default would have changed the behaviour for one of the two kinds and forced a
migration. Example: a document is moved from project P1 to P2; its old versions keep inheriting
from P1 today. With `current` as the default for role parents they would inherit from P2.

**Why H4 goes by the type of the command group:** Every command group already carries a type
(`read`, `write`, `delete`), also those of an application. Reading operations concern the version
itself and follow this specification. Writing operations cannot change a version; whoever checks
them on a version means the object that is actually written, mostly the current one. Rejecting
them would have blocked existing commands such as "restore version" without a gain in security
(H3); deciding them through the roles of the version would have given write rights through a gate.

**Why the exact revision (H12, H15):** Whoever navigates through the anchor automatically stays in
r1. The same content in another revision (S1@r10 instead of S1@r11) is deliberately not covered:
whoever reaches such a version on another way is rejected.

Consequence for the user interface: a history view lists the versions in which an object changed
(D@r10, D@r20 ...). An anchored version such as D@r11 is usually not among them, because the
release is created in a revision in which D does not change. A P user therefore finds nothing
through the history of D; they reach released versions through the anchors (the releases). A view
"released versions" therefore lists the anchors, not the history.

**Why composition only (H17):** Referenced objects are independent and have their own security. If
they were included, an anchor could radiate to arbitrary objects. A referenced object can still get
roles from the anchor (H23, variant V2). But if its role is to come from its own data in the
version (`anchored`), it needs a historic reference to the object that the rule names in
`anchors`, or a composition.

**Why `MIXED` is not allowed as an anchor reference (H33.6):** A mixed reference can hold current
values besides historic ones. A current value points to no fixed version and therefore cannot
cover anything; whether a value counts would depend on how it was set. Whoever wants to anchor a
version uses a historic reference.

**Why no declaration of the anchor references of its own (H10, H12):** A backward step over a
historic reference already today finds exactly the current objects pointing to (Y, r). A
declaration that first introduces this search is therefore not necessary. Which references open a
gate is stated at the rule (`anchors`). A list does not help for efficiency: in a generic table an
index for a single reference cannot be created, and the link tables already have it. Decisive is
the kind of storage, and the engine can check that itself (H11).

**Why every rule names its anchor references (H20):** Whoever may set a reference can already
today establish access to its target through an inheritance rule; this is protected by the write
right and by the fact that the rule explicitly names this path. If every historic reference opened
the gate of every `anchored` rule, a reference introduced later could unlock a rule that was never
meant for it. Example: `Comment#about` is a historic reference, and P' users may discover and
comment the current D (deviating from 7.3). A P' user comments the current D in r35, reads their
comment, and rule A would give them S1@r35, that is practically the working version. Because A
names only `m:Release#snapshot`, the gate stays closed. More than the write right on the anchor and
`Discover` on the target is not required for setting an anchor reference, exactly as for every
other reference.

**Creating an anchor means giving read access:** Whoever may create an anchor or set the
references from which the groups at the anchor come (in the example `Release#partner`) thereby
decides which groups read covered versions. This is intended and corresponds to today's behaviour:
whoever may set `Release#partner` makes the release readable for this partner through rule C1
already today. The rights for it (`Create` on the anchor type, `Write` on these references) are
therefore to be given like the right to release versions. The engine deliberately does not
additionally require `Read` on the target version; this is the responsibility of the configuration
of the application (variant V9). The developer documentation points this out for `anchored`.

**Why gate and role from the same group (H19):** The role computation works with groups: rules
deliver groups, inheritance rules ask for groups at the target, the `SecurityStorage` stores
groups. If gate and role could come from arbitrary different groups, the result could no longer be
expressed as a set of groups, and the engine would need a second, user-related way of computing
for historic versions. In business terms the binding is more correct anyway: if Paul belongs to
partner P and Q, the release R1 applies to P, and S3 belongs to Q in r11. Paul does not see
S3@r11, because nothing was ever released for Q.

If one of the two groups contains the other, in contrast, the binding is satisfied: a release to a
sub-team of the partner applies to the sub-team (V10), and a person named individually gets the
role if they belong to the group for which the release was made (V11). A common subgroup of two
otherwise independent groups deliberately does not count: otherwise the result would depend on
whether somebody is a member of P and Q directly or through a subgroup (V7).

**Why every matching anchor is sufficient (H19):** The gate only asks whether the version was
released to the group, not by whom or on which way it may read the anchor.

**Why roles from the anchor through inheritance rules (H23):** Unlike an access parent, the
reference is used backwards (it points from the anchor to the version), and the version does not
hand over the decision completely: its own rules keep taking part in the decision. A rule kind of
its own is not necessary for that.

**Deleted objects (H27):** Not expressible is the mixture "former editors see their versions, but
only as long as the object exists". Whoever releases former versions as an archive usually wants
to keep them after the deletion as well.

**Direct assignments (H24):** If a person is to see explicitly only certain versions, they get the
role on the anchor, and a rule with an anchor step passes it on. An assignment on the object
itself releases every version, including the current one.

**Cycles (H28, H29):** The meaning is unambiguous, because all rules only add: more roles never
lead to fewer rights. A cycle in the configuration is not a configuration error, because it is
harmless as long as the data never closes it. The warning works on (type, role), because two types
can give each other roles without a role depending on itself: in the example Release gives
RELEASE_RECIPIENT to Document (E1) and Document gives EDITOR to Release (F).

**Caching (H30, H31):** The decision about an unchanged version depends on current data (anchor
created or deleted, roles on the anchor). Because every such change is a commit and thereby a new
revision, the `AccessDecisionCache` needs no invalidation of its own. The price is that the roles
are recomputed in every interaction. Within a running transaction a decision can be outdated,
exactly as today for current objects (H32); a special rule only for historic versions would be
inconsistent.

## 7. Example (reference test case)

### 7.1 Model

- `Document`: composition parts `sections: Section[*]`, reference `editors: Person[*]`
- `Section`: reference `partner: Partner`
- `Partner`: reference `group: tl.accounts:Group` (the group of the users of the partner)
- `Release`: historic reference `snapshot: Document`, reference `partner: Partner`,
  reference `recipients: Person[*]`

`Release#snapshot` is stored in a link table or a column of its own with an index (H11).

### 7.2 Role rules

| Rule | Type | Path | Role | `history` |
|---|---|---|---|---|
| A | Section | `partner`, `Partner#group` | PARTNER_READER | `anchored`, `anchors="m:Release#snapshot"` |
| B | Document | `editors` | EDITOR | `current` |
| B' | Section | `Document#sections` backwards, then `editors` | EDITOR | `current` |
| C1 | Release | `partner`, `Partner#group` | RELEASE_PARTNER | `current` |
| C2 | Release | `recipients` | RELEASE_RECIPIENT | `current` |
| E1 | Document | `Release#snapshot` backwards; inherits from RELEASE_RECIPIENT | RELEASE_RECIPIENT | – (anchor step, H22) |
| E1' | Section | `Document#sections` backwards, `Release#snapshot` backwards; inherits from RELEASE_RECIPIENT | RELEASE_RECIPIENT | – (anchor step) |
| E2 | Document | like E1; inherits from RELEASE_PARTNER | RELEASE_PARTNER | – (anchor step) |
| E2' | Section | like E1'; inherits from RELEASE_PARTNER | RELEASE_PARTNER | – (anchor step) |
| F | Release | `snapshot`; inherits from EDITOR | EDITOR | `current` (which editor counts is determined by B) |

```xml
<rule id="A" meta-element="m:Section" role="PARTNER_READER" history="anchored"
	anchors="m:Release#snapshot">
	<step attribute="m:Section#partner"/>
	<step attribute="m:Partner#group"/>
</rule>

<inheritance-rule id="E1" meta-element="m:Document" role="RELEASE_RECIPIENT"
	source-role="RELEASE_RECIPIENT">
	<path>
		<step attribute="m:Release#snapshot" inverse="true"/>
	</path>
</inheritance-rule>

<inheritance-rule id="E1'" meta-element="m:Section" role="RELEASE_RECIPIENT"
	source-role="RELEASE_RECIPIENT">
	<path>
		<step attribute="m:Document#sections" inverse="true"/>
		<step attribute="m:Release#snapshot" inverse="true"/>
	</path>
</inheritance-rule>

<inheritance-rule id="F" meta-element="m:Release" role="EDITOR" source-role="EDITOR">
	<path>
		<step attribute="m:Release#snapshot"/>
	</path>
</inheritance-rule>
```

Explanations:

- A, B, B', C1 and C2 are rules of type `reference`. A path must end at a person or group,
  therefore A and C1 lead over `Partner#group`. The E rules and F are of type `inheritance`; one
  rule is needed per role passed on. The E rules are the only source for RELEASE_RECIPIENT and
  RELEASE_PARTNER on Document and Section (H35).
- That composition parts are covered and only the exact revision counts results for E1' from the
  navigation: from S1@r11, `sections` backwards leads to D@r11, and from there `snapshot`
  backwards finds only anchors pointing exactly to D@r11.
- E2' passes RELEASE_PARTNER on to the sections as well. The role has no right there, so E2' can
  be omitted. It stands here only so that the passing on stays symmetric to E1'.
- F: `snapshot` delivers the historic version, e.g. D@r11 (H9). Whether the user holds EDITOR on
  D@r11 is decided by rule B (H6). B is `current`, so today's editor counts. If B were `historic`,
  the editor at the time of the release would count (Anna for R1, Bernd for R2 and R3). `history`
  on F only determines in which state the path is navigated from the release on. `anchored` would
  be wrong: then nobody would get EDITOR on the current release.
- Historic versions of the releases themselves (e.g. R1@r15): C1, C2 and F are set to `current`,
  so an old version of a release is seen by whoever may read the release today, as today. For F
  the value makes no difference, because `snapshot` points to D@r11 in every version of R1.
- A rule `<rule>` with the path `snapshot` → `editors` would not work: it reads `editors` as of
  r11 and would find Anna, also with `history="current"`, because already the value of `snapshot`
  is a historic object.
- B': with rule A, `Section` has a role rule of its own. That switches off the composition default
  (access parent to the document). Moreover, the composition default only hands the decision
  upwards, no roles downwards. EDITOR on sections must therefore be given explicitly: with rule B'
  or a role parent rule from Section to the document. Without B' the current sections would be
  readable for nobody; the coverage check warns about that (H34.5).
- Expected warnings: for the base example the coverage check reports none of the warnings from
  H34. In particular there is no cycle according to H34.3: E1 gives (Document, RELEASE_RECIPIENT)
  from (Release, RELEASE_RECIPIENT), F gives (Release, EDITOR) from (Document, EDITOR), and the
  gate of A leads from (Section, PARTNER_READER) to the roles with the read right on Release; no
  role depends on itself.

### 7.3 Rights on the types

| Type | Read for | Discover for |
|---|---|---|
| Document | EDITOR, RELEASE_RECIPIENT | RELEASE_PARTNER (*) |
| Section | EDITOR, PARTNER_READER, RELEASE_RECIPIENT | |
| Release | EDITOR, RELEASE_PARTNER, RELEASE_RECIPIENT | |

In addition an attribute right, so that the partner can read the list of sections:

```xml
<part name="m:Document#sections">
	<grant operation="Read" roles="EDITOR, RELEASE_RECIPIENT, RELEASE_PARTNER"/>
</part>
```

An attribute right replaces the right on the type for this attribute, therefore all three roles
are named in it.

(*) The `Discover` grant for RELEASE_PARTNER on Document is strictly speaking superfluous:
according to R2 of the [Discover specification](doc:access-discover-spec), the attribute right on
`sections` already implies `Discover` on the document. It stands here so that the intention is
visible.

RELEASE_PARTNER has two tasks:
1. On the release (rule C1): the partner may read `snapshot`, thereby the version is anchored for
   them (H18).
2. In the covered version (through rule E2): on the document it gives `Discover` and, through the
   attribute right, `Read` on `sections`. The partner can navigate from the release through
   `snapshot` to the document and through `sections` to their sections and sees the label of the
   document, but not its other attributes. `Discover` alone would not make `sections` readable.
   Without the role, already `snapshot` would deliver nothing, because every reference filters
   objects the user may not discover ([Discover specification](doc:access-discover-spec), R4).

On Section, RELEASE_PARTNER has no right. Which sections the partner sees is decided by rule A
alone. The list `sections` of D@r11 therefore contains only S1 and S2 for P users; S3 is removed by
the existing Discover filtering.

### 7.4 Timeline

| Revision | Change |
|---|---|
| r10 | Document D with S1 (partner P), S2 (partner P) and S3 (partner Q). Editor: Anna. |
| r11 | Release R1: `snapshot` → D@r11, `partner` = P |
| r15 | S1 is edited (intermediate version, never released) |
| r20 | S1, S2 are moved to P', S3 to P. Editor: Bernd instead of Anna. |
| r21 | Release R2: `snapshot` → D@r21, `partner` = P' |
| r22 | Release R3: `snapshot` → D@r22, `partner` empty, `recipients` = Erik (external recipient) |
| r30 | D, S1, S2, S3 are edited further (current working version) |

In each step the current version of D is released. The historic reference fixes it in the revision
in which the release is created (H14).

| Section | Partner r10–r19 | Partner from r20 |
|---|---|---|
| S1 | P | P' |
| S2 | P | P' |
| S3 | Q | P |

Covered are (H15, H16):
- by R1 (`snapshot` → D@r11): D@r11, S1@r11, S2@r11, S3@r11
- by R2 (`snapshot` → D@r21): D@r21, S1@r21, S2@r21, S3@r21
- by R3 (`snapshot` → D@r22): D@r22, S1@r22, S2@r22, S3@r22

All other historic versions are covered by no release, e.g. S1@r10 and S1@r15.

### 7.5 Expected result

"yes/no" refers to reading. The rationale names the rule or requirement that decides. "R1
readable" means in the tables: the group is a gate group of R1, so it may read `snapshot` on R1
(H18).

**Users of partner P**

| Object | Result | Rationale |
|---|---|---|
| R1 | yes | rule C1 |
| R2, R3 | no | no role |
| S1@r11, S2@r11 | yes | covered by R1, R1 readable (gate open), rule A in r11 → P |
| S3@r11 | no | rule A in r11 → Q |
| S1@r10, S1@r15 | no | covered by no release (gate closed), although `partner` points to P there |
| S3@r21 | no | rule A in r21 → P, but only covered by R2, which P cannot read |
| S1, S3 current | no | `anchored` gives no role in the current version (H20) |
| D@r11 | only Discover and `sections` | rule E2 inherits RELEASE_PARTNER from R1 |
| `sections` of D@r11 | S1, S2 | S3 is filtered out (no Discover) |
| D in other versions | no | covered by no anchor readable for P |

**Users of partner P'**

| Object | Result | Rationale |
|---|---|---|
| R2 | yes | rule C1 |
| S1@r11 | no | only covered by R1, R1 not readable; moreover rule A delivers P in r11, not P' |
| S1@r21, S2@r21 | yes | covered by R2, R2 readable, rule A in r21 → P' |
| S3@r21 | no | rule A in r21 → P |
| S1@r22 | no | only covered by R3, R3 not readable |
| S1 current | no | `anchored` |
| D@r21 | only Discover and `sections` | rule E2 inherits RELEASE_PARTNER from R2 |

**Users of partner Q**

| Object | Result | Rationale |
|---|---|---|
| S3@r11 | no | rule A in r11 → Q, but no release readable for Q |

**Anna (former editor):** sees nothing, not even a release. B and B' are set to `current`,
currently Bernd is the editor; F asks rule B.

**Bernd (current editor):** sees D and S1 to S3 in every version, including the current one (B,
B'). He sees the releases R1 to R3 as well: F inherits EDITOR from D@r11, D@r21 and D@r22, and
there he is the editor according to B.

**Erik (external recipient of R3)**

| Object | Result | Rationale |
|---|---|---|
| R3 | yes | rule C2 |
| D@r22, S1@r22, S2@r22, S3@r22 | yes | E1 and E1' inherit RELEASE_RECIPIENT from R3 |
| D@r21, S1@r21 | no | only covered by R2, although equal in content |
| D@r11, S1@r15 | no | not covered by R3 |
| D, S1 current | no | E1 and E1' find no anchor from the current version (H22) |

#### A check step by step

Pia is a member of the group G_P that `P.group` points to. Checked is
`isAllowed(Pia, S2@r11, Read)`. How Pia got to S2@r11 plays no role: the check depends only on the
object, its revision and the user.

1. **Bypass.** Pia is not an administrator, so the check is made.
2. **Checked version.** `Read` is a reading command group, so the check is made on S2@r11 and not
   on the current S2 (H4).
3. **Who decides.** `Section` has role rules of its own, so the type decides itself and does not
   delegate to the document. `Read` is held there by EDITOR, PARTNER_READER and RELEASE_RECIPIENT.
4. **Roles on S2@r11.** The four rules on `Section` are evaluated:

   | Rule | Evaluation | Result for Pia |
   |---|---|---|
   | A (`anchored`) | see below | G_P gets PARTNER_READER |
   | B' (`current`) | current S2 → D → `editors` = Bernd | nothing |
   | E1' | S2@r11 → D@r11 → R1; nobody holds RELEASE_RECIPIENT on R1 | nothing |
   | E2' | the same way; the group G_P holds RELEASE_PARTNER on R1 | G_P gets RELEASE_PARTNER, but the role has no right on `Section` |

5. **Decision.** Pia is a member of G_P, G_P holds PARTNER_READER, PARTNER_READER has `Read`. The
   check delivers "yes".

Rule A needs two results for that which must fit together (H19):

- **Path groups.** The path is read as of r11: `S2@r11.partner` is P, `P.group` is G_P. The path
  group is G_P.
- **Gate groups.** The search goes bottom-up over the container chain (H21): first S2@r11, then
  D@r11. At each chain element the search goes backwards over `Release#snapshot` with the exact
  revision. No release points to S2@r11, exactly R1 points to D@r11. Gate groups of R1 are the
  groups that may read `snapshot` on R1 (H18). There is no attribute right for `snapshot`, so the
  roles with `Read` on `Release` count: EDITOR, RELEASE_PARTNER and RELEASE_RECIPIENT. On R1, G_P
  holds the role RELEASE_PARTNER (rule C1, `R1.partner` = P) and Bernd the role EDITOR (rule F).
  The gate groups are G_P and Bernd's representative group.
- **Matching.** The path group G_P is contained in the gate group G_P (the same group). A gives
  PARTNER_READER to G_P.

From this follows:

- The path of A is read in r11, whereas the roles on R1 and Pia's membership in G_P apply in the
  current state (H2). If Pia loses the membership or `R1.partner` is changed, S2@r11 is
  immediately no longer readable for her.
- For S2@r10 or S2@r15 the path would lead to G_P in the same way, but the backward search would
  find no release. The gate would stay closed, Pia would get no role.
- Whether Pia can navigate from R1 through `snapshot` and `sections` to S2@r11 depends on
  RELEASE_PARTNER on D@r11 (rule E2). For the read check on S2@r11 itself this is not necessary.

### 7.6 Variants

Each variant changes the base example only in the point named.

**V1 Deleted section (H27):** S2 is deleted in r35. P keeps seeing S2@r11 through R1, the release
stays complete. If rule A were `current`, S2 would be missing in R1 for P, and also S1@r11, because
S1 belongs to P' today. If R1 is deleted, P sees neither S1@r11 nor S2@r11.

**V2 Referenced object (H17, H23):** D points through the normal reference `Document#glossary` to
a glossary G. G@r22 is not covered by R3, but gets roles from the anchor through a rule with an
anchor step:

```xml
<inheritance-rule id="E3" meta-element="m:Glossary" role="RELEASE_RECIPIENT"
	source-role="RELEASE_RECIPIENT">
	<path>
		<step attribute="m:Document#glossary" inverse="true"/>
		<step attribute="m:Release#snapshot" inverse="true"/>
	</path>
</inheritance-rule>
```

With a read right for RELEASE_RECIPIENT on Glossary, Erik sees G@r22, but no other version of G.
The gate of an `anchored` rule on Glossary, in contrast, is never open (warning H34.1).

**V3 Deeper composition (H16, H26):** Sections have paragraphs (`Section#paragraphs`). A paragraph
of S1@r11 is covered by R1. If the type paragraph has no role rules of its own, it hands the
decision to S1@r11 through the composition default; a rule per level is not necessary.

**V4 Direct assignment (H24):** Erik gets RELEASE_RECIPIENT assigned directly on R3 instead of
through `recipients`: the same result as above. If he gets the role directly on D, he sees every
version of D, including the current one, but no section: Section has rules of its own (A, B', E1',
E2'), the composition default to the document does not apply there.

**V5 Cycle (H28, H29):** Additional rule G ("whoever may read a section in the version may also
read the release"):

```xml
<inheritance-rule id="G" meta-element="m:Release" role="RELEASE_PARTNER" source-role="PARTNER_READER">
	<path>
		<step attribute="m:Release#snapshot"/>
		<step attribute="m:Document#sections"/>
	</path>
</inheritance-rule>
```

Users of Q, checked is S3@r11: the gate of rule A asks whether Q may read R1. Rule C1: no. Rule G
asks for PARTNER_READER on the sections of D@r11 (S1@r11, S2@r11, S3@r11), that is again for rule
A and its gate. Result: no, as without rule G. The coverage check warns (H34.3) with the cycle "A
(gate through `Release#snapshot`) → Release, G → Section". Without the gate edge it would not be
detectable, because A is a reference rule whose path ends at a group.

**V6 Editor at the time of the release (H6, H5):** Rule B is set to `historic` (B' as well). Anna
sees R1 and the versions of D and S1 to S3 up to r19; Bernd sees R2, R3 and the versions from r20
on as well as the current version.

**V7 User in two partner groups (H19):** Paul is a member of P and Q. On S3@r11 the path group of
rule A is Q, the gate group P (R1 is for P). Neither of the two contains the other, so rule A
gives no role: Paul does not see S3@r11. He sees S1@r11 and S2@r11 through P like every P user.
The same holds if instead of Paul a group g is a member of P and of Q: the members of g do not see
S3@r11 either.

**V8 Anchor with access parent (H18):** `Release` is a composition part of a project (`Project`),
has no role rules of its own and no grants of its own and decides through the composition default
at the project. `Project` has `Read` for the role PROJECT_MEMBER, and the group P holds
PROJECT_MEMBER on the project of R1. The gate groups of R1 are the groups with PROJECT_MEMBER on
the project (H18.2), that is P; the gate of rule A to S1@r11 is open for P. The rules C1, C2, F
and the E rules are omitted in this variant. Without resolving the chain no group would hold a
role on R1, and the gate would be closed for everyone.

**V9 Release to another partner:** A P user may create releases and choose the partner freely.
They create R1' with `snapshot` → D@r11 and `partner` = Q. Thereby the gate of rule A to S3@r11 is
open for Q, rule A delivers Q in r11, and Q reads S3@r11. This is intended: whoever may create
releases and set their partner may release. If a partner is to release only for itself, the
application must restrict setting `Release#partner` accordingly.

**V10 Release to a sub-team (H19):** The group g (sub-team) is a member of the partner group P.
Instead of R1 there is the release R1'' with `snapshot` → D@r11 and a partner whose `group` is g;
the gate group of R1'' is g. On S1@r11 the path group of rule A is P. g is contained in P, so g
gets the role: the members of g see S1@r11, the other members of P do not.

**V11 Person named individually (H19):** Additional rule A2 on Section like A, but with the path
`Section#reviewers: Person[*]`. Rita is a reviewer of S3 in r11 and a member of P. On S3@r11 the
path group of A2 is Rita's representative group, the gate group P (R1). Rita's representative
group is contained in P, so Rita gets PARTNER_READER on S3@r11. A reviewer who does not belong to
P does not get the role.

**V12 Cycle with fixed point (H28, H40), independent of the base example:** A project gives MEMBER
through a reference rule (`members`) and additionally inherits MEMBER from its releases. A release
inherits MEMBER from the project and has `Read` for MEMBER. A section has an inheritance rule with
`history="anchored"` and `anchors` on the release, source role MEMBER at the project. The group g
is in `members`. Least fixed point: g holds MEMBER on the project and thereby on the release, so
it is path group and gate group; g gets the role on the covered section. The result must be the
same no matter whether the path or the gate is evaluated first. A "nobody holds MEMBER on the
release" computed provisionally while evaluating the path must not close the gate.

**V13 Release of an area and of a sub-area through ordinary references (H22, H23, H7, H26),
independent of the base example.** The model from the ticket: the path to the anchor runs over an
ordinary reference and over parent sections, not over the composition to the target of the anchor
reference.

Model:
- `Area`: reference `editors: Person[*]`
- `Section`: reference `area: Area`, composition parts `children: Section[*]` (subsections) and
  `parts: Part[*]`
- `Release`: historic reference `snapshot: Area` (release of a whole area), historic reference
  `roots: Section[*]` (release of a sub-area through its root sections), reference
  `recipients: Person[*]`. Both historic references are stored in link tables (H11).

Rules:

| Rule | Kind | Type | Path | Role | `history` |
|---|---|---|---|---|---|
| K1 | reference rule | Release | `recipients` | RELEASE_READER | `current` |
| K2 | inheritance rule | Area | `Release#snapshot` backwards; from RELEASE_READER | READER | – (anchor step) |
| K3 | inheritance rule | Section | `Release#roots` backwards; from RELEASE_READER | READER | – (anchor step) |
| K4 | role parent rule | Section | `area` | all roles of the area | default `historic` |
| K5 | role parent rule | Section | `Section#children` backwards | all roles of the parent section | default `historic` |
| K6 | reference rule | Area | `editors` | EDITOR | `current` |

Rights: `Read` on Area and Section for EDITOR and READER, on Release for RELEASE_READER. `Part`
has no rules of its own and decides through the composition default at its section.

Timeline:

| Revision | Change |
|---|---|
| r10 | Area A with the sections S1 and S2; S1 has the subsection S1.1 and the part T1. Editor of A: Bernd. |
| r11 | Release RA: `snapshot` → A@r11, `recipients` = Erik |
| r12 | Release RT: `roots` → S1@r12, `recipients` = Tina |
| r20 | S1.1 is moved below S2, S1 is deleted |

Expected result (reading):

| Who | Object | Result | Rationale |
|---|---|---|---|
| Erik | A@r11 | yes | K2 from RA |
| Erik | S1@r11, S2@r11, S1.1@r11 | yes | K4: role parent A@r11, READER there according to K2 |
| Erik | T1@r11 | yes | composition default → S1@r11 |
| Erik | A@r12, S1@r12, everything current | no | no release points to r12 for him; K2 and K3 deliver nothing from the current object |
| Tina | S1@r12 | yes | K3 from RT |
| Tina | S1.1@r12, T1@r12 | yes | K5: the parent section in r12 is S1; composition default → S1@r12 |
| Tina | S2@r12, A@r12 | no | K4 leads to A@r12, to which no release points; RT does not name S2 |
| Tina | S1.1@r12 after r20 | yes | In r12, S1.1 belongs to S1; moving it and deleting S1 do not change the version r12 |
| Tina | S1.1 current | no | K3 and the role parents of the current S1.1 (S2, A) give her no role |
| Bernd | all versions of A, S1, S2, S1.1, T1 and everything current | yes | K6 on A (`current`), through K4 and K5 on the sections, also for versions never released |

Explanations:
- Only the anchor step in K2 and K3 crosses the revision boundary. K4 and K5 are navigated as of r
  on a historic version (default `historic` for role parent rules), and which roles the role
  parent holds there is decided by its rules including K2 and K3 (H6, H7).
- Several ways are an OR: a section gets READER through the area (K4), through a parent section
  (K5) or directly (K3). An access parent with several candidates is not needed for that.
- The roles of the identity keep applying: Bernd keeps the view on all versions through K6.
- Without sub-area releases it would be simpler: `Section` gets `Area` as access parent
  (`<access-parent>` over `area`) and no rules of its own; the access parent is navigated in the
  version (H26), and A@r decides with K2 and K6. Rules of its own such as K3 would then have no
  effect, therefore this variant uses role parents.
- Expected warnings: none. K5 does form the edge (Section, R) → (Section, R), but a role parent
  rule with `historic` counts as a kind of edge of today (H34.3).
- A rule with `history="anchored"` on Section and `anchors="m:Release#snapshot"` would have a gate
  here that is never open: sections are not composition parts of the area (H17, warning H34.1).

## 8. Open points

Not implemented yet:

- **`changeLog` (H37, H41):** The restriction of the change log builds on ticket #30436, which is
  not part of the engine yet. The additional checks of old values have to be added together with
  it.
- **Index for historic references in columns of their own (H11):** `AbstractMOReference#getIndex`
  is unchanged, since an index for each existing historic reference changes the database schema of
  every application. A reference used as anchor is to be stored in a link table; the coverage
  check reports a reference in a column of its own (H34.6).
- **Bundling for single checks (H39):** When a list of objects is filtered
  (`getAllowedBusinessObjects`), the first step of each rule navigated in the versions (anchor
  steps included) and the stored roles are requested once for all versions of a level of role
  parents. The number of requests then depends on the depth of the chains of role parents and not
  on the number of objects. A check of a single version is not bundled: it costs one request to
  the security storage per version, as the check of a current object does, and one request per
  navigated reference whose values are not loaded yet.

Checks still to be done:

- **Measuring the runtime (5.11):** measured for 1000 versions of one revision with a chain of role
  parents of depth 10 and one anchor (`TestHistoricSecurityRuntime`); the example with many
  sections and many releases is not measured yet.
- **H12 for columns of their own:** the exact search is verified only for the link table.
- **Further sources of historic versions:** whether there are further functions besides
  `inRevision` and `changeLog` that deliver historic versions (H36, H37).

Decided during the implementation:

- **Anchor reference in flex attributes (H33.8):** an error at startup instead of a warning, since
  a rule that silently never grants a role is hard to notice.
- **Self-referring edges (H34.3):** an inheritance rule or role parent rule leading from a type and
  role to the same type and role is not reported as a cycle.
- **Rebuild of the security storage (H30.6):** The stored version of the rule configuration
  contains a version of the storage content defined by the engine. The first startup with the new
  engine therefore detects a changed configuration and rebuilds the storage; no migration file is
  needed.
- **Discover attributes and the gate (H18):** A gate group needs the read right on the anchor
  reference by a grant on the attribute or on the type. That the reference is declared as discover
  attribute does not open the gate.

## Appendix A: Notes on the implementation

- **Branching the check:** Roles from rules with `historic`, `anchored` or an anchor step are
  computed during the check for historic objects; from the `SecurityStorage` only entries of
  `current` rules and direct assignments come for them (H30, H31). Rules with a forward step over
  a historic reference are not written into the `SecurityStorage` at all and are computed during
  the check on current objects as well (H30.2).
- **Rules not stored and version-dependent roles (H30.3–H30.5):** determine once when loading the
  rules; from this follow the rules that are not written into the `SecurityStorage`, and the
  answer of `StorageAccessManager#internalHasRoleOnAnyObject`.
- **Entry points (H30.7):** `RoleComputation#findAllowedBusinessObjects` today decides through
  object IDs from the `SecurityStorage` alone; for historic versions and for version-dependent
  roles or roles from rules not stored, the decision must be made per object.
- **Groups:** The role computation stays group based. An `anchored` rule compares every path group
  with every gate group and gives the role to the smaller of the two if one contains the other
  (H19). This needs the membership between groups and between a person and their groups.
- **Anchor step without backward navigation (H11):** `PathNavigation` calls `tReferers` unguarded
  today; for a reference without `ReferenceStorage` the step must deliver the empty set.
- **Anchor step:** The search already exists (`tReferers` through `ReferenceStorage#getReferers`,
  H12). New is only that it is not executed from a current object (H13).
- **Index:** `AbstractMOReference#getIndex` creates the index for historic references in columns
  of their own if `use-default-index` is set (H11). The link tables already have it.
- **Navigating role rules in the historic version**, matching groups through their ID (H2, H5).
- **Cycles:** a recursion guard of its own according to H29 with the mark "provisional" and the
  set of open questions a provisional result is based on (H40). The mark "is being computed" of
  the `AccessDecisionCache` stays for access parents, but does not carry H29.
- **Configuration:** attributes `history` and `anchors` on role rules and role parent rules
  (`ElementAccessManager.Config`, H5, H20), checks according to H33.
- **Sources of historic versions:** `inRevision` filters by `DiscoverRight` when secured (H36).
  `ChangeSetRestriction` (#30436) checks old values against `Change#oldObject` and filters
  `Change#oldObject` itself (H37).
- **Direct assignments on historic versions (H24):** `RoleAssignmentMemo#covers` excludes historic
  objects; the fallback `BoundedRole#queryAssignedRoles` queries with the historic handle
  (`BoundedRole.java:695-704`). Whether that finds today's assignments is unchecked. A test must
  clarify that; the lookup goes through the identity of the object in the current state.
- **Documentation of `anchored`:** a note that the right to create an anchor or to set the
  references from which the groups at the anchor come at the same time gives read access to
  covered versions (rationale "Creating an anchor means giving read access", V9).
- **Coverage check and documentation ([access configuration](doc:access-configuration)):**
  `history`, `anchors`, warnings according to H34, role sources according to H35. The editor of
  the security definition reads and edits `history` and `anchors`.

## Appendix B: Migration

For the ticket with the keyword `RequiresMigration`:

1. **Backward step over a historic reference.** In a role rule or role parent rule, a step with
   `inverse="true"` over a reference with `history-type="historic"` delivers nothing any more from
   a current object; on historic versions the rule is evaluated as of their revision. Affected
   rules are found in the `<role-rules>` and `<security-parents>` of the application. Whoever
   needs the previous behaviour must lead the rule over a reference of type `current`; this
   usually requires an additional reference in the model and a data migration.
2. **Writing commands on historic versions.** Commands whose command group has the type `write` or
   `delete` and that are checked on a historic version are decided through the current object. If
   the object was moved below another role parent or access parent since the version, today's one
   counts.
3. **Fallback paths.** With the rebuild strategy `COMPUTE`, with the automatic update switched off
   and in an `ElementAccessManager` without storage, role rules for historic versions are no
   longer navigated from the historic object, but from the current data as on the way through the
   storage. Whoever wants the old behaviour sets `history="historic"` on the rule.
4. **Warnings.** The coverage check can report new warnings (H34.7, H34.8).

The security storage is automatically rebuilt once at the first start after the update (H30.6).
The first start therefore takes longer; a manual step is not necessary.
