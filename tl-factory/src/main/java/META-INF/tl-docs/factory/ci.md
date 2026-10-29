---
description: Read before configuring or working on the CI of the control application (the build of the project repositories on a CI server) - CIService with its optional provider and pipeline-template, CIProvider (pipelineFile, ensureJob, findJob, startBuild with the queue, lastBuild, console bounded to its end) and its value types CIJob, CIBuild, CIBuildState, CIBuildStart, CIConsole, the test double RecordingCIProvider, the CI job of a project (tl.factory:Project ciJob and ciJobUrl), CI as opt-in per project (no CI with the creation of a workspace; Set up CI with ProjectWorkspaces.setUpCI and ProjectCI.setUp: the Jenkinsfile from /WEB-INF/factory/ci/Jenkinsfile committed with GitHostingProvider.commitFile unless the repository has one, the protection extended by the pipeline file if the token may administer the repository, ensureJob and the first build, the retry after a failure, ciSetUpUnavailable and its reasons; the pipeline file protected only with a CI job, ProjectWorkspaces.protectedFiles, withPipelineFile, withoutPipelineFile), the CI route of the workspace proxy (CIRoute at /ci with POST /builds, GET /builds/last, GET /builds/<n>/log, max-log-size, TL_FACTORY_CI_URL, the agent instructions with curl), the views (factoryCiEnabled, factoryCiSetUpUnavailable, factorySetUpCI, factoryCiUnavailable, factoryStartBuild, factoryLastBuild, the commands Set up CI, Start CI build and Refresh CI build, the ciBuild channel), the Jenkins implementation JenkinsCIProvider of the module tl-factory-jenkins (its configuration from the environment variables tl_factory_jenkins_url, tl_factory_jenkins_user, tl_factory_jenkins_folder and tl_factory_jenkins_scm_credentials, switched off by an empty base-url through ServerProviderConfig, base-url, user, the secret tl_factory_jenkins_token, credentials, folder, scm-credentials-id, script-path, job-template pipeline-job.xml with its placeholders, timeout, queue-wait, the JSON API, createItem and config.xml, build and the queue item, progressiveText with X-Text-Size and the console notes, no CSRF crumb with an API token), the Jenkins-side prerequisites (user and permissions, SCM credential, agent with JDK and Maven, plugins), the public Jenkins URL the pages are reported below and what an operator configures for it, its integration test against a Jenkins container next to a Gitea (JenkinsTestServer, TestJenkinsCIProvider, src/test/docker/jenkins with Dockerfile and its targets ci and devel, plugins.txt and the init script tl-factory.groovy with the TL_JENKINS_* variables), the end-to-end test TestProjectCI of a new project, the set up of its CI and its first build against an external Gitea and Jenkins and updating Jenkins and its plugins (Renovate).
order: 40
---

# CI

The application of a project can be built by a CI server: a project whose CI is set up has a job
there, which builds the default branch of its repository on the [git hosting
service](doc:factory/git-hosting) with the pipeline definition in the repository. Developing an
application needs no CI: a new project gets neither a pipeline definition nor a job; a user sets up
its CI when it is wanted (*Set up CI*). The service `CIService` holds the provider of the CI server;
without a provider, CI is off and the control application works as without it.

