---
description: Read before configuring or working on the CI of the control application (the build of the project repositories on a CI server) - CIService with its optional provider and pipeline-template, CIProvider (pipelineFile, ensureJob, startBuild with the queue, lastBuild, console bounded to its end) and its value types CIJob, CIBuild, CIBuildState, CIBuildStart, CIConsole, the test double RecordingCIProvider, the CI job of a project (tl.factory:Project ciJob and ciJobUrl), how a workspace sets it up (the Jenkinsfile in the first commit of a new application from /WEB-INF/factory/ci/Jenkinsfile, the warning for an adopted repository without one, ensureJob and the first build after protectBranch, a CI failure only a warning, CIPipeline as RepositorySource.Pipeline, the pipeline file added to the protected files), the CI route of the workspace proxy (CIRoute at /ci with POST /builds, GET /builds/last, GET /builds/<n>/log, max-log-size, TL_FACTORY_CI_URL, the agent instructions with curl), the views (factoryCiUnavailable, factoryStartBuild, factoryLastBuild, the commands Start CI build and Refresh CI build, the ciBuild channel), the Jenkins implementation JenkinsCIProvider of the module tl-factory-jenkins (its configuration from the environment variables tl_factory_jenkins_url, tl_factory_jenkins_user, tl_factory_jenkins_folder and tl_factory_jenkins_scm_credentials, switched off by an empty base-url through ServerProviderConfig, base-url, user, the secret tl_factory_jenkins_token, credentials, folder, scm-credentials-id, script-path, job-template pipeline-job.xml with its placeholders, timeout, queue-wait, the JSON API, createItem and config.xml, build and the queue item, progressiveText with X-Text-Size and the console notes, no CSRF crumb with an API token), the Jenkins-side prerequisites (user and permissions, SCM credential, agent with JDK and Maven, plugins), its integration test against a Jenkins container next to a Gitea (JenkinsTestServer, TestJenkinsCIProvider, src/test/docker/jenkins with Dockerfile and its targets ci and devel, plugins.txt and the init script tl-factory.groovy with the TL_JENKINS_* variables), the end-to-end test TestProjectCI of a new project and its first build against an external Gitea and Jenkins and updating Jenkins and its plugins (Renovate).
order: 40
---

# CI

The application of a project can be built by a CI server: every project repository on the [git
hosting service](doc:factory/git-hosting) gets a job there, which builds its default branch with
the pipeline definition in the repository. The service `CIService` holds the provider of the CI
server; without a provider, CI is off and the control application works as without it.

| Class | Role |
|---|---|
| `CIProvider` | Platform-neutral access to the CI server: the path of the pipeline definition (`pipelineFile`), the job of a repository (`ensureJob`, create or update), a build (`startBuild`), the last build (`lastBuild`), the end of the console output of a build (`console`). |
| `CIJob`, `CIBuild`, `CIBuildState`, `CIBuildStart`, `CIConsole` | The values of the operations: the job (name, page), a build (number, state, page, start time, duration), its state (`RUNNING`, `SUCCESS`, `FAILURE`, `UNSTABLE`, `ABORTED`, `NOT_BUILT`), a started build (number, or only the queue reference while it waits for an executor), the console output (text, whether it was cut). |
| `CIService` | Service holding the configured provider (`isEnabled()`, `getProvider()` fails with `ERROR_NO_CI` without one) and the pipeline template of a new application. |
| `CIPipeline` | The `RepositorySource.Pipeline` of a workspace: the pipeline definition in the first commit, the job set up after the branch protection. |
| `ProjectCI` | The operations on a stored project: why no build can be started, start a build, the last build. |
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
| `tl_factory_jenkins_url` | *(empty)* | `base-url`, the URL of the Jenkins; empty switches CI off. |
| `tl_factory_jenkins_user` | `tl-factory` | `user`, the Jenkins user of the requests. |
| `tl_factory_jenkins_folder` | `tl-factory` | `folder` of the jobs; empty for the top level. |
| `tl_factory_jenkins_scm_credentials` | *(empty)* | `scm-credentials-id`, the Jenkins credential the jobs check out with; empty for none. |
| `tl_factory_jenkins_token` | - | The API token of the user, a [named secret](doc:credentials/secret-stores) read by the default `credentials`. |

