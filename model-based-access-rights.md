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
3. **Security parent hierarchy** that enables role inheritance along object structures.
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

The model defines four fundamental operations, aligned with the existing `CommandGroupType`:

| Operation | Maps to CommandGroupType | Meaning |
|-----------|--------------------------|---------|
| `READ` | `CommandGroupType.READ` | View/query instances and their attribute values |
| `WRITE` | `CommandGroupType.WRITE` | Modify attribute values of existing instances |
| `CREATE` | `CommandGroupType.WRITE` | Create new instances |
| `DELETE` | `CommandGroupType.DELETE` | Delete instances |

Custom operations can extend this set, mapping to custom `BoundCommandGroup` definitions.

#### 2.3.2 Type-Level Access Rules

A **type access rule** defines which roles are permitted to perform which operations on instances of a given type:

```
TypeAccessRule:
  type:       TLClass          -- The model type this rule applies to
  operation:  Operation         -- READ, WRITE, CREATE, DELETE
  roles:      Set<BoundedRole>  -- Roles that are permitted
  inherit:    boolean           -- Whether the rule applies to sub-types
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
  attribute:  TLStructuredTypePart  -- The specific attribute
  operation:  Operation              -- READ or WRITE
  roles:      Set<BoundedRole>       -- Roles that are permitted
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
  module:     TLModule          -- The model module
  operation:  Operation
  roles:      Set<BoundedRole>
```

Precedence (most specific wins):
1. Attribute-level rules
2. Type-level rules
3. Module-level defaults

#### 2.3.5 Relationship to Existing Role Assignments

The model-based access rules define **which roles are needed** for an operation on a type. The existing role assignment mechanism continues to determine **which users have which roles on which instances**.

This separation is key: the new type-level rules only replace the view-based `PersBoundComp` configuration -- the part that was previously tied to the UI. The instance-level role assignments (direct `hasRole` records, rule-based role derivation via `RoleRule`, and security parent chain inheritance) remain unchanged. This means access control stays fine-grained at the instance level. For example, if user U has the `Editor` role on Customer instance C1 (via a direct assignment or a role rule) but not on Customer instance C2, then U can modify C1 but not C2 -- even though both are of type `Customer` and the type-level rule permits `Editor` to write `Customer` instances.

```
Access check for "Can user U perform operation O on instance I of type T?":

1. Look up TypeAccessRule(T, O) → required roles R
   (Falls back to module default if no type-level rule exists)

2. AccessManager.hasRole(U, I, R) → true/false
   (Uses existing role assignments, role rules, security parent chain)
```

For attribute-level checks:
```
Access check for "Can user U perform operation O on attribute A of instance I?":

1. Look up AttributeAccessRule(A, O)
   - If exists: required roles R = intersection(TypeAccessRule.roles, AttributeAccessRule.roles)
   - If not: required roles R = TypeAccessRule(A.owner, O).roles

2. AccessManager.hasRole(U, I, R) → true/false
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
            <grant operation="create" roles="Editor, Manager"/>
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
            <grant operation="create" roles="Manager"/>
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
```

This replaces the per-view `PersBoundComp` configuration for views that follow the standard pattern. Views with special requirements can still override with explicit view-level configuration.

### 2.6 API for Programmatic Access

A new service interface provides programmatic access to model-based permissions:

```java
public interface ModelAccessRights {

    /**
     * Returns the roles that are permitted to perform the given operation
     * on instances of the given type.
     */
    Set<BoundedRole> getAllowedRoles(TLClass type, Operation operation);

    /**
     * Returns the roles that are permitted to perform the given operation
     * on the given attribute.
     */
    Set<BoundedRole> getAllowedRoles(TLStructuredTypePart attribute, Operation operation);

    /**
     * Checks whether the given person can perform the given operation
     * on the given instance.
     */
    boolean isAllowed(Person person, TLObject instance, Operation operation);

    /**
     * Checks whether the given person can perform the given operation
     * on the given attribute of the given instance.
     */
    boolean isAllowed(Person person, TLObject instance,
                      TLStructuredTypePart attribute, Operation operation);

    /**
     * Returns all types that the given person can perform the given
     * operation on (based on type-level rules; instance-level checks
     * still required for specific objects).
     */
    Set<TLClass> getAccessibleTypes(Person person, Operation operation);
}
```

### 2.7 Integration Points

#### 2.7.1 AI Assistant

The AI assistant uses the `ModelAccessRights` API directly:
- Before reading data: `isAllowed(currentUser, instance, READ)`
- Before modifying data: `isAllowed(currentUser, instance, WRITE)`
- Before creating objects: check `getAllowedRoles(type, CREATE)` against user's roles
- To explain permissions: `getAllowedRoles(type, operation)` → map to users

#### 2.7.2 REST API / OpenAPI

REST endpoints check model-level permissions instead of simulating view access:
- GET: `isAllowed(user, instance, READ)`
- PUT/PATCH: `isAllowed(user, instance, attribute, WRITE)` for each modified attribute
- POST: `isAllowed(user, type, CREATE)`
- DELETE: `isAllowed(user, instance, DELETE)`

#### 2.7.3 Existing UI

The existing UI continues to work unchanged. Views that have explicit `PersBoundComp` configurations use them. Views can optionally switch to derived permissions by configuring their `SecurityObjectProvider` to use model-based access rules.

#### 2.7.4 TL-Script

New TL-Script functions expose model-based access checks:
```
canRead($instance)
canWrite($instance)
canCreate($type)
canDelete($instance)
canReadAttribute($instance, $attribute)
canWriteAttribute($instance, $attribute)
```

### 2.8 Migration Path

1. **Phase 1 -- Introduce model-based access rules**: Add the annotation-based configuration and the `ModelAccessRights` API. Both systems coexist; views continue to use `PersBoundComp`.

2. **Phase 2 -- AI and API integration**: The AI assistant and REST APIs use `ModelAccessRights` for their access checks.

3. **Phase 3 -- Derived view permissions**: Views can opt in to deriving their permissions from model-based rules. A compatibility mode detects inconsistencies between view-based and model-based configurations.

4. **Phase 4 -- Model-based as default**: New views default to derived permissions. Existing view-based configurations are migrated where possible. View-level overrides remain available for exceptional cases.

### 2.9 Answering the Key Questions

With model-based access rights in place:

**"Which users can read instances of type Customer?"**
→ `getAllowedRoles(Customer, READ)` gives the roles. Cross-referencing with role assignments on specific instances gives the users.

**"Why can't user Meier edit this order?"**
→ `getAllowedRoles(Order, WRITE)` = {Editor, Manager}. `AccessManager.getRoles(Meier, thisOrder)` = {Viewer}. Meier lacks the Editor or Manager role on this order.

**"Which data can the AI assistant access for the current user?"**
→ `getAccessibleTypes(currentUser, READ)` gives all readable types. For each type, instance-level checks filter to the specific objects the user can see.

**"What would change if we give role X write access to type T?"**
→ The impact is immediately visible: all users with role X on any T instance would gain write access. No need to trace through view configurations.
