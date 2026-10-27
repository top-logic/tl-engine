---
description: Read before configuring or working on the git hosting service of the control application (the repositories of the projects on a hosting service) - GitHostingService with its optional provider, GitHostingProvider (createRepository, findRepository with RepositoryName.parse for a clone URL or owner/name, protectBranch, gitUrl, gitCredentials), GitRepository, the repository of a project (tl.factory:Project repository and repositoryUrl) and how a workspace gets it (RepositorySource as ApplicationSource of ApplicationWorkspaces.create: create, record, generate and initial push before protectBranch, clone of an existing or adopted repository with the pom.xml check of artifactId and tl.version, retry after a failed run, cleanup and deletion keeping the repository, factoryGitHosting and factoryCheckRepository of the create form, the agent instructions with --append-system-prompt), the protected-files of GitHostingService and the commit identity user-name / user-email of the GitRoute, the git route of the workspace proxy (GitRoute at /git passing git's smart HTTP protocol through - info/refs, git-upload-pack, git-receive-pack -, the repository of the project of the workspace only, FORWARDED_REQUEST_HEADERS with Git-Protocol and Content-Encoding, the 401 challenge with WWW-Authenticate, the credential helper through GIT_CONFIG_COUNT / GIT_CONFIG_KEY_n / GIT_CONFIG_VALUE_n and TL_FACTORY_PROXY_TOKEN, TL_FACTORY_GIT_URL, remoteUrl and projectRemoteUrl, git commands of the control application with issueProcessToken), what Gitea accepts for git over HTTP, the test double RecordingGitHostingProvider, the Gitea implementation GiteaHostingProvider of the module tl-factory-gitea (base-url, owner, credentials, the secret tl_factory_gitea_token and the scopes of its access token, required-approvals), what its branch protection enforces in Gitea (no force push, protected_file_patterns, block_admin_merge_override), the error keys, the client generated from the OpenAPI specification of Gitea with openapi-generator-maven-plugin (gitea.version, apisToGenerate, FILTER, modelsToGenerate, the exclusions from doclint and TLDoclet), its integration tests against a Gitea container (GiteaTestServer, TestGiteaHostingProvider, TestGitRouteGitea with the git command line, TestRepositoryWorkspaces with Docker workspaces), TestProjectRepository, and updating Gitea.
order: 30
---

# Git hosting

The sources of a project can live in a repository on a git hosting service, in one organization
of the control application. The service `GitHostingService` holds the provider of the hosting
service; without a provider, git hosting is off and the control application works without
repositories.

| Class | Role |
|---|---|
| `GitHostingProvider` | Platform-neutral access to the hosting service: create a repository of the configured owner, find an existing repository, protect a branch; the URL and the credentials of git's smart HTTP protocol (`gitUrl`, `gitCredentials`) for the git route. |
| `GitRepository` | A repository: full name (`owner/name`), HTTP clone URL, web URL, default branch. |
| `RepositoryName` | Owner and name of a repository; `parse` reads what a user enters - `owner/name`, the clone URL, the URL of a page of the repository, an SSH clone URL - against the base URL of the service. |
| `GitHostingService` | Service holding the configured provider; `isEnabled()` tells whether there is one, `getProvider()` fails with `ERROR_NO_GIT_HOSTING` otherwise. |
| `GitRoute` | The route of the workspace proxy through which a workspace fetches from and pushes to the repository of its project, see [the git route](#the-git-route). |
| `GiteaHostingProvider` | The implementation for Gitea, in the module `tl-factory-gitea`. |
| `RecordingGitHostingProvider` | Test double in the tests of `tl-factory`: repositories and branch protections in memory, the calls recorded. |

## Configuration

`tl-factory` registers the service without provider. An installation that uses git hosting adds
the module `tl-factory-gitea` (`com.top-logic:tl-factory-gitea`) and configures the provider:

```xml
<config service-class="com.top_logic.factory.git.GitHostingService">
	<instance class="com.top_logic.factory.git.GitHostingService">
		<provider class="com.top_logic.factory.gitea.GiteaHostingProvider"
			base-url="https://git.example.com/"
			owner="factory-projects"
		/>
	</instance>
</config>
```

| Property | Default | Meaning |
|---|---|---|
| `base-url` | *(mandatory)* | URL of the Gitea server, its `ROOT_URL`; the REST API is expected at `api/v1` below it. |
| `owner` | *(mandatory)* | Organization the repositories are created in. |
| `credentials` | the token in the secret `tl_factory_gitea_token` | How the requests are authenticated, any `CredentialProvider`, see [credential providers](doc:credentials/credential-providers). |
| `timeout` | `30s` | How long a connection and each request may take. |
| `required-approvals` | `1` | Approvals a pull request to a protected branch needs. |

Without `credentials`, the provider sends the access token in the named secret
`tl_factory_gitea_token` as `Authorization: token <token>`; the secret is read through the secret
store of the installation for each request (see [secret stores](doc:credentials/secret-stores)),
so a rotated token takes effect without a restart. A request rejected with 401 is repeated once
with fresh credentials.

### The access token

The token is an access token of a Gitea user with the scopes

- `write:organization` - creating a repository in the organization,
- `write:repository` - reading repositories and creating and changing their branch protection.

The user must be allowed to create repositories in the organization and to administer them: a
member of the team *Owners* of the organization, or of a team with the permission to create
repositories and administrator access to them. A token with fewer scopes or a user with fewer
rights gets `ERROR_PERMISSION_DENIED__REPOSITORY`.

## Repositories

- `createRepository(name, description)` creates an empty, private repository of the owner, without
  initial commit; the first push creates its default branch (the `DEFAULT_BRANCH` of the server,
  usually `main`).
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
| `protected_file_patterns` | the patterns joined by `;`, e.g. `Jenkinsfile;deploy/**` |
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

Protect a branch after the push that creates it. Gitea (checked with 28.2.0) rejects the push
creating a branch whose rule has protected file patterns - the pre-receive hook fails with
*cannot find the start commit* - so the first push into an empty repository happens before
`protectBranch`.

The token of the control application is the one of an organization owner; with it, a direct push
is restricted like any other, a merge of a pull request is not.

## Errors

The provider reports failures as `TopLogicException` with the keys of
`com.top_logic.factory.git.I18NConstants`:

| Key | When |
|---|---|
| `ERROR_REPOSITORY_EXISTS__REPOSITORY` | The owner has a repository of the name (Gitea: 409). |
| `ERROR_INVALID_REPOSITORY_NAME__NAME` | Gitea does not accept the name (422). |
| `ERROR_OWNER_NOT_FOUND__OWNER` | The organization does not exist (404 when creating). |
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

With git hosting switched on, `ProjectWorkspaces.create` (TL-Script `factoryCreateWorkspace`) puts
a `RepositorySource` on the `ApplicationSpec` of the new workspace. `ApplicationWorkspaces.create`
runs this `ApplicationSource` in the started workspace in place of the archetype generation, then
builds the application as always. All git commands run in the workspace through the git route,
with a token issued for them by `issueProcessToken` and revoked as soon as they are done:

| Case | Steps |
|---|---|
| New application: `repository` empty | `createRepository(artifactId, project name)` and record it on the project at once, in a transaction of its own; `git ls-remote` finds the repository empty; generate from the archetype; `git init --initial-branch=<default branch>`, `git add --all`, `git commit` (message `RepositorySource.INITIAL_COMMIT_MESSAGE` and the engine version), `git remote add origin <remote>`, `git push --set-upstream origin <default branch>`; `protectBranch(default branch, protected files)`; build. |
| Existing repository: `repository` set | `findRepository(reference)`, not found fails with `ERROR_UNKNOWN_REPOSITORY__REFERENCE`; record it under its full name; `git ls-remote` finds commits; `git clone <remote> /workspace/<artifactId>`; check the `pom.xml`; `protectBranch`; build. |
| No git hosting provider | Generate from the archetype and build; the project has no repository. |

The remote is `GitRoute.remoteUrl(<route URL>, repository)`; without workspace proxy or git route
the source fails with `ERROR_NO_GIT_ROUTE`. The branch is protected after the first push, which
Gitea rejects for a branch with protected files (see [branch protection](#branch-protection)), and
also for an existing repository, which has its default branch already.

**Adopting a repository.** The form creating a project has the field *Existing repository* while
git hosting is on (TL-Script `factoryGitHosting()`): the full name `owner/name`, the clone URL or the
URL of a page of a repository, as `RepositoryName.parse` reads it. The constraint of the attribute
(`factoryCheckRepository($object, $value)`, `RepositorySource.checkReference`) asks
`findRepository` and names the reference in `ERROR_UNKNOWN_REPOSITORY__REFERENCE` if there is no
such repository; it does not ask again once the project has recorded its repository (its
`repositoryUrl` is set), so storing the recorded repository does not contact the hosting service.
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
default branch, that it must not force push, and which files are protected; with
[CI](doc:factory/ci#the-ci-route), also how it builds the application on the CI server.

### Protected files and commit identity

| Setting | Default | Meaning |
|---|---|---|
| `protected-files` of `GitHostingService` (`%FACTORY_GIT_PROTECTED_FILES%`, environment `tl_factory_git_protected_files`) | `Jenkinsfile, deploy/**` | Glob patterns of the files no push to the default branch of a project repository may change; with [CI](doc:factory/ci#the-pipeline-definition), the pipeline file of the CI server is added if it is not listed. |
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
`path`). The workspace gets no credential of the hosting service; the route adds the one of the
control application. The branch protection of the hosting service - no force push, protected
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
repository of a workspace whose project has none, and every request while `GitHostingService` has
no provider. The repository must be the one of the project of the workspace the token was issued
for, owner and name compared without regard to case; a refused repository is logged at level INFO
and answered like one that does not exist.

### Forwarding

The route forwards to `gitUrl(repository)` of the provider - for Gitea the clone URL below its
`base-url` -, with the owner and name as stored in the project, and the query rebuilt from the
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

`tl-factory` configures the route next to the LLM route:

```xml
<routes>
	<route class="com.top_logic.factory.llm.LLMRoute" provider="%FACTORY_LLM_PROVIDER%"/>
	<route class="com.top_logic.factory.git.GitRoute"
		user-email="%FACTORY_GIT_USER_EMAIL%"
		user-name="%FACTORY_GIT_USER_NAME%"
	/>
</routes>
```

It is inactive - every request answered with 404 - while `GitHostingService` has no provider. The
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
  and what the protection enforces on real pushes.
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
  protects the branch; a workspace created anew clones the same commit, and a push of the agent's
  environment changing the `Jenkinsfile` is refused while another one passes; a second project
  adopting the repository by its clone URL clones it.

`TestProjectRepository` of `tl-factory` checks the steps of all cases against a
`RecordingGitHostingProvider` and a simulated git in a `RecordingWorkspaceProvider`: the order
push before protection before build, the revoked token of the push, the retries, the checks of an
adopted repository, and no repository without git hosting.

Without a reachable Docker daemon, run without the build's system property (e.g. from the IDE
without `-Dtl.factory.gitea.image=gitea/gitea:<version>`), or - for `TestGitRouteGitea` - without
`git` on the path, the suite is empty. `TestGitRoute` and `TestGitRouteServlet` of `tl-factory`
test the route against a fake hosting service.

```bash
mvn test -DskipTests=false -pl tl-factory-gitea
mvn test -DskipTests=false -pl tl-factory-gitea -Dtest=test.com.top_logic.factory.gitea.TestRepositoryWorkspaces -Dtl.factory.integration=true
```

## Updating Gitea

Renovate proposes a new Gitea version by changing `gitea.version` (a `customManagers` entry of
`renovate.json` reads the comment `<!-- renovate: datasource=docker depName=gitea/gitea -->` above
it). Such a pull request fails until the specification of the new version is added: the enforcer
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
