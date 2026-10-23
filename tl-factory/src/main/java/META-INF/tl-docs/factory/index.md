---
description: Read before running, configuring or embedding the control application for agent-built TopLogic applications (tl-factory, tl-factory-app) - projects and their workspace containers (WorkspaceService, DockerWorkspaceProvider, ApplicationWorkspaces, ProjectWorkspaces, the tl.factory model and the factory* TL-Script functions), the tl_factory_* environment variables, and running the control application on the host or in a container (Docker socket proxy, remote daemon with TLS, named volumes only, the workspace network, public host, label-based ownership), and before running commands in a workspace (startExec, ExecProcess with stdin, terminate and timeouts), following the log of its application (followLog) or letting workspaces reach the control application (extra hosts, host-gateway), before giving workspaces access to the Anthropic API (LLMProxyService, LLMProxyServlet at /llm-proxy, ANTHROPIC_BASE_URL, tokens per agent run in ANTHROPIC_AUTH_TOKEN with issue/revoke and max-token-lifetime, allowed-sources with workspace-network, tl_factory_llm_proxy_url, tl_factory_llm_allowed_sources), before changing the workspace image (the non-root user tl, Claude Code, the tl-claude-tools plugin, the Playwright MCP server with headless Chromium, pinned versions, volumes owned by root), and before updating its tool versions or moving workspaces to a new image (the Renovate customManagers of the Dockerfile, the integration test before a merge, "Update workspace", WorkspaceProvider.recreate, the image attribute, factoryUpdate, factoryImageOutdated).
---

# Control application (tl-factory)

