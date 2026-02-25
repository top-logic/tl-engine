# Model-Based Access Rights for TopLogic

## 1. Status Quo: The Current Access Rights System

### 1.1 Overview

TopLogic's current access rights system controls who can do what through a combination of two concerns:

- **Role assignments on model instances**: Who has which role on which business object?
- **View-based permission configuration**: Which roles can execute which command groups in which views?

The first concern is already model-based. The second is not -- it is tied to the layout structure of the application's user interface.

### 1.2 Core Concepts

#### 1.2.1 Roles (`BoundedRole`)

A role (e.g. "Manager", "Editor", "Viewer") is a named entity stored persistently in the knowledge base (`BoundedRole` / table `BoundedRole`). Roles can be:

- **Global**: Not bound to any scope. Available everywhere.
- **Scoped**: Bound to a `TLModule` via the `definesRole` association. Only available within that module's context.

Roles are defined in the `tl.accounts` model module as the type `tl.accounts:Role`.

#### 1.2.2 Users, Groups, and Role Assignments

- A **Person** (`tl.accounts:Person`) is a user account.
- A **Group** (`tl.accounts:Group`) is a named set of members (persons and other groups).
- Each person has a **representative group** -- a 1:1 group that represents the person for role assignment purposes.
- Persons can also be members of additional **named groups** (e.g. "Sales Team", "Management").

Role assignments are stored as `hasRole` records (`tl.accounts:RoleAssignment`) with three references:

