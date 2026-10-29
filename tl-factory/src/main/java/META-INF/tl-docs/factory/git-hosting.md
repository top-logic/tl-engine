---
description: Read before configuring or working on the git hosting of the control application (the repositories of the projects on a hosting service, per git connection) - git connections (tl.factory:GitConnection with name, providerType, url, owner, tenant, tenants, secretName; Project#gitConnection and Project#branchProtected), built-in connections of the installation (connections of GitHostingService, synchronized at startup by GitConnections.synchronizeBuiltIn, an organization per tenant by GitConnections.tenantOwner, created on first use) and own connections of a tenant (GitConnections.create/update/replaceToken/delete with the access token in the writable secret store under a generated name, refused while projects use it, the TL-Script functions factoryCreateGitConnection, factoryUpdateGitConnection, factoryReplaceGitToken, factoryDeleteGitConnection, factoryGitConnections, factoryOwnGitConnections, factoryDefaultGitConnection, factoryCheckGitConnection, factoryGitCapabilities, factoryGitProviderTypes, factoryGitBuiltIn, factoryGitOwner), who reads and changes connections (role parent GitConnection#tenants, grants), the provider types (GitProviderType registered under provider-types, GitConnectionSettings, GitConnectionKey, GitHostingService.provider and forget), GitHostingProvider (capabilities with GitCapabilities, createRepository, findRepository with RepositoryName.parse for a clone URL or owner/name, protectBranch, commitFile through the API without a clone, gitUrl, gitCredentials), GitRepository with canAdminister, the repository of a project (tl.factory:Project repository and repositoryUrl) and how a workspace gets it (RepositorySource as ApplicationSource of ApplicationWorkspaces.create: create, record, generate and initial push before protectBranch, the protection skipped with a warning and branchProtected false when the token may not administer the repository, clone of an existing or adopted repository with the pom.xml check of artifactId and tl.version, retry after a failed run, cleanup and deletion keeping the repository, factoryGitHosting and factoryCheckRepository of the create form with checkNewRepository, the views of the connections (factory/git-connections.view.xml and its dialogs, my-git-connections.view.xml, the tab in tenants.view.xml) and of the project, the agent instructions with --append-system-prompt), the protected-files of GitHostingService and the commit identity user-name / user-email of the GitRoute, the git route of the workspace proxy (GitRoute at /git passing git's smart HTTP protocol through to the provider of the connection of the project - GitRoute.Target, workspaceTarget, info/refs, git-upload-pack, git-receive-pack -, the repository of the project of the workspace only, FORWARDED_REQUEST_HEADERS with Git-Protocol and Content-Encoding, the 401 challenge with WWW-Authenticate, the credential helper through GIT_CONFIG_COUNT / GIT_CONFIG_KEY_n / GIT_CONFIG_VALUE_n and TL_FACTORY_PROXY_TOKEN, TL_FACTORY_GIT_URL, remoteUrl and projectRemoteUrl, git commands of the control application with issueProcessToken), what Gitea accepts for git over HTTP, the test doubles RecordingGitHostingProvider, RecordingGitProviderType and MemorySecretStore, the Gitea type GiteaProviderType of the module tl-factory-gitea (name gitea, timeout, required-approvals from tl_factory_gitea_required_approvals) with GiteaHostingProvider (the scopes of its access token write:organization, write:repository, read:user, the capabilities from userGetCurrent and orgGetUserPermissions with the scopes of the token checked by a creation request without name, the organization created with orgCreate, the namespace of the user, the built-in connection of tl-factory-app from tl_factory_gitea_url, tl_factory_gitea_owner and the secret tl_factory_gitea_token), what its branch protection enforces in Gitea (no force push, protected_file_patterns, block_admin_merge_override), commitFile with repoGetContents and repoCreateFile, the error keys, the client generated from the OpenAPI specification of Gitea with openapi-generator-maven-plugin (gitea.version, apisToGenerate, FILTER, modelsToGenerate, the exclusions from doclint and TLDoclet), its integration tests against a Gitea container (GiteaTestServer, TestGiteaHostingProvider, TestGiteaSwitch, TestGitRouteGitea with the git command line, TestRepositoryWorkspaces with Docker workspaces), TestGitConnections, TestProjectRepository, and updating Gitea.
order: 30
---

# Git hosting

The sources of a project can live in a repository on a git hosting service. The project reaches the
service through a **git connection**: the type of the server (e.g. Gitea), its URL, the owner the
repositories live in (an organization or a user), and credentials. A tenant chooses the connection
of a project when the project is created; without any connection to choose, the control application
works without repositories.

| Class | Role |
|---|---|
| `GitConnections` | The git connections in the model (`tl.factory:GitConnection`): the built-in connections of the installation, the own connections of the tenants with their access tokens, the choices of a new project, and the provider of a connection, see [git connections](#git-connections). |
| `GitHostingService` | Service holding the registered `GitProviderType`s, the built-in connections of the installation and one `GitHostingProvider` per connection in use; the protected files. |
| `GitProviderType` | A kind of hosting service, registered by name: creates the provider of a connection (`createProvider(GitConnectionSettings)`) and the credentials sending an access token kept in a named secret (`tokenCredentials`). |
| `GitHostingProvider` | Platform-neutral access to the hosting service of one connection: what its credentials may do (`capabilities`), create a repository of its owner, find an existing repository, protect a branch, commit a new file to a branch without a clone (`commitFile`, e.g. the pipeline file when the [CI](doc:factory/ci#setting-up-the-ci) of a project is set up); the URL and the credentials of git's smart HTTP protocol (`gitUrl`, `gitCredentials`) for the git route. |
| `GitRepository` | A repository: full name (`owner/name`), HTTP clone URL, web URL, default branch, and whether the credentials may administer it (`canAdminister`). |
| `RepositoryName` | Owner and name of a repository; `parse` reads what a user enters - `owner/name`, the clone URL, the URL of a page of the repository, an SSH clone URL - against the base URL of the service. |
| `GitRoute` | The route of the workspace proxy through which a workspace fetches from and pushes to the repository of its project, see [the git route](#the-git-route). |
| `GiteaProviderType` / `GiteaHostingProvider` | The type and the provider for Gitea, in the module `tl-factory-gitea`. |
| `RecordingGitHostingProvider`, `RecordingGitProviderType`, `MemorySecretStore` | Test doubles in the tests of `tl-factory`: repositories and branch protections in memory, the calls recorded; a provider type `recording` creating them; a writable secret store in memory. |

## Git connections

A `tl.factory:GitConnection` has a `name`, the `providerType` (the name of a registered
`GitProviderType`, options `factoryGitProviderTypes()`), the `url` of the server and the `owner`.
There are two kinds:

- **Built-in** connections are configured in the installation (`connections` of
  `GitHostingService`) and offered to every tenant; they have no `tenant`. They authenticate with the
  credentials of the installation. Every tenant gets an organization of its own on the server, so
  that the tenants never see the repositories of each other: its name is the configured `owner`, a
  dash and the identifier of the tenant (`GitConnections.tenantOwner`, lower case letters, digits
  and dashes, at most 39 characters), which does not change when the tenant is renamed and is never
  reused. The provider of a built-in connection creates the organization with the first repository
  of the tenant (`GitConnectionSettings.createOwner`).
- **Own** connections are created by the administrators of a tenant (`tl.factory.Verwalter`, or an
  operator) with an access token of their own; `tenant` is the tenant. The token is stored in the
  [writable secret store](doc:credentials/secret-stores#secrets-written-by-the-application) under a
  generated name `tl_factory_git_<random>`, held in the hidden attribute `secretName`; it is never
  stored in the model, never shown again and only replaced: `replaceToken` stores the new token under
  a new name and removes the old secret. Deleting the connection removes its secret. Without a
  writable secret store, creating a connection fails with `ERROR_NO_WRITABLE_SECRET_STORE`.

`GitConnections.synchronizeBuiltIn()` runs when `ProjectWorkspaces` starts: every switched-on
built-in connection gets an object of its name with the type, URL and owner of the configuration,
offered to every tenant; the object of a connection that is no longer configured is deleted unless
projects live on it (then it stays, and is not available). A tenant created by `TenantMembers.createTenant`
is offered the built-in connections at once (`offerBuiltIn`).

**Access.** The attribute `tenants` holds the tenants whose members may choose the connection: its
tenant, or every tenant for a built-in connection. It is the role parent of the connection (rule
`TenantsForGitConnection`), and the security root is the other one (`SecRootForGitConnection`), so
the members of these tenants and the operators read it. Only an operator writes a connection through
the model; the administrators of a tenant change the own connections through the functions below,
which check the right to write the tenant and commit without access checks
(`FactoryAccess.commitUnchecked`), like the management of the members. A built-in connection is
changed by its configuration only (`ERROR_BUILT_IN_CONNECTION__CONNECTION`).

| TL-Script | Java | What |
|---|---|---|
| `factoryCreateGitConnection(tenant, name, providerType, url, owner, token)` | `GitConnections.create` | Creates an own connection; the type checks the URL without contacting the server. |
| `factoryUpdateGitConnection(connection, name, providerType, url, owner)` | `update` | Changes it; type, URL and owner only while no project lives on it (`ERROR_CONNECTION_SERVER_IN_USE__CONNECTION_PROJECTS`). |
| `factoryReplaceGitToken(connection, token)` | `replaceToken` | Replaces its access token. |
| `factoryDeleteGitConnection(connection)` | `delete` | Deletes it and its secret; refused while projects live on it (`ERROR_CONNECTION_IN_USE__CONNECTION_PROJECTS`, naming them). |
| `factoryGitConnections(tenant)` | `choices` | The connections a new project of the tenant may live on: the available built-in connections, then the available own ones; empty tenant for the tenant of the current user. |
| `factoryDefaultGitConnection()` | `defaultChoice` | The first of them, the default of `Project#gitConnection`. |
| `factoryOwnGitConnections(tenant)` | `ownConnections` | The own connections of a tenant. |
| `factoryGitBuiltIn(connection)` | `isBuiltIn` | Whether it is built in. |
| `factoryGitOwner(connection, tenant)` | `ownerFor` | The owner the repositories of the tenant are created in: the organization of the tenant on a built-in connection; empty tenant for the tenant of the current user. |
| `factoryGitCapabilities(connection, tenant)` | `capabilities` | What the token may do for the repositories of the tenant: a dictionary with `createRepositories`, `protectBranches`, `owner` and, if the service could not tell, `problem`. |
| `factoryCheckGitConnection(project, value)` | `checkProjectConnection` | The constraint of `Project#gitConnection`: a project that is created needs one of the choices of its tenant, if there are any. |
| `factoryGitHosting()` | | Whether the current user has a connection to choose. |

A connection is **available** if its type is registered and, for a built-in connection, it is
configured and switched on. The provider of an unavailable connection is `null`: the git route
answers 404, and creating a workspace of a project on it fails with
`ERROR_CONNECTION_UNAVAILABLE__CONNECTION`.

**The project.** `Project#gitConnection` is chosen when the project is created (default
`factoryDefaultGitConnection()`, options `factoryGitConnections($self.get(`tl.factory:Project#tenant`))`)
and read-only afterwards. `FactoryModel.getGitConnection(project)` reads it,
`GitConnections.projectProvider(project)` gives the provider of the connection for the tenant of the
project. `Project#branchProtected` records whether the default branch of the repository is
protected, see [how a project gets its repository](#how-a-project-gets-its-repository).

### The views

| View (`factory/…`) | Shows |
|---|---|
| `git-connections.view.xml` | The connections of the tenant in its channel `tenant`: the built-in ones (kind *built-in*, read-only), then the own ones, each with type, URL, the owner of the tenant (`factoryGitOwner`) and the rights of the token (`factoryGitCapabilities`, or its problem). *Create connection*, *Edit*, *Replace token* and *Delete* need the right to write the tenant (`<access><operation name="Write" object="$tenant" type="tl.factory:Tenant"/></access>`) and are disabled for a built-in connection; *Delete* reports the projects living on the connection. |
| `create-git-connection.view.xml`, `change-git-connection.view.xml`, `replace-git-token.view.xml` | The dialogs; they edit transient objects of `tl.factory.input` (`NewGitConnection`, `ChangedGitConnection`, `NewGitToken`), whose `token` is entered in a masked field (`PasswordInputControlProvider`) and handed to the function only. |
| `my-git-connections.view.xml` | The connections of the tenant of the user, the sidebar item *Git connections* of `tl-factory-app`. |
| `tenants.view.xml` | For the operator, the connections of the selected tenant in the tab *Git connections* beside its members. |
| `project-detail.view.xml` | The connection of the project with its kind, and a warning when the default branch of the repository is not protected (`branchProtected` explicitly false, not while it is empty). The initial root password of the workspace is shown to whoever may read it: the attribute grant on `tl.factory:Workspace#initialPassword` leaves out the readers of the tenant. |
| `create-project.view.xml` | The field *Git connection* and the field *Existing repository* with a hint that an empty field creates a new repository. |

### Providers and capabilities

`GitHostingService.provider(GitConnectionKey)` hands out one provider per key and creates it on the
first request. The key holds everything the provider depends on - the identifier of the connection,
the type, URL, owner, the built-in connection or the secret name of the token, whether it creates
its owner -, so a changed connection, e.g. after a new token, gets a new provider; `forget(connection)`
drops the providers of a connection after a change. Creating a provider does not contact the server.

`GitHostingProvider.capabilities()` asks the hosting service what the credentials may do in the
owner: `GitCapabilities` with `createRepositories`, `protectBranches` and a `problem` if the service
could not be asked (then nothing is assumed to be allowed). An owner the provider creates itself and
that does not exist yet counts as fully accessible. A single repository tells for itself whether the
credentials may administer it (`GitRepository.canAdminister()`).

## Configuration

`tl-factory` registers the service without provider type and without built-in connection. A module
supporting a kind of hosting service registers its type; `tl-factory-gitea` (`com.top-logic:tl-factory-gitea`)
does so in `tl-factory-gitea.conf.config.xml`:

```xml
<config service-class="com.top_logic.factory.git.GitHostingService">
	<instance class="com.top_logic.factory.git.GitHostingService">
		<provider-types>
			<provider-type name="gitea"
				class="com.top_logic.factory.gitea.GiteaProviderType"
				required-approvals="%FACTORY_GITEA_REQUIRED_APPROVALS%"
			/>
		</provider-types>
	</instance>
</config>
```

| Property of `GiteaProviderType` | Default | Meaning |
|---|---|---|
| `name` | *(mandatory)* | The name the connections name the type by, `gitea`. |
| `timeout` | `30s` | How long a connection and each request may take. |
| `required-approvals` | `1` (`tl_factory_gitea_required_approvals`) | Approvals a pull request to a protected branch needs. |

The installation adds its built-in connections. `tl-factory-app` configures one in
`tl-factory-app.conf.config.xml`, from the environment:

```xml
<config service-class="com.top_logic.factory.git.GitHostingService">
	<instance class="com.top_logic.factory.git.GitHostingService">
		<connections>
			<connection name="gitea"
				owner="%FACTORY_GITEA_OWNER%"
				provider-type="gitea"
				token-secret="tl_factory_gitea_token"
				url="%FACTORY_GITEA_URL%"
			/>
		</connections>
	</instance>
</config>
```

| Property of a built-in connection | Meaning |
|---|---|
| `name` | The name of the connection, also the name of its object in the model. |
| `provider-type` | The name of a registered type; an unknown type fails the start of the service. |
| `url` | The URL of the server (Gitea: its `ROOT_URL`, the REST API at `api/v1` below it); empty switches the connection off. |
| `owner` | The start of the names of the organizations of the tenants. |
| `token-secret` | The [named secret](doc:credentials/secret-stores) holding the access token of the installation, sent as the type sends an access token (Gitea: `Authorization: token <token>`), read for each request, so a rotated token takes effect without a restart. |
| `credentials` | Any `CredentialProvider` instead of the token secret, see [credential providers](doc:credentials/credential-providers). |

| Variable of `tl-factory-app` | Default | Meaning |
|---|---|---|
| `tl_factory_gitea_url` | *(empty)* | `url` of the built-in connection; empty switches it off. |
| `tl_factory_gitea_owner` | `tl-factory` | `owner` of the built-in connection. |
| `tl_factory_gitea_token` | - | The access token of the installation, see [the access token](#the-access-token). |

A request rejected with 401 is repeated once with fresh credentials.

### The access token

A Gitea access token of a connection - the one of the installation and those the tenants enter -
needs the scopes

- `write:organization` - creating a repository in an organization, and the organizations of the
  tenants on a built-in connection,
- `write:repository` - reading repositories and creating and changing their branch protection,
- `read:user` - telling its rights (`capabilities`, from the user of the token and its permissions
  in the organization); without it, the capabilities report
  `ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM`,
- `write:user` - only for repositories in the namespace of the user of the token.

The user must be allowed to create repositories in the organization and to administer them to
protect their branches: a member of the team *Owners* of the organization, or of a team with the
permission to create repositories and administrator access to them. On a built-in connection, the
user of the token creates the organizations of the tenants and owns them; Gitea must allow it to
create organizations (an administrator always may). A token with fewer scopes or a user with fewer
rights gets `ERROR_PERMISSION_DENIED__REPOSITORY`; a repository the user may not administer is
reported by `canAdminister()`, and its branch stays unprotected.

The permissions of the user do not tell the scopes of the token, and Gitea shows a token not its own
scopes. Where the permissions allow creating, `capabilities` therefore sends the creation request
(`createCurrentUserRepo` in the namespace of the user, `createOrgRepo` in an organization, `orgCreate`
for an organization the provider creates) without a name: Gitea checks the scopes first and answers
403 naming the missing one (e.g. `write:user`), otherwise 422 for the missing name, which creates
nothing. A missing scope makes the rights unknown (`ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM` with
Gitea's message), so the capabilities never claim a right the token does not have.

## Repositories

- `createRepository(name, description)` creates an empty, private repository of the owner, without
  initial commit; the first push creates its default branch (the `DEFAULT_BRANCH` of the server,
  usually `main`). `GiteaHostingProvider` creates it in the organization of the owner; if there is no
  such organization and the owner is the user of the token, in the namespace of the user. A provider
  creating its owner first creates the organization (private, `orgCreate`) unless it exists.
- `findRepository(reference)` accepts what a user enters to adopt an existing repository and
  returns `null` if it names no repository of the server or the repository does not exist or is not
  readable with the token (Gitea answers 404 or 403). Repositories of any owner are found.

## Branch protection

`protectBranch(repository, branch, protectedFiles)` creates a branch protection rule named like the
branch, or replaces the settings of an existing one. `GiteaHostingProvider` sets:

| Setting | Value |
|---|---|
| `enable_push` | on, without push allowlist: every user with write access may push |
| `enable_force_push` | off |
| `protected_file_patterns` | the patterns joined by `;`, e.g. `deploy/**;Jenkinsfile` |
| `unprotected_file_patterns` | none |
| `block_admin_merge_override` | off, without bypass allowlist |
| `required_approvals` | `required-approvals` |
| `block_on_rejected_reviews`, `dismiss_stale_approvals` | on |

Gitea then enforces on the branch:

- It cannot be deleted, and a push that rewrites its history is rejected (*protected from force
  push*) - for every user, administrators included.
- A push that changes a protected file is rejected (*protected from changing file*) - for every
  user, administrators included. Gitea matches the lower-cased path against the patterns; `*`
  matches neither `/` nor `.`, `**` any sequence.
- A pull request that changes a protected file can be merged only by a user who may bypass the
  protection: an administrator of the repository. His merge ignores the required approvals. For
  every other user, Gitea blocks the merge for the changed protected files.
- Every other pull request needs `required-approvals` approvals and no rejecting review; a new
  commit dismisses the approvals.
- Any other push that adds commits on top of the branch is accepted.

A protected file cannot be committed through the contents API of Gitea either, so a file to be
protected is committed first: `commitFile(repository, branch, path, content, message)` asks Gitea
for the file on the branch (`repoGetContents`) and creates it if it is missing (`repoCreateFile`,
committed by the user of the token, the content Base64-encoded); a file that exists is kept and
the call answers `false`.

Protect a branch after the push that creates it. Gitea (checked with 28.2.0) rejects the push
creating a branch whose rule has protected file patterns - the pre-receive hook fails with
*cannot find the start commit* - so the first push into an empty repository happens before
`protectBranch`.

The token of a connection that may protect the branch is the one of an organization owner or
administrator; with it, a direct push is restricted like any other, a merge of a pull request is
not.

## Errors

The provider reports failures as `TopLogicException` with the keys of
`com.top_logic.factory.git.I18NConstants`:

| Key | When |
|---|---|
| `ERROR_REPOSITORY_EXISTS__REPOSITORY` | The owner has a repository of the name (Gitea: 409). |
| `ERROR_INVALID_REPOSITORY_NAME__NAME` | Gitea does not accept the name (422). |
| `ERROR_OWNER_NOT_FOUND__OWNER` | The organization does not exist (404 when creating, or in the capabilities of a provider not creating its owner). |
| `ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM` | `capabilities` cannot learn the rights of the token, e.g. it lacks the scope `read:user`. |
| `ERROR_REPOSITORY_NOT_FOUND__REPOSITORY` | The repository to protect does not exist. |
| `ERROR_PERMISSION_DENIED__REPOSITORY` | The token lacks a scope or its user a right (403); the details carry Gitea's message. |
| `ERROR_AUTHENTICATION_FAILED__SERVICE` | Gitea rejects the token also on the second attempt (401). |
| `ERROR_SERVICE_UNREACHABLE__SERVICE_PROBLEM` | No answer, e.g. connection refused or timeout. |
| `ERROR_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE` | Any other answer, with status and Gitea's message. |

## The repository of a project

A project has at most one repository, the source of truth of its application. `tl.factory:Project`
holds it in `repository`, its full name `owner/name`, and `repositoryUrl`, the page of the
repository in the web interface; both are read-only once the project exists.
`FactoryModel.getRepository(project)` reads the full name as `RepositoryName`,
`FactoryModel.setRepository(project, repository)` sets both from a `GitRepository`. The detail view
of a project shows the repository as a link to its page.

### How a project gets its repository

For a project with a git connection, `ProjectWorkspaces.create` (TL-Script `factoryCreateWorkspace`)
puts a `RepositorySource` with the provider of the connection on the `ApplicationSpec` of the new
workspace. `ApplicationWorkspaces.create`
runs this `ApplicationSource` in the started workspace in place of the archetype generation, then
builds the application as always. All git commands run in the workspace through the git route,
with a token issued for them by `issueProcessToken` and revoked as soon as they are done:

| Case | Steps |
|---|---|
| New application: `repository` empty | `createRepository(artifactId, project name)` in the owner of the connection (on a built-in connection the organization of the tenant) and record it on the project at once, in a transaction of its own; `git ls-remote` finds the repository empty; generate from the archetype; `git init --initial-branch=<default branch>`, `git add --all`, `git commit` (message `RepositorySource.INITIAL_COMMIT_MESSAGE` and the engine version), `git remote add origin <remote>`, `git push --set-upstream origin <default branch>`; `protectBranch(default branch, protected files)`; build. |
| Existing repository: `repository` set | `findRepository(reference)`, not found fails with `ERROR_UNKNOWN_REPOSITORY__REFERENCE`; record it under its full name; `git ls-remote` finds commits; `git clone <remote> /workspace/<artifactId>`; check the `pom.xml`; `protectBranch`; build. |
| No git connection | Generate from the archetype and build; the project has no repository. |
| Connection not available | The creation fails with `ERROR_CONNECTION_UNAVAILABLE__CONNECTION`. |

The branch is protected only if the credentials may administer the repository
(`GitRepository.canAdminister()`); otherwise the step is skipped, the output gets the line
`Warning: The access token of the git connection may not administer the repository ...` naming the
protected files, and the build goes on. The outcome is recorded in `Project#branchProtected`
(`true` or `false`, in a transaction of its own); the attribute is a `tl.core:Tristate`, empty until
this step decides, so the detail view warns about an unprotected branch only after the attempt. The agent of a project whose branch is not
protected is told no protected files.

The remote is `GitRoute.remoteUrl(<route URL>, repository)`; without workspace proxy or git route
the source fails with `ERROR_NO_GIT_ROUTE`. The branch is protected after the first push, which
Gitea rejects for a branch with protected files (see [branch protection](#branch-protection)), and
also for an existing repository, which has its default branch already.

**Adopting a repository.** The form creating a project has the field *Existing repository* while
the user has a git connection to choose (TL-Script `factoryGitHosting()`): the full name
`owner/name`, the clone URL or the URL of a page of a repository on the server of the chosen
connection, as `RepositoryName.parse` reads it. The constraint of the attribute
(`factoryCheckRepository($object, $value)`, `RepositorySource.checkReference`) asks
`findRepository` of the provider of the chosen connection and names the reference in `ERROR_UNKNOWN_REPOSITORY__REFERENCE` if there is no
such repository; it does not ask again once the project has recorded its repository (its
`repositoryUrl` is set), so storing the recorded repository does not contact the hosting service.
Left empty for a project being created (`GitConnections.isCreating`: a transient draft or a form
editing one), the constraint asks the capabilities of the provider (`RepositorySource.checkNewRepository`)
and requires an existing repository if the token may not create repositories in the owner
(`ERROR_NO_NEW_REPOSITORY__OWNER`) or its rights are unknown (`ERROR_NEW_REPOSITORY_UNKNOWN__OWNER_PROBLEM`).
The form reads the chosen connection through the overlay, so the check runs again when it changes.
The group, artifact and engine version are entered as for a new application. After the clone,
`RepositorySource.Pom` reads the `pom.xml`: its own `artifactId` must be the artifact of the project
(`ERROR_ARTIFACT_MISMATCH__REPOSITORY_FOUND_EXPECTED`), and if it names the TopLogic version
literally - the property `tl.version` of an application of the archetype `tl-archetype-app`,
otherwise the version of the parent `tl-parent-app` - it must be the engine version of the project
(`ERROR_ENGINE_VERSION_MISMATCH__REPOSITORY_FOUND_EXPECTED`). A repository without `pom.xml` fails
with `ERROR_NO_POM__REPOSITORY`. A failed check leaves the branch unprotected.

**Retry.** A failure puts the workspace into state `ERROR` with the output in its log. The
repository created by a failed run stays recorded, so the retry - *Clean up*, then *Create
workspace* - finds it instead of failing with `ERROR_REPOSITORY_EXISTS__REPOSITORY`:

- Failed before the first push (e.g. the push was rejected): `git ls-remote` finds the repository
  empty, and the retry generates and pushes again.
- Failed after the push (e.g. the protection or the build): the retry clones the repository.

**Cleanup and deletion keep the repository.** *Clean up* removes the workspace with its clone; the
project keeps its repository, and the next workspace clones it - work the agent did not push is
lost with the workspace. Deleting the project leaves the repository on the hosting service as
well; a new project of the same artifact then fails with `ERROR_REPOSITORY_EXISTS__REPOSITORY` and
adopts the repository by entering it instead.

**The agent.** For a project with repository, `StoredConversation.instructions()` tells the coding
agent (`RepositorySource.agentInstructions`, passed by `ClaudeCodeHarness` as
`--append-system-prompt`) that its work reaches the application only by commit and push to the
default branch, that it must not force push, and which files are protected
(`ProjectWorkspaces.protectedFiles`); for a project with a [CI](doc:factory/ci#the-ci-route) job,
also how it builds the application on the CI server.

### Protected files and commit identity

| Setting | Default | Meaning |
|---|---|---|
| `protected-files` of `GitHostingService` (`%FACTORY_GIT_PROTECTED_FILES%`, environment `tl_factory_git_protected_files`) | `deploy/**` | Glob patterns of the files no push to the default branch of a project repository may change; the pipeline file of the CI server is protected only for a project whose [CI](doc:factory/ci#the-pipeline-definition) is set up, and is added then, even if it is not listed. |
| `user-name` of `GitRoute` (`%FACTORY_GIT_USER_NAME%`, `tl_factory_git_user_name`) | `TopLogic Factory` | Author and committer name of the commits made in a workspace; empty for none. |
| `user-email` of `GitRoute` (`%FACTORY_GIT_USER_EMAIL%`, `tl_factory_git_user_email`) | `tl-factory@localhost` | Author and committer e-mail address; empty for none. |

The protected files are a policy of the repositories on the hosting service, independent of the
provider and of the route, so they belong to `GitHostingService`. The identity belongs to the git
route: it contributes `user.name` and `user.email` (`GitRoute.GIT_USER_NAME`,
`GitRoute.GIT_USER_EMAIL`) to the git configuration of every process with a proxy token - the
agent and the git commands of the control application alike -, after the credential helper. As
`GIT_CONFIG_*` entries they take precedence over the configuration files in the workspace; the
container has no identity in its environment.

## The git route

The agent in a workspace fetches from and pushes to the repository of its project through the
route `GitRoute` of the [workspace proxy](doc:factory#the-workspace-proxy), at `/git` (its
`path`). The workspace gets no credential of the hosting service; the route adds the one of the git
connection of the project. `GitRoute.workspaceTarget(workspaceId)` resolves the workspace to its
project, the repository of the project and the provider of its connection (`GitRoute.Target`), for
every request, so a changed connection takes effect at once. The branch protection of the hosting service - no force push, protected
files only through a reviewed pull request - applies to these pushes as to any other, and its
rejection reaches the git client as `remote:` lines.

### Requests

A repository is addressed below the route as `/<owner>/<name>.git` (the suffix `.git` may be
missing). Only git's smart HTTP protocol passes:

| Method | Path below the repository | Query |
|---|---|---|
| `GET` | `info/refs` | `service=git-upload-pack` (fetch, clone) or `service=git-receive-pack` (push) |
| `POST` | `git-upload-pack` | |
| `POST` | `git-receive-pack` | |

Every other request is answered with 404: another path or method (the dumb protocol, LFS,
`git-upload-archive`), a path with dot or empty segments, the repository of another project, any
repository of a workspace whose project has none or whose project has no available git connection.
The repository must be the one of the project of the workspace the token was issued for, owner and name compared without regard to case; a refused repository is logged at level INFO
and answered like one that does not exist.

### Forwarding

The route forwards to `gitUrl(repository)` of the provider of the connection - for Gitea the clone
URL below the URL of the server -, with the owner and name as stored in the project, and the query rebuilt from the
service alone. Of the request, only the headers of git's protocol pass
(`GitRoute.FORWARDED_REQUEST_HEADERS`): `Content-Type`, `Content-Encoding` (git compresses
`git-upload-pack` requests with gzip), `Git-Protocol` (protocol version 2), `Accept`,
`Accept-Encoding`, `Accept-Language`, `User-Agent`. Everything else is dropped - the token of the
client, cookies, and headers the hosting service might trust from the address of the control
application, e.g. the user header `X-WEBAUTH-USER` of Gitea's reverse proxy authentication. The
request is authenticated with `gitCredentials()` of the provider. The body is streamed to the
hosting service as it arrives, never held as a whole; status, headers (`Content-Type`,
`Cache-Control`, `Content-Encoding`, ...) and body of the answer are passed back as they come,
except cookies and hop-by-hop headers.

| Setting of the route | Default | Meaning |
|---|---|---|
| `path` | `/git` | Path of the route below the proxy. |
| `connect-timeout` | `10s` | How long connecting to the hosting service may take. |
| `response-timeout` | `30min` | How long the hosting service may take until the status of its answer; it answers a push after receiving all of it, so this includes the transfer of the largest push. |

If the hosting service rejects the credentials (401), the route invalidates them (see [credential
providers](doc:credentials/credential-providers)). A `GET` of `info/refs` has no body and is
repeated once with fresh credentials; a `POST` cannot be repeated - its body is streamed and gone
- and is answered with 502, as is a second rejection. Since git starts every fetch and push with
`info/refs`, rotated credentials are picked up there. The client never gets a 401 of the hosting
service, which it would take for a rejection of its own token.

Refusals of the proxy are answered as plain text with their status (403, 429). A missing or
invalid token is answered with 401 and `WWW-Authenticate: Basic realm="TopLogic workspace proxy"`:
git sends its first request without credentials and asks its credential helper only after such a
challenge.

### Git in the workspace

The token reaches git without a file in the container, through the environment of the process
(`ProxyTokens.processEnv`):

- `TL_FACTORY_PROXY_TOKEN` (`ProxyTokens.ENV_PROXY_TOKEN`) holds the token of the process; the
  proxy exports it for every process, independent of the routes.
- The route contributes git configuration entries (`ProxyRoute.gitConfig(routeUrl)`), which the
  proxy passes as `GIT_CONFIG_COUNT`, `GIT_CONFIG_KEY_<n>`, `GIT_CONFIG_VALUE_<n>` (git 2.31 or
  newer; `GitEnvironment`). The proxy collects the entries of all routes into this one set, so no
  route overwrites the entries of another. The git route sets, for the URL of the route,
  `credential.<route URL>.helper` first to the empty value - dropping helpers configured before
  for that URL - and then to an inline helper (`GitRoute.CREDENTIAL_HELPER`) that answers `get`
  with `username=tl-factory` and `password=$TL_FACTORY_PROXY_TOKEN` and ignores `store` and
  `erase`. Git matches the URL as a prefix of the path of a request, so the token goes to the
  route only, never to another host; neither key nor value contains the token.
- A workspace container gets the URL of the route in `TL_FACTORY_GIT_URL` (no secret). The remote
  of the repository of a project is `<TL_FACTORY_GIT_URL>/<owner>/<name>.git`
  (`GitRoute.remoteUrl`); `GitRoute.projectRemoteUrl(project)` gives it for a stored project
  through the running proxy.

A git command the control application itself runs in a workspace, e.g. a clone, gets a token of its
own for that command: `ProxyTokens.issueProcessToken(workspaceId, lifetime)` issues it together with
the environment, and closing the `ProcessToken` revokes it.

```java
try (ProcessToken token = WorkspaceProxyService.getInstance().issueProcessToken(workspaceId, lifetime)) {
	provider.exec(workspaceId, ExecCommand.of("git", "clone", remote, dir).putEnv(token.env()), output);
}
```

### Gitea and git over HTTP

Gitea (checked with 28.2.0) accepts an access token for git over HTTP as `Authorization: token
<token>` - the scheme of its API requests -, as bearer token and as password (or user name) of
basic authentication; `GiteaHostingProvider.gitCredentials()` are the credentials of its API
requests. Its token needs `write:repository` for a push. Gitea answers a request without
credentials with 401 and `WWW-Authenticate: Basic realm="Gitea"`, finds repositories also without
`.git` and without regard to case, and serves protocol version 2.

### Configuration

`tl-factory` configures the route next to the LLM route and the [CI route](doc:factory/ci#the-ci-route):

```xml
<routes>
	<route class="com.top_logic.factory.llm.LLMRoute" provider="%FACTORY_LLM_PROVIDER%"/>
	<route class="com.top_logic.factory.git.GitRoute"
		user-email="%FACTORY_GIT_USER_EMAIL%"
		user-name="%FACTORY_GIT_USER_NAME%"
	/>
	<route class="com.top_logic.factory.ci.CIRoute"/>
</routes>
```

A workspace whose project has no repository or no available git connection gets every request
answered with 404. The
`WorkspaceProxyServlet` of the web application is mapped to `/git/*` as well; there, the servlet
container resolves or refuses dot segments before the route sees the path.

## The generated Gitea client

`tl-factory-gitea` calls Gitea only through a client generated from the OpenAPI (Swagger 2.0)
specification of Gitea:

- The specification is checked in as `src/main/openapi/gitea-<version>.swagger.json`, the version
  is the property `gitea.version` of the module's POM - the only place it is pinned. The same
  property names the image of the integration test (system property `tl.factory.gitea.image`).
- `openapi-generator-maven-plugin` (version `openapi-generator.version` of `tl-bom`) generates it in
  `generate-sources` into `target/generated-sources/openapi`, package
  `com.top_logic.factory.gitea.client`, with the library `native` (JDK `HttpClient`, Jackson), Jakarta
  annotations, no tests, no documentation, no build files. The generated sources are not checked in.
- Only the operations the module uses are generated: `apisToGenerate` selects the tags,
  the normalizer rule `FILTER=operationId:...` the operations, `modelsToGenerate` their models with
  all models they refer to. An additional operation goes into `FILTER`, its models into
  `modelsToGenerate`; a missing model fails the compilation.
- The generated package is excluded from the conformance checks: from javac's doclint by
  `-Xdoclint/package:-com.top_logic.factory.gitea.client,...` and from the TLDoclet by
  `excludePackageNames` of the `maven-javadoc-plugin`. The import check (Checkstyle in `validate`)
  and `TestComment` only see `src/`.
- The credentials are added by the request interceptor of the `ApiClient`
  (`credentials.authenticate(RequestAuthentication.of(builder))`), which runs for every request.

## The integration tests

`GiteaTestServer` starts the pinned Gitea image in a container (SQLite, `INSTALL_LOCK`), creates
an administrator and two access tokens with `gitea admin user create` /
`gitea admin user generate-access-token` and an organization; each test class gets a server of
its own through `GiteaTestServer.suite(testClass, ...)`.

- `TestGiteaHostingProvider` checks creating, finding and protecting repositories, the errors,
  what the protection enforces on real pushes, committing a file through the API (on top of the
  default branch, also in a directory, kept when it exists, refused for a protected file and a
  missing repository, a push changing it rejected once it is protected), the capabilities of the administrator's token, of a
  token of a plain user (created by `GiteaTestServer.createUser`: nothing in the organization,
  everything in its own namespace, a repository it may only write to not administrable and its
  protection refused) and of a token without `read:user`, and the organization a provider creates
  for its owner (private, with the first repository, invisible to another user).
- `TestGiteaSwitch` checks the Gitea type in a `GitHostingService`: a built-in connection without
  URL is switched off, the provider per key, `forget`, and the refusal of an invalid URL.
- `TestGitRouteGitea` runs the git command line of the host (with an empty home and nothing but
  the environment of a workspace process) against a `GitRoute` on a `WorkspaceProxyListener`:
  clone of an empty repository, first and second push, a fetch with protocol version 2, the
  refused repository of another project, a revoked token, the challenge without credential
  helper, and the rejections of a push changing a protected file and of a force push, with
  Gitea's message reaching git.
- `TestRepositoryWorkspaces` (only with `-Dtl.factory.integration=true`; it builds the workspace
  image, fills the release repository and builds three applications) runs `RepositorySource` in
  Docker workspaces that reach a `GitRoute` on the host as `host.docker.internal`
  (`extra-hosts=host.docker.internal:host-gateway`, the listener on `0.0.0.0`): the first workspace
  creates the repository, pushes the generated application with the configured identity and
  protects the branch; the `Jenkinsfile` is committed through the API and protected, as setting up
  the CI does; a workspace created anew clones that commit, and a push of the agent's environment
  changing the `Jenkinsfile` is refused while another one passes; a second project
  adopting the repository by its clone URL clones it.

`TestProjectRepository` of `tl-factory` checks the steps of all cases against a
`RecordingGitHostingProvider` and a simulated git in a `RecordingWorkspaceProvider`: the order
push before protection before build, the revoked token of the push, the retries, the checks of an
adopted repository, the branch left unprotected (and recorded so) when the token may not administer
the repository, and no repository without git connection. `TestGitConnections` checks the
connections with the `RecordingGitProviderType` and the `MemorySecretStore` of the test
configuration: the built-in connection read by every tenant with an organization per tenant, the
own connections with their token in the secret store, who may create, change, replace the token of
and delete them (administrators of the own tenant and operators; not the developers, not the
administrators of another tenant), the deletion and the change of the server refused while a
project lives on the connection, the secret removed with the connection, and the git route
resolving the provider of the connection of the project of a workspace.

Without a reachable Docker daemon, run without the build's system property (e.g. from the IDE
without `-Dtl.factory.gitea.image=gitea/gitea:<version>`), or - for `TestGitRouteGitea` - without
`git` on the path, the suite is empty. `TestGitRoute` and `TestGitRouteServlet` of `tl-factory`
test the route against a fake hosting service. The whole flow of a new project and the set up of its CI with its first
build against an external Gitea and Jenkins is `TestProjectCI` of `tl-factory-jenkins`, see [the tests of
CI](doc:factory/ci#the-tests).

```bash
mvn test -DskipTests=false -pl tl-factory-gitea
mvn test -DskipTests=false -pl tl-factory-gitea -Dtest=test.com.top_logic.factory.gitea.TestRepositoryWorkspaces -Dtl.factory.integration=true
```

## Updating Gitea

Renovate proposes a new Gitea version by changing `gitea.version` (a `customManagers` entry of
`renovate.json` reads the comment `<!-- renovate: datasource=docker depName=gitea/gitea -->` above
it), together with the image of the [local development setup](doc:factory#local-setup) in
`tl-factory-app/devel/compose.yaml` (comment `# renovate: datasource=docker depName=gitea/gitea`
above `image:`), in one pull request (package rule *Gitea of tl-factory*). Such a pull request fails until the specification of the new version is added: the enforcer
rule `gitea-specification` of the module stops the build in `validate`, naming the missing file.
The pull request carries the label `manual-step` and these steps in its description
(`prBodyNotes` of the package rule):

1. Fetch it from a server of the new version:
   `docker run -d --name gitea-spec -p 127.0.0.1:3000:3000 -e GITEA__security__INSTALL_LOCK=true gitea/gitea:<version>`,
   then `curl -o tl-factory-gitea/src/main/openapi/gitea-<version>.swagger.json http://127.0.0.1:3000/swagger.v1.json`
   and `docker rm -f gitea-spec`.
2. Delete the specification of the old version.
3. Build the module (a model newly referred to goes into `modelsToGenerate`) and run the integration
   test.
