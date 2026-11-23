---
description: Read before configuring or checking who may read, write, create or delete objects of a model type - grants in <security-config> (SecurityConfigurationService), attributes readable with Discover (discover-attributes), operation declarations (<operations>, implied-by), the internal / without-security marks, access parents (also restricted to operations, `operations` of an access parent; several access parents with <any-of>, <referer>, a multi-valued <target> or a <script> yielding several objects; a custom AccessParentDefinition combining access parents), role rules of the AccessManager, the autoconf files and the SecurityCoverageCheck.
order: 20
---

# Model based access configuration

How TopLogic decides whether a user may read, write, create or delete an object of a model type,
where that is configured, and how to check that the configuration is complete.

## The access check in one sentence

A user may perform an operation on an object if the user **holds** a role on that object that is
**granted** the operation on the object's type. Two independent definitions feed this check:

| Question | Answered by | Configured in |
|---|---|---|
| Which roles is the operation *granted* on the **type**? | grants (access rights) | `SecurityConfigurationService` |
| Which roles does the user *hold* on the **object**? | role sources: role rules, role parent rules, direct role assignments | `AccessManager` (`ElementAccessManager`) |

Both must be satisfied. A role parent inherits **roles**, never grants: a type with a role parent
rule but no grant is unreadable for every user except a technical admin, however many roles the
user inherits from the container.

