---
description: Read before running, configuring or embedding the control application for agent-built TopLogic applications (tl-factory, tl-factory-app) - projects and their workspace containers (WorkspaceService, DockerWorkspaceProvider, ApplicationWorkspaces, ProjectWorkspaces, the tl.factory model and the factory* TL-Script functions), the tl_factory_* environment variables, and running the control application on the host or in a container (Docker socket proxy, remote daemon with TLS, named volumes only, the workspace network, public host, label-based ownership), and before running commands in a workspace (startExec, ExecProcess with stdin, terminate and timeouts), following the log of its application (followLog) or letting workspaces reach the control application (extra hosts, host-gateway).
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
| `WorkspaceProvider` | Platform-neutral lifecycle of a workspace container: create, start, stop, remove, status, list, cleanup, commands in the container, the log of its main process, image build, shared volumes. |
| `DockerWorkspaceProvider` | The implementation on the Docker Engine API. |
| `WorkspaceService` | Service holding the configured provider and the `ApplicationWorkspaces`. |
| `ApplicationWorkspaces` | Generates an empty application from `tl-archetype-app` in a workspace, builds it and runs it as the main process of the container on port 8080. The engine artifacts of a version are resolved once into a shared, read-only release repository. |
| `ProjectWorkspaces` | Service running the operations on the workspaces of the stored projects in the background and storing their state and output; reconciles the stored states with the existing containers after a restart. |
| `FactoryFunctions` | The TL-Script functions the views call: `factoryCreateWorkspace`, `factoryStart`, `factoryStop`, `factoryCleanup`, `factoryEngineVersion`, `factoryCheckGroupId`, `factoryCheckArtifactId`. |

The model module `tl.factory` has the classes `Project` (name, group ID, artifact ID, engine
version, the workspace as a composition) and `Workspace` (workspace ID, status, host port,
preview URL, initial root password, the output of the last operation as `log`) and the
enumeration `WorkspaceStatus`: *Created*, *Building*, *Starting*, *Running*, *Stopping*,
*Stopped*, *Cleaning up*, *Error*, *Unknown*.

A new workspace gets a random initial password, which the container passes to the application in
the environment variable `tl_initial_password` (`ApplicationWorkspaces.ENV_INITIAL_PASSWORD`); the
application sets it for its account `root` when it creates its database. The detail view shows it
under the preview link. It is the password of a development workspace and is no longer valid once
it is changed in the application.

The output of the application itself - the main process of the container - goes to the log of the
container in the container platform (`docker logs <container>`); the control application only
shows the output of its own operations.

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
| `tl_factory_log_flush_interval` | `1000` | Milliseconds between two commits of the output of a running operation. |
| `tl_factory_max_log_size` | `65536` | Characters of the output of an operation that are stored. |

Further properties of the Docker provider (`tls-cert-path`, `connect-timeout`, `label-namespace`,
`name-prefix`, `stop-timeout`, `terminate-timeout`) are set in the configuration of `WorkspaceService` of the
application.

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