| Class | Role |
|---|---|
| `CIProvider` | Platform-neutral access to the CI server: the path of the pipeline definition (`pipelineFile`), the job of a repository (`ensureJob`, create or update), a build (`startBuild`), the last build (`lastBuild`), the end of the console output of a build (`console`). |
| `CIJob`, `CIBuild`, `CIBuildState`, `CIBuildStart`, `CIConsole` | The values of the operations: the job (name, page), a build (number, state, page, start time, duration), its state (`RUNNING`, `SUCCESS`, `FAILURE`, `UNSTABLE`, `ABORTED`, `NOT_BUILT`), a started build (number, or only the queue reference while it waits for an executor), the console output (text, whether it was cut). |
| `CIService` | Service holding the configured provider (`isEnabled()`, `getProvider()` fails with `ERROR_NO_CI` without one) and the pipeline template a repository gets when its CI is set up. |
| `ProjectCI` | The operations on a stored project: why its CI cannot be set up (`setUpUnavailable`), setting it up (`setUp`, with the outcome `ProjectCI.Setup`), why no build can be started (`unavailable`), start a build, the last build, the protected files with and without the pipeline file (`withPipelineFile`, `withoutPipelineFile`). |
| `CIRoute` | The route of the workspace proxy through which the agent builds its application, see [the CI route](#the-ci-route). |
| `JenkinsCIProvider` | The implementation for Jenkins, in the module `tl-factory-jenkins`. |
| `RecordingCIProvider` | Test double in the tests of `tl-factory`: jobs and builds in memory, the calls recorded, builds finished and consoles set by the test. |

## Configuration

`tl-factory` registers the service without provider. An installation that uses CI adds the module
`tl-factory-jenkins` (`com.top-logic:tl-factory-jenkins`; `tl-factory-app` depends on it). Its
configuration `tl-factory-jenkins.conf.config.xml` adds the provider, with its settings taken from
the environment:

```xml
<config service-class="com.top_logic.factory.ci.CIService">
	<instance class="com.top_logic.factory.ci.CIService">
		<provider class="com.top_logic.factory.jenkins.JenkinsCIProvider"
			base-url="%FACTORY_JENKINS_URL%"
			folder="%FACTORY_JENKINS_FOLDER%"
			scm-credentials-id="%FACTORY_JENKINS_SCM_CREDENTIALS%"
			user="%FACTORY_JENKINS_USER%"
		/>
	</instance>
</config>
```

| Variable | Default | Meaning |
|---|---|---|
| `tl_factory_jenkins_url` | *(empty)* | `base-url`, the URL under which the control application reaches the Jenkins; empty switches CI off. |
| `tl_factory_jenkins_user` | `tl-factory` | `user`, the Jenkins user of the requests. |
| `tl_factory_jenkins_folder` | `tl-factory` | `folder` of the jobs; empty for the top level. |
| `tl_factory_jenkins_scm_credentials` | *(empty)* | `scm-credentials-id`, the Jenkins credential the jobs check out with; empty for none. |
| `tl_factory_jenkins_token` | - | The API token of the user, a [named secret](doc:credentials/secret-stores) read by the default `credentials`. |

Like a built-in git connection without URL, a provider whose `base-url` is empty is switched off:
`CIService` does not create it and is disabled as without provider (`ServerProviderConfig.isSwitchedOn`).
CI needs git hosting: without it, no project has a repository to build. It builds the projects on a
[built-in git connection](doc:factory/git-hosting#git-connections) only, whose repositories the SCM
credential of the CI server reads; the CI of a project on a git connection of its tenant cannot be
set up (`ProjectWorkspaces.getCI(project)`, `ERROR_CI_NEEDS_BUILT_IN_CONNECTION__PROJECT`).

| Property of `CIService` | Default | Meaning |
|---|---|---|
| `provider` | none: CI off | The CI server. |
| `pipeline-template` | `/WEB-INF/factory/ci/Jenkinsfile` | Web application resource whose content a repository without pipeline definition gets when the CI of its project is set up. An application overrides the default by a resource of the same path, or names another one. |

| Property of `JenkinsCIProvider` | Default | Meaning |
|---|---|---|
| `base-url` | *(empty: off)* | URL under which the control application reaches the Jenkins. The pages of jobs and builds are the ones Jenkins reports, below its Jenkins URL, see [the public URL](#the-public-url-of-jenkins). |
| `user` | none | Jenkins user of the requests, if `credentials` is not set. |
| `credentials` | basic authentication of `user` with the API token in the secret `tl_factory_jenkins_token` | How the requests are authenticated, any `CredentialProvider`, see [credential providers](doc:credentials/credential-providers). |
| `folder` | `tl-factory` | Folder of the jobs, nested folders separated by `/`; empty for the top level. Created if missing. |
| `scm-credentials-id` | none | ID of the Jenkins credential the jobs check out with. |
| `script-path` | `Jenkinsfile` | Path of the pipeline definition in a repository, the `pipelineFile()` of the provider. |
| `job-template` | `/WEB-INF/factory/jenkins/pipeline-job.xml` | Web application resource with the configuration of a job, see [the job](#the-job). |
| `timeout` | `30s` | How long a connection and each request may take. |
| `queue-wait` | `10s` | How long starting a build waits for it to leave the queue and get its number. |

Without `credentials`, the provider sends `user` and the API token in the named secret
`tl_factory_jenkins_token` with basic authentication (`BasicCredentials` takes its user literally,
the token from a `SecretSource`); the secret is read through the secret store of the installation
for each request (see [secret stores](doc:credentials/secret-stores)), so a rotated token takes
effect without a restart. A request rejected with 401 is repeated once with fresh credentials;
rejected again, it fails with `ERROR_CI_AUTHENTICATION_FAILED__SERVICE`. Neither `user` nor
`credentials` is a configuration error.

### Prerequisites in Jenkins

- **Plugins**: Pipeline (`workflow-job`, `workflow-cps`, and for the default Jenkinsfile the
  declarative pipeline `pipeline-model-definition` with the steps of `workflow-basic-steps` and
  `workflow-durable-task-step`), Git (`git`), Credentials (`credentials`), Folders
  (`cloudbees-folder`).
- **A user with an API token** for the control application, with the permissions Overall/Read,
  Job/Create, Job/Configure, Job/Read and Job/Build in the folder of the jobs (and Job/Create in
  its parent, if the folder is to be created). Use an API token, not a password: Jenkins exempts
  requests authenticated with an API token from its CSRF protection, so the provider fetches no
  crumb; a request with a password is refused for every changing request.
- **The SCM credential**: a Jenkins credential with read access to the repositories of the git
  hosting service, e.g. *Username with password* holding a Gitea user and an access token with
  `read:repository`. Its ID is `scm-credentials-id`. The control application never sends a git
  credential to Jenkins; Jenkins clones from the clone URL of the repository on the hosting
  service, not through the workspace proxy.
- **An agent for the builds** (`agent any` in the default Jenkinsfile): a JDK 17 or newer and
  Maven (`mvn`) on the path, and access to the TopLogic Maven repositories that the `pom.xml` of an
  application generated from `tl-archetype-app` names itself (`https://dev.top-logic.com/nexus/...`),
  or a mirror in the agent's Maven settings. The workspaces fill their Maven repository from
  `tl_factory_repository_url`; the CI agent does not see it.

### The public URL of Jenkins

The links to jobs and builds the control application shows - `ciJobUrl` of a project, the build of
the `ciBuild` channel - are the pages Jenkins reports in the `url` fields of its JSON API, below
its Jenkins URL; the control application never rewrites them. For the Jenkins in production, the
operator therefore configures:

- **The Jenkins URL** (*Manage Jenkins*, *System*, *Jenkins Location*; `TL_JENKINS_URL` of the
  image of `tl-factory-jenkins`, which its init script applies at every start): the public URL
  under which the browsers of the users reach Jenkins, e.g. `https://ci.example.com/`. `base-url`
  may be another address of the same server.
- **Access to the clone URLs**: the jobs clone from the clone URLs the git hosting service reports,
  below its [public URL](doc:factory/git-hosting#the-public-url-of-the-hosting-service).
- **Accounts of the users in Jenkins**: Jenkins shows its pages only to users it knows (the image
  of `tl-factory-jenkins` gives anonymous users no read access). Single sign-on and access derived
  from the membership in the tenant are added by ticket #30052.

When the Jenkins URL changes, the recorded job pages follow: the control application refreshes the
links of the projects after every start (`ProjectWorkspaces.refreshLinks` with `findJob`, see
[the public URL of the hosting service](doc:factory/git-hosting#the-public-url-of-the-hosting-service)),
and *Start CI build* records the job anew when its page changed.

## The pipeline definition

When the CI of a project is set up, a repository without pipeline definition gets the content of
`pipeline-template` as `pipelineFile()` (`Jenkinsfile`). The default,
`tl-factory/src/main/webapp/WEB-INF/factory/ci/Jenkinsfile`, is a declarative pipeline with one
stage running `mvn -B verify`, keeping 20 builds and no concurrent ones.

The pipeline file of a project with CI job is a protected file, so neither the agent nor the
control application changes it, only a reviewed pull request; before, it is not protected, so that
setting up the CI can commit it. `ProjectWorkspaces.protectedFiles(project, ci)` - the files a
workspace protects the default branch with and the agent is told - is `protected-files` of
`GitHostingService` (default `deploy/**`) without `pipelineFile()` (`ProjectCI.withoutPipelineFile`,
even if it is listed there) and, for a project with CI job, with it (`ProjectCI.withPipelineFile`).

## The CI job of a project

`tl.factory:Project` holds its job in `ciJob`, the name the provider identifies it by
(`FactoryModel.getCIJob`), and `ciJobUrl`, its page; both are read-only and set by the control
application (`FactoryModel.setCIJob`).

### Setting up the CI

Creating a workspace sets up no CI: the first commit of a new application has no pipeline
definition, the default branch is protected without the pipeline file, and the CI server is not
contacted. *Set up CI* (`ProjectWorkspaces.setUpCI`, TL-Script `factorySetUpCI($project)`) does it
later, through the APIs of the hosting service and the CI server, never through the working tree of
the workspace, which belongs to the agent (`ProjectCI.setUp`):

1. The pipeline file is committed to the default branch with the message
   `ProjectCI.PIPELINE_COMMIT_MESSAGE` (`GitHostingProvider.commitFile`), unless the branch has one -
   e.g. an adopted repository with a pipeline of its own, which is kept.
2. If the token of the git connection may administer the repository (`GitRepository.canAdminister`),
   the default branch is protected with the protected files and the pipeline file
   (`protectBranch`), and `Project#branchProtected` is set; otherwise the protection and
   `branchProtected` stay as they are, and the message of the command says that the pipeline file
   is not protected (`CI_SET_UP_UNPROTECTED__JOB_BUILD_FILE`).
3. `ensureJob(repository, project name)` creates the job, it is recorded on the project in a
   transaction of its own, and its first build is started (`startBuild`); the message names the job
   and the build (`CI_SET_UP__JOB_BUILD`).

A failure in a step - the hosting service or the CI server not reachable, credentials rejected, a
missing permission - ends the command with its message. The steps can be repeated, so a retry
completes a set up that failed: a committed pipeline file is kept, the protection is replaced, an
existing job is reused (`ensureJob` creates the job and its folders if they are missing and
replaces the configuration of an existing one). A set up that failed only at the first build
leaves the job recorded; *Start CI build* starts it.

`ProjectWorkspaces.ciSetUpUnavailable` (TL-Script `factoryCiSetUpUnavailable($project)`) tells why
the CI of a project cannot be set up: CI switched off (`ERROR_NO_CI`), the project not on a
built-in git connection (`ERROR_CI_NEEDS_BUILT_IN_CONNECTION__PROJECT`), without repository
(`ERROR_NO_REPOSITORY__PROJECT`), its repository without the application - no workspace has pushed
or cloned it yet, `branchProtected` is empty (`ERROR_NO_SOURCES__PROJECT`) - or a CI job already
(`ERROR_HAS_CI_JOB__PROJECT`). Setting up the CI needs the right to write the project.

### The project and its views

The project panel of `projects.view.xml` has three commands and the detail view a line for CI:

- **Set up CI** (`setUpCI`) is shown while CI is switched on (`factoryCiEnabled()`) and the selected
  project has no CI job, disabled with the reason of `factoryCiSetUpUnavailable($project)`, e.g.
  for a project on a git connection of its tenant. It asks for confirmation, sets up the CI and
  shows the message.
- **Start CI build** (`startBuild`, TL-Script `factoryStartBuild($project)`) is shown for a project
  with CI job. It runs `ensureJob` - a job the CI server lost is created again, its configuration
  corrected -, records the job if it changed, starts a build and shows which
  (`BUILD_STARTED__NUMBER_JOB`, or `BUILD_QUEUED__JOB` while it waits for an executor). It is
  disabled with the reason of `factoryCiUnavailable($project)`, e.g. CI switched off since.
- **Refresh CI build**, shown for a project with CI job, asks the CI server again for the last
  build.
- The channel `ciBuild` shows the job as link to its page and the last build - number with link,
  state, start time, duration - from `factoryLastBuild($project)`, a dictionary with the keys
  `number`, `state`, `stateLabel`, `url`, `startTime`, `durationSeconds`, or only `error` if the CI
  server cannot be asked; nothing for a project without CI job. It is computed when the project is
  selected or changes and when one of the commands writes the channel `ciRefresh`; nothing polls
  the CI server in the background.

## The CI route

The agent in a workspace builds its application through the route `CIRoute` of the [workspace
proxy](doc:factory#the-workspace-proxy), at `/ci` (its `path`). It is a small API of the control
application, the same for every CI server, not a passthrough to Jenkins: the workspace gets no
credential of the CI server and reaches no job but the one of its project.

| Request | Answer |
|---|---|
| `POST /ci/builds` | Starts a build of the job, `200` with JSON: `job`, `number` (`null` while queued), `queued`, `url`. |
| `GET /ci/builds/last` | `200` with JSON: `job`, `number`, `state` (a `CIBuildState`), `url`, `startTime` (ISO 8601), `durationMillis`; `404` if the job has no build. |
| `GET /ci/builds/<number>/log` | `200` with the end of the console output as plain text, at most `max-log-size` bytes (default 64 KB) of complete lines; a cut output starts with the line `[... earlier output omitted ...]`. `404` for a build that does not exist. |

The JSON field names are the constants `CIRoute.FIELD_*`. Every other request - another path or
method - is answered with 404, as is every request of a workspace whose project has no CI job and
every request while `CIService` has no provider. A failure of the CI server is answered with 502
and a fixed text; the details go to the log of the control application. The token checks are those
of every route: the token of the agent process, as bearer token, from the accepted sources, within
its rate limit.

A workspace container gets the URL of the route in `TL_FACTORY_CI_URL` (`CIRoute.ENV_CI_URL`, no
secret). For a project with CI job, with CI switched on and a CI route in the proxy,
`StoredConversation.projectInstructions` adds `CIRoute.agentInstructions()` to what the agent is
told; the instructions are computed for every launch of the agent, so it learns about the CI with
its next message after the set up:

```bash
curl -X POST -H "Authorization: Bearer $TL_FACTORY_PROXY_TOKEN" "$TL_FACTORY_CI_URL/builds"
curl -H "Authorization: Bearer $TL_FACTORY_PROXY_TOKEN" "$TL_FACTORY_CI_URL/builds/last"
```

`tl-factory` configures the route next to the git route
(`<route class="com.top_logic.factory.ci.CIRoute"/>`); the `WorkspaceProxyServlet` is mapped to
`/ci/*` as well.

## Jenkins

### The job

The job of a repository `<owner>/<name>` is `<folder>/<owner>/<name>`: one folder per owner below
`folder`, so the names of repositories of different owners never collide. Its configuration is
`job-template` with the placeholders `@DESCRIPTION@`, `@REPOSITORY_URL@` (the clone URL),
`@CREDENTIALS_ID@` (`scm-credentials-id`), `@BRANCH@` (the default branch) and `@SCRIPT_PATH@`
(`script-path`), each replaced by its value escaped for XML (`JenkinsCIProvider.fill`). The default
template is a pipeline job (`flow-definition`) with *Pipeline script from SCM*
(`CpsScmFlowDefinition`): Git, the branch `*/<default branch>`, lightweight checkout of the
pipeline file. An application overrides it by a resource of the same path, e.g. to add build
triggers or properties.

### Requests

| Operation | Requests |
|---|---|
| `ensureJob` | `GET <item>/api/json?tree=url` for each folder and the job (404: missing); `POST <parent>/createItem?name=<name>` with the folder configuration (`<com.cloudbees.hudson.plugins.folder.Folder/>`) or the job configuration as `application/xml`; for an existing job `POST <job>/config.xml`; the page of a created job is asked again. |
| `findJob` | `GET <job>/api/json?tree=url` (404: no job). |
| `startBuild` | `POST <job>/build?delay=0sec` answers `201` with the queue item in `Location`; `GET queue/item/<id>/api/json?tree=cancelled,executable[number,url]` until the item has its build, at most `queue-wait`; a build still waiting gets the page of the job. |
| `lastBuild` | `GET <job>/lastBuild/api/json?tree=number,result,building,timestamp,duration,url`; `404` is no build, or no job if `GET <job>/api/json` is 404 as well. `building` or no `result` is `RUNNING`, an unknown result `FAILURE`. |
| `console` | `HEAD <job>/<number>/logText/progressiveText?start=0` for the size of the output (`X-Text-Size`), then `GET ...?start=<size - max>`; the first, incomplete line is dropped, and the console notes of Jenkins (`ESC[8mha:...ESC[0m`, the hidden annotations of the raw output) are removed. The bound applies to the raw output, so the plain text returned is shorter. |

`<item>` is `job/<segment>/` for each segment of the full name, URL-encoded, below `base-url`. The
pages the provider reports are the `url` fields of the answers, below the Jenkins URL; an answer
without one gets the path below `base-url`. Answers with 403 fail with
`ERROR_CI_PERMISSION_DENIED__JOB`, 404 of a job with `ERROR_CI_JOB_NOT_FOUND__JOB`, of a build
with `ERROR_CI_BUILD_NOT_FOUND__JOB_BUILD`, no answer with
`ERROR_CI_UNREACHABLE__SERVICE_PROBLEM`, anything else with
`ERROR_CI_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE` (keys of
`com.top_logic.factory.ci.I18NConstants`).

Checked with Jenkins 2.580.1: requests authenticated with an API token (basic authentication)
create items, change configurations and start builds without a CSRF crumb; a wrong token is
answered with 401; `createItem` of an existing name answers 400 (the provider then updates the
configuration); `progressiveText` with an offset beyond the output returns the whole output, which
is why the provider asks for the size first.

## The tests

`TestCIRoute` of `tl-factory` checks the route against a `RecordingCIProvider` on a
`WorkspaceProxyListener`: the three requests and their JSON, a queued build, the bounded console
with its mark, 404 for other requests, for a workspace without job and with CI off, the token
check, and 502 for a failing CI server. `TestProjectRepository` checks that creating a workspace
sets up no CI and protects without the pipeline file, setting up the CI (the pipeline file
committed before it is protected, kept in a repository that has one, the job recorded and its
first build, the retry after a failing CI server, an unprotected pipeline file when the token may
not administer the repository, the refusals without CI, repository, application or built-in
connection), the protected files and agent instructions with and without CI job, and the functions
of the views. `TestTenantAccess` checks that a reader may not set up the CI.

`TestJenkinsCIProvider` of `tl-factory-jenkins` runs against a real Jenkins (`JenkinsTestServer`):
the test builds the target `ci` of the image context `tl-factory-jenkins/src/test/docker/jenkins` -
the system property `tl.factory.jenkins.context` set by the build names it -, and starts it on a
Docker network of its own next to a Gitea of `GiteaTestServer` (test-jar of `tl-factory-gitea`, the
version its client is generated from), which Jenkins reaches as `http://gitea:3000/`. The init
script `init.groovy.d/tl-factory.groovy` sets Jenkins up at every start from the environment of the
container: the administrator `TL_JENKINS_ADMIN` with the password `TL_JENKINS_ADMIN_PASSWORD` and
the API token `TL_JENKINS_API_TOKEN` (`addFixedNewToken`, kept while it is valid), full control for
logged-in users, the SCM credential `TL_JENKINS_SCM_CREDENTIALS_ID` with `TL_JENKINS_SCM_USER` and
`TL_JENKINS_SCM_PASSWORD` (here the read token of the Gitea), `TL_JENKINS_EXECUTORS` executors on
the built-in node (default 2) and optionally the Jenkins URL `TL_JENKINS_URL`; the setup wizard is
off. The image installs the script as `tl-factory.groovy.override`, so that the start script of the
image copies it into an existing Jenkins home as well. The test Jenkins gets the Jenkins URL on the
host name `localhost` while the provider talks to `127.0.0.1`, and checks that the pages are reported
below the Jenkins URL. The test checks: `ensureJob` creating the folders and the job and replacing its configuration
(clone URL, branch, credential, escaped description), a build of an `echo` pipeline pushed to the
Gitea ending `SUCCESS` with its console and a cut console, a build of an `error` pipeline ending
`FAILURE` with the error in its console, missing jobs and builds, and a wrong token failing with
`ERROR_CI_AUTHENTICATION_FAILED__SERVICE`. Containers and the network are removed afterwards.
Building the image (cached by Docker after the first run) and starting both servers takes well
under a minute; without a reachable Docker daemon or without the system property (e.g. from the
IDE) the suite is empty. `TestJenkinsSwitch` checks that an empty `base-url` switches CI off.

```bash
mvn test -DskipTests=false -pl tl-factory-jenkins
mvn test -DskipTests=false -pl tl-factory-jenkins -Dtest=test.com.top_logic.factory.jenkins.TestJenkinsCIProvider
```

`TestProjectCI` of `tl-factory-jenkins` runs the creation flow of a new project end to end against
an external Gitea and Jenkins, e.g. the [local setup](doc:factory#local-setup): it starts
`GitHostingService` and `CIService` from the configuration of the modules - the environment
variables and secrets of the control application -, takes a provider of the Gitea type for
`tl_factory_gitea_url`, the organization `tl_factory_gitea_owner` (default `tl-factory`) and the
token `tl_factory_gitea_token`, creates a Docker workspace whose new
application is pushed to a new repository through the git route of a workspace proxy on the host,
sets up its CI with `ProjectCI.setUp` (the default `Jenkinsfile` committed through the API of
Gitea, the protection extended, the job created), and waits for the first build of its job, which must end `SUCCESS`
(`mvn -B verify` of the generated application); the end of the console is printed. It runs only
with `-Dtl.factory.integration=true`, `tl_factory_gitea_url` and `tl_factory_jenkins_url` set and a
Docker daemon, and leaves a repository and a job of a new name behind:

```bash
export tl_factory_gitea_url=http://localhost:3300/ tl_factory_jenkins_url=http://localhost:8380/
export tl_factory_jenkins_scm_credentials=tl-factory-gitea
export tl_factory_gitea_token="$(cat ~/.top-logic/secrets/tl_factory_gitea_token)"
export tl_factory_jenkins_token="$(cat ~/.top-logic/secrets/tl_factory_jenkins_token)"
mvn test -DskipTests=false -pl tl-factory-jenkins -Dtest=test.com.top_logic.factory.jenkins.TestProjectCI -Dtl.factory.integration=true
```

## Updating Jenkins

The Jenkins of the tests and of the [local setup](doc:factory#local-setup) is pinned in one
place, its image context `tl-factory-jenkins/src/test/docker/jenkins`:

- `Dockerfile`: the LTS release in `ARG JENKINS_VERSION`, e.g. `2.580.1-lts-jdk21`, annotated with
  `# renovate: datasource=docker depName=jenkins/jenkins`; a `customManagers` entry of
  `renovate.json` reads it, and the tag suffix `-lts-jdk21` keeps the updates on the LTS line with
  Java 21. The target `ci` is the Jenkins of the tests; the target `devel` adds Maven
  (`ARG MAVEN_VERSION`, annotated the same way) and the Maven settings of the workspace image
  (`settings.xml` of its image context, given as the additional build context `workspace-image`,
  with `REPOSITORY_URL` for its repository), the build tools of the default Jenkinsfile.
- `plugins.txt`: every plugin with its version, the ones the jobs need and all their dependencies,
  installed by `jenkins-plugin-cli`. Renovate's manager `jenkins` proposes updates of each.

Both are grouped into one pull request (package rule *Jenkins of tl-factory-jenkins*), which is not
merged automatically: run `mvn test -DskipTests=false -pl tl-factory-jenkins` first, and
`TestProjectCI` against the local setup rebuilt with `tl-factory-devel.sh` for the build of a real
application. If an updated
plugin needs a newer or an additional dependency, `jenkins-plugin-cli` fails the image build naming
it; list all plugins the image gets after the update with

```bash
docker run --rm --entrypoint sh tl-factory-jenkins-test:latest -c 'cd /usr/share/jenkins/ref/plugins && for f in *.jpi; do echo "${f%.jpi}:$(unzip -p $f META-INF/MANIFEST.MF | grep ^Plugin-Version: | tr -d "\r" | cut -d" " -f2)"; done'
```

and pin them in `plugins.txt`.
