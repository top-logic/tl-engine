---
description: Read before running, configuring or embedding the control application for agent-built TopLogic applications (tl-factory, tl-factory-app) - projects and their workspace containers (WorkspaceService, DockerWorkspaceProvider, ApplicationWorkspaces, ProjectWorkspaces, the tl.factory model and the factory* TL-Script functions), the tl_factory_* environment variables, and running the control application on the host or in a container (Docker socket proxy, remote daemon with TLS, named volumes only, the workspace network, public host, label-based ownership), and before running commands in a workspace (startExec, ExecProcess with stdin, terminate and timeouts), following the log of its application (followLog) or letting workspaces reach the control application (extra hosts, host-gateway), before giving workspaces access to services of the control application's environment through the workspace proxy or adding a route to it (WorkspaceProxyService, its routes, ProxyRoute, WorkspaceProxyHandler with the dispatch by path, the token as bearer token or basic-auth password, ProxyForwarding with the streamed request body, proxy-url) or the git route (GitRoute at /git, see factory/git-hosting) or the CI route (CIRoute at /ci, see factory/ci) or the Anthropic API (LLMRoute at /llm-proxy, ANTHROPIC_BASE_URL, tokens per agent run in ANTHROPIC_AUTH_TOKEN and TL_FACTORY_PROXY_TOKEN with issue/revoke/processEnv/issueProcessToken and max-token-lifetime, the git configuration of the routes (gitConfig, GitEnvironment), allowed-sources with workspace-network, the port of its own (WorkspaceProxyListener, listen-address, listen-port), WorkspaceProxyServlet with servlet-enabled, TLS for the proxy, the token checks (TokenGrants, rate-limit with 429 and Retry-After, binding to the first source address), tl_factory_proxy_url, tl_factory_proxy_allowed_sources), before hardening or changing the limits of the workspace containers (memory-limit, cpu-limit, pids-limit, CapDrop ALL, no-new-privileges, host-ip, isolate-workspaces with enable_icc, recreating the workspace network, tl_factory_host_ip, tl_factory_workspace_*), before running the control application on Windows (WSL2), before changing the workspace image or the supervision of its application (the init process, the entry script as supervisor, tl-workspace-app and TL_APP_CONTROL, a failed start, APP_STOPPED, application-check-interval, the non-root user tl, Claude Code, the tl-claude-tools plugin, the Playwright MCP server with headless Chromium, pinned versions, volumes owned by root, no command as root), and before updating its tool versions or the base image digest or moving workspaces to a new image (the Renovate customManagers of the Dockerfile, the integration test before a merge, "Update workspace", WorkspaceProvider.recreate, the image attribute, factoryUpdate, factoryImageOutdated), and before working on the coding agent of a workspace (WorkspaceAgentService, WorkspaceAgent, AgentHarness, ClaudeCodeHarness, ClaudeCodeProtocol, the stream-json mapping to AgentEvents, tl.factory:AgentConversation as chat subject, AgentConversationResolver, --resume with the stored session ID, the agent-sessions volume, a lost conversation (isSessionLost), idle timeout, token per process, a refused token ending the run, --thinking-display, interrupt, tl_factory_agent_*), and before working on the views giving the agent a task and showing its conversations or the application log (Give a task, factoryCreateConversation, factoryAgentUnavailable, chatStart, the Agent tab, WorkspaceLogSource, WorkspaceLogFollow, the Application log tab), and before running the whole loop on one machine with the local development setup tl-factory-app/devel (compose.yaml with Gitea and Jenkins, tl-factory-devel.sh up / down / reset, the generated credentials in ~/.top-logic/secrets with the key tl_secret_store_key of the database secret store of deploy/local, the clone URLs with insteadOf, the environment of tl-factory-app).
---

# Control application (tl-factory)

The control application manages TopLogic applications that are built by agents. Every managed
application is a **project**; its application is generated, built and run in a **workspace**: a
container of its own with named volumes for the sources, the Maven repository and the database,
and for the conversations of its coding agent.
The control application creates, starts, stops and removes these containers; no workspace container
gets access to the container platform itself.

- **`tl-factory`** is a fragment holding everything: the container abstraction, the model, the
  background operations and the views. Another application embeds the control application by
  depending on it and linking the view `factory/projects.view.xml` from its shell.
- **`tl-factory-app`** is the runnable application: `tl-factory` in the React view shell, with the
  login page in front of it (`root` / `root1234` on a fresh database) and the item *Projects* in
  the sidebar.

## Building blocks