A type that needs neither has an **access parent** instead: its objects delegate the decision,
grants and roles, to another object, or to several, any of which may allow the operation - the
*whole* decision, unless the access parent is restricted to some operations, see
[Access parent](#access-parent-delegating-the-decision). A composition part gets its container as
access parent by default, so the rows of a composition table, the comments of a ticket or the
attachments of a comment need no definition of their own. See
[Access parent](#access-parent-delegating-the-decision).

The check itself is `SecurityConfigurationService.isAllowed(Person, TLObject, BoundCommandGroup)`.
It applies to every persistent object, whatever its implementation class; a transient object (a
draft of a create dialog, a form object editing a persistent object) is not checked. Every
persistent wrapper can hold roles (`AbstractWrapper` is a `BoundObject`); a persistent object that
cannot (a plain knowledge item standing for itself) grants no role, so only the bypasses, the mark
`without-security` and an access parent give access to it.


1. Bypasses: code running with bypassed security checks (`ThreadContext.isSecurityBypassed()`), a
   system command group, and an administrator (`ThreadContext.isAdmin()`, the one check whether the
   current context has administrative rights: the person of the current context is an administrator)
   are allowed; a restricted user is denied operations outside the restricted set. The security checks are bypassed in a
   system context (`ThreadContext.inSystemContext(caller, job)`, no person) and within
   `ThreadContext.withoutSecurityChecks(job)`, which keeps the person of the current context, e.g. as
   author of the changes the job commits. `TLContextManager.inPersonContext(person, job)` runs a job
   for the given person with checked security; without a person, every access is denied. An object that is not yet committed is not checked (its
   roles are computed at commit time).
2. The decision for the person, object and operation is answered from the memo of the current
   interaction, if it was made before (see [Caching](#caching-of-decisions)).
3. A type marked `without-security` is not checked at all.
4. A type with an access parent asks the object the relation leads to, with the same operation, or
   with `Write` in place of `Create` and `Delete`; an operation
   [implying](#operations-which-operations-apply-to-a-type) the requested one on the type is asked
   as well. A relation leading to several objects (see
   [Several access parents](#several-access-parents-any-of-referer)) asks each of them until one
   allows the operation. With `operations` on the access parent, only the delegated operations
   (and delegated implying ones) are asked; if the parent does not allow them, the decision
   continues at step 5 with the roles on the object itself, as for the operations not delegated.
   The parent may delegate further. An object whose relation leads nowhere (no container, an empty
   reference) is denied the delegated operations (without `operations`: every operation), unless
   the own grants of the type imply them. The same holds where the chain of access parents runs in
   a cycle: the check that reaches itself again is denied, so an unrestricted type is denied
   altogether, while a restricted one is still decided by its own roles; another access parent of
   the object may still allow the operation.
5. The roles granted the operation, or an operation implying it, on the object's type are looked
   up (for a module singleton, the
   singleton's own grants take precedence; `Discover` on such a singleton is implied by its own
   grants and by the rights on the attributes and compositions of its type).
6. `AccessManager.hasRole(person, object, roles)` decides whether the user holds one of them on the
   object.
7. For an attribute value: the attribute rights in effect for the object's type and the attribute's
   name are looked up (see [Attribute rights](#attribute-rights-which-types-a-part-applies-to)). If
   they grant the operation, they **replace** the class grant for this attribute - the user must
   hold one of those roles, on the object or, for a delegating object, on the end of one of its
   chains of access parents (for an access parent restricted to operations: the chain is followed
   only while the requested operation is delegated); a grant without roles denies everyone. Without
   attribute-level grants the class-level decision stands.

Model elements (types, attributes, modules, the classifiers of an enumeration, all objects of
`tl.model`) are meta-data, not business objects: every user may discover and read them and the
values of their attributes, without a grant on `tl.model`, so a reflective script (the target type
of a reference, whether an attribute is multiple or composite) works for every user. This rule is applied
before the steps above; a deleted model element stays inaccessible. Modifying, creating and deleting a model element is checked like for any other
object, so no application needs a definition for `tl.model`.

`Discover` - every right on an object or one of its attributes implies the operation `Discover`, see [the Discover specification](doc:access-discover-spec).
How code and TL-Script ask for a decision (`ModelAccessRights`, `canRead`, ...) and which objects
are exempt from the check is described in [checking access in Java and TL-Script](doc:access-checks).
The classic layout UI does not check attribute rights; an application using model based security
with `Discover`-only roles must use the React view layer for such types.
On which object a view of the classic layout UI checks access (`securityObject`, `isSecurityMaster`)
is described in [access checks of layouts](doc:access-layouts).

## Grants: which roles may do what on a type

Grants live in the `SecurityConfigurationService` configuration (`<security-config>`). Each entry
names a model element and lists rules; the rules of all configuration layers (framework, modules,
application, in-app edits) are **appended**, so an application extends the framework's rights
instead of replacing them.

```xml
<config service-class="com.top_logic.model.security.SecurityConfigurationService">
	<instance>
		<security-config>
			<!-- An operation of the application, declared on the type it applies to. -->
			<class name="tl.demo.projectManagement:ProjectScope">
				<operations>
					<operation name="Show" implied-by="Read"/>
					<operation name="Approve"/>
				</operations>
				<grant operation="Approve" roles="demo.react.ProjectMember"/>
			</class>
			<!-- Grants on one class. -->
			<class name="tl.accounts:Person">
				<grant inherit="true" operation="Read" roles="PersonalProfile"/>
				<grant inherit="true" operation="Write" roles="PersonalProfile"/>
			</class>
			<!-- Baseline for every class of a module. -->
			<module name="tl.demo.projectManagement">
				<grant inherit="true" operation="Read" roles="demo.react.ProjectMember, demo.react.ProjectAdmin"/>
			</module>
			<!-- Grants on a module singleton, taking precedence over the class grants. -->
			<singleton name="SecurityStructure#ROOT">
				<grant operation="Read" roles="Viewer"/>
			</singleton>
			<!-- Grants on the values of one attribute, replace the class grants for this attribute. -->
			<part name="tl.accounts:Person#password">
				<grant operation="Read" roles="Administrator"/>
			</part>
			<!-- Marks, see below. -->
			<class name="my.module:Cache" internal="true"/>
			<class name="my.module:PublicNotice" without-security="true"/>
			<!-- Access parents, see below: the container through any composition, the container
			     through one composition, the target of a reference, a computed parent, and no
			     delegation. Several access parents (<any-of>, <referer>) are shown further below. -->
			<class name="demo.tickets:Attachment">
				<access-parent><container/></access-parent>
			</class>
			<class name="demo.tickets:Comment">
				<access-parent><container reference="demo.tickets:Ticket#comments"/></access-parent>
			</class>
			<class name="demo.tickets:Reminder">
				<access-parent><target reference="demo.tickets:Reminder#ticket"/></access-parent>
			</class>
			<class name="demo.tickets:Task">
				<access-parent>
					<script expr="t -> $t.get(`demo.tickets:Task#milestone`).container()"/>
				</access-parent>
			</class>
			<class name="demo.tickets:Signature">
				<access-parent><self/></access-parent>
				<grant operation="Read" roles="Signer"/>
			</class>
		</security-config>
	</instance>
</config>
```

- `operation` is a command group id that applies to the type: one of the built-in operations
  `Read`, `Write`, `Create`, `Delete`, `Discover`, `Export`, or an operation declared for the type,
  see [Operations](#operations-which-operations-apply-to-a-type). The operation `Discover` is
  implied by every other right on the object or one of its attributes, so it is rarely granted on
  its own; see [the Discover specification](doc:access-discover-spec).
- `roles` is a comma separated list of role names (`InitialRolesManager` declares roles).
- `<grant>` adds roles, `<revoke>` takes them away again, in the order the rules appear. A
  `<revoke>` with `roles` removes these roles and leaves the operation restricted to the remaining
  ones (possibly none). A `<revoke>` without `roles` drops the operation's entry: on a `<class>` or
  `<module>` the operation is then granted to nobody (including what the module and the
  generalizations contributed), on a `<part>` the attribute is no longer restricted and the class
  decision applies. A later `<grant>` may grant the operation again.
- `inherit="true"` passes the rule on to the specializations of the type. The rights of a type are
  computed as: what its generalizations pass on, plus the module rules of its module, plus its own
  rules. A module rule applies to every class of the module, also to specializations defined in
  the module, whatever its `inherit`; `inherit="true"` additionally passes it on to
  specializations defined in other modules.

### Attribute rights: which types a `<part>` applies to

A `<part name="T#a">` entry configures the rights of the attribute `a` for the type `T` and all
its specializations, no matter whether `T` defines the attribute, overrides it or only inherits it.
For an object, the **most specific** entry on the way from its type to its generalizations decides:

- An entry of the type itself wins.
- Otherwise the entries the generalizations pass on count, and an entry of a type is dropped when
  an entry of one of its specializations is passed on as well.
- An entry replaces the entries of the generalizations for **all** operations: an entry of `T#a`
  granting only `Read` leaves `Write` on `a` of `T` to the class rights, even if a generalization
  restricts `Write`.
- A generalization without an entry for the attribute adds nothing.
- If two independent generalizations pass on entries with **different** rights and the type has no
  entry of its own, the start fails with a configuration error. Entries with equal rights are no
  conflict. An entry of the type itself resolves the conflict.

Example with the type `Named` defining `name`, its specialization `Foo` overriding `name`, and its
specializations `Bar` and `Bar`'s specialization `Sub` inheriting `name` unchanged:

```xml
<part name="my.module:Named#name">
	<grant operation="Write" roles="Editor"/>
</part>
<part name="my.module:Foo#name">
	<grant operation="Read" roles="Keeper"/>
</part>
<part name="my.module:Sub#name">
	<grant operation="Read" roles="Keeper"/>
</part>
```

- `name` of a `Bar` is read by the class rights of `Bar` and written by `Editor` only.
- `name` of a `Foo` is read by `Keeper` only and written by the class rights of `Foo`, since the
  entry of `Foo` replaces that of `Named`.
- `name` of a `Sub` (and of its specializations) behaves like that of a `Foo`, while `Bar` and the
  other specializations of `Bar` keep the rights of `Named`.

The check of an object uses its type: TL-Script `` $bar.get(`my.module:Named#name`) `` sees the rights
of `Bar`, `` $foo.get(`my.module:Named#name`) `` those of `Foo`. A question without an object names the
type explicitly with `ModelAccessRights.getAllowedRoles(TLClass, TLStructuredTypePart, ...)` and
`hasGrant(TLClass, TLStructuredTypePart, ...)`. The variants without a type refer to the owner of
the given attribute, except that `hasGrant(TLStructuredTypePart, ...)` is conservative: it is true as
soon as the owner or any of its specializations has an attribute right for the attribute's name, so
that TL-Script never compiles an access into a query that skips the check a specialization needs.

The "Access rights…" dialog of a type shows and edits the attribute rights in effect for that type
and stores a change as an entry of that type. When the type has no entry of its own for the
attribute yet, the dialog creates it with the rights the type inherits, so that an edit changes
nothing but the grant edited.

A custom operation can also authorize a configured TL-Script function as a whole, see
[privileged TL-Script functions](doc:access-privileged-scripts). The UI units of the React view
layer (navigation items, tabs, sections, tiles) name the operation they check and the object they
check it on with `<access-control>` and are decided by these grants as well (article
`view-layer/access-control` of the module `com.top_logic.layout.view`). The operation `Administration` of the
engine's administration views is declared on the security root (`<singleton
name="SecurityStructure#ROOT">`) and granted there.

### Discover attributes: what `Discover` makes readable

An object a user may only discover shows its label, but none of its attributes (see
[the Discover right](doc:access-discover-spec)). Where a user must tell the objects apart by more
than their label - e.g. in an option list that shows the objects to choose from as a table - the
class entry names the attributes that `Discover` makes readable:

```xml
<class name="my.module:Product" discover-attributes="name,number">
	<grant inherit="true" operation="Read" roles="Editor"/>
	<grant inherit="true" operation="Discover" roles="Buyer"/>
</class>
```

A `Buyer` sees `name` and `number` of a `Product` it may discover, but no other attribute.

- Only reading is extended. Writing a discover attribute still requires `Write`.
- An attribute with a read grant of its own (`<part>` entry granting `Read`) is decided by that grant,
  not by the discover attributes.
- When a discover attribute is a reference, a target the user may not discover is still filtered
  (TL-Script, tables) or masked (form fields).
- The setting applies to the class and its specializations. A specialization with a setting of its own
  replaces it; an empty setting (`discover-attributes=""`) withdraws all discover attributes. A class
  entry without the setting keeps the setting of the generalizations.
- If two independent generalizations pass on different settings and the type has none of its own, the
  start fails with a configuration error.
- An attribute name the class does not have is a configuration error. The editor of the access rights
  offers the attributes of the class to choose from.
- A module entry has no discover attributes: configure them on a common generalization instead.

The question without an object is `ModelAccessRights.getDiscoverAttributes(TLClass)`. In the
"Access rights…" dialog of a type, the `Read` cell of a discover attribute without a read grant of its
own says that the attribute is readable with `Discover`.

### Operations: which operations apply to a type

An operation is a command group with a global name: `Approve` on a project and `Approve` on a
device is the same operation, the grants on the two types decide separately. Only an operation
that **applies** to a type may be granted on it:

- The built-in operations `Read`, `Write`, `Create`, `Delete`, `Discover` and `Export` apply to
  every type without a declaration (`ApplicableOperations.BUILT_IN`).
- Any other operation applies where an `<operation name="..."/>` entry in the `<operations>` of a
  `<class>`, `<module>` or `<singleton>` entry declares it. A class declaration applies to the
  class and all its specializations, a module declaration to every class of the module and their
  specializations, a singleton declaration to that singleton object (in addition to the operations
  of its type).
- The declarations of all configuration layers are appended, like the grants.
- The operation must be a command group of the `CommandGroupRegistry` and needs labels
  `commandGroup.<Name>` and `commandGroup.<Name>.tooltip` in a resource bundle of the application.
- A type with an access parent may declare operations, too (it has no grants of its own for the
  delegated operations): a custom operation on such a type, if delegated, is checked as the same
  operation on the parent, so it must apply to the parent type as well, unless it is implied by an
  operation the parent has.

**`implied-by="X"`**: whoever holds the operation `X` on an object also holds the declared
operation on it. This generalizes "every right implies `Discover`": an application that grants
nothing beyond the model rights declares `<operation name="Show" implied-by="Read"/>`, and a
later explicit `<grant operation="Show" .../>` changes no view. Implications are transitive
(`Inspect` implied by `Show`, `Show` implied by `Read`: a reader holds `Inspect`); several
declarations of the same operation add their implying operations. The implied roles count
everywhere a grant counts: `isAllowed`, `getAllowedRoles(TLClass, BoundCommandGroup)`,
`getAccessibleTypes`, the checks on a singleton and the coverage check. For a type with an access
parent, the implying operation is checked on the parent (with `operations`: only if it is delegated).

**Configuration errors**, reported when the service starts:

- a `<grant>` or `<revoke>` of an operation that does not apply to the type of the entry (for a
  `<module>` entry: neither built-in nor declared for the module; for a `<part>` entry: not applying
  to the owner of the attribute; for a `<singleton>` entry: not applying to the singleton),
- an `implied-by` naming an operation that does not apply where the declaration is made,
- `implied-by` declarations forming a cycle (also one combining class and module declarations),
- a declaration of a built-in operation, and an operation or `implied-by` naming a command group
  that does not exist.

In Java, `ModelAccessRights.getOperations(TLClass)` (and `getObjectOperations(TLObject)`, which
includes the declarations of a singleton) returns the `ApplicableOperations` of a type:
`appliesTo(operation)`, `getOperations()`, `getImpliedBy(operation)` (declared) and
`getImplying(operation)` (transitive). `ModelAccessRights.appliesTo(TLClass, BoundCommandGroup)` is
the short form.

### The two marks

`<class>` and `<module>` entries carry two boolean marks. They exclude each other: a configuration
setting both on one entry is rejected at startup (constraint `NotBothTrue`), the in-app editor drops
the one mark when the other is set, and the access rights dialog refuses to save both.

- **`without-security="true"`** — the type is not access controlled: every user may access its
  objects and attribute values and may create such objects; only a restricted user stays excluded.
  Specializations inherit the mark; on a module it covers every class of the module.
  **Warning:** the mark is dangerous — every user may read, modify, create and delete such objects,
  whatever roles they hold. Use it only as a temporary measure (e.g. right after an upgrade) until
  the type's grants and roles are configured properly; a type only the application code touches is
  `internal`, not `without-security`.
- **`internal="true"`** — the type is used by the application code alone and never accessed on
  behalf of a user. The access check is *not* changed (a user asking for such an object is denied,
  since no role is granted), but the coverage check does not report the missing definition.
  Specializations inherit the mark; on a module it covers every class of the module.

The engine sets these marks for its own types in `com.top_logic.element/…/ElementConf.config.xml`:
the modules `tl.changelog` and `tl.imagegallery` are `internal` (maintained by the engine's own
code; `tl.changelog` additionally grants `Read` to the role `PersonalProfile`, which every user holds
on the own account, so that a table offers the columns of its transient change sets, see the
section "Access rights" of the article `change-log` of `com.top_logic.element`), the class `tl.core:Revision` is `without-security` (commit metadata readable by everyone; a
revision cannot hold roles).

### Access parent: delegating the decision

A `<class>` entry with an `<access-parent>` element other than `<self/>` makes the type
delegate to its **access parent**, the object whose access definition decides. Unless the access
parent is restricted to some operations (see below), the type has no
grants, no marks and no roles of its own (an entry combining them is rejected at startup and by the
editor); with `operations`, grants are allowed for the operations that are not delegated, while the
marks `internal` and `without-security` remain rejected:

- Read, Write and Export on the object are Read, Write and Export on the parent.
- Create and Delete of the object are **Write** on the parent, since a part is created or deleted
  by editing the whole. Creating an object of such a type in a context requires Write on that
  context; creating one without a context (the transient-then-copy pattern of the view layer) is
  not restricted, since the object is inaccessible until it is put into a container, which is a
  write of the container. With several access parents, only a container candidate decides the
  creation, see [Several access parents](#several-access-parents-any-of-referer).
- The parent may delegate further; the chain is followed at runtime. An object whose relation
  leads nowhere is denied the delegated operations (without `operations`: every operation), unless
  the own grants of the type imply them. A chain running in a cycle is logged as an error (as info
  where the access parent has several candidates): the check reaching itself again is denied, so an
  unrestricted type is denied altogether, while a restricted one is still decided by its own roles.
- Attribute-level `<part>` grants on the type are checked against the roles the user holds at the
  end of the chain (for an access parent restricted to operations: at the object deciding the
  requested operation, the object itself where the operation is not delegated; for several access
  parents: at the end of any of the chains).
- A historic object (one of an earlier revision) is checked with its access parents in that
  revision: its container, the targets of its references and the objects referring to it are those
  of the revision, not the current ones.
- Specializations inherit the setting unless they have one of their own.

The `<access-parent>` element holds exactly one definition of the relation. The kind of relation is
stated explicitly, the reference only names the way. There is no guessing from the shape of the
reference, so a to-one composition a type both owns and is held in (`Node#detail : Node`) is
unambiguous:

| `<access-parent>` content | Access parent |
|---|---|
| (no `<access-parent>` element) | inherited from the generalizations, else the default below |
| `<container/>` | the container, whichever composition holds the object (`TLObject.tContainer()`) |
| `<container reference="..."/>` with a composition holding the type | the container, only when it holds the object through that composition (navigated backwards) |
| `<target reference="..."/>` with a reference of the type (mandatory) | the objects the reference points to (navigated forwards), each of a multi-valued reference |
| `<referer reference="..."/>` with a reference pointing to the type | the objects pointing to the object through the reference (navigated backwards), any cardinality |
| `<script expr="..."/>` | the objects a TL-Script function computes, see below |
| `<any-of>` with several of the above | the access parents of all candidates, see [Several access parents](#several-access-parents-any-of-referer) |
| `<self/>` | none: the type decides through its own grants and roles |

A reference not fitting the definition (a composition not holding the type for `container`, a
foreign reference for `target`, a reference whose target type does not fit the type for `referer`)
and an empty `<access-parent/>` are configuration errors.

**Delegating only some operations (`operations`).** The building blocks `<container>`, `<target>`,
`<referer>` and `<script>` take an optional attribute `operations="Read,Write,..."`
(comma-separated, the operation names of the grants). Without it everything is delegated as
described above. With it the access parent decides only the listed operations; the type decides
all others itself:

- The list is literal: **Create** and **Delete** are delegated only if listed, `Write` does not bring
  them along. **Discover** is delegated whenever any operation is delegated.
- The names are the operations on the delegating object. The mapping of Create and Delete to Write
  on the parent happens afterwards.
- An operation `Y` is allowed through the parent if `Y` is delegated and the parent allows `Y`, or
  if a delegated operation `X` implies `Y`
  (see [operations](#operations-which-operations-apply-to-a-type)) and the parent allows `X`, also
  when `Y` itself is not delegated. Conversely, an operation `X` that is not delegated may imply a
  delegated `Y`: holding `X` by a grant of the type on the object itself allows `Y` as well.
- The operations that are not delegated fall back to the grants of the type itself, checked with
  the roles the user holds on the object itself (assignments, role rules, role parents). Without a
  grant the operation is denied.
- A type with own grants for an operation it delegates is a configuration error (also for a
  specialization inheriting the restricted access parent). Grants inherited with `inherit="true"` from
  a generalization for delegated operations are dropped silently.
- Unknown or inapplicable operation names are configuration errors. `operations` is not allowed on
  `<self/>`, on `<any-of>` nor on `<access-parent>` itself.
- At runtime, the role holder (the object whose roles decide, see step 7 above), the explanation of
  the effective access and the type-level checks (commands, view elements) work per operation.
- In the tooling, the grants editor shows grant columns only for the operations the type decides
  itself, and the coverage check treats the built-in operations of such a type like those of a
  self-deciding type and reports declared operations that are neither delegated nor granted.

Example: an entry of a catalog is readable by everybody who may read the catalog, but writing it is
a right of its own, granted to a role the user holds on the entry itself:

```xml
<class name="my.module:Entry">
	<access-parent>
		<target reference="my.module:Entry#catalog" operations="Read"/>
	</access-parent>
	<grant operation="Write" roles="Editor"/>
</class>
```

Read and Discover of an entry follow the catalog; Write is allowed to holders of `Editor` on the
entry. A Read grant on `my.module:Entry` would be a configuration error.

**Upgrading from `TL_8.0.0-alpha9`.** The attributes `access-parent="..."` and `access-reference="..."`
of a `<class>` entry are gone and rejected at startup: `access-parent="container"` becomes
`<access-parent><container/></access-parent>`, `access-parent="container" access-reference="X"`
becomes `<access-parent><container reference="X"/></access-parent>`, `access-parent="target"
access-reference="X"` becomes `<access-parent><target reference="X"/></access-parent>`,
`access-parent="self"` becomes `<access-parent><self/></access-parent>`, and `access-parent="auto"`
is dropped. Check the `WEB-INF/autoconf/com.top_logic.model.security.SecurityConfigurationService.config.xml`
of the application, too: the access rights dialog of the coverage view writes there.

**Computed access parent (TL-Script).** Where none of the fixed relations fits - several steps, a
backward navigation over a reference that is no composition, a multi-valued or derived attribute -
a TL-Script function computes the access parent (`ScriptAccessParent`, module `tl-model-search`):

- The function receives the object and yields its access parents: one object, or a collection of
  objects, each of them an access parent of its own (allowed as soon as one of them allows the
  operation, see [Several access parents](#several-access-parents-any-of-referer)).
- `null` or an empty collection: no access parent, so nobody but the technical admin may access
  the object (with `operations`: the grants of the type decide). Not logged. A `null` inside a
  collection is ignored.
- A value that is no object or an object without access control of its own (one that cannot hold
  roles, a transient object for instance) is skipped and logged as an error; the other objects
  remain access parents. Following an object nobody decides access for would open the object to
  every user.
- Only committed objects count: an object created in the running transaction is skipped without a
  verdict, since whoever creates it sets it up without any check on the object it would open. An
  object whose type is `without-security` is not access controlled and opens the object to
  everybody.
- A failure of the function denies access and is logged as an error.
- The function is evaluated without access checks: it can navigate through objects the user may
  not read, and it does not recurse into the check it is part of.
- A check that depends on no concrete object (the commands of a type) counts a type with a
  computed access parent as accessible, since the types of its parents are unknown; the check of
  each object follows the function.
- The coverage view shows such a type as delegated, with the script text in the access parent
  column.

**The default for composition parts.** A type held in a composition that has no role rule, no role
parent rule, no marks and no `<access-parent>` setting (own or inherited) delegates to its container by
default, whichever composition holds the object. Most helper types of an application therefore need
no configuration at all. A role rule or a role parent rule applying to the type (inherited ones
included), one of the marks, or any `<access-parent>` setting switches the default off.

- `<container/>` without a reference states the default explicitly. It is needed for
  a composition part that inherits a role rule but should delegate nevertheless; the coverage check
  then reports the shadowed rules.
- `<self/>` switches the default off for a composition part that decides for itself,
  e.g. with its own grants and roles assigned directly on its objects (which do not count as a
  role source for the default). It also stops a specialization from inheriting the access parent
  of its generalization.

In Java, `ModelAccessRights.getAccessParent(TLClass)` returns the relation in effect as an
`AccessParentFunction` (`ContainerRelation` - configured or default, any or one composition -,
`TargetRelation`, `RefererRelation`, `ScriptAccessParent`, `AnyOfRelation`, wrapped in a
`RestrictedRelation` where `operations` restricts it; `null` for a type deciding for itself).
`AccessParentFunction.resolveAll(object, operation)` yields the access parents deciding an
operation, `ModelAccessRights.getRoleHolders(object, operation)` the objects at the ends of their
chains, whose roles decide. The configured side is an `AccessParentDefinition`, resolved once per
type at startup; an application can add its own implementation with a tag name of its own, see
[Combining access parents in an application](#combining-access-parents-in-an-application).
`AccessManager.hasRoleSource(TLClass)` is the hook the default consults.

### Several access parents (`<any-of>`, `<referer>`)

An object may have several access parents: the objects referring to it, the targets of a
multi-valued reference, the objects a script yields, or the access parents of several building
blocks combined with `<any-of>`. Each access parent gives a verdict of its own, and the operation
is allowed **as soon as one of them allows it**.

Example: a contact is readable by everybody who may read a document naming it as author or
reviewer, or who may read a task it is assigned to. Write and everything else stay with the grants
of the contact itself:

```xml
<class name="my.module:Contact">
	<access-parent>
		<any-of>
			<referer reference="my.module:Document#author" operations="Read"/>
			<referer reference="my.module:Document#reviewers" operations="Read"/>
			<referer reference="my.module:Task#assignee" operations="Read"/>
		</any-of>
	</access-parent>
	<grant operation="Write" roles="ContactEditor"/>
</class>
```

Example: a part keeps every right of the folder holding it, and is additionally readable by
everybody who may read a document relating it:

```xml
<class name="my.module:Part">
	<access-parent>
		<any-of>
			<container/>
			<referer reference="my.module:Document#relatedParts" operations="Read"/>
		</any-of>
	</access-parent>
</class>
```

A reader of such a document reads the part, but may neither write nor delete it; the owner of the
folder may do everything with it.

- **`<referer reference="T#r"/>`**: the access parents are the objects holding the object in the
  reference `r`, navigated backwards (`TLObject.tReferers`); `r` may have any cardinality. Its
  owner `T` is the type of the access parents; the target type of `r` must fit the configured type,
  which is checked when the configuration is loaded.
- **`<target reference="..."/>`** of a multi-valued reference: every object the reference points to
  is an access parent of its own.
- **`<any-of>`** holds the building blocks (`<container>`, `<target>`, `<referer>`, `<script>`) as
  its candidates; the access parents of the object are those of all candidates. A building block
  directly in `<access-parent>` behaves like a single candidate. A nested `<any-of>` adds its
  candidates, an `<any-of>` with a single candidate is that candidate. `<self/>` as candidate, an
  empty `<any-of>` and `operations` on `<any-of>` are configuration errors; each candidate restricts
  its own operations.
- **A candidate not delegating an operation abstains** for it. An operation delegated by no
  candidate is decided by the grants of the type itself, as for a single restricted access parent.
  Discover is delegated by a candidate delegating any operation.
- **No access parent, no verdict.** An object for which no candidate yields an access parent (no
  referring object, an empty reference) has no verdict from the parents: as with a single access
  parent, the grants of the type decide where a restricted candidate leaves them room (the contact
  above, read by nobody but through a document or task), otherwise the operation is denied.
- **Only committed, access controlled objects count.** An object referring to the object, or
  yielded by a script, that was created in the running transaction gives no verdict: whoever
  creates it sets it up without a check on the object it refers to, so it could otherwise open any
  object. A referring object that cannot hold roles gives no verdict either; it is skipped and
  logged as an error, like such an object yielded by a script. Only a referring object whose type
  is `without-security` opens the object to everybody, since every user may access it.
- **Create and initial values.** Nothing refers to an object being created, so only the candidates
  leading to the container (`<container>`) decide whether it may be created and which initial
  attribute values may be set (Write on the container, as described above). An `<any-of>` without
  a container candidate delegating Create does not delegate Create at all: the grants of the type
  itself decide it, checked with the roles on the context the object is created in, and such a
  `Create` grant is allowed even though every other operation is delegated.
- **Cycles.** Access parents referring to each other (two nodes pointing to each other with `next`)
  form a cycle. The path that reaches a check under way again is denied, while another candidate
  may still allow the operation. A denial computed while such a cycle was hit is not stored in the
  [memo of the interaction](#caching-of-decisions), so the object closing the cycle is decided anew
  when it is checked on its own. The cycle is logged as info where the access parent has several
  candidates (a matter of the data), as an error otherwise.
- **Role holders and explanation.** `getRoleHolders` yields the ends of all chains, each deciding
  the operation for itself. The explanation of the effective access (React admin hub) lists every
  object deciding an operation; a user may perform it when holding an admitting role on one of
  them.
- **Type level.** A check that depends on no concrete object (the commands of a type, the
  accessible types) counts a type with several candidates as accessible as soon as one of the
  parent types of one candidate is; a container candidate leads to the types owning a composition
  of the type, a script candidate to unknown types, which count as accessible. This is a pre-filter:
  the check of each object decides.
- **Tooling.** In the access rights dialog, the reference options of the building blocks work
  inside `<any-of>` as well. The grants editor shows grant columns for the operations no candidate
  delegates (and `Create` where no container candidate delegates it); the coverage view names every
  candidate in the access parent column, separated by semicolons.

### Combining access parents in an application

`<any-of>` combines its candidates by OR. An application needing another combination (all of
several access parents, for instance) adds an `AccessParentDefinition` of its own with a tag name
of its own, like any custom access parent definition:

- Its configuration holds the building blocks as a list of
  `PolymorphicConfiguration<? extends AccessParentDefinition>`, and extends `ConfigPart` (as
  `AnyOfAccessParent.Config` does), so that the building blocks inside it offer their reference
  options in the access rights dialog.
- It overrides `resolveWith(context, type, candidates)` and resolves each building block through
  the given `CandidateResolver`, which applies the `operations` of the building block as well.
- The resulting `AccessParentFunction` overrides `allows(object, operation, check)`, which combines
  the verdicts: `check.test(parent, operation)` checks the operation on one access parent.

The contract of `allows`, on which caching, cycle handling and the type level rely:

1. `resolveAll(object, operation)` yields at least every object `allows` may pass to the check.
2. `allows` returns `true` only when the check returned `true` for at least one of these objects.
3. The decision is monotonic: a check returning `true` for more objects never turns an allowed
   operation into a denied one. (A check closing a cycle answers `false` provisionally; allowed
   decisions computed meanwhile are stored.)

A combinator that is not an OR returns `false` from `isDisjunctive()`. The users holding an
admitting role on one of the role holders are then a superset of the users the combinator admits,
and the explanation of the effective access says so. `delegates(operation)` tells which
operations the combinator decides; `delegatesAll()` tells whether it decides every operation, so
that the type has no grants of its own. By default this is the case unless `getOperations()`
lists the delegated operations: a combinator of restricted building blocks that does not override
`delegatesAll()` decides every operation, denying those no building block delegates, and the grants
the type inherits do not apply. `getParentTypes(type)` returns the union of the parent types of
the building blocks, `null` where they are not known statically.

### Caching of decisions

Every decision of `isAllowed` is memoized per interaction (request) in an `AccessDecisionCache`
stored on the `InteractionContext`, keyed by person, object and operation. A composition table of
two hundred rows that all delegate to one container costs one real check of the container. The
memo is replaced when the knowledge base has a newer revision than the one it was created for, so
a command that commits and then renders does not reuse decisions from before its change; outside
an interaction a memo serves a single check and its delegations. The memo also detects cycles: a
decision is marked while it is being computed, and a chain of access parents that reaches the mark
again is denied. Such a denial depends on the path the check came along, so a denial computed while
a mark was reached is not stored; allowed decisions are.

## Role sources: which roles a user holds on an object

Roles on an object come from three sources, all collected by `ElementAccessManager.getRoles`:

1. **Direct assignments** — a `BoundedRole` assigned to a person or group on a specific object
   (`getLocalAndGlobalAndGroupRoles`). This is data, not a definition: it says nothing about the
   next object that is created.
2. **Role rules** — computed roles, configured in `<role-rules>` of the `AccessManager`.
3. **Role parent rules** — the object inherits every role the user holds on its role parents,
   configured in `<security-parents>` of the `AccessManager` (the tag keeps its historical name;
   the concept is called *role parent* since it passes on roles, not the access decision — that is
   the access parent above). Parents are collected transitively, so a sub-project reaches the
   project through its parent project. The Java API keeps the historical name as well
   (`BoundObject.getSecurityParents()`).

```xml
<config service-class="com.top_logic.tool.boundsec.manager.AccessManager">
	<instance>
		<role-rules>
			<rules>
				<!-- Every member of the group "users" holds AccountReader on every account. -->
				<rule id="AccountsReadableByUsers" inherit="true"
					meta-element="tl.accounts:Person" role="demo.react.AccountReader">
					<group-with-name name="users"/>
				</rule>
				<!-- Identity rule without a path: the account itself is the role holder, so a
				     user holds PersonalProfile on exactly their own account. -->
				<rule id="PersonalProfileForPerson" inherit="true"
					meta-element="tl.accounts:Person" role="PersonalProfile"/>
			</rules>
		</role-rules>
		<security-parents>
			<rules>
				<!-- A sub-project inherits the roles of the project containing it, on top of the
				     roles its own rules deliver: the rule navigates the composition
				     Project#subProjects backwards. -->
				<rule id="SubProject_roleParent" inherit="true" meta-element="demo.projects:SubProject">
					<step attribute="demo.projects:Project#subProjects" inverse="true"/>
				</rule>
			</rules>
		</security-parents>
	</instance>
</config>
```

- `meta-element` is the type the rule applies to, `inherit="true"` extends it to specializations.
- A rule's `<step attribute="…" inverse="true|false"/>` elements form the navigation path from the
  object to the role holder (role rule) or to the role parent: `inverse="false"` follows the
  attribute value, `inverse="true"` the objects referencing the object through the attribute.
- Several role parent rules for one type (or a path reaching several objects) give an object
  several role parents; the user holds the roles of all of them, collected transitively with a
  guard against cycles.
- A role parent is the right choice where the type has grants of its own on top of the inherited
  roles (a sub-project with its own roles). Where the type has nothing of its own, an access parent
  is simpler and cheaper.
- A role parent rule has **no operation filter**: it passes on roles, and the grants of the type
  itself decide which operations those roles give. To treat roles of different sources differently,
  derive distinct roles with role rules of type `inheritance` and grant them separately. (Delegating
  selected operations to another object is what `operations` of an access parent does.)
- A role rule of type `inheritance` (`source-role`, `source-meta-element`) derives the role from a
  role the user holds on the object the path leads to; a rule of type `reference` derives it from
  a relation to a group.

### Roles held on the security root

The security root is never a role parent implicitly: an object whose type has no role parent rule
has no role parent at all. For the roles held on the root to reach the objects of a type, a role
parent rule must name it explicitly via `<singleton module="SecurityStructure"/>`, either on a
common base type with `inherit="true"` or per type. The `<singleton>` step yields a singleton of a
module, independent of the object: `module` names the module, `singleton-name` the singleton
(default `ROOT`). A rule may combine it with other rules of the type:

```xml
<security-parents>
	<rules>
		<rule id="Task_milestone" meta-element="myapp:Task">
			<step attribute="myapp:Task#milestone" inverse="false"/>
		</rule>
		<rule id="Task_root" meta-element="myapp:Task">
			<singleton module="SecurityStructure"/>
		</rule>
	</rules>
</security-parents>
```

The engine ships such rules for the account types `tl.accounts:Group` and `tl.accounts:Address`
(`tl.accounts.config.xml`) and for the units and currencies, `tl.units:AbstractUnit` with
`inherit="true"` (`tl.units.config.xml`). The grants are the application's: for its users to read
the currencies, it grants `Read` on the module `tl.units` to a role they hold on the security root.

## Where the files are

- Framework and module defaults: `WEB-INF/conf/*.config.xml` of the modules (for example
  `com.top_logic.element/…/personalProfileSecurity.config.xml`).
- Application: its own `WEB-INF/conf/<app>.config.xml` (the React demo keeps both services in
  `demoReactConf.config.xml`).
- In-app edits: `WEB-INF/autoconf/<service class>.config.xml`, written by
  `InAppServiceConfigStore`, one file per service (`…AccessManager.config.xml` for rules,
  `…SecurityConfigurationService.config.xml` for grants and marks). These files layer onto the
  base configuration and are read at startup and by "Apply configuration".

## Checking that the definition is complete: the coverage check

`SecurityCoverageCheck` (module `com.top_logic.element`) analyses every non-abstract class of every
module not listed in its `excluded-modules` and reports, per type:

| Finding | Meaning | Fix |
|---|---|---|
| no role source | neither a role rule nor a role parent rule applies and no composition holds the objects, so no user can hold a role and every access is denied (direct assignments do not count as a definition) | access parent through a reference, role parent rule, role rule, or mark internal |
| no read grant | no role is granted `Read` on the type (after module and inherited grants), so its objects are inaccessible to every user | grant on the type or its module, or mark internal |
| dead grant | an operation is granted to roles that no rule can deliver where the operation is checked; the grant never takes effect. `Create` is checked where an object is created (`isAllowedCreate(person, type, context)`): on a container, i.e. a concrete class whose composition - as in effect on that class, a narrowing override included - holds the type or one of its generalizations, or on its role parents; on the security root for a type no composition holds. Every other operation is checked on the type or its role parents | add a rule delivering the role there, or grant a role delivered there |
| shadowed rules | the type has a configured access parent, but role rules or role parent rules apply to it; they have no effect | delete the rules, or drop the access parent |
| ungranted operation | an operation declared for the type (not a built-in one) is held by no role there, neither by a grant nor through an implying operation, so every check of it on the type is refused | grant it to a role, or declare it `implied-by` an operation the roles hold |
| inapplicable operation | a view unit, a command, a form or a privileged script checks an operation on the type that does not apply to it (a check on the security root is reported for the type of the root) | declare the operation for the type, or check another one |
| unknown operation | such a place checks an operation that is no registered command group | register the command group, or check another one |
| unchecked grant | an operation declared for the application (not a built-in one) is granted on the type, but no view unit, command, form or privileged script checks it - nor an operation it implies - on objects of the type; the grant has no effect | remove the grant, or add the check that should use it |

The places checking an operation are reported to the check by the `OperationUseProvider`s of its
`operation-uses`: `com.top_logic.layout.view` contributes the `OperationUseIndex`, which scans the
`*.view.xml` files (`<access-control>`, the `<access>` of commands, `<form edit-operation>`) and the
privileged scripts of the configured TL-Script functions (see the section "Administration of
grants" of the article `view-layer/access-control` of that module). A place
naming the type it checks is checked against that type and its concrete specializations; a type
delegating to an access parent, restricted to operations or not, is not checked for applicability
by the coverage analysis (the check at startup, which reports an operation not applying to the type
of a place as a configuration error, covers every type). A place not declaring its type (a
privileged script, a form, a command whose unit lies in another view file) is only checked for an
unknown operation; such findings belong to no type and are reported by
`SecurityCoverageAnalysis.getUnattachedFindings()`.

A grant of an operation counts as checked on a type when a place checks the operation itself, or an
operation it implies (holding an operation means holding every operation it implies; an operation
implying a built-in one is always in use, since the model access rights check the built-in
operations on every access), on objects of the type: on the type or one of its generalizations, on a
type whose objects delegate to an access parent of the type, on the security root when the root is of
the type, or without declaring the checked type at all - a place that does not declare its type
(privileged script, form, command outside a unit of its view file) counts as checking every type, so
that it never causes an unchecked-grant finding. Grants of built-in operations are never reported.
The unchecked grants are only reported when the places checking an operation are known, i.e. when a
module contributes an `OperationUseProvider` (as `com.top_logic.layout.view` does).

A type is **covered** when it has a role source and at least one role may read it, **delegated**
when it has an access parent (whether the parent is accessible is reported with the parent's type;
a type whose access parent is restricted to operations is delegated, too, but additionally gets the
findings of the operations it decides itself: no role source, an operation neither delegated nor
granted, dead or unchecked grants),
**exempt** when it is internal or without security, **incomplete** otherwise.

- At startup the findings of the access definition are logged at INFO (`log-findings="false"`
  switches the log off, not the analysis); the places checking an operation are read on demand, so
  their findings appear in the admin hub and in `TestSecurityCoverage`, not in the startup log.
- In the React admin hub (Administration → Access control → Coverage) the same analysis is a table
  with a findings detail; the "Access parent" column shows what a delegating type delegates to.
  Its commands edit the definition: role parent and role rules, the grants, marks and access parent
  of a type ("Access rights…"), the grants and marks of its module, mark a type internal or exclude
  it from access control. Every edit is written to the
  `autoconf` files; "Apply configuration" reads the files again and restarts the access services,
  and only then does the table show the change. An access parent the underlying configuration
  sets is dropped - by a mark, or by removing it in the dialog - by storing `<self/>`, written with
  `config:override="true"`, since an access parent left out would leave that one in effect.
- The grants of a type are edited as a matrix in Administration → Access control → Grants: a row
  per role a user can hold on the type (`SecurityCoverageAnalysis.getHoldableRoles`) or holding a
  grant that takes effect (`SecurityCoverageAnalysis.isLiveGrant`, e.g. a `Create` grant to a role
  delivered on the container), a row marked ineffective per role none of whose grants takes
  effect, a column per
  operation applying to it (see the section "Administration of grants" of the article
  `view-layer/access-control`). A changed
  cell is stored as a single `<grant>` or `<revoke>` of the role in the entry of the type
  (`SecurityDefinitionEditor.setGranted`), not inherited by its specializations, and takes effect
  with "Apply configuration".
- An application makes the check part of its test suite with `test-security-coverage="true"` in
  the `ElementTestCollector` global test configuration; `TestSecurityCoverage` then fails on every
  incomplete type and on every finding belonging to no type.
