---
description: Read before working on the tenants of the control application (tl-factory) and who may do what - tl.factory:Tenant with administrators, developers and readers and the enumeration TenantRole, one tenant per user (Tenants.checkMembers, factoryCheckTenantMembers), the roles tl.factory.Operator, tl.factory.Verwalter, tl.factory.Entwickler and tl.factory.Leser with their role rules, security parents (tenant to project, workspace and agent conversation by their container, accounts and git connections) and grants, the operation ManageTenants of the page Tenants, the management of tenants and members (TenantMembers.createTenant, createMember, changeRole, removeMember with FactoryAccess.commitUnchecked; factoryCreateTenant, factoryCreateMember, factoryChangeMemberRole, factoryRemoveMember, factoryMembers, factoryMemberRole, factoryCurrentTenant, factoryEmail, factoryProjects), Project#tenant, the write check of the workspace operations and agent messages (FactoryAccess.checkWrite), the views tenants.view.xml, members.view.xml, my-tenant.view.xml and their dialogs, the test TestTenantAccess and the test helper TestTenants.
order: 20
---

# Tenants

The control application serves several tenants (*Mandanten*). A tenant owns its projects - with
their workspaces and agent conversations - and its own git connections; its members see nothing of
another tenant. The operators of the installation manage all tenants.

## Model

`tl.factory:Tenant` has a unique `name` and one reference to `tl.accounts:Person` per role:
`administrators`, `developers`, `readers` (`FactoryModel.TENANT_MEMBERS`). The enumeration
`tl.factory:TenantRole` mirrors them (`TenantRole` with `ADMINISTRATOR`, `DEVELOPER`, `READER`,
`getReference()`). A user is a member of one tenant at most: the constraint of the three references,
`factoryCheckTenantMembers($object, $value)` (`Tenants.checkMembers`), names a user who is a member
of another tenant. `Tenants.tenantOf(person)` finds the tenant of a user.

`tl.factory:Project#tenant` is mandatory and read-only once the project exists. A new project
belongs to the tenant of its creator (default `factoryCurrentTenant()`); a user of no tenant - an
operator - chooses it in the dialog. `factoryProjects()` (`Tenants.projects`) lists the projects of
the tenant of the current user, all projects for a user who may read every project.

## Roles and rights

| Role | Held on | May |
|---|---|---|
| `tl.factory.Operator` | the security root, assigned in the role administration | everything: create and delete tenants, manage the members and connections of every tenant, work on every project; the page *Tenants*. |
| `tl.factory.Verwalter` | a tenant, as one of its `administrators` | manage the members and the own git connections of the tenant, work on its projects. |
| `tl.factory.Entwickler` | a tenant, as one of its `developers` | create, change and delete the projects of the tenant, work in their workspaces and talk to their agents. |
| `tl.factory.Leser` | a tenant, as one of its `readers` | read the tenant, its members, connections and projects; not the initial passwords of the workspaces. |

The configuration lives in `tl-factory.conf.config.xml`:

- **Role rules** (`TenantAdministrators`, `TenantDevelopers`, `TenantReaders`): a member holds the
  role of its reference on the tenant.
- **Security parents**: the security root for every tenant and project (`SecRootForFactoryTenant`,
  `SecRootForFactoryProject`), so the role of the operators reaches them; the tenant for its
  projects (`TenantForFactoryProject`). A `Workspace` and an `AgentConversation` follow their
  container (project, workspace). An account inherits the roles held on its tenant
  (`TenantForAdministrator`, `TenantForDeveloper`, `TenantForReader`), so the members of a tenant
  see each other. A git connection inherits from the tenants it is offered to, see [git
  hosting](doc:factory/git-hosting#git-connections).
- **Grants**: `tl.factory:Tenant` is read by all four roles, written by operators and
  administrators, created and deleted by operators; `tl.factory:Project` is read by all, written,
  created and deleted by operators, administrators and developers; `tl.accounts:Person` is read by
  all four (inherited); the attribute `tl.factory:Workspace#initialPassword` is read by all but the
  readers, since it logs into the application of the workspace with all rights.
- **The page *Tenants*** of `tl-factory-app` is shown to the holders of the operation
  `ManageTenants` on the security root (`<access-control operation="ManageTenants"/>`), granted to
  the operators; the technical super user `root` sees it as well.

The operations that change a workspace without writing it through the model - create, start, stop,
update and clean up a workspace, set up CI, start a build, create an agent conversation and send a
message to the agent - check the right to write the project or conversation
(`FactoryAccess.checkWrite`), so a reader may watch but not act. A message of the chat is checked
with the security of its user on the thread of the chat.

## Managing tenants and members

`TenantMembers` holds the operations. The accounts of the members belong to their tenant: a member
is created with a new account, and removing a member deletes its account. The right is checked for
the current user - creating a tenant needs the right to create `tl.factory:Tenant`, the other
operations the right to write the tenant -; the changes then run without access checks in a
transaction of their own (`FactoryAccess.commitUnchecked`), since an administrator of a tenant
holds no rights on accounts and the security root. The current user stays the author of the
changes.

| TL-Script | Java | What |
|---|---|---|
| `factoryCreateTenant(name, login, firstName, lastName, email, password)` | `TenantMembers.createTenant` | Creates a tenant with the account of its first administrator and offers it the built-in git connections. |
| `factoryCreateMember(tenant, role, login, firstName, lastName, email, password)` | `createMember` | Creates a member with a new account in the given role (a classifier of `TenantRole`); a login name in use is refused. |
| `factoryChangeMemberRole(tenant, member, role)` | `changeRole` | Replaces the roles of the member in the tenant by the given one; the last administrator keeps the role. |
| `factoryRemoveMember(tenant, member)` | `removeMember` | Removes the member and deletes its account; nobody removes himself or the last administrator. |
| `factoryMembers(tenant)` | `members` | The members in any role. |
| `factoryMemberRole(tenant, member)` | `roleOf` | The role of a member. |
| `factoryCurrentTenant()` | `Tenants.tenantOf` | The tenant of the current user, nothing for a user of no tenant. |
| `factoryEmail(account)` | `email` | The e-mail address of an account. |

A tenant is deleted by an operator once it has no members and no projects.

## The views

| View (`factory/…`) | Shows |
|---|---|
| `tenants.view.xml` | For the operators (sidebar item *Tenants*): all tenants, *Create tenant* (`create-tenant.view.xml`) and *Delete tenant*; beside the list the selected tenant with the tabs *Members* and *Git connections*. |
| `my-tenant.view.xml` | The members of the tenant of the user (sidebar item *Members*); a user of no tenant is told so. |
| `members.view.xml` | The members of the tenant in its channel `tenant` with login, name, e-mail and role; *Create member* (`create-member.view.xml`), *Change role* (`change-member-role.view.xml`) and *Remove member* for whoever may write the tenant. |

The dialogs edit transient objects of the module `tl.factory.input` (`NewTenant`, `NewMember`,
`MemberRole`), which is marked `internal`, and hand them to the functions above.

Two gaps outside the module still restrict the members of a tenant; `tl-factory/WORKLIST.md` lists
them with the tickets closing them.

## The tests

`TestTenantAccess` checks the rights against a knowledge base with two tenants, their members in
each role and an operator: what each role reads and writes of tenants, projects, workspaces and
conversations, the initial password hidden from readers, the operations needing the right to write,
the access to the preview route, one tenant per user, the management of members and tenants with
its refusals, the script functions, and that the access configuration covers the model. Tests of
other parts create their tenant and projects with the helper `TestTenants`.