| Class | Role |
|---|---|
| `WorkspaceProvider` | Platform-neutral lifecycle of a workspace container: create, start, stop, remove, create anew on another image (`recreate`), status, list, cleanup, commands in the container, the log of its main process, image build, shared volumes. |
| `DockerWorkspaceProvider` | The implementation on the Docker Engine API. |
| `WorkspaceService` | Service holding the configured provider and the `ApplicationWorkspaces`. |
| `ApplicationWorkspaces` | Generates an empty application from `tl-archetype-app` in a workspace - or takes its sources from another `ApplicationSource`, e.g. the repository of the project (see [git hosting](doc:factory/git-hosting#how-a-project-gets-its-repository)) -, builds it and runs it under the supervision of the main process of the container on port 8080. The engine artifacts of a version are resolved once into a shared, read-only release repository. |
| `ProjectWorkspaces` | Service running the operations on the workspaces of the stored projects in the background and storing their state and output; reconciles the stored states with the existing containers after a restart. |
| `WorkspaceProxyService` / `WorkspaceProxyHandler` | Access of the workspaces to services of the control application's environment without a credential in the workspace, through routes such as `LLMRoute` to the Anthropic Messages API, `GitRoute` to the repository of the project and `CIRoute` to its CI job, on a port of its own (`WorkspaceProxyListener`) or in the web application (`WorkspaceProxyServlet`), see [the workspace proxy](#the-workspace-proxy). |
| `GitConnections` / `GitHostingService` / `GitHostingProvider` | The repositories of the projects on git hosting services, optional: each project chooses a git connection - a built-in one of the installation, or one its tenant created with an access token of its own; `GiteaProviderType` of the module `tl-factory-gitea` for Gitea, see [git hosting](doc:factory/git-hosting). |
| `CIService` / `CIProvider` | The CI jobs building the repositories of the projects, optional; `JenkinsCIProvider` of the module `tl-factory-jenkins` for Jenkins, see [CI](doc:factory/ci). |
| `WorkspaceAgentService` / `WorkspaceAgent` | The coding agent of a workspace answering a chat, see [the agent in the workspace](#the-agent-in-the-workspace). |
| `FactoryFunctions` | The TL-Script functions the views call: `factoryCreateWorkspace`, `factoryStart`, `factoryStop`, `factoryCleanup`, `factoryUpdate`, `factoryImageOutdated`, `factoryEngineVersion`, `factoryCheckGroupId`, `factoryCheckArtifactId`; for CI `factoryCiUnavailable`, `factoryStartBuild`, `factoryLastBuild` (see [CI](doc:factory/ci#the-project-and-its-views)). |

The coordinates of a project are checked by `factoryCheckGroupId` and `factoryCheckArtifactId`
(`ProjectWorkspaces.checkGroupId`, `checkArtifactId`): the archetype derives the Java package of
the application from them - the group, then each part of the artifact between dashes as a name -,
so every name of the group and every part of the artifact must be a Java identifier and none a
reserved word of Java (`my-app2`, not `my-2app` or `my-new-app`). The artifact ID, lower case
letters, digits and dashes, also names the context path, the repository and the CI job.

The model module `tl.factory` has the classes `Project` (name, group ID, artifact ID, engine
version, its git connection and its repository there, the workspace as a composition),
`GitConnection` (see [git connections](doc:factory/git-hosting#git-connections)) and `Workspace` (workspace ID, status, host port,
preview URL, initial root password, the image its container was created from, the output of the
last operation as `log`) and the
enumeration `WorkspaceStatus`: *Created*, *Building*, *Starting*, *Running*, *Application stopped*,
*Stopping*, *Stopped*, *Cleaning up*, *Updating*, *Error*, *Unknown*. *Application stopped*
(`APP_STOPPED`) means: the container runs, its application does not - it was stopped or failed to
start. The agent works in such a workspace (see [the agent in the
workspace](#the-agent-in-the-workspace)), *Start* starts only the application, *Stop* stops the
container.

A new workspace gets a random initial password, which the container passes to the application in
the environment variable `tl_initial_password` (`ApplicationWorkspaces.ENV_INITIAL_PASSWORD`); the
application sets it for its account `root` when it creates its database. The detail view shows it
under the preview link. It is the password of a development workspace and is no longer valid once
it is changed in the application.

The output of the application itself - copied to the output of the main process of the container,
its supervisor - goes to the log of the container in the container platform (`docker logs <container>`); the control application only
shows the output of its own operations.

## The workspace image

`ApplicationWorkspaces.imageContext()` holds the files the image is built from (the `Dockerfile`,
the entry script, the control command `tl-workspace-app`, the user instructions `CLAUDE.md` of Claude
Code and the Maven settings in the package `com.top_logic.factory.workspace.app.image`);
the tag of the image is the hash of these files, so a change of a pinned version in the
`Dockerfile` leads to a new image. On top of `eclipse-temurin:21-jdk`, pinned by digest, it
contains:

- Maven, checked against its published SHA-512 checksum, git, and Node with npm from the Node.js
  distribution, checked against its `SHASUMS256.txt`.
- **Claude Code** (`ARG CLAUDE_CODE_VERSION`), installed with npm. The image sets
  `DISABLE_AUTOUPDATER`, `CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC`, `DISABLE_TELEMETRY`,
  `DISABLE_ERROR_REPORTING`, `CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY` and
  `CLAUDE_CODE_DISABLE_OFFICIAL_MARKETPLACE_AUTOINSTALL`: Claude Code neither updates itself nor its
  plugins and sends nothing but its model requests. The image holds no credentials.
- The Claude Code plugin **`tl-claude-tools`** (skills and the `tl-mcp` server) from its public
  repository at the commit `ARG TL_CLAUDE_TOOLS_COMMIT`, checked out to `/opt/tl-claude-tools` and
  registered as a local marketplace `toplogic` for the workspace user.
- The **Playwright MCP server** (`ARG PLAYWRIGHT_MCP_VERSION`), registered for the workspace user as
  MCP server `playwright`, with the headless shell of Chromium and its system libraries. Snapshots
  and screenshots of the server go to `/tmp/playwright-mcp`, not into the sources.
- The **entry script** supervising the application and the command **`tl-workspace-app`**
  controlling it, see [the application under supervision](#the-application-under-supervision).
- **User instructions** of Claude Code (`~/.claude/CLAUDE.md` of `tl`): the application already runs
  on port 8080 and is restarted with `tl-workspace-app restart`, never started a second time; the
  login; no API keys are needed; browser checks with the Playwright MCP server.

The entry script and every command run as the user `tl` (`ApplicationWorkspaces.WORKSPACE_USER`,
uid 1000), not as root - Claude Code refuses to bypass its permission prompts as root. No command
runs as root, not even one of the control application: the containers have no capabilities (see
[hardening](#hardening-of-the-workspace-containers)), so root could do nothing in them `tl` cannot,
and the Docker provider terminates a command as the user it runs as. The image creates the mount
points of all volumes (`ApplicationWorkspaces.VOLUME_PATHS`) owned by `tl`, so that a fresh named
volume mounted there belongs to `tl` as well.

Workspaces and release repositories of images that ran as root:

- A **workspace** keeps the image it was created with and runs as root further on; the provider
  never deletes an image. Its volumes belong to root, so [updating it](#updating-a-workspace) does
  not help - the user `tl` cannot write its sources; clean it up and create the workspace anew to
  get the agent tools.
- A **release repository** filled by root stays usable once it is complete: the workspaces mount
  it read-only. An incomplete one that `tl` cannot write is removed and created anew, empty, before
  it is filled.

### The application under supervision

The application is not the main process of the container but runs under its supervisor, the entry
script, so that it can be stopped and restarted without ending the container - Claude Code and
every other command in the container would end with it.

- **Init.** The Docker provider creates every container with Docker's init process
  (`HostConfig.withInit`) as PID 1: it reaps orphaned processes - the JVMs and tools of commands
  that ended before their children - and forwards the stop signal to the entry script.
- **Supervisor.** Until the file `.tl-factory-built` exists in the source volume, the entry script
  only waits. Then it runs `mvn -B` in the directory of the application with the port 8080 and the
  [context path](#context-path-and-external-url) of the workspace, `TL_FACTORY_CONTEXT_PATH`, and
  the environment of the container (`tl_initial_password` among it). The output goes both to the
  output of the entry script - the container log, so `followLog` and the tab *Application log* show
  every run - and to the log of the current run, `$TL_WORKSPACE_APP_STATE/app.log`. Stopping the
  container (`SIGTERM`) stops the application gracefully and ends the script. The supervisor
  reports in the container log, in lines starting with `[tl-workspace-app]`, when it starts the
  application, when the application is up (`The application is up at <url>.`), when and why it
  stops it (on request, because the container stops, or after a failed start) and with which exit
  code it ended.
- **Failure.** An application that ends by itself has *failed* and is not started again; the
  container keeps running, so that it can be repaired and started. A TopLogic application whose
  initialization fails does not end - its server runs and answers with errors -, so the supervisor
  also reads the log of the run: the server reports `Server started:` after the application
  reported `up and running`. A run whose log shows the first but not the second for
  `TL_WORKSPACE_APP_START_GRACE` seconds (default 5) is stopped and failed as well.
- **Control.** `tl-workspace-app <status|start|stop|restart|log> [<app-module-path>]`
  (`ApplicationWorkspaces.APP_CONTROL_COMMAND`) talks to the supervisor through files in its state
  directory `TL_WORKSPACE_APP_STATE` (`/tmp/tl-workspace-app`): it writes the desired state (`run` or
  `stop`), the supervisor starts or stops the application within a second and publishes its
  state (`unbuilt`, `starting`, `running`, `stopping`, `stopped`, `failed`), the number of runs,
  the process ID and the exit code of the last run. `start` and `restart` wait until the
  application answers at `http://localhost:8080<context path>/` (`TL_WORKSPACE_APP_TIMEOUT`,
  default 300 seconds) and print `url:`, `log:` and `login: root / <initial password>`; a run that
  fails meanwhile fails the command with the end of its log. `stop` leaves the container running.
  `status` prints the state and exits 0 if the application answers, 4 if it failed, 3 otherwise
  (`ApplicationWorkspaces.APP_STATUS_*`); `log` prints the end of the log of the current run
  (`TL_WORKSPACE_APP_LOG_LINES` lines). A module path, if given, must be the directory of the
  application.
- **The `tl-app` skill.** The image sets `TL_APP_CONTROL=tl-workspace-app`
  (`ApplicationWorkspaces.ENV_APP_CONTROL`); the skill of the TopLogic plugin then hands `start`,
  `stop` and `restart` to that command instead of starting a second instance, which would fail on
  the locked H2 database of the running one.

In the control application:

- **Waiting for the application** (`ApplicationWorkspaces.awaitApplication`, after creating,
  starting or updating a workspace) asks `tl-workspace-app status` whenever the preview URL does
  not answer yet. If the application failed, the wait ends at once with an
  `ApplicationFailedException`, after writing the end of the application log to the output of the
  operation; the workspace becomes *Application stopped*.
- **Start** of a workspace whose container runs (*Application stopped*) runs
  `tl-workspace-app start` (`ApplicationWorkspaces.startApplication`) instead of starting the
  container, with the start timeout of the control application.
- **Application check.** Every `application-check-interval` milliseconds (default 10000, `0`
  switches it off) `ProjectWorkspaces` asks the workspaces in state *Running* or *Application
  stopped* on which no operation runs for the state of their application
  (`ApplicationWorkspaces.applicationState`): a stopped or failed application makes a running
  workspace *Application stopped*, one that answers again makes it *Running* - e.g. after the agent
  repaired and restarted it. An application starting or stopping changes nothing.
- **After a restart** of the control application, the reconciliation asks every running container
  the same way.

A container on an image without `tl-workspace-app` tells no application state; it is treated as
before (running container = running application).

### Context path and external URL

The application of a workspace answers under a context path of its own, which may have several
segments: by default `<control context>/preview/<workspace>/<artifactId>`, the path of the
workspace below the [preview route](#the-preview-route) of the control application, e.g.
`/tl-factory-app/preview/my-app-1a2b3c4d/my-app`. The pattern is the property `context-path` of the
`applications` of `WorkspaceService` (`tl_factory_context_path`); `{context}` stands for the
context path of the control application (`%APP_CONTEXT%`, empty at the root), `{workspace}` for
the identifier of the workspace, `{artifact}` for the artifact of the application
(`ApplicationWorkspaces.contextPath`). Every segment consists of letters, digits, `.`, `_`, `~`
and `-`; no segment may be `view` or `servlet`, where the client of a TopLogic application finds
its routes, and the first one not `admin`, where the development server stops a running instance.
The artifact of a project is checked for these names; the identifier of a workspace is never one
of them, it ends with `-` and eight hex digits.

- **Stored per workspace.** A workspace records the context path its container is created with
  (`contextPath`, `FactoryModel.WORKSPACE_CONTEXT_PATH`); creating and updating a workspace takes
  the one of the configured pattern. A workspace without recorded context path has a container
  created before the context path was recorded, which serves the application under
  `/<artifactId>`. Every URL of an application the control application builds - waiting for the
  application, the preview URL of a running workspace - is the preview URL of the workspace
  (`http://<public host>:<host port>/`) followed by this context path
  (`ApplicationWorkspaces.applicationUrl`).
- **In the container.** The container gets the context path in `TL_FACTORY_CONTEXT_PATH`
  (`ApplicationWorkspaces.ENV_CONTEXT_PATH`); the supervisor starts the application with it, and
  `tl-workspace-app` probes and prints the URL with it. The supervisor passes port and context
  path as the arguments of the launch, `-Dexec.args=-port <port> -contextPath <path>`, which
  replace the arguments the parent POM configures for `exec:java`: an application built on a
  TopLogic version whose `tl-parent-app` has no property `tl.contextPath` is started under the
  context path as well.
- **External URL.** `application-origin` (`tl_factory_application_origin`) is the origin under
  which a browser reaches the applications, e.g. `https://factory.example.com`; the containers get
  it as `tl_host` (`ApplicationWorkspaces.ENV_HOST`), the external URL of a TopLogic application
  (`%HOST%`). Empty (the default), `tl_host` is not set and an application takes
  `http://localhost:8080`, the address in its container. The development server of an application
  requests `<tl_host>/admin/stop` on every start to stop an instance running before; the origin
  must answer that request with another status than 200, e.g. 404: a 200 with the body `OK`
  stops the server answering there, a 200 with another body keeps the application from starting.

### The preview route

A browser reaches the application of a workspace through the control application:
`<control origin><control context>/preview/<workspace>/<artifactId>/...` (`PreviewServlet`,
mapped to `/preview/*` in the `web-fragment.xml` of `tl-factory`) is a reverse proxy to the
published port of the workspace. The path is forwarded unchanged - no URL of a page, redirect or
cookie is rewritten -, which is why the application serves exactly the browser-visible path,
including the context path of the control application.

- **What is forwarded.** The first segment below `/preview` names the workspace. Only a path that,
  as sent and decoded, is the stored context path of that workspace or below it, without `.` or
  `..` segments (also with path parameters such as `..;`) and without backslash, is forwarded
  (`PreviewRoute.target`); everything else is answered with 404 and reaches no container, so
  neither `/admin/stop` of the development server nor another path of the workspace or of another
  workspace can be requested through the route. A workspace whose stored context path is not below
  `/preview/<its identifier>` of the current control context - one created with another pattern or
  before the context path was recorded - is reached only after an update.
- **Access check.** The client is the user the `SessionService` registered for the HTTP session at
  the login to the control application; the request passes only if this user may read the project
  of the workspace (`StoredPreviewWorkspaces`, `ModelAccessRights` with the user's rights): the
  members of the tenant and the operators. Without a logged-in session the answer is 403 ("Log in
  to the control application"), for another user 403, for an unknown workspace 404, for a
  workspace that is not `RUNNING` or `STARTING` or has no published port 503, and 503 as well if
  the application does not accept the connection.
- **Target.** `http://<address>:<host port>` with the stored host port of the workspace; the address
  is the one the ports are published on (`WorkspaceProvider.publishedAddress()`): `host-ip` of the
  Docker provider (`tl_factory_host_ip`), the loopback address if it is empty or a wildcard. The
  published ports stay bound to that address; the route is what makes a workspace reachable from
  other hosts. The context path contains the identifier of the workspace, so a stale host port
  that now belongs to another workspace answers 404.
- **Headers and streams.** Method, body and all headers except the hop-by-hop headers pass; the
  application gets `X-Forwarded-For`, `X-Forwarded-Proto` and `X-Forwarded-Host`, and the `Host`
  of its own address. Redirects are passed back, not followed: TopLogic (Jetty) answers with a
  path-absolute `Location`, which stays below the origin of the control application. The answer
  is streamed back and flushed piece by piece, so Server-Sent Events of the React UI arrive at
  once (the heartbeat of the stream keeps the connection busy); its headers replace the ones the
  filters of the control application set, e.g. `X-Frame-Options`. Websocket upgrades are not
  forwarded; the TopLogic UI does not use websockets. The forwarding uses the HTTP client of the
  JDK (`ProxyForwarding`), not a Jetty proxy servlet: the development server hides the Jetty
  classes from the web application.
- **No thread per stream.** The route answers asynchronously (`ServletProxyExchange.startAsync`,
  servlet `async-supported`): the request goes out with `HttpClient.sendAsync`, the body comes
  back as a publisher and is written with non-blocking output, one piece requested after the
  client took the one before. No thread of the control application waits for the application or
  for the client, so open preview tabs do not use up its thread pool. A client that goes away
  cancels the body, which closes the connection to the container. The routes of the workspace
  proxy (LLM, git) forward the same way.
- **Cookies.** The control application and the application both use the session cookie
  `JSESSIONID`, with the paths `<control context>` and `<context path of the workspace>`, so the
  browser sends both with every request of the route. Jetty takes the first of several session
  cookies that names a valid session of its own, so each server finds its session. The route
  removes the session cookie of the control application - the one whose value is the session of the
  request - from what it forwards, so the application of the workspace, code of the tenant, never
  sees it (`PreviewRoute.forwardedCookies`). A cookie the application sets passes only if it has no
  `Domain` and its `Path` is absent or below its context path (`PreviewRoute.isConfinedCookie`), so
  it can neither replace the session cookie of the control application nor set cookies of other
  workspaces.

### Pinned versions

Every tool version of the image is pinned in the `Dockerfile` - `MAVEN_VERSION`, `NODE_VERSION`,
`CLAUDE_CODE_VERSION`, `PLAYWRIGHT_MCP_VERSION`, the commit `TL_CLAUDE_TOOLS_COMMIT` of the
plugin, and the base image `eclipse-temurin:21-jdk` by the digest of its tag (`FROM
eclipse-temurin:21-jdk@sha256:...`). Nothing is installed as "latest":

- **The tag is the content hash.** An image is built again only when a file of its context changes.
  With an unpinned version, a release of a tool would never reach an existing image, and two
  control applications building the same files at different times would run different tools under
  the same tag.
- **The output of Claude Code is parsed.** The control application reads the stream-json output of
  Claude Code; a new version may change it, so it is tested before workspaces use it.
- **Supply chain.** What runs in a workspace changes only by a reviewed change of the repository;
  Claude Code updates neither itself nor its plugins (`DISABLE_AUTOUPDATER`).

Renovate proposes the updates. Each pinned `ARG` has a comment above it naming its source,
`# renovate: datasource=<datasource> depName=<name>` - `maven` for Maven
(`org.apache.maven:apache-maven`), `node-version` for Node, `npm` for Claude Code and
`@playwright/mcp`, and `git-refs` with `packageName=<repository URL> currentValue=main` for the
plugin, whose commit Renovate moves to the head of `main`. The `customManagers` of `renovate.json`
read these comments and the `FROM` line, whose digest Renovate moves with the `docker` datasource
(a rule keeps the tag `21-jdk`, so only new digests of it are proposed); its `packageRules` group the updates into one pull request *workspace image
tools* per week (a major update in a pull request of its own) - each update needs the integration
test below, so a batch is tested once - and restrict Node to LTS lines from 22 on, the version
Claude Code needs.

An update is taken over like this:

1. Renovate opens the pull request changing the `ARG`s.
2. Before merging, run the integration test locally - CI does not run it, it needs a Docker daemon
   and takes minutes:
   `mvn test -DskipTests=false -pl tl-factory -Dtest=test.com.top_logic.factory.workspace.app.TestApplicationWorkspaces -Dtl.factory.integration=true`.
   It builds the new image, creates an application, checks Claude Code in the pinned version, the
   plugin, the Playwright MCP server with its browser, the init process, the supervisor with
   `tl-workspace-app` and the `tl-app` skill handing its commands to it, and updates the workspace
   to another image.
   A new Claude Code version additionally needs a check of the stream-json parsing, see [the agent in
   the workspace](#the-agent-in-the-workspace).
3. Merge. The control application builds the new image on the next creation or update of a
   workspace; existing workspaces show *Image outdated* and are moved with *Update workspace*.

### Updating a workspace

A workspace container keeps the image it was created from. The workspace records this image
(`image`, `FactoryModel.WORKSPACE_IMAGE`) when its container is created; a workspace whose
container was created from another image than the current one -
`ApplicationWorkspaces.currentImage()`, the repository tagged with the content hash of the image
context - is outdated (`factoryImageOutdated`). A workspace without recorded image has no container
yet - it is being built, or its build failed before - and is not outdated. After a restart, the
comparison with the containers (`WorkspaceReconciliation`) takes over the image of every existing
container, so that a container created before the image was recorded is judged by its image as
well. An outdated workspace shows *Image outdated* in the detail view, and the command *Update
workspace* (`factoryUpdate`, `ProjectWorkspaces.update`) is enabled while no operation runs on the
workspace.

The update is an operation like the others, in state *Updating* with its output in the log:

- It builds the current image if needed, makes sure the release repository is complete and creates
  the container anew (`WorkspaceProvider.recreate`): the provider makes the image available,
  removes the old container - stopping it if it runs - and creates the new one from the same spec
  on the existing named volumes. Sources, Maven repository and database stay, so do the
  conversations of the agent and the initial password of the workspace. A workspace whose container is lost but whose volumes exist gets a
  container again this way.
- The new container serves the application under the context path of the configured pattern,
  which the workspace records, see [context path](#context-path-and-external-url).
- If the proxy is switched on, the new container gets the current URLs of its routes, e.g.
  `ANTHROPIC_BASE_URL`.
- A workspace that was running is started again and waited for like by *Start*; otherwise it ends
  in *Created*. A failure ends in *Error* with the reason in the log; the update can be repeated.

## Commands and the log of a workspace

`WorkspaceProvider.startExec` starts a command in a running workspace and returns an `ExecProcess`:
the caller writes to the standard input of the command (`write`, `flush`, `closeStdin`), receives
its output incrementally through an `ExecOutput`, waits for it (`waitFor` with a timeout,
`exitCode`) and ends it (`terminate`). `close` releases the handle and kills a command that still
runs. A long-lived command reading requests from its input - an agent process, for example - is run
this way.

`WorkspaceProvider.exec` runs a command to its end on top of this handle: the command reads an
empty input, and a command that exceeds the timeout of its `ExecCommand` (`ExecCommand.DEFAULT_TIMEOUT`,
ten minutes, if it sets none) is terminated and fails the call with a timeout error. The Maven runs
of `ApplicationWorkspaces` (generating, building, filling a release repository) use the
`build-timeout` instead.

`WorkspaceProvider.followLog` delivers the output of the main process of a workspace - the
supervisor and the application it runs, across restarts of the application - from a number of last lines and/or a point in time on, and then what it writes while
it runs, until the returned `LogFollower` is closed or the main process ends.

The tab *Application log* of the workspace detail (`factory/app-log.view.xml`) shows that output
while it is written. Its source channel is fed by `WorkspaceLogSource`, a `ChannelSource` that
follows the workspace on the channel named by `workspace` and publishes the last `tail-lines` lines
(default 500) as one text, at most every `update-interval` of the channel:

```xml
<source-channel name="appLog" update-interval="1000">
    <source class="com.top_logic.factory.project.WorkspaceLogSource" workspace="workspace" tail-lines="500"/>
</source-channel>
```

Each displayed view has a delivery of its own (`WorkspaceLogFollow`), which ends when the view is
no longer displayed. Another workspace on the channel replaces the delivery. The delivery ends with
the main process - the workspace stops or is updated -; the next commit changing the workspace, its
new state, starts it anew with the last lines of the new output. The provider is asked off the
threads of the windows and commits; a workspace without container shows no output.

In the Docker provider:

- The Docker API has no way to signal an exec instance. A command therefore runs under a short
  `sh` script that writes the process ID of the command as first line of its output (the provider
  removes the line) and replaces itself by the command. A process started by `docker exec` leads its
  own process group, so `terminate` runs `kill -TERM -<pid>` in the container for the whole
  group, as the user of the command - no other user may signal it in a container without
  capabilities -, and `kill -KILL -<pid>` if the group has not ended after `terminate-timeout`
  seconds. The workspace image needs `sh` and `kill`.
- The input of a command is written over a connection to the daemon that the provider opens itself
  (Unix socket or TCP, with TLS if `tls-cert-path` is set): the daemon closes the input of the
  command only when the client half-closes the connection, which the Docker client library does
  not do.

An operation is started from a command: the function stores the state the operation is in and
returns; a worker thread of `ProjectWorkspaces` runs the operation in a system context. Its output
is collected and committed at most every `log-flush-interval` milliseconds, only the last
`max-log-size` characters are kept. The view `factory/projects.view.xml` follows these commits
through a source channel fed by `CommitSource` (see
[channels fed by a server-side source](doc:view-layer/channels#channels-fed-by-a-server-side-source-source-channel)).
Only one operation runs on a workspace at a time.

*Clean up* removes the container and the volumes of a workspace and the workspace object; the
project stays and can create a new workspace. A project is deleted only after its workspace is
cleaned up.

After a restart, the stored states are compared with the containers that exist
(`WorkspaceReconciliation`): an operation interrupted by the restart while building or cleaning up
leaves the workspace in *Error*, a workspace whose container is gone is in *Error*, and every other
workspace takes the state of its container - a running container whose application is stopped or
failed makes it *Application stopped*. The comparison runs in the background, so the
application starts also when the container platform is not reachable; the workspaces are *Unknown*
then.

## Configuration

`tl-factory.conf.xml` defines an alias for every setting, read from an environment variable:

| Variable | Default | Meaning |
|---|---|---|
| `tl_factory_docker_host` | `unix:///var/run/docker.sock` | URI of the Docker daemon, `unix://` or `tcp://`. |
| `tl_factory_docker_network` | `tl-factory` | Network all workspace containers join; created if missing. |
| `tl_factory_public_host` | `localhost` | Host name under which a browser reaches the published ports; the preview URL is built from it. |
| `tl_factory_host_port_min` / `tl_factory_host_port_max` | `20000` / `20999` | Range of host ports the container port 8080 is published on. |
| `tl_factory_host_ip` | `127.0.0.1` | Address of the daemon host the ports are published on; empty for all addresses, see [hardening](#hardening-of-the-workspace-containers). |
| `tl_factory_docker_isolate` | `true` | Whether the workspace containers are kept from reaching each other on the workspace network; applies when the network is created. |
| `tl_factory_workspace_memory` | `8g` | Memory a workspace container may use; `0` for no limit. |
| `tl_factory_workspace_cpus` | `4` | CPUs a workspace container may use, e.g. `1.5`; cut to the CPUs of the daemon host; `0` for no limit. |
| `tl_factory_workspace_pids` | `4096` | Processes and threads a workspace container may run at once; `0` for no limit. |
| `tl_factory_engine_version` | `8.0.0-alpha11` | Engine version a new project proposes. |
| `tl_factory_image_repository` | `tl-factory-workspace` | Name of the workspace image; the tag is the content hash of its files. |
| `tl_factory_repository_url` | TopLogic Nexus | Maven repository the TopLogic releases are taken from. |
| `tl_factory_start_timeout` | `600` | Seconds a started application has to answer. |
| `tl_factory_build_timeout` | `1800` | Seconds a Maven run in a workspace (generate, build, fill a release repository) may take before it is terminated. |
| `tl_factory_context_path` | `{context}/preview/{workspace}/{artifact}` | Pattern of the context path the application of a workspace is served under, see [context path](#context-path-and-external-url). |
| `tl_factory_application_origin` | *(empty)* | Origin under which a browser reaches the applications of the workspaces, given to them as `tl_host`; empty if not known. |
| `tl_factory_docker_extra_hosts` | *(empty)* | Comma separated `<host>:<address>` entries added to the host names of new workspace containers, e.g. `host.docker.internal:host-gateway`. |
| `tl_factory_proxy_url` | *(empty)* | URL of the root of the workspace proxy as the workspace containers reach it, without the path of a route, e.g. `http://host.docker.internal:8091`; empty switches the proxy off, see [the workspace proxy](#the-workspace-proxy). |
| `tl_factory_proxy_address` | `127.0.0.1` | Address the proxy listens on with its own port, see [the port of the proxy](#the-port-of-the-proxy). |
| `tl_factory_proxy_port` | `8091` | Port of the proxy, apart from the port of the control application; `0` for none. |
| `tl_factory_proxy_servlet` | `false` | Whether the proxy also answers in the web application, at the paths of `WorkspaceProxyServlet` (`/llm-proxy/`). |
| `tl_factory_proxy_rate_limit` | `120` | Requests per minute a token may make; `0` for no limit. |
| `tl_factory_proxy_allowed_sources` | *(empty)* | Comma separated client addresses the workspace proxy accepts: CIDR ranges (IPv4 or IPv6) or `workspace-network`; empty accepts every address, see [accepted sources](#accepted-sources). |
| `tl_factory_git_protected_files` | `deploy/**` | Comma separated glob patterns of the files of a project repository no push to its default branch may change; the pipeline file is added once the CI of the project is set up, see [git hosting](doc:factory/git-hosting#protected-files-and-commit-identity). |
| `tl_factory_git_user_name` / `tl_factory_git_user_email` | `TopLogic Factory` / `tl-factory@localhost` | Author and committer of the commits made in the workspaces, by the agent and the control application; empty for none. |
| `tl_factory_llm_provider` | `anthropic` | Name of the Anthropic provider of `LLMProviderService` whose API key, base URL, version and timeouts the LLM route of the proxy uses. |
| `tl_factory_agent_model` | *(empty)* | Model the coding agent works with, e.g. `claude-sonnet-4-6` or `sonnet`; empty for the default of Claude Code. |
| `tl_factory_agent_thinking_display` | `summarized` | How the thinking of the agent appears in its output: `summarized` for a summary the chat shows, `omitted` for none, empty for the default of Claude Code (none in print mode). |
| `tl_factory_agent_extra_args` | *(empty)* | Comma separated further arguments of the command line of the agent, e.g. `--max-turns,50`. |
| `tl_factory_agent_idle_timeout` | `15min` | How long the process of an agent conversation is kept after its last answer. |
| `tl_factory_agent_token_lifetime` | `4h` | Lifetime of the proxy token of an agent process, at most `max-token-lifetime` of the proxy. |
| `tl_factory_log_flush_interval` | `1000` | Milliseconds between two commits of the output of a running operation. |
| `tl_factory_max_log_size` | `65536` | Characters of the output of an operation that are stored. |

Further properties of the Docker provider (`tls-cert-path`, `connect-timeout`, `label-namespace`,
`name-prefix`, `stop-timeout`, `terminate-timeout`) are set in the configuration of `WorkspaceService` of the
application.

`tl-factory-app` depends on the modules `tl-factory-gitea` and `tl-factory-jenkins`: `tl_factory_gitea_url`
and `tl_factory_gitea_owner` switch on its built-in [git connection](doc:factory/git-hosting#configuration)
to a Gitea, `tl_factory_jenkins_*` [CI](doc:factory/ci#configuration); without their URLs, both are
off. The access tokens the tenants enter for their own git connections are kept in the writable
secret store: the deployment `deploy/local` of `tl-factory-app` appends a `database-store` to the
development secret store (`tl-factory-devel-secrets.config.xml`, key `tl_secret_store_key`, see
[secret stores](doc:credentials/secret-stores#databasesecretstore)); a production installation
configures its secret store with a writable store, otherwise its tenants cannot create git
connections.

### Hardening of the workspace containers

An agent works in its workspace with all permissions of Claude Code skipped. What it - or a
dependency, a web page, a prompt injection - does there must stay in that workspace. The Docker
provider creates every workspace container like this:

- **Resource limits** (`memory-limit`, `cpu-limit`, `pids-limit`): one workspace - a Maven build,
  Chromium, a fork bomb of the agent - cannot starve the others. The default of 8 GiB holds the
  Java processes of a workspace (the application, a Maven build and the `tl-mcp` server of the
  agent, each taking a quarter of the limit as default heap) beside Claude Code and the headless
  Chromium; 4 CPUs keep a build fast without taking a server; 4096 processes and threads leave
  room for several JVMs and the browser and stop a fork bomb. A CPU limit above the CPUs of the
  daemon host is cut to them, which the daemon would refuse otherwise. Under a rootless daemon
  the limits need the cgroup controllers delegated to its user, see
  [the Docker server article](doc:factory/docker-server#resource-limits).
- **No capabilities, no new privileges**: all capabilities are dropped (`CapDrop=ALL`) and
  `no-new-privileges` is set, so root in the container can do nothing `tl` cannot, and no setuid
  program gains rights. Nothing the image or the application needs at runtime requires a
  capability: Docker's init process, the entry script, Java, Maven, Node, Claude Code and the
  headless Chromium of the Playwright MCP server, which Playwright starts without the Chromium
  sandbox (`--no-sandbox`, its default in a container). `TestApplicationWorkspaces` checks the
  browser through the MCP server in such a container.
- **The published port on one address** (`host-ip`, `tl_factory_host_ip`, default `127.0.0.1`): an
  unfinished application with its initial password is not reachable from the whole network. A
  rootful Docker daemon publishes ports by DNAT rules that come before the firewall rules of the
  host - UFW does not block them -, so the address is the restriction that holds. A server sets
  the address explicitly, e.g. the internal address a reverse proxy forwards to. The
  [preview route](#the-preview-route) of the control application makes the previews reachable
  through its login, with the published ports staying on that address.
- **No traffic between workspaces** (`isolate-workspaces`, `tl_factory_docker_isolate`): the
  workspace network is created with `com.docker.network.bridge.enable_icc=false`
  (`DockerWorkspaceProvider.OPTION_ENABLE_ICC`). A workspace reaches the daemon host through the
  gateway of its network - the control application and its workspace proxy in host mode, the outside -,
  but not the application or database of another workspace. Together with the published port on
  the loopback address, it does not reach that application through the host either. The option
  is fixed when a network is created: the provider does not change an existing network with
  another setting, which other containers may use, but logs a warning once. Such a network -
  e.g. `tl-factory` of a control application from before this setting - is removed while no
  container is attached (`docker network rm tl-factory`, after stopping the workspaces or
  disconnecting them); the next workspace that is created or updated creates it anew.

## The workspace proxy

A workspace uses services that need a credential - the Anthropic Messages API for Claude Code -,
but no credential ever enters a workspace container: whatever runs in the container - the agent,
the generated application, a dependency of it - could read it. The container talks to these
services through the workspace proxy of the control application (`WorkspaceProxyService`)
instead, which adds the credentials of the control application.

- **Routes.** Each service is a route of the proxy (`ProxyRoute`) below a path of its own, listed
  in `routes` of `WorkspaceProxyService`; `tl-factory` configures the [LLM route](#llm-access)
  at `/llm-proxy`, the [git route](doc:factory/git-hosting#the-git-route) at `/git` and the
  [CI route](doc:factory/ci#the-ci-route) at `/ci`. A route is a polymorphic configuration
  (`<route class="..." path="..."/>`, keyed by its `path`, which starts with a slash and has none
  at its end).
- **The URL.** `proxy-url` (`tl_factory_proxy_url`) is the root of the proxy as the workspace
  containers reach it, without the path of a route, e.g. `http://host.docker.internal:8091`; a
  route is reached at that URL followed by its path. Empty switches the proxy off: workspaces get
  no URL, no token is issued, and every request is answered with 404. If it is switched on, every
  workspace container gets what the routes need to find the proxy (`ProxyRoute.containerEnv`,
  `WorkspaceProxyService.containerEnv()`), e.g. the URL of the LLM route in `ANTHROPIC_BASE_URL`.
  It is no secret and the only thing about the proxy the container gets.
- **The token.** The container gets no token. A token belongs to one agent run: the control
  application issues it when it starts the run (`WorkspaceProxyService.issue(workspaceId,
  lifetime)`) and passes it only to the process of the run, in the environment of its command
  (`ProxyTokens.processEnv(token)`, put on the `ExecCommand` by the harness, never on the
  container); neither the application of the workspace nor `docker inspect` sees it. The
  environment holds the token in `TL_FACTORY_PROXY_TOKEN` (`ProxyTokens.ENV_PROXY_TOKEN`), what
  the routes add (`ProxyRoute.processEnv`, e.g. `ANTHROPIC_AUTH_TOKEN`), and the git configuration
  of all routes (`ProxyRoute.gitConfig`, e.g. the credential helper of the git route) as one set of
  `GIT_CONFIG_COUNT` / `GIT_CONFIG_KEY_<n>` / `GIT_CONFIG_VALUE_<n>` (`GitEnvironment`), so that
  routes cannot overwrite each other's entries. A command of the control application in a
  workspace gets a token of its own the same way: `issueProcessToken(workspaceId, lifetime)`
  returns token and environment as `ProcessToken`, whose `close()` revokes the token. A token is valid for every route of the proxy. It is `tlwst_` followed by a
  random secret of about 256 bits (`SecretTokens` of `tl-ai-core`). It is valid until the run
  ends and the token is revoked (`revoke`), until its lifetime ends - at most `max-token-lifetime`
  of `WorkspaceProxyService`, four hours by default; a longer lifetime asked for is cut to it -, or
  until its workspace is removed (*Clean up* revokes all its tokens, `revokeAll`, and a token of a
  workspace that no longer exists is refused anyway). Tokens are kept in memory only, by their
  SHA-256: nothing is stored in the database, and a restart of the control application
  invalidates all tokens - together with the runs it started, which end with it.
- **The checks.** `WorkspaceProxyHandler` chooses the route whose path is the longest prefix of the
  path of the request (followed by a slash or its end); a request reaching no route is answered
  with 404. Before the route sees the request, the checks common to all routes are made: a request
  from an address that is not accepted (see [accepted sources](#accepted-sources)) is refused with
  403, a missing, unknown, revoked or expired token with 401, a token used from another address
  than its first request with 403, and a token over its rate limit with 429 (see [token
  checks](#token-checks)). The token is read from `Authorization: Bearer <token>` or as the
  password of `Authorization: Basic` - the user name is ignored -, so that clients that only speak
  basic authentication can present it. The route answers a refusal in the format of its protocol
  (`ProxyRoute.sendError`) and gets the admitted request with the workspace of its token.
- **Forwarding.** `ProxyForwarding` holds what the routes share: the request headers that are
  never forwarded (`Authorization`, cookies, `Host`, the length and the hop-by-hop headers), the
  request body streamed to the upstream as it arrives - with the length of the client, or in
  chunks without one -, never held as a whole, and the answer passed back with status and headers,
  its body flushed as it arrives.

The workspace containers must reach the URL of the proxy, see [the port of the
proxy](#the-port-of-the-proxy) and *Reaching the control application* in the operation modes
below. The proxy answers any valid token, whatever state its workspace is in.

### LLM access

Claude Code in a workspace talks to the Anthropic Messages API through the route `LLMRoute` of
the workspace proxy, at `/llm-proxy` (its `path`).

- **Environment.** A workspace container gets the URL of the route in `ANTHROPIC_BASE_URL`, e.g.
  `http://host.docker.internal:8091/llm-proxy`; an agent process its token in
  `ANTHROPIC_AUTH_TOKEN`, which Claude Code sends as `Authorization: Bearer <token>`.
- **The route.** It forwards `POST` of `/v1/messages` and `/v1/messages/count_tokens` (with their
  query string) to the base URL of the configured provider; every other path and method is
  answered with 404. Refusals of the proxy are answered as errors of the Anthropic API
  (`authentication_error`, `permission_error`, `rate_limit_error`). It drops the credentials
  (`Authorization`, `x-api-key`), cookies, `Host`, `Accept-Encoding` and the hop-by-hop headers of
  the request, sets `x-api-key` to the API key of the provider and `anthropic-version` to its
  version, unless the client sent one; a client's `anthropic-beta` is kept. Status, headers and
  body of the answer are passed back, the body flushed as it arrives, so that a streamed answer
  (server-sent events) reaches the agent while it is produced. The API key appears neither in a
  log nor in an answer.
- **The key.** It is the `api-key` of the Anthropic provider named by `provider` of the route
  (`tl_factory_llm_provider`) in the `LLMProviderService` of `tl-ai-core`. `tl-factory-app`
  configures a provider `anthropic` without models whose key is read from the environment
  variable `tl_anthropic_api_key`.

### The port of the proxy

The port of the control application is the one its users reach, from wherever they are; the
proxy needs not be reachable there. `WorkspaceProxyService` therefore serves the proxy on a port
of its own: `WorkspaceProxyListener`, an HTTP server of the JDK independent of the server the
control application runs in, listens on `listen-address`:`listen-port`
(`tl_factory_proxy_address`, default `127.0.0.1`, and `tl_factory_proxy_port`, default
`8091`) and answers at the paths of the routes only. All requests go to one handler, which
chooses the route, so that every other path is answered the same way. It is opened only while the
proxy is switched on by its URL; port `0` opens none. A port that cannot be bound is logged as
error, and the workspaces have no access to the routes then.

The address depends on how the workspaces reach the control application:

| Operation mode | `listen-address` | `tl_factory_proxy_url` |
|---|---|---|
| Control application on the host of the daemon | the address `host-gateway` stands for, the gateway of Docker's default bridge (`docker network inspect bridge`, e.g. `172.17.0.1`): the workspaces reach it, other hosts do not | `http://host.docker.internal:8091` with the extra host `host.docker.internal:host-gateway` |
| Control application in a container of the daemon | `0.0.0.0` in its container, the port not published; see [isolation](#hardening-of-the-workspace-containers) for how the workspaces reach the container | `http://<address of the container>:8091` |
| [Two daemons](doc:factory/docker-server#how-control-application-and-workspaces-reach-each-other) | `0.0.0.0` in its container, the port published on an internal address of the server only, never on all addresses | `http://<internal address of the server>:8091` |
| Behind a reverse proxy or tunnel on the same host | `127.0.0.1`, the default | the URL of the reverse proxy |

The servlet `WorkspaceProxyServlet` of the web application is mapped to the paths of the routes
(`/llm-proxy/*`, `/git/*` and `/ci/*` in the `web-fragment.xml` of `tl-factory`; a route served there
needs a mapping of its path) and answers only if `servlet-enabled` (`tl_factory_proxy_servlet`) is set;
otherwise it answers with 404. `proxy-url` is then the URL of the web application, e.g.
`http://host.docker.internal:8080/tl-factory-app`. It is the alternative where no second port can
be opened: then a reverse proxy in front of the control application must refuse the paths of the
routes to clients from outside (e.g. `location /tl-factory-app/llm-proxy/ { deny all; }` in nginx)
and pass them only on the address the workspaces use.

**TLS.** The token travels in the `Authorization` header. Whenever the proxy is not reached on the
host itself or over a network of the daemon - a VM, a remote daemon, another host, the cloud -, it
must be reached over TLS: a reverse proxy terminating TLS in front of the port of the proxy, and
`tl_factory_proxy_url` with `https://`. The workspace must trust its certificate: a
certificate of a public CA needs nothing, one of an own CA is added to the image or given to Claude
Code in `NODE_EXTRA_CA_CERTS`.

### Token checks

Beside its validity, every request is checked against its token (`TokenGrants`):

- **Rate limit.** A token may make `rate-limit` requests per minute (`tl_factory_proxy_rate_limit`,
  default 120, `0` for none), in bursts up to that number; its allowance refills continuously. A
  request beyond is answered with 429, `rate_limit_error` and the seconds to wait in `Retry-After`,
  which Claude Code waits for before it repeats the request. 120 leave an agent working at full
  speed - a request per step plus token counting and side requests of a small model - room, and
  limit what a token that got out of the workspace can cost. A budget per token or workspace is not
  enforced.
- **Source binding.** A token is bound to the address its first admitted request comes from
  (trust on first use, `getRemoteAddr()` of the connection, never a header). A request from another
  address is answered with 403 and logged as warning with the address and the workspace; the token
  is not revoked and keeps working from its own address. The binding is always on. It means most
  where the proxy sees the address of the workspace container - the control application on the
  host or on the workspace network: a token that leaks out of the workspace is useless elsewhere.
  Behind NAT, a userland proxy, a rootless daemon or a reverse proxy, all workspaces share one
  source address; the binding then only refuses a token used from another address than that one.

### Accepted sources

`allowed-sources` of `WorkspaceProxyService` (`tl_factory_proxy_allowed_sources`) restricts the clients
of the proxy by their address, in addition to the token. An entry is a range in CIDR notation,
IPv4 or IPv6, a single address, or `workspace-network`: the ranges of the network the workspace
containers are attached to, as the container platform reports them
(`WorkspaceProvider.networkSubnets()`, for Docker the IPAM configuration of
`tl_factory_docker_network`). They are read on the first request and again when an address
matches no range, at most every 30 seconds. An empty list accepts every address. A refused request
is answered with 403 and logged at level INFO with its address. The address checked is the one of
the connection (`getRemoteAddr()`), never a header such as `X-Forwarded-For`.

Which setting fits depends on how a request of a workspace reaches the control application:

| Operation mode | Source address of a workspace request | Setting |
|---|---|---|
| Control application on the host, workspaces reach it through `host-gateway` | the address of the container on the workspace network | `workspace-network` |
| Control application as container of the same daemon, connected to the workspace network | the address of the container on the workspace network | `workspace-network` |
| Two daemons ([the rootless setup](doc:factory/docker-server#how-control-application-and-workspaces-reach-each-other)), a reverse proxy in front of the control application, or a port published through the Docker userland proxy | a gateway or the proxy, the same for workspaces and for clients from outside | empty |

In the last case an address list cannot tell workspaces from other clients. Leave it empty; the
port of the proxy, bound to an address only the workspaces reach, its token and its rate limit
remain the checks.

## The agent in the workspace

A chat whose subject is an `AgentConversation` (`tl.factory`) is answered by the coding agent
working in the conversation's workspace instead of the agent of the control application: the
`AgentConversationResolver`, registered in the `ConversationBackendService` of `tl-chat`, hands it
to the `WorkspaceAgent` of the conversation, which `WorkspaceAgentService` keeps per conversation
(see [a conversation answered from outside](doc:ai/chat-ui#a-conversation-answered-from-outside)).
The chat offers nothing beyond sending a message (`BackendFeatures.NONE`). Cleaning up a workspace
stops its agents and deletes its conversations with it; the chat sessions about them stay without
subject.

**In the workspace detail.** *Give a task* (`factory/assign-task.view.xml`) asks for a task and
gives it to the agent: `factoryCreateConversation(workspace, task)` creates the conversation with
the task (`task`), and `chatStart('workspace-agent', conversation, task)` of `tl-chat` creates the
chat session of the current user about it and sends the task as its first message through the chat
`workspace-agent` of the window, the chat of the tab *Agent*; the session is put on that chat's
`input` channel, and `<show-view view="factory/workspace-agent.view.xml"/>` brings the tab *Agent*
into view, so it shows the run while it is written. The command is offered while
`factoryAgentUnavailable(workspace)` names no reason: the container of the workspace runs
(*Running* or *Application stopped* - the agent is needed most when the application does not
start) on the current image, the agent service runs and the workspace proxy is switched on. The tab *Agent* (`factory/workspace-agent.view.xml`)
lists the conversations of the workspace, newest first, beside the chat of the selected one; its
subject is the conversation, so a message typed there continues it, also a conversation another
user started, whose first message then creates that user's chat session about it.

**The process.** The agent is described by an `AgentHarness` - its command line and its
protocol -, `ClaudeCodeHarness` for Claude Code; the process itself is handled by `WorkspaceAgent`
the same for every harness. One process serves one conversation:

- It is started on the first message, as `claude -p --input-format stream-json
  --output-format stream-json --verbose --include-partial-messages --permission-mode
  bypassPermissions [--thinking-display <display>] [--model <model>] [--resume <session ID>]
  [<extra args>]`, as the user `tl` in
  the directory of the application (`/workspace/<artifact ID>`). The workspace must be running;
  otherwise the message is refused with a sentence saying so.
- **Token per process.** The process gets a token of the workspace proxy of its own, issued for
  `token-lifetime` and passed only in the environment of its command (`AgentLaunch.env()`, e.g.
  `ANTHROPIC_AUTH_TOKEN`); it is revoked when the process ends. A process whose token has less than half of its lifetime left is replaced before
  the next message.
- It is kept between the messages and stopped after `idle-timeout` without a message (its input is
  closed, after `stop-timeout` it is terminated).
- **Resume.** Claude Code keeps the conversation under a session ID, which `system/init` reports;
  it is stored in `sessionId` of the conversation in a transaction of its own as soon as it is
  known. A new process - after the idle timeout, a token renewal, a crash or a restart of the
  control application - continues the conversation with `--resume <session ID>`; the session ID
  stays the same. Claude Code keeps its conversations in `~/.claude/projects`
  (`ApplicationWorkspaces.AGENT_SESSIONS_PATH`), which is the named volume `agent-sessions` of the
  workspace, so a conversation also continues after *Update workspace* created the container anew.
  Only that directory is a volume: the plugin, the MCP servers and `CLAUDE.md` in `~/.claude` come
  from the current image.
- **Lost conversation.** A process that does not find the conversation it is to resume - a
  workspace created before the volume existed, a removed volume - answers with an error `result`
  "No conversation found with session ID ..." and ends. `ClaudeCodeProtocol` lets nothing of that
  reach the chat (`AgentProtocol.isSessionLost()`); `WorkspaceAgent` clears the stored session ID,
  starts a process with a new conversation for the same run and message, and the run begins with
  the text `NOTICE_CONVERSATION_LOST`: the agent answers without the earlier course.
- A message is one line `{"type":"user","message":{"role":"user","content":[{"type":"text","text":...}]}}`
  on the input of the process. The output is split into lines by a thread of the provider and
  queued; the run drains the queue on the chat's thread until the `result` line, so that every
  event reaches the chat on that thread. Error output is logged.
- **Stop.** A stop of the user writes `{"type":"control_request","request_id":...,"request":{"subtype":"interrupt"}}`;
  Claude Code answers with a `control_response` and ends the run with a `result` of subtype
  `error_during_execution`, which ends the run as `ABORTED`. The process is kept. A process that
  does not stop within `interrupt-timeout` is terminated.
- A process that ends during a run ends the run with a `ProviderError` `PROCESS_EXIT` and `ERROR`.
- **Refused token.** A request the proxy refuses with 401 or 403 ends the run at once with a
  `ProviderError` `API_401` / `API_403` and `ERROR`, saying that the next message starts the agent
  anew (`I18NConstants.ERROR_LLM_ACCESS_REFUSED__STATUS`). Claude Code would repeat the request
  with the same token up to ten times over about three minutes; the protocol reports the process
  as spent (`AgentProtocol.isSpent`), and `WorkspaceAgent` terminates it. The next message starts a
  new process with a new token.
- **Thinking.** In print mode, Claude Code asks the API to omit the text of the thinking: the
  thinking blocks of the stream carry their signature only, and the chat would show no thinking.
  `--thinking-display summarized` (`thinking-display` of `ClaudeCodeHarness`,
  `tl_factory_agent_thinking_display`, the default) asks for the summary of the thinking, which
  arrives as `thinking_delta` text. The option is not listed by `claude --help` of the pinned
  version (2.1.309); it is checked by `TestClaudeCodeIntegration.testRecordThinking`.

**The mapping** (`ClaudeCodeProtocol`, from the stream-json output of the pinned Claude Code
version):

| Line of Claude Code | Event |
|---|---|
| `system/init` | `RunStarted` with the model, once per message; the session ID is stored. |
| `stream_event` `message_start` | `TurnCompleted` of the previous request, `TurnStarted`: a turn is one request of the model with the tools it calls. |
| `stream_event` `content_block_delta` | `TextDelta` for `text_delta`, `ThinkingDelta` for a non-empty `thinking_delta`. |
| `stream_event` `message_delta` | Its usage becomes the usage of the turn. |
| `assistant` | `ToolCallStarted` per `tool_use` block, with its complete input. Text blocks were streamed already and are skipped - except in a message that was not streamed, e.g. the one Claude Code writes when the proxy refuses its token. |
| `user` with `tool_result` | `ToolCallCompleted`, or `ToolCallFailed` for `is_error`. |
| `system/api_retry` | `RetryScheduled` with the HTTP status as kind (`API_529`), the attempt and the delay; Claude Code repeats a failed request up to ten times with growing delays. For 401 and 403 - the token is refused - the run ends instead, see *Refused token* above. |
| `system/compact_boundary` | `CompactionStarted` and `CompactionCompleted`. |
| `result` | `TurnCompleted` of the last request, `RunCompleted`: `COMPLETE` for `success`, `MAX_TURNS`, `BUDGET_EXHAUSTED`, `ABORTED` after a stop, otherwise `ProviderError` (`API_<status>` or the subtype) and `ERROR`. The usage is the one of the result, the cost the sum of `model_usage_since_last_result` - `total_cost_usd` counts the whole session over all its processes. Its `contextWindow` is what `WorkspaceAgent.contextWindow()` reports to the chat from then on. |

Lines of a subagent (`parent_tool_use_id` set) contribute their tool calls only; its answer reaches
the main agent as the result of its tool call. Hooks, status lines and control responses are
ignored. A tool call still open when the run ends gets a `ToolCallFailed`.

The test fixtures of the mapping are recorded from the pinned Claude Code version by
`TestClaudeCodeIntegration` (`-Dtl.factory.integration=true`, the API key in
`tl_anthropic_api_key`, `-Dtl.factory.record=<fixtures directory>`), which runs Claude Code in a
workspace container against a `WorkspaceProxyServlet` with an `LLMRoute` of the test and a conversation through
`WorkspaceAgent`. A new Claude Code version is checked by recording the fixtures anew and running
`TestClaudeCodeProtocol` and `TestWorkspaceAgent` on them.

## Operation modes

### Control application on the host

The simplest mode, used during development: the control application runs as a process on a host
with a Docker daemon and talks to it through the default socket `unix:///var/run/docker.sock`. The
user running it must be allowed to use the socket. Preview links point to
`http://localhost:<host port>/<artifact ID>/`; for access from other machines,
`tl_factory_public_host` names the host as the browsers see it.

A workspace container reaches the control application through the gateway of the daemon: with
`tl_factory_docker_extra_hosts=host.docker.internal:host-gateway`, new containers resolve
`host.docker.internal` to the address of the host on Docker's default bridge (`docker0`, e.g.
`172.17.0.1`), and reach the workspace proxy under `http://host.docker.internal:8091` when it
listens on that address (`tl_factory_proxy_address=172.17.0.1`, see [the port of the
proxy](#the-port-of-the-proxy)). A firewall of the host must let the workspace network through to
that port. Containers created before the setting keep their host names until they are created
anew. The published ports of the workspaces are bound to `127.0.0.1`, where the control
application and a browser on the host reach them.

### Control application in a container

Access to the Docker API is root access to the host. A control application running in a container
therefore never gets the raw socket of the system daemon. On a Docker server shared with other
applications, use a dedicated rootless daemon; [the Docker server article](doc:factory/docker-server)
explains why and how to set it up. The remaining options:

- **Socket proxy**: mount the socket into a proxy container (e.g. a docker socket proxy) that only
  lets through the API calls the provider uses - containers, exec, images (build), volumes,
  networks, ping - and point `tl_factory_docker_host` to `tcp://<proxy>:2375`. Proxy and control
  application share a network of their own that no workspace joins.
- **Remote daemon**: `tl_factory_docker_host=tcp://<daemon>:2376` with TLS; the client certificates
  are given by the property `tls-cert-path` of the provider.

In both cases:

- **Only named volumes.** The provider creates named volumes for the workspaces and the release
  repository and never bind-mounts a path. A path of the control application's container would mean
  nothing to the daemon anyway, which resolves bind mounts on its own host.
- **The workspace network.** The control application waits for a started application by requesting
  its preview URL. In a container, `localhost` is the container itself: connect the control
  application's container to the workspace network (`tl_factory_docker_network`) or give
  `tl_factory_public_host` a name that resolves to the daemon host from inside the container as
  well as for the browsers.
- **Public host.** `tl_factory_public_host` is the name browsers use to reach the published ports
  of the daemon host.
- **Reaching the control application.** A workspace container reaches the control application by
  the name of its container on the workspace network, e.g. `http://tl-factory-app:8091`,
  once the control application's container is connected to that network (see above). No extra
  host is needed then; `host-gateway` names the daemon host, which reaches the control application
  only through a port it publishes there. On the [isolated](#hardening-of-the-workspace-containers)
  workspace network, however, no container reaches another - the control application's container
  neither reaches the workspaces nor is reached by them. Either switch the isolation off
  (`tl_factory_docker_isolate=false`) and accept that workspaces reach each other, or keep the
  control application off the workspace network: it reaches the previews through
  `tl_factory_public_host` and the published ports (`tl_factory_host_ip` an address its container
  reaches), and the workspaces reach its proxy through a port it publishes on the gateway address
  of the default bridge, as `host.docker.internal:host-gateway`.
- **Ownership by labels.** The provider marks everything it creates with labels in its
  `label-namespace` and lists, cleans up and removes only objects carrying them - never by name. The
  control application's own container carries no such label and is never touched; two control
  applications on one daemon need different label namespaces.

### Windows (WSL2)

Run the control application **inside WSL2**, with a Docker daemon in the same WSL2 distribution
(Docker Engine installed there, or Docker Desktop with its WSL2 integration), and talk to it
through the Unix socket `unix:///var/run/docker.sock`. A control application running on Windows
itself would reach Docker Desktop only through the named pipe `npipe://`: the commands of a
workspace that read their standard input - the agent among them - need a connection the provider
opens itself (`HijackedConnection`), which knows Unix sockets and TCP, not named pipes. The
browsers on Windows reach the control application and the previews through `localhost`, which WSL2
forwards; keep the sources of the control application and its database inside the WSL2 file system
for speed. The resource limits of the workspaces apply inside the WSL2 VM, whose own size is set in
`.wslconfig`.

## Local setup

`tl-factory-app/devel` runs the services of a complete loop on one machine - a Gitea as [git
hosting](doc:factory/git-hosting) and a Jenkins as [CI server](doc:factory/ci) - for the control
application running on the host (see [control application on the
host](#control-application-on-the-host)):

| File | Content |
|---|---|
| `compose.yaml` | Gitea of the version `tl-factory-gitea` generates its client from (`gitea.version`), and the Jenkins of `tl-factory-jenkins` (target `devel` of its image context: the pinned release and plugins with Maven and the Maven settings of the workspace image), on the Docker network `tl-factory-devel`. Gitea is published at `127.0.0.1:3300`, Jenkins at `127.0.0.1:8380` (`TL_FACTORY_DEVEL_GITEA_PORT`, `TL_FACTORY_DEVEL_JENKINS_PORT`). |
| `tl-factory-devel.sh` | Starts, stops and resets the setup. |

```bash
tl-factory-app/devel/tl-factory-devel.sh          # start; set up on the first start
tl-factory-app/devel/tl-factory-devel.sh down     # stop, keep the data
tl-factory-app/devel/tl-factory-devel.sh reset    # remove containers, data and generated credentials
```

The first start creates the Gitea administrator `factory-admin`, the organization `tl-factory`, an
access token with the scopes of the control application (`write:organization`, `write:repository`,
`read:user`) and a read token for Jenkins, and the Jenkins administrator `tl-factory` with an API
token and the SCM credential `tl-factory-gitea` holding the read token. The built-in git connection
of the control application creates the organization of each tenant (`tl-factory-<tenant>`) as
`factory-admin` on its first repository. All credentials are generated and written to the secret
folder of the developer (`tl_devel_secrets_dir`, default `~/.top-logic/secrets`, mode 600): the
secrets `tl_factory_gitea_token` and `tl_factory_jenkins_token` of the control application, the key
`tl_secret_store_key` of its database secret store (only if the folder has none; it is never
replaced or removed, also not by `reset`, since the tokens the tenants entered are unreadable
without it), the passwords
and tokens of the setup in `tl_factory_devel_gitea.env` and `tl_factory_devel_jenkins.env` (the
environment of the Jenkins container). The script prints the URLs and logins. A start of an
existing setup keeps the credentials; one that no longer works, e.g. a revoked token, is
generated anew. A secret file of the control application holding a value not generated by the
setup is never overwritten: the script stops and asks to move it.

**URLs.** Gitea reports its clone URLs below its `ROOT_URL` `http://localhost:3300/`, which the
control application, its workspace proxy and the browsers reach. Jenkins clones from these URLs
as well: in its container, a system git configuration rewrites them to the Gitea service on the
network of the setup (`url.http://gitea:3000/.insteadOf http://localhost:3300/`). The workspaces
never talk to Gitea or Jenkins; they reach both through the [workspace
proxy](#the-workspace-proxy).

**The control application** runs on the host with this environment; the secrets are read from
the secret folder by the development secret store of `deploy/local`:

```bash
export tl_factory_gitea_url=http://localhost:3300/
export tl_factory_jenkins_url=http://localhost:8380/
export tl_factory_jenkins_scm_credentials=tl-factory-gitea
# The workspace proxy, reached by the workspaces through the gateway of Docker's default bridge.
export tl_factory_proxy_url=http://host.docker.internal:8091
export tl_factory_proxy_address=172.17.0.1
export tl_factory_docker_extra_hosts=host.docker.internal:host-gateway
export tl_factory_proxy_allowed_sources=workspace-network
```

`tl_factory_proxy_address` is the gateway of the default bridge (`docker network inspect bridge`).
The agent of a workspace needs an Anthropic API key in `tl_anthropic_api_key` in addition; creating
projects and workspaces, their repositories and CI builds work without. `TestProjectCI` runs the
creation of a new project, the set up of its CI and its first build against the setup, see [the tests](doc:factory/ci#the-tests).