The control application manages TopLogic applications that are built by agents. Every managed
application is a **project**; its application is generated, built and run in a **workspace**: a
container of its own with named volumes for the sources, the Maven repository and the database.
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
| `ApplicationWorkspaces` | Generates an empty application from `tl-archetype-app` in a workspace, builds it and runs it as the main process of the container on port 8080. The engine artifacts of a version are resolved once into a shared, read-only release repository. |
| `ProjectWorkspaces` | Service running the operations on the workspaces of the stored projects in the background and storing their state and output; reconciles the stored states with the existing containers after a restart. |
| `LLMProxyService` / `LLMProxyServlet` | Access of the workspaces to the Anthropic Messages API through the control application, see [LLM access](#llm-access). |
| `FactoryFunctions` | The TL-Script functions the views call: `factoryCreateWorkspace`, `factoryStart`, `factoryStop`, `factoryCleanup`, `factoryUpdate`, `factoryImageOutdated`, `factoryEngineVersion`, `factoryCheckGroupId`, `factoryCheckArtifactId`. |

The model module `tl.factory` has the classes `Project` (name, group ID, artifact ID, engine
version, the workspace as a composition) and `Workspace` (workspace ID, status, host port,
preview URL, initial root password, the image its container was created from, the output of the
last operation as `log`) and the
enumeration `WorkspaceStatus`: *Created*, *Building*, *Starting*, *Running*, *Stopping*,
*Stopped*, *Cleaning up*, *Updating*, *Error*, *Unknown*.

A new workspace gets a random initial password, which the container passes to the application in
the environment variable `tl_initial_password` (`ApplicationWorkspaces.ENV_INITIAL_PASSWORD`); the
application sets it for its account `root` when it creates its database. The detail view shows it
under the preview link. It is the password of a development workspace and is no longer valid once
it is changed in the application.

The output of the application itself - the main process of the container - goes to the log of the
container in the container platform (`docker logs <container>`); the control application only
shows the output of its own operations.

## The workspace image

`ApplicationWorkspaces.imageContext()` holds the files the image is built from (the `Dockerfile`,
the entry script and the Maven settings in the package `com.top_logic.factory.workspace.app.image`);
the tag of the image is the hash of these files, so a change of a pinned version in the
`Dockerfile` leads to a new image. On top of `eclipse-temurin:21-jdk` it contains:

- Maven, git, and Node with npm from the Node.js distribution.
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

The entry script and every command run as the user `tl` (`ApplicationWorkspaces.WORKSPACE_USER`,
uid 1000), not as root - Claude Code refuses to bypass its permission prompts as root. A command
runs as root only if its `ExecCommand` says so (`setUser(ApplicationWorkspaces.ROOT_USER)`); the
Docker provider signals commands as root. The image creates the mount points of all volumes
(`ApplicationWorkspaces.VOLUME_PATHS`) owned by `tl`, so that a fresh named volume mounted there
belongs to `tl` as well.

Workspaces and release repositories of images that ran as root:

- A **workspace** keeps the image it was created with and runs as root further on; the provider
  never deletes an image. Its volumes belong to root, so [updating it](#updating-a-workspace) does
  not help - the user `tl` cannot write its sources; clean it up and create the workspace anew to
  get the agent tools.
- A **release repository** filled by root stays usable: the workspaces mount it read-only. Before
  a release repository is filled, the resolver takes it over for `tl` (`chown -R` as root).

### Pinned versions

Every tool version of the image is pinned in the `Dockerfile` - `MAVEN_VERSION`, `NODE_VERSION`,
`CLAUDE_CODE_VERSION`, `PLAYWRIGHT_MCP_VERSION` and the commit `TL_CLAUDE_TOOLS_COMMIT` of the
plugin; the base image `eclipse-temurin:21-jdk` is the one exception and follows its tag. Nothing is
installed as "latest":

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
read these comments; its `packageRules` group the updates into one pull request *workspace image
tools* per week (a major update in a pull request of its own) - each update needs the integration
test below, so a batch is tested once - and restrict Node to LTS lines from 22 on, the version
Claude Code needs.

An update is taken over like this:

1. Renovate opens the pull request changing the `ARG`s.
2. Before merging, run the integration test locally - CI does not run it, it needs a Docker daemon
   and takes minutes:
   `mvn test -DskipTests=false -pl tl-factory -Dtest=test.com.top_logic.factory.workspace.app.TestApplicationWorkspaces -Dtl.factory.integration=true`.
   It builds the new image, creates an application, checks Claude Code in the pinned version, the
   plugin, the Playwright MCP server with its browser, and updates the workspace to another image.
   A new Claude Code version additionally needs a check of the stream-json parsing.
3. Merge. The control application builds the new image on the next creation or update of a
   workspace; existing workspaces show *Image outdated* and are moved with *Update workspace*.

### Updating a workspace

A workspace container keeps the image it was created from. The workspace records this image
(`image`, `FactoryModel.WORKSPACE_IMAGE`); a workspace whose image is not the current one -
`ApplicationWorkspaces.currentImage()`, the repository tagged with the content hash of the image
context - or that has no image recorded is outdated (`factoryImageOutdated`). The detail view then
shows *Image outdated*, and the command *Update workspace* (`factoryUpdate`,
`ProjectWorkspaces.update`) is enabled while no operation runs on the workspace.

The update is an operation like the others, in state *Updating* with its output in the log:

- It builds the current image if needed, makes sure the release repository is complete and creates
  the container anew (`WorkspaceProvider.recreate`): the provider makes the image available,
  removes the old container - stopping it if it runs - and creates the new one from the same spec
  on the existing named volumes. Sources, Maven repository and database stay, so does the initial
  password of the workspace. A workspace whose container is lost but whose volumes exist gets a
  container again this way.
- If the proxy is switched on, the new container gets its current URL in `ANTHROPIC_BASE_URL`.
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
application - from a number of last lines and/or a point in time on, and then what it writes while
it runs, until the returned `LogFollower` is closed or the main process ends.

In the Docker provider:

- The Docker API has no way to signal an exec instance. A command therefore runs under a short
  `sh` script that writes the process ID of the command as first line of its output (the provider
  removes the line) and replaces itself by the command. A process started by `docker exec` leads its
  own process group, so `terminate` runs `kill -TERM -<pid>` in the container (as root) for the
  whole group, and `kill -KILL -<pid>` if the group has not ended after `terminate-timeout`
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
workspace takes the state of its container. The comparison runs in the background, so the
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
| `tl_factory_engine_version` | `8.0.0-alpha11` | Engine version a new project proposes. |
| `tl_factory_image_repository` | `tl-factory-workspace` | Name of the workspace image; the tag is the content hash of its files. |
| `tl_factory_repository_url` | TopLogic Nexus | Maven repository the TopLogic releases are taken from. |
| `tl_factory_start_timeout` | `600` | Seconds a started application has to answer. |
| `tl_factory_build_timeout` | `1800` | Seconds a Maven run in a workspace (generate, build, fill a release repository) may take before it is terminated. |
| `tl_factory_docker_extra_hosts` | *(empty)* | Comma separated `<host>:<address>` entries added to the host names of new workspace containers, e.g. `host.docker.internal:host-gateway`. |
| `tl_factory_llm_proxy_url` | *(empty)* | URL of the LLM proxy as the workspace containers reach it, e.g. `http://host.docker.internal:8080/tl-factory-app/llm-proxy`; empty switches the proxy off, see [LLM access](#llm-access). |
| `tl_factory_llm_allowed_sources` | *(empty)* | Comma separated client addresses the LLM proxy accepts: CIDR ranges (IPv4 or IPv6) or `workspace-network`; empty accepts every address, see [LLM access](#llm-access). |
| `tl_factory_llm_provider` | `anthropic` | Name of the Anthropic provider of `LLMProviderService` whose API key, base URL, version and timeouts the proxy uses. |
| `tl_factory_log_flush_interval` | `1000` | Milliseconds between two commits of the output of a running operation. |
| `tl_factory_max_log_size` | `65536` | Characters of the output of an operation that are stored. |

Further properties of the Docker provider (`tls-cert-path`, `connect-timeout`, `label-namespace`,
`name-prefix`, `stop-timeout`, `terminate-timeout`) are set in the configuration of `WorkspaceService` of the
application.

## LLM access

Claude Code in a workspace talks to the Anthropic Messages API, but no API key ever enters a
workspace container: whatever runs in the container - the agent, the generated application, a
dependency of it - could read it. The container talks to the API through the control application
instead.

- **The URL.** If the proxy is switched on, every workspace container gets the URL of the proxy,
  `tl_factory_llm_proxy_url`, in the environment variable `ANTHROPIC_BASE_URL`. It is no secret
  and the only thing about the proxy the container gets.
- **The token.** The container gets no token. A token belongs to one agent run: the control
  application issues it when it starts the run (`LLMProxyService.issue(workspaceId, lifetime)`)
  and passes it only to the process of the run, in `ANTHROPIC_AUTH_TOKEN` of its command
  (`ExecCommand`); neither the application of the workspace nor `docker inspect` sees it. Claude
  Code sends it as `Authorization: Bearer <token>`. A token is `tlwst_` followed by a random
  secret of about 256 bits (`SecretTokens` of `tl-ai-core`). It is valid until the run ends and
  the token is revoked (`revoke`), until its lifetime ends - at most `max-token-lifetime` of
  `LLMProxyService`, four hours by default; a longer lifetime asked for is cut to it -, or until
  its workspace is removed (*Clean up* revokes all its tokens, `revokeAll`, and a token of a
  workspace that no longer exists is refused anyway). Tokens are kept in memory only, by their
  SHA-256: nothing is stored in the database, and a restart of the control application
  invalidates all tokens - together with the runs it started, which end with it.
- **The proxy.** `LLMProxyServlet` at `/llm-proxy/*` of the control application forwards `POST`
  of `/v1/messages` and `/v1/messages/count_tokens` (with their query string) to the base URL of
  the configured provider; every other path and method is answered with 404, a request from an
  address that is not accepted (see below) with 403, a missing, unknown, revoked or expired token
  with 401. It drops the credentials (`Authorization`, `x-api-key`), cookies, `Host`,
  `Accept-Encoding` and the hop-by-hop headers of the request, sets `x-api-key` to the API key of
  the provider and `anthropic-version` to its version, unless the client sent one; a client's
  `anthropic-beta` is kept. Status, headers and body of the answer are passed back, the body
  flushed as it arrives, so that a streamed answer (server-sent events) reaches the agent while it
  is produced. The API key appears neither in a log nor in an answer.
- **The key.** It is the `api-key` of the Anthropic provider named by `tl_factory_llm_provider` in
  the `LLMProviderService` of `tl-ai-core`. `tl-factory-app` configures a provider `anthropic`
  without models whose key is read from the environment variable `tl_anthropic_api_key`.

The workspace containers must reach the URL of the proxy, see *Reaching the control application*
in the operation modes below: e.g. `http://host.docker.internal:8080/tl-factory-app/llm-proxy`
with the extra host `host.docker.internal:host-gateway` when the control application runs on the
host, or `http://tl-factory-app:8080/tl-factory-app/llm-proxy` when its container is connected to
the workspace network. The proxy answers any valid token, whatever state its workspace is in; it
neither limits nor counts the requests.

### Accepted sources

`allowed-sources` of `LLMProxyService` (`tl_factory_llm_allowed_sources`) restricts the clients
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

In the last case an address list cannot tell workspaces from other clients. Leave it empty and
block the path `/llm-proxy/` for clients from outside at the reverse proxy in front of the
control application instead; the token remains the check.

## Operation modes

### Control application on the host

The simplest mode, used during development: the control application runs as a process on a host
with a Docker daemon and talks to it through the default socket `unix:///var/run/docker.sock`. The
user running it must be allowed to use the socket. Preview links point to
`http://localhost:<host port>/<artifact ID>/`; for access from other machines,
`tl_factory_public_host` names the host as the browsers see it.

A workspace container reaches the control application through the gateway of the daemon: with
`tl_factory_docker_extra_hosts=host.docker.internal:host-gateway`, new containers resolve
`host.docker.internal` to the address of the host on their network, and reach the control
application under `http://host.docker.internal:<port of the control application>/`. The control
application must listen on that address, not only on `127.0.0.1`, and a firewall of the host must
let the workspace network through. Containers created before the setting keep their host names
until they are created anew.

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
  the name of its container on the workspace network, e.g. `http://tl-factory-app:8080/`, once the
  control application's container is connected to that network (see above). No extra host is
  needed then; `host-gateway` names the daemon host, which reaches the control application only
  through a port it publishes there.
- **Ownership by labels.** The provider marks everything it creates with labels in its
  `label-namespace` and lists, cleans up and removes only objects carrying them - never by name. The
  control application's own container carries no such label and is never touched; two control
  applications on one daemon need different label namespaces.