| Attribute | Meaning |
|-----------|---------|
| `source` (object) | The context object (business object or security root) on which the role is granted |
| `dest` (role) | The role being granted |
| `owner` (group) | The group (or person's representative group) receiving the role |

This means: "Group G has role R on object O."

When checking whether a person has a role on an object:
1. The person's representative group is checked.
2. All groups the person belongs to are checked.
3. The check walks up the **security parent hierarchy** (`BoundObject.getSecurityParent()`), inheriting roles from parent objects.

#### 1.2.3 Role Rules (`ElementAccessManager`)

Beyond direct role assignments, the `ElementAccessManager` supports **rule-based role derivation**. Role rules (`RoleRule`) define dynamic role grants based on attribute values and relationships of business objects:

- **Reference rules**: Follow a path of attribute references from a business object to reach groups. Those groups receive a specified role on the object. Example: "The groups referenced by the `responsibleTeam` attribute of a `Project` instance receive the `Editor` role on that project."
- **Inheritance rules**: Derive roles from roles on related objects. Example: "If a user has the `Manager` role on a `Department`, they also get the `Viewer` role on all `Project` instances belonging to that department."

Role rules are configured per `TLClass` (type) and can be inherited to sub-types.

#### 1.2.4 Command Groups (`BoundCommandGroup`)

Commands in TopLogic are classified into **command groups**, each with a type:

| Command Group | Type | Meaning |
|---------------|------|---------|
| `Read` | READ | View/display operations |
| `Write` | WRITE | Modify operations |
| `Create` | WRITE | Object creation |
| `Delete` | DELETE | Object deletion |
| `Export` | READ | Export operations |
| `System` | READ | System commands (bypass security) |

Applications can define additional custom command groups (e.g. `Finish`, `Approve`).

#### 1.2.5 Views and Security (`CompoundSecurityLayout`)

The application's UI is organized as a tree of layout components. Security boundaries are defined by `CompoundSecurityLayout` nodes, which group components into logical units as experienced by the user.

Each `CompoundSecurityLayout` has a corresponding persistent security configuration (`PersBoundComp`) that stores:

**For this view: which roles are allowed to execute which command groups.**

This is a mapping:
```
(CompoundSecurityLayout, BoundCommandGroup) → Set<BoundedRole>
```

Stored via `needsRole` associations:
```
PersBoundComp --needsRole[cmdGrp=X]--> BoundedRole
```

Example configuration for a "Customer Details" view:
- Read: allowed for roles `Viewer`, `Editor`, `Manager`
- Write: allowed for roles `Editor`, `Manager`
- Delete: allowed for role `Manager`

#### 1.2.6 Security Object Provider

Each view has a `SecurityObjectProvider` that maps the view's current model to the `BoundObject` on which role assignments are checked. Common strategies:

- **Model**: Use the view's current model directly (e.g., the selected Customer instance).
- **Master**: Use the master component's model.
- **Security root**: Use the application's global security root (for views not tied to specific instances).
- **Path-based**: Navigate from the model through a defined path to reach the security context.
- **Null**: No security check (for system views).

#### 1.2.7 The Complete Security Check

When a user tries to access a view or execute a command, the check proceeds as follows:

```
1. BoundChecker.allow(person, securityObject, commandGroup)
   │
   ├─ System command group? → ALLOW
   ├─ Admin user? → ALLOW
   ├─ Restricted user + write/delete command? → DENY
   │
   ├─ 2. getRolesForCommandGroup(commandGroup)
   │     → Looks up PersBoundComp (VIEW-BASED configuration)
   │     → Returns the set of roles that may execute this command group
   │     → If empty: DENY
   │
   └─ 3. AccessManager.hasRole(person, securityObject, accessRoles)
         → Determines person's roles on the security object (MODEL-BASED)
         → Checks direct role assignments (hasRole records)
         → Checks group memberships
         → Walks security parent hierarchy
         → Evaluates role rules (ElementAccessManager)
         → If person has any of the access roles: ALLOW
         → Otherwise: DENY
```

### 1.3 What is Already Model-Based

The following aspects are already model-based and independent of views:

1. **Role assignments** on business objects (hasRole records).
2. **Role rules** configured per TLClass that derive roles from object attributes and relationships.
3. **Security parent hierarchy walk** that enables role inheritance along object structures. However, which object is the security parent of a given instance is currently defined in Java code (`BoundObject.getSecurityParent()`), not declaratively -- see section 1.4.
4. **Role scoping** to TLModules.

### 1.4 What is View-Based (The Gap)

The critical missing link is **step 2** of the security check: the mapping from command groups to roles is configured **per view**, not per model type. This means:

- To answer "Who can edit Customer instances?", one must:
  1. Find all views that display Customer instances.
  2. For each view, look up which roles can execute Write commands.
  3. For each such role, determine which users have that role on which Customer instances.

- Different views showing the same type can have different permission configurations. A Customer might be editable in the "CRM" view but read-only in the "Reporting" view, even though it's the same data.

- There is no single place that says "Role X can read/write/create/delete instances of type T." This information is scattered across view configurations.

- For non-UI access paths (AI assistants, REST APIs, batch jobs), there is no natural view context to evaluate.

- The **security parent relationship** is hardcoded in Java (`BoundObject.getSecurityParent()`). Which object is the security parent of a given instance cannot be configured declaratively. Moreover, only a single security parent per object is supported, which is insufficient when an object participates in multiple overlapping contexts.

### 1.5 Architectural Diagram of the Status Quo

```
                     ┌─────────────────────┐
                     │   User (Person)      │
                     │   + Representative   │
                     │     Group            │
                     │   + Group            │
                     │     Memberships      │
                     └─────────┬───────────┘
                               │
                    "User accesses a view"
                               │
                               ▼
              ┌────────────────────────────────┐
              │  CompoundSecurityLayout (View)  │
              │                                │
              │  PersBoundComp:                │
              │    Read  → {Viewer, Editor}    │  ◄── VIEW-BASED
              │    Write → {Editor}            │      (which roles for which
              │    Delete → {Manager}          │       command groups)
              └────────────────┬───────────────┘
                               │
                    "Which roles are needed?"
                               │
                               ▼
              ┌────────────────────────────────┐
              │  SecurityObjectProvider         │
              │  Maps view model → BoundObject  │
              └────────────────┬───────────────┘
                               │
                    "Does user have a needed role
                     on the security object?"
                               │
                               ▼
              ┌────────────────────────────────┐
              │  AccessManager                  │
              │                                │
              │  ┌─ Direct role assignments ─┐ │
              │  │  hasRole(obj, role, group) │ │
              │  └──────────────────────────┘ │  ◄── MODEL-BASED
              │  ┌─ Role rules ─────────────┐ │      (who has which role
              │  │  Attribute-based          │ │       on which object)
              │  │  derivation per TLClass   │ │
              │  └──────────────────────────┘ │
              │  ┌─ Security parent chain ──┐ │
              │  │  Role inheritance         │ │
              │  └──────────────────────────┘ │
              └────────────────────────────────┘
```

## 2. Goal: Model-Based Access Rights Definition

### 2.1 Objective

Introduce a model-level access rights definition that answers these questions directly, without reference to views:

1. **Which users can read instances of type T?**
2. **Which users can update instances of type T?**
3. **Which users can create instances of type T?**
4. **Which users can delete instances of type T?**
5. **Which attributes of type T can user U read/modify?**
6. **Can user U access this specific instance of type T?**

### 2.2 Design Principles

1. **Model-first**: Access rights are defined on model elements (types, attributes), not on views. Views derive their permissions from the model-level definitions.

2. **Single source of truth**: There is one place that defines "Role X can perform operation O on type T." All access paths (UI, AI assistant, REST API, batch jobs) consult the same definition.

3. **Backward compatible**: The existing view-based configuration continues to work. The model-based definition is an additional layer that can coexist with and eventually replace the view-based configuration.

4. **Type-level and instance-level**: Type-level definitions say what is generally possible ("Editors can modify Customers"). Instance-level checks determine whether a specific user actually has the required role on a specific instance. The existing role assignment and role rule mechanisms are fully preserved, providing fine-grained instance-level access control: user U may have role R on instance I1 of type T but not on instance I2 of the same type.

5. **Granularity**: Permissions can be defined at the type level (all attributes) or at the attribute level (individual properties and references).

### 2.3 Proposed Model

#### 2.3.1 Operations

Operations in the model-based access rights system are `BoundCommandGroup`s -- the same type already used in the existing system. The built-in command groups cover fundamental data access:

| BoundCommandGroup | CommandGroupType | Meaning |
|-------------------|------------------|---------|
| `Read` | READ | View/query instances and their attribute values |
| `Write` | WRITE | Modify attribute values of existing instances |
| `Create` | WRITE | Create new instances (see section 2.3.6) |
| `Delete` | DELETE | Delete instances |

In addition, types can declare **custom business operations** (e.g. `Approve`, `Cancel`) using custom `BoundCommandGroup`s. See section 2.3.7.

#### 2.3.2 Type-Level Access Rules

A **type access rule** defines which roles are permitted to perform which operations on instances of a given type:

```
TypeAccessRule:
  type:          TLClass            -- The model type this rule applies to
  commandGroup:  BoundCommandGroup   -- Read, Write, Delete, or a custom command group
  roles:         Set<BoundedRole>    -- Roles that are permitted
  inherit:       boolean             -- Whether the rule applies to sub-types
```

Example:
```
type: myapp:Customer
operation: READ
roles: {Viewer, Editor, Manager}
inherit: true

type: myapp:Customer
operation: WRITE
roles: {Editor, Manager}
inherit: true

type: myapp:Customer
operation: CREATE
roles: {Editor, Manager}
inherit: false

type: myapp:Customer
operation: DELETE
roles: {Manager}
inherit: false
```

#### 2.3.3 Attribute-Level Access Rules

An **attribute access rule** refines permissions for individual attributes:

```
AttributeAccessRule:
  attribute:     TLStructuredTypePart  -- The specific attribute
  commandGroup:  BoundCommandGroup      -- Typically Read or Write
  roles:         Set<BoundedRole>       -- Roles that are permitted
```

Attribute-level rules **restrict** the type-level permissions. If no attribute-level rule exists, the type-level rule applies. If an attribute-level rule exists, only the roles listed in the attribute-level rule have access, provided they also have the corresponding type-level access.

Example:
```
attribute: myapp:Customer#salary
operation: READ
roles: {Manager}
-- Only Managers can see the salary field, even though Editors can see other Customer fields

attribute: myapp:Customer#status
operation: WRITE
roles: {Manager}
-- Only Managers can change the status, even though Editors can modify other fields
```

#### 2.3.4 Module-Level Defaults

Access rules can be defined at the **module level** as defaults for all types within that module:

```
ModuleAccessDefault:
  module:        TLModule            -- The model module
  commandGroup:  BoundCommandGroup    -- The command group
  roles:         Set<BoundedRole>
```

Precedence (most specific wins):
1. Attribute-level rules
2. Type-level rules
3. Module-level defaults

#### 2.3.5 Relationship to Existing Role Assignments

The model-based access rules define **which roles are needed** for an operation on a type. The existing role assignment mechanism continues to determine **which users have which roles on which instances**.

This separation is key: the new type-level rules only replace the view-based `PersBoundComp` configuration -- the part that was previously tied to the UI. The instance-level role assignments (direct `hasRole` records, rule-based role derivation via `RoleRule`, and security parent chain inheritance) remain unchanged. This means access control stays fine-grained at the instance level. For example, if user U has the `Editor` role on Customer instance C1 (via a direct assignment or a role rule) but not on Customer instance C2, then U can modify C1 but not C2 -- even though both are of type `Customer` and the type-level rule permits `Editor` to write `Customer` instances.

```
Access check for "Can user U perform command group G on instance I of type T?":

1. Look up TypeAccessRule(T, G) → required roles R
   (Falls back to module default if no type-level rule exists)

2. AccessManager.hasRole(U, I, R) → true/false
   (Uses existing role assignments, role rules, security parent chain)
```

For attribute-level checks:
```
Access check for "Can user U perform command group G on attribute A of instance I?":

1. Look up AttributeAccessRule(A, G)
   - If exists: required roles R = intersection(TypeAccessRule.roles, AttributeAccessRule.roles)
   - If not: required roles R = TypeAccessRule(A.owner, G).roles

2. AccessManager.hasRole(U, I, R) → true/false
```

#### 2.3.6 Object Creation

Creating an object requires satisfying two conditions that are checked independently:

**Condition 1 — CREATE right on the target type**: The user must hold a role that is granted the `Create` command group on the type being created. This is a standard type-level check (section 2.3.5), with the composition parent as the context object (or the module's security root when no parent exists):

```
TypeAccessRule(myapp:Milestone, Create) → required roles R_create
AccessManager.hasRole(U, contextObject, R_create)
```

**Condition 2 — WRITE right on the composition context** (only when a parent object exists): The user must additionally hold a write right on the parent that will contain the new object. This is the standard attribute-level check from section 2.3.5, with `A = Project#milestones` and `G = Write`:

```
AttributeAccessRule(Project#milestones, Write) defined?
  → required roles R_write = intersection(TypeAccessRule(Project, Write), AttributeAccessRule(..., Write))
else
  → required roles R_write = TypeAccessRule(Project, Write).roles

AccessManager.hasRole(U, P, R_write)
```

Both conditions must be satisfied. Full example:

```
"Can user U create a Milestone in Project P (via Project#milestones)?"

  Condition 1: TypeAccessRule(myapp:Milestone, Create) → R_create
               AccessManager.hasRole(U, P, R_create)

  Condition 2: attribute-level Write check on Project#milestones → R_write
               AccessManager.hasRole(U, P, R_write)

  → ALLOW only if both are true
```

**Top-level objects**: When no composition parent exists (e.g., creating a top-level Project), only condition 1 applies, using the module's security root as the context object. Condition 2 is vacuously satisfied since there is no containing object. The model is consistent across both cases: condition 1 is always required; condition 2 adds the container check whenever a container is present.

This design preserves context-sensitivity: user U may be permitted to create Milestones in general (condition 1) but only in projects where U holds a sufficient role (condition 2). Attribute-level granularity is also preserved: a user might be allowed to add milestones to a project (`Project#milestones`) but not sub-projects (`Project#subProjects`), controlled by separate attribute-level WRITE rules on the parent.

Configuration:
```xml
<class name="Milestone">
    <annotations>
        <access-rights>
            <grant operation="read"   roles="Viewer, Editor, Manager"/>
            <grant operation="write"  roles="Editor, Manager"/>
            <grant operation="create" roles="Editor, Manager"/>
            <grant operation="delete" roles="Manager"/>
        </access-rights>
    </annotations>
</class>

<class name="Project">
    <annotations>
        <access-rights>
            <grant operation="read"  roles="Viewer, Editor, Manager"/>
            <grant operation="write" roles="Editor, Manager"/>
        </access-rights>
    </annotations>
    <attributes>
        <reference name="milestones" type="myapp:Milestone" kind="list"
                   composition="true">
            <annotations>
                <access-rights>
                    <!-- Condition 2: who can add/remove milestones in a project -->
                    <grant operation="write" roles="Editor, Manager"/>
                </access-rights>
            </annotations>
        </reference>
        <reference name="subProjects" type="myapp:Project" kind="list"
                   composition="true">
            <annotations>
                <access-rights>
                    <!-- Only Managers can create/remove sub-projects -->
                    <grant operation="write" roles="Manager"/>
                </access-rights>
            </annotations>
        </reference>
    </attributes>
</class>
```

#### 2.3.7 Custom Business Operations

Real-world applications often have complex operations that go beyond simple data access. An "Approve Order" operation might change `order.status` to "approved", create a new `ApprovalRecord`, and update `project.approvedBudget`. Such operations cannot be meaningfully decomposed into atomic READ/WRITE/DELETE checks:

- **The permission is semantically distinct from the data it touches.** A regular Editor might be allowed to modify `Order#status` in general (e.g., changing it to "in progress"), but "Approve" requires the `Approver` role -- not because of *which* data is modified, but because of the *business meaning* of the action.
- **Deriving permissions from atomic operations is impractical.** It would require analyzing all data modifications the operation performs in advance, and the intersection of the role sets for each atomic operation might be too restrictive, too permissive, or simply not what the application intends.

The solution is to allow **custom `BoundCommandGroup`s on types**. Since both built-in operations (Read, Write, Delete) and custom business operations (Approve, Cancel) are `BoundCommandGroup`s, they use the same `TypeAccessRule` structure (see section 2.3.2). Custom command groups simply appear as additional grants in the type's access rights configuration. The access check is a single check against the context instance -- the same mechanism as for Read/Write/Delete. No decomposition into atomic operations is needed.

Example:
```
type: myapp:Order
commandGroup: Approve
roles: {Manager, Approver}
inherit: false

type: myapp:Order
commandGroup: Cancel
roles: {Manager}
inherit: false
```

The access check:
```
"Can user U approve Order instance O?"

  1. Look up TypeAccessRule(Order, Approve) → required roles R = {Manager, Approver}

  2. AccessManager.hasRole(U, O, R) → true/false
     (Same instance-level check as for data access operations)
```

The difference to the current system is that the `(BoundCommandGroup → roles)` mapping lives on the model type, not on a view. A command group like `Approve` configured in a view's `PersBoundComp` today becomes a grant on the `Order` type in the model-based system.

**Three levels of access rights** emerge:

1. **Data access operations** (READ, WRITE, DELETE): Defined on types and attributes, covering straightforward data access from any access path (UI, AI, REST, batch).
2. **Custom business operations** (approve, cancel, finish, ...): Defined on types, covering complex domain-specific actions. Checked against the context instance. The implementation of what the operation actually does remains opaque to the access rights system.
3. **Attribute-level restrictions**: Refine data access for individual properties and composition references.

Configuration example:
```xml
<class name="Order">
    <annotations>
        <access-rights>
            <grant operation="read"    roles="Viewer, Editor, Manager, Approver"/>
            <grant operation="write"   roles="Editor, Manager"/>
            <grant operation="delete"  roles="Manager"/>
            <grant operation="approve" roles="Manager, Approver"/>
            <grant operation="cancel"  roles="Manager"/>
        </access-rights>
    </annotations>
</class>
```

The AI assistant or REST API uses the data access operations (READ, WRITE, DELETE) and attribute-level restrictions for general data access. When invoking a specific business operation (e.g., through a tool or endpoint that corresponds to "approve this order"), it checks the custom operation.

#### 2.3.8 Configurable Security Parent Rules

The security parent hierarchy determines how role assignments are inherited across object structures: if user U has a role on a parent object, U implicitly has that role on all descendants. Currently the parent relationship is a Java method (`BoundObject.getSecurityParent()`) that must be overridden per type, making the security hierarchy opaque and inflexible.

Model-based access rights introduce **security parent rules** that define the parent relationship declaratively, using the same path navigation mechanism already used by role rules.

A **security parent rule** specifies, for instances of a given type, which related objects act as security parents:

```
SecurityParentRule:
  type:     TLClass            -- The model type this rule applies to
  path:     List<PathElement>  -- Attribute path from the instance to its security parent(s)
  inherit:  boolean            -- Whether the rule applies to sub-types
```

Multiple security parent rules for the same type yield multiple security parents per instance. This is essential when an object participates in multiple overlapping contexts:

```xml
<!-- Milestone's security parent is its Project -->
<security-parent-rule meta-element="myapp:Milestone" inherit="false">
    <path>
        <step attribute="project" inverse="false" meta-element="myapp:Milestone"/>
    </path>
</security-parent-rule>

<!-- Task has two security parents: its Milestone AND its Sprint -->
<security-parent-rule meta-element="myapp:Task" inherit="false">
    <path>
        <step attribute="milestone" inverse="false" meta-element="myapp:Task"/>
    </path>
</security-parent-rule>

<security-parent-rule meta-element="myapp:Task" inherit="false">
    <path>
        <step attribute="sprint" inverse="false" meta-element="myapp:Task"/>
    </path>
</security-parent-rule>
```

Paths follow the same `PathElement` semantics as role rules: `inverse="false"` follows a forward reference (the attribute value), `inverse="true"` navigates backwards (objects referencing this instance via the named attribute). Multi-step paths are supported.

**Multiple security parents and DAG traversal**

With multiple security parents, the security context forms a directed acyclic graph (DAG) rather than a chain. All places that previously walked the parent chain linearly become a breadth-first traversal over this DAG, with a visited-set guard against cycles:

```
Role check for instance I:
  1. Initialize queue = {I}, visited = {}
  2. While queue is not empty:
     a. Dequeue current object C
     b. If C is already in visited: skip
     c. Add C to visited
     d. Accumulate role assignments on C
     e. Enqueue all security parents of C
  3. Result: union of roles found at any visited node
```

**Backward compatibility**

If no security parent rule is configured for a type, the existing Java-based `BoundObject.getSecurityParent()` is used as a fallback. The `BoundObject` interface gains a default `getSecurityParents()` method:

```java
default Collection<? extends BoundObject> getSecurityParents() {
    BoundObject single = getSecurityParent();  // legacy fallback
    return single == null ? Collections.emptyList() : Collections.singletonList(single);
}
```

`AbstractBoundWrapper` overrides `getSecurityParents()` to consult configured `SecurityParentRule`s via `ElementAccessManager` first, and falls back to `getSecurityParent()` only when no rules are configured for the type.

**Integration with `ElementAccessManager`**

The `ElementAccessManager` loads and manages security parent rules alongside role rules: it resolves the `inherit` flag, propagates rules to sub-types, caches rules per `TLClass` for efficient lookup, and tracks which attributes participate in security parent paths so that cached role computations can be invalidated when those attributes change.

**Model annotation form**

Following the model-first principle, security parent rules can also be expressed as annotations on `TLClass` definitions in `*.model.xml` files:

```xml
<class name="Task">
    <annotations>
        <security-parents>
            <parent-path>
                <step attribute="milestone"/>
            </parent-path>
            <parent-path>
                <step attribute="sprint"/>
            </parent-path>
        </security-parents>
    </annotations>
</class>
```

Model annotations take precedence over config-file-based rules, consistent with the precedence rule in section 2.4.3.

#### 2.3.9 PersBoundComp as a TL Model Type

Some UI components have no typed domain model -- navigation menus, dashboards, administration panels, workflow overview screens. Because no `TLClass` is associated with these views, the type-level access rules from section 2.3.2 cannot be used directly.

**One class for all components**

The solution is to introduce a single new `TLClass` -- `tl.accounts:BoundComponent` -- and make every `PersBoundComp` instance in the knowledge base an instance of this class. This integrates `PersBoundComp` into the TL model without requiring one class per component.

`tl.accounts:BoundComponent` is a regular `TLClass` defined once in the `tl.accounts` model module. It carries the attributes that the existing `PersBoundComp` mechanism already maintains per instance:

| Attribute | Type | Meaning |
|-----------|------|---------|
| `name` | `String` | The component's identity key |
| `commandGroupRoles` | association | Per-instance mapping of command group → allowed roles (replaces the existing `needsRole` association) |
| `roleRules` | association | Per-instance role rules (see below) |
| `securityParent` | `tl.accounts:BoundComponent` | Parent component for role inheritance (section 2.3.8) |

**Per-instance access rights**

For domain types, access rights are defined at the type level via `TypeAccessRule` and shared by all instances. For `BoundComponent`, the access rights are **per-instance**: each component instance carries its own `(commandGroup → roles)` mapping stored in `commandGroupRoles`. This matches the existing `PersBoundComp` semantics exactly -- the `TypeAccessRule` mechanism is not used for `BoundComponent` instances.

The admin UI manages `BoundComponent` instances through the standard TL model tooling, just like any other business object.

**Per-instance role rules**

Role rules for a `BoundComponent` instance are stored as associations on the instance (in `roleRules`) and evaluated by `ElementAccessManager`. Two rule kinds are supported:

| Rule kind | Meaning |
|-----------|---------|
| `group-role` | All members of a named group receive a given role on this component instance |
| `type-role` | Users who hold a given role on any instance of a specified domain type receive a target role on this component instance |

**Security parent chain**

The `securityParent` attribute points from one `BoundComponent` instance to another, enabling role inheritance up the component hierarchy. The DAG traversal from section 2.3.8 applies unchanged.

**Security check**

The `SecurityObjectProvider` for a model-free component returns the `BoundComponent` instance identified by the component's name. The access check then proceeds as:

```
1. SecurityObjectProvider returns the BoundComponent instance P

2. Look up P.commandGroupRoles[commandGroup] → required roles R
   (per-instance lookup, not a TypeAccessRule)

3. AccessManager.hasRole(user, P, R)
   ├─ Checks direct hasRole records on P
   ├─ Evaluates role rules stored in P.roleRules
   └─ Walks P.securityParent chain (DAG traversal, section 2.3.8)
```

### 2.4 Configuration Model

The access rules are stored as part of the TL model, using annotations on model elements.

#### 2.4.1 Type-Level Configuration

Access rules are configured as annotations on `TLClass` definitions in `*.model.xml` files:

```xml
<class name="Customer">
    <annotations>
        <access-rights>
            <grant operation="read"   roles="Viewer, Editor, Manager"/>
            <grant operation="write"  roles="Editor, Manager"/>
            <grant operation="delete" roles="Manager"/>
        </access-rights>
    </annotations>
    <attributes>
        <property name="name" type="tl.core:String"/>
        <property name="salary" type="tl.core:Double">
            <annotations>
                <access-rights>
                    <grant operation="read"  roles="Manager"/>
                    <grant operation="write" roles="Manager"/>
                </access-rights>
            </annotations>
        </property>
        <reference name="contacts" type="myapp:Contact" kind="list"
                   composition="true">
            <annotations>
                <access-rights>
                    <!-- Controls who can create/remove contacts for this customer -->
                    <grant operation="write" roles="Editor, Manager"/>
                </access-rights>
            </annotations>
        </reference>
    </attributes>
</class>
```

#### 2.4.2 Module-Level Configuration

```xml
<module name="myapp">
    <annotations>
        <access-rights>
            <grant operation="read"   roles="Viewer, Editor, Manager"/>
            <grant operation="write"  roles="Editor, Manager"/>
            <grant operation="delete" roles="Manager"/>
        </access-rights>
    </annotations>
</module>
```

#### 2.4.3 In-App Configuration

Model-based access rights can also be configured through the administration UI, stored persistently in the knowledge base, and exported/imported as part of the application configuration. The in-app configuration takes precedence over the XML-based configuration.

### 2.5 Deriving View Permissions from Model Permissions

Once model-based access rules exist, view permissions can be **derived** rather than independently configured:

1. A view's `SecurityObjectProvider` determines the type of objects being displayed.
2. The type's access rules define the available operations.
3. The view's command groups are mapped to operations.
4. The roles for each command group are derived from the type's access rules.

```
View "Customer Details" displays type "myapp:Customer"
  → Read commands require: TypeAccessRule(Customer, READ).roles = {Viewer, Editor, Manager}
  → Write commands require: TypeAccessRule(Customer, WRITE).roles = {Editor, Manager}
  → Delete commands require: TypeAccessRule(Customer, DELETE).roles = {Manager}

View "Order Processing" displays type "myapp:Order"
  → Read commands require: TypeAccessRule(Order, READ).roles = {Viewer, Editor, Manager, Approver}
  → Write commands require: TypeAccessRule(Order, WRITE).roles = {Editor, Manager}
  → "Approve" commands require: TypeAccessRule(Order, Approve).roles = {Manager, Approver}
```

Custom `BoundCommandGroup`s in views are mapped to custom operations on the displayed type. This replaces the per-view `PersBoundComp` configuration for views that follow the standard pattern. Views with special requirements can still override with explicit view-level configuration.

### 2.6 API for Programmatic Access

A new service interface provides programmatic access to model-based permissions:

Since built-in operations (Read, Write, Delete) and custom business operations (Approve, Cancel, ...) are both `BoundCommandGroup`s, the API uses `BoundCommandGroup` as the unified operation type. There is no need for a separate `Operation` enum.

```java
public interface ModelAccessRights {

    /**
     * Returns the roles that are permitted to perform the given command group
     * on instances of the given type. Applies to both built-in command groups
     * (Read, Write, Delete) and custom business operations (Approve, Cancel, etc.).
     */
    Set<BoundedRole> getAllowedRoles(TLClass type, BoundCommandGroup commandGroup);

    /**
     * Returns the roles that are permitted to perform the given command group
     * on the given attribute. Relevant for READ and WRITE command groups to
     * implement attribute-level access restrictions.
     */
    Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute,
                                     BoundCommandGroup commandGroup);

    /**
     * Checks whether the given person can perform the given command group
     * on the given instance. Works for both built-in and custom operations.
     */
    boolean isAllowed(Person person, TLObject instance,
                      BoundCommandGroup commandGroup);

    /**
     * Checks whether the given person can perform the given command group
     * on the given attribute of the given instance.
     */
    boolean isAllowed(Person person, TLObject instance,
                      TLStructuredTypePart attribute,
                      BoundCommandGroup commandGroup);

    /**
     * Checks whether the given person can create a new child object in
     * the given composition attribute of the given parent instance.
     *
     * Convenience method equivalent to
     * isAllowed(person, parent, compositionAttribute, SimpleBoundCommandGroup.WRITE).
     */
    boolean isAllowedCreate(Person person, TLObject parent,
                            TLStructuredTypePart compositionAttribute);

    /**
     * Returns all types that the given person can perform the given
     * command group on (based on type-level rules; instance-level checks
     * still required for specific objects).
     */
    Set<TLClass> getAccessibleTypes(Person person, BoundCommandGroup commandGroup);
}
```

### 2.7 Integration Points

#### 2.7.1 AI Assistant

The AI assistant uses the `ModelAccessRights` API directly:
- Before reading data: `isAllowed(currentUser, instance, Read)`
- Before modifying data: `isAllowed(currentUser, instance, Write)`
- Before creating objects: `isAllowedCreate(currentUser, parent, compositionAttr)`
- Before business operations: `isAllowed(currentUser, instance, Approve)`
- To explain permissions: `getAllowedRoles(type, commandGroup)` → map to users

#### 2.7.2 REST API / OpenAPI

REST endpoints check model-level permissions instead of simulating view access:
- GET: `isAllowed(user, instance, Read)`
- PUT/PATCH: `isAllowed(user, instance, attribute, Write)` for each modified attribute
- POST: `isAllowedCreate(user, parent, compositionAttribute)`
- DELETE: `isAllowed(user, instance, Delete)`

#### 2.7.3 Existing UI

The existing UI continues to work unchanged. Views that have explicit `PersBoundComp` configurations use them. Views can optionally switch to derived permissions by configuring their `SecurityObjectProvider` to use model-based access rules.

#### 2.7.4 TL-Script

New TL-Script functions expose model-based access checks:
```
canRead($instance)
canWrite($instance)
canCreate($parent, $compositionAttribute)
canDelete($instance)
canReadAttribute($instance, $attribute)
canWriteAttribute($instance, $attribute)
canExecute($instance, $customOperation)
```

### 2.8 Migration Path

1. **Phase 1 -- Introduce model-based access rules**: Add the annotation-based configuration and the `ModelAccessRights` API. Both systems coexist; views continue to use `PersBoundComp`. Also introduce `SecurityParentRule` configuration and the `getSecurityParents()` API; existing Java overrides of `getSecurityParent()` continue to work as a fallback.

2. **Phase 2 -- AI and API integration**: The AI assistant and REST APIs use `ModelAccessRights` for their access checks.

3. **Phase 3 -- Derived view permissions**: Views can opt in to deriving their permissions from model-based rules. A compatibility mode detects inconsistencies between view-based and model-based configurations.

4. **Phase 4 -- Model-based as default**: New views default to derived permissions. Existing view-based configurations are migrated where possible. View-level overrides remain available for exceptional cases.

### 2.9 Answering the Key Questions

With model-based access rights in place:

**"Which users can read instances of type Customer?"**
→ `getAllowedRoles(Customer, Read)` gives the roles. Cross-referencing with role assignments on specific instances gives the users.

**"Why can't user Meier edit this order?"**
→ `getAllowedRoles(Order, Write)` = {Editor, Manager}. `AccessManager.getRoles(Meier, thisOrder)` = {Viewer}. Meier lacks the Editor or Manager role on this order.

**"Which data can the AI assistant access for the current user?"**
→ `getAccessibleTypes(currentUser, Read)` gives all readable types. For each type, instance-level checks filter to the specific objects the user can see.

**"Who can approve orders?"**
→ `getAllowedRoles(Order, Approve)` = {Manager, Approver}. The permission for the complex "approve" operation is defined directly on the Order type, independent of which views offer an Approve button.

**"What would change if we give role X write access to type T?"**
→ The impact is immediately visible: all users with role X on any T instance would gain write access. No need to trace through view configurations.
