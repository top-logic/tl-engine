---
description: Read before configuring or working on the git hosting service of the control application (the repositories of the projects on a hosting service) - GitHostingService with its optional provider, GitHostingProvider (createRepository, findRepository with RepositoryName.parse for a clone URL or owner/name, protectBranch), GitRepository, the test double RecordingGitHostingProvider, the Gitea implementation GiteaHostingProvider of the module tl-factory-gitea (base-url, owner, credentials, the secret tl_factory_gitea_token and the scopes of its access token, required-approvals), what its branch protection enforces in Gitea (no force push, protected_file_patterns, block_admin_merge_override), the error keys, the client generated from the OpenAPI specification of Gitea with openapi-generator-maven-plugin (gitea.version, apisToGenerate, FILTER, modelsToGenerate, the exclusions from doclint and TLDoclet), its integration test against a Gitea container, and updating Gitea.
order: 30
---

# Git hosting

The sources of a project can live in a repository on a git hosting service, in one organization
of the control application. The service `GitHostingService` holds the provider of the hosting
service; without a provider, git hosting is off and the control application works without
repositories.

| Class | Role |
|---|---|
| `GitHostingProvider` | Platform-neutral access to the hosting service: create a repository of the configured owner, find an existing repository, protect a branch. |
| `GitRepository` | A repository: full name (`owner/name`), HTTP clone URL, web URL, default branch. |
| `RepositoryName` | Owner and name of a repository; `parse` reads what a user enters - `owner/name`, the clone URL, the URL of a page of the repository, an SSH clone URL - against the base URL of the service. |
| `GitHostingService` | Service holding the configured provider; `isEnabled()` tells whether there is one, `getProvider()` fails with `ERROR_NO_GIT_HOSTING` otherwise. |
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

## The integration test

`TestGiteaHostingProvider` starts the pinned Gitea image in a container (SQLite, `INSTALL_LOCK`),
creates an administrator and two access tokens with `gitea admin user create` /
`gitea admin user generate-access-token` and an organization, and checks creating, finding and
protecting repositories, the errors, and what the protection enforces on real pushes. Without a
reachable Docker daemon, or run without the build's system property (e.g. from the IDE without
`-Dtl.factory.gitea.image=gitea/gitea:<version>`), the suite is empty.

```bash
mvn test -DskipTests=false -pl tl-factory-gitea
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
