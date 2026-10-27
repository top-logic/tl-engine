---
description: Read before deploying the control application (tl-factory-app) on a shared Docker server, or when an administrator asks what access it needs - the Docker API calls DockerWorkspaceProvider makes, why the raw system socket is never given to it, what a socket proxy does and does not restrict, the recommended dedicated rootless Docker daemon with its setup, resource cap and checks, the resource limits per workspace with the delegation of the cgroup controllers (Delegate=cpu cpuset io memory pids), the published ports on one host address, recreating the workspace network for its isolation, a dedicated remote daemon with TLS as alternative, and how control application and workspaces reach each other in each setup, including the port of the workspace proxy, TLS and which accepted sources of the workspace proxy fit.
order: 10
---

# The control application on a Docker server

The control application starts and stops containers. On a Docker server that runs other
applications as well, this raises the question an administrator rightly asks first: what can the
control application do to the rest of the server? This article answers it, compares the possible
setups and describes the recommended one step by step. The general configuration is in
[the chapter overview](doc:factory#configuration).

## What the control application does with Docker

`DockerWorkspaceProvider` uses a small, fixed part of the Docker Engine API:

| API area | Calls | Purpose |
|---|---|---|
| System | ping | check that the daemon is reachable |
| Images | build, inspect, pull | build the workspace image from the files shipped in `tl-factory`, pull its base images |
| Containers | create, start, stop, inspect, list, remove, logs | one container per workspace, its state and the output of its application |
| Exec | create, start, inspect | run commands in a workspace container (generate and build the application, run the agent), with standard input |
| Volumes | create, inspect, list, remove | named volumes for sources, Maven repository and database of a workspace, one shared release repository per engine version |
| Networks | create, list, remove | the workspace network |

What it never does, as can be checked in `DockerWorkspaceProvider`:

- It creates no privileged container and never uses the host PID namespace. Every workspace
  container drops all capabilities, runs with `no-new-privileges`, and is limited in memory, CPUs
  and processes; no command runs as root in it (see
  [hardening](doc:factory#hardening-of-the-workspace-containers)).
- Workspaces join only the configured workspace network, which it creates as a bridge network
  whose containers cannot reach each other (`enable_icc=false`). Their ports are published on one
  address of the host, `127.0.0.1` unless configured otherwise.
- It never bind-mounts a host path. All storage is in named volumes.
- It builds images only from its own build context; no build gets host paths or extra
  entitlements.
- It lists, stops and removes only objects carrying the labels of its `label-namespace`. Containers,
  volumes and networks of other applications are never touched by name or by accident.
- No workspace container gets access to Docker. The agents working in the workspaces cannot start
  containers.

This describes the code. A security setup must not rely on it alone: an attacker who takes over
the control application can make any call the Docker API it reaches allows.

## Why the control application never gets the system socket

The socket of the system daemon (`/var/run/docker.sock`) is root access to the host. Whoever can
create a container through it can create a privileged one, or one that bind-mounts `/`, and is root
on the server. Mounting it into the control application's container would make every weakness of
the control application - and of everything it runs - a weakness of the whole server.

## Possible setups

| Setup | What a taken-over control application can do | Assessment |
|---|---|---|
| System socket mounted | everything, root on the host | never |
| System daemon behind a socket proxy | everything the allowed endpoints allow, on **all** containers of the server | only on a server dedicated to the factory |
| **Dedicated rootless daemon** | control the factory's own containers; no root on the host, no access to other applications | **recommended** |
| Dedicated daemon on its own VM, `tcp://` with TLS | control that VM's containers | strongest isolation, more operations work |

### Why a socket proxy is not enough on a shared server

A socket proxy (e.g. `tecnativa/docker-socket-proxy`) lets through requests by URL path and
method. It does not inspect what a request asks for:

- `POST /containers/create` is needed, so the proxy also passes a create request for a privileged
  container or one mounting `/`. That is root on the host again.
- Exec, stop and remove are needed and apply to **every** container on the daemon - including the
  other applications and their databases. Labels restrict what the control application touches by
  its own code, not what the API lets it do.

The proxy narrows the attack surface (no swarm, secrets, plugins, system settings), but on a shared
server it does not separate the factory from the other applications. It is fine on a server that
runs nothing but the factory.

On such a server, `tecnativa/docker-socket-proxy` needs the sections `CONTAINERS`, `EXEC`, `IMAGES`,
`BUILD`, `VOLUMES`, `NETWORKS` and `POST` switched on (`=1`); the ping is allowed by default. The
commands in a workspace pass the proxy unchanged: the start of an exec instance switches the
connection to the raw streams (`Upgrade: tcp`, status 101), and the end of the input of a command
is the client half-closing its side of the connection - the proxy (HAProxy in tunnel mode) passes
both on. `TestDockerWorkspaceProvider.testExecThroughSocketProxy` checks this with the pinned
version `v0.5.0` (`-Dtl.factory.integration=true`); `testExecOverTcp` and `testExecOverTls` check
plain TCP and TLS with client certificates through a forwarder of the test to the local socket.

### Why a dedicated rootless daemon is

A rootless Docker daemon runs as an ordinary user. Everything it starts runs in a user namespace of
that user: root inside a workspace container - and a privileged container, should one ever be
created - is that unprivileged user on the host.

- **No root on the host.** Through its daemon the control application has at most the rights of
  the user `tl-factory`.
- **Separation from the other applications.** The rootless daemon sees only its own containers,
  volumes and networks. The system daemon and its applications are out of reach.
- **One resource cap for the whole factory.** All workspaces run in the systemd user slice of
  `tl-factory`; one limit on that slice caps memory and CPU of all of them together, whatever the
  agents build. The limits of each workspace container come on top, so that one workspace cannot
  starve the others.
- **Full API, no proxy.** The control application may use the complete API of its own daemon.
  There is no endpoint list to maintain, and commands with standard input run over the direct
  socket.

## Setting up the dedicated rootless daemon

The commands assume Ubuntu with Docker CE from the Docker repository.

1. **User and daemon** (as root):

   ```bash
   apt install docker-ce-rootless-extras uidmap
   useradd --create-home --shell /bin/bash tl-factory
   loginctl enable-linger tl-factory          # the daemon runs without a login
   ```

   As `tl-factory` (e.g. `machinectl shell tl-factory@`):

   ```bash
   dockerd-rootless-setuptool.sh install
   systemctl --user enable --now docker
   ```

   The socket is `/run/user/<uid of tl-factory>/docker.sock`.

2. **Resource cap** (as root), for example:

   ```bash
   systemctl set-property user-<uid>.slice MemoryMax=32G CPUQuota=800%
   ```

   and delegate the cgroup controllers to the user, see [resource limits](#resource-limits).

3. **The control application.** It runs as a container of the system daemon, as other applications
   do, and gets the rootless socket mounted - that socket, not the system socket:

   ```bash
   docker run ... \
     -v /run/user/<uid>/docker.sock:/var/run/docker.sock \
     -e tl_factory_public_host=<name of the server> \
     <image of tl-factory-app>
   ```

   `tl_factory_docker_host` keeps its default `unix:///var/run/docker.sock`. The process in the
   container must be allowed to use the socket; it belongs to `tl-factory`.

4. **Ports.** The workspaces publish their applications on the server in the range
   `tl_factory_host_port_min`..`tl_factory_host_port_max` (default 20000-20999), on the address
   `tl_factory_host_ip`. Its default `127.0.0.1` keeps them on the server; set it explicitly, to
   the internal address a reverse proxy forwards to - which puts the previews behind the login it
   demands -, never to all addresses of a server reachable from the internet. A rootful daemon
   publishes ports by DNAT rules that come before the firewall of the host - UFW does not block
   them -, so this address is the restriction that holds.

5. **The workspace network.** The first workspace creates it with the isolation of the workspaces
   (`enable_icc=false`). A network created before, without that option, is not changed - the
   control application logs a warning; remove it while no container is attached (`docker network
   rm tl-factory`) and update the workspaces, which creates it anew.

### How control application and workspaces reach each other

Control application and workspaces run on two different daemons, so they share no Docker network.
They reach each other through ports published on the server:

- The control application reaches a started application under its preview URL
  `http://<tl_factory_public_host>:<host port>/...`. The server's name must resolve to the server
  from inside the control application's container as well.
- A workspace reaches the control application under the URL the control application is published
  at, with the server's name, e.g. `http://<name of the server>:<published port>/`.

The workspace proxy of the control application listens on a port of its own
([the port of the proxy](doc:factory#the-port-of-the-proxy), default `8091`). Publish it from the
control application's container on an internal address of the server only, e.g.
`-p 10.0.0.5:8091:8091`, never on all addresses, and give the workspaces
`tl_factory_proxy_url=http://10.0.0.5:8091`. The proxy sees a request of a workspace
coming from the server's gateway or from the userland proxy of the daemon that publishes the port,
not from the workspace network, and the same for all workspaces. Its `allowed-sources` therefore
stay empty in this setup (`tl_factory_proxy_allowed_sources`, see
[accepted sources](doc:factory#accepted-sources)), and the binding of a token to its first source
address only refuses addresses other than that gateway
([token checks](doc:factory#token-checks)): the address the port is published on, the token and
its rate limit are the checks. Wherever the proxy is reached over a network between hosts, it is
reached over TLS.

### Checks for the administrator

- `docker ps` (system daemon) does not list the workspace containers;
  `docker -H unix:///run/user/<uid>/docker.sock ps` lists only them.
- `ps -eo user,cmd | grep java` on the host shows the processes of the workspaces under
  `tl-factory` or one of its subordinate user IDs, never under root.
- `systemctl status user-<uid>.slice` shows the memory and CPU used by all workspaces together.
- `docker -H unix:///run/user/<uid>/docker.sock inspect --format '{{.HostConfig.Memory}}
  {{.HostConfig.NanoCpus}} {{.HostConfig.PidsLimit}} {{.HostConfig.CapDrop}}
  {{.HostConfig.SecurityOpt}}' <workspace container>` shows the limits, `[ALL]` and
  `[no-new-privileges]`.

## Resource limits

Every workspace container is limited in memory (`tl_factory_workspace_memory`, default `8g`), CPUs
(`tl_factory_workspace_cpus`, default `4`) and processes (`tl_factory_workspace_pids`, default
`4096`), see [hardening](doc:factory#hardening-of-the-workspace-containers). A rootful daemon
applies them as they are. A rootless daemon can set only the cgroup controllers systemd delegates
to its user, by default `memory` and `pids`: without the `cpu` controller it refuses to create a
container with a CPU limit, and IO limits need `io`. Delegate the controllers once, as root, in
`/etc/systemd/system/user@.service.d/delegate.conf`:

```ini
[Service]
Delegate=cpu cpuset io memory pids
```

then `systemctl daemon-reload`, and restart the daemon of `tl-factory` (as that user,
`systemctl --user restart docker`). `cat /sys/fs/cgroup/user.slice/user-<uid>.slice/user@<uid>.service/cgroup.controllers`
then lists `cpu`. Without the delegation, set `tl_factory_workspace_cpus=0`. The limits need
cgroup v2 (`docker info` reports `Cgroup Version: 2`).

## A dedicated daemon on its own VM

For the strongest isolation the workspaces run on a VM of their own. Its daemon is reached over
`tcp://<VM>:2376` with TLS client certificates (property `tls-cert-path` of the provider), and
`tl_factory_public_host` names the VM. Control application and workspaces reach each other as
described for the rootless daemon, through ports published on the VM and on the server of the
control application.