Like a built-in git connection without URL, a provider whose `base-url` is empty is switched off:
`CIService` does not create it and is disabled as without provider (`ServerProviderConfig.isSwitchedOn`).
CI needs git hosting: without it, no project has a repository to build. It builds the projects on a
[built-in git connection](doc:factory/git-hosting#git-connections) only, whose repositories the SCM
credential of the CI server reads; a project on a git connection of its tenant has no CI job
(`ProjectWorkspaces.getCI(project)`, `ERROR_CI_NEEDS_BUILT_IN_CONNECTION__PROJECT`).

| Property of `CIService` | Default | Meaning |
|---|---|---|
| `provider` | none: CI off | The CI server. |
| `pipeline-template` | `/WEB-INF/factory/ci/Jenkinsfile` | Web application resource whose content a new application gets as its pipeline definition. An application overrides the default by a resource of the same path, or names another one. |

| Property of `JenkinsCIProvider` | Default | Meaning |
|---|---|---|
| `base-url` | *(empty: off)* | URL of the Jenkins, its "Jenkins URL"; the pages of jobs and builds are reported below it. |
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

## The pipeline definition

A new application gets the content of `pipeline-template` as `pipelineFile()` (`Jenkinsfile`) in its
first commit. The default, `tl-factory/src/main/webapp/WEB-INF/factory/ci/Jenkinsfile`, is a
declarative pipeline with one stage running `mvn -B verify`, keeping 20 builds and no concurrent
ones. The pipeline file is a protected file: `ProjectWorkspaces.getProtectedFiles()` adds
`pipelineFile()` to `protected-files` of `GitHostingService` if it is not listed literally (the
default `Jenkinsfile, deploy/**` lists it), so neither the agent nor the control application
changes it after the first commit, only a reviewed pull request.

## The CI job of a project

`tl.factory:Project` holds its job in `ciJob`, the name the provider identifies it by
(`FactoryModel.getCIJob`), and `ciJobUrl`, its page; both are read-only and set by the control
application (`FactoryModel.setCIJob`).

### How a workspace sets it up

With CI switched on, `ProjectWorkspaces.create` gives the `RepositorySource` of the workspace a
`CIPipeline` (`RepositorySource.Pipeline`). In addition to the steps of [git
hosting](doc:factory/git-hosting#how-a-project-gets-its-repository):

| Case | CI steps |
|---|---|
| New application | After the generation, the pipeline file is written into the application (`sh -c RepositorySource.WRITE_FILE`, the content in the environment variable `TL_FACTORY_FILE_CONTENT`) and becomes part of the first commit. |
| Cloned repository | The repository keeps its own pipeline file. Without one, the output of the operation gets the warning *The repository ... has no CI pipeline Jenkinsfile*; its builds fail until a reviewed pull request adds one. |
| Both | After `protectBranch`: `ensureJob(repository, project name)`, the job is recorded on the project in a transaction of its own, and the first build is started (`startBuild`). The output names the job and the build. |

A failure of the CI server in these steps - not reachable, credentials rejected, a missing
permission - is a line `Warning: Setting up the CI job failed, ...` in the output of the operation,
never a workspace in state `ERROR`: coding does not depend on the CI server. The job is then set up
by the next *Start CI build*.

`ensureJob` is idempotent: it creates the job and its folders if they are missing and replaces the
configuration of an existing job, so it also corrects a job changed in Jenkins.

### The project and its views

The project panel of `projects.view.xml` has two commands and the detail view a line for CI:

- **Start CI build** (`startBuild`, TL-Script `factoryStartBuild($project)`) runs `ensureJob` - a
  missing job is created, its configuration corrected -, records the job if it changed, starts a
  build and shows which (`BUILD_STARTED__NUMBER_JOB`, or `BUILD_QUEUED__JOB` while it waits for an
  executor). It is disabled with the reason of `factoryCiUnavailable($project)`: CI switched off, the project
  not on a built-in git connection, or without repository.
- **Refresh CI build** asks the CI server again for the last build.
- The channel `ciBuild` shows the job as link to its page and the last build - number with link,
  state, start time, duration - from `factoryLastBuild($project)`, a dictionary with the keys
  `number`, `state`, `stateLabel`, `url`, `startTime`, `durationSeconds`, or only `error` if the CI
  server cannot be asked. It is computed when the project is selected or changes and when one of
  the commands writes the channel `ciRefresh`; nothing polls the CI server in the background.

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
secret). With CI switched on and a CI route in the proxy, `StoredConversation.instructions()` adds
`CIRoute.agentInstructions()` to what the agent is told:

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
| `ensureJob` | `GET <item>/api/json?tree=_class` for each folder and the job (404: missing); `POST <parent>/createItem?name=<name>` with the folder configuration (`<com.cloudbees.hudson.plugins.folder.Folder/>`) or the job configuration as `application/xml`; for an existing job `POST <job>/config.xml`. |
| `startBuild` | `POST <job>/build?delay=0sec` answers `201` with the queue item in `Location`; `GET queue/item/<id>/api/json?tree=cancelled,executable[number]` until the item has its build, at most `queue-wait`. |
| `lastBuild` | `GET <job>/lastBuild/api/json?tree=number,result,building,timestamp,duration`; `404` is no build, or no job if `GET <job>/api/json` is 404 as well. `building` or no `result` is `RUNNING`, an unknown result `FAILURE`. |
| `console` | `HEAD <job>/<number>/logText/progressiveText?start=0` for the size of the output (`X-Text-Size`), then `GET ...?start=<size - max>`; the first, incomplete line is dropped, and the console notes of Jenkins (`ESC[8mha:...ESC[0m`, the hidden annotations of the raw output) are removed. The bound applies to the raw output, so the plain text returned is shorter. |

`<item>` is `job/<segment>/` for each segment of the full name, URL-encoded. The pages the
provider reports are these paths below `base-url`. Answers with 403 fail with
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
check, and 502 for a failing CI server. `TestProjectRepository` checks the creation flow with CI
(the pipeline file in the first commit of a new application, none written for an adopted
repository and the warning without one, `ensureJob` and the first build after the protection, a
failing CI server only a warning) and the functions of the views.

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
off. The test checks: `ensureJob` creating the folders and the job and replacing its configuration
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
application is pushed with the default `Jenkinsfile` to a new repository through the git route of
a workspace proxy on the host, and waits for the first build of its job, which must end `SUCCESS`
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
