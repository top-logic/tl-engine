---
description: Read before building or running the container image of an application - the runtime image top-logic/tl-app-runtime (tl-app-runtime/ of the engine, built by its build.sh) that an application image extends with FROM plus its unpacked WAR at the root context, what the runtime image provides (Tomcat as user toplogic uid 10001, data volume /var/lib/toplogic as tl_storage_path, secrets from the files of /run/secrets through tl_config, RemoteIpValve, no shutdown port, JVM options in bin/setenv.sh, tl_node_name, tl_max_ram_percentage), and the files the archetype generates for it (src/main/docker/Dockerfile, src/main/docker/compose.yaml with PostgreSQL, bin/image.sh).
order: 75
---

# The container image

An application runs in a container on the runtime image of TopLogic, `top-logic/tl-app-runtime`,
tagged with the TopLogic version. The image of the application adds nothing but the application
itself, unpacked, at the root of the server:

```dockerfile
FROM top-logic/tl-app-runtime:8.0.0
COPY --chown=toplogic:toplogic target/my-app-1.0-app/ /usr/local/tomcat/webapps/ROOT/
```

The folder `target/<artifact>-<version>-app/` is the unpacked application that the build of an
application writes next to its WAR (packaging `tl-app` or parent `tl-parent-app`). Build it for
operation, with the driver and the configuration of its database and the deploy aspect of
production:

```
mvn clean package -Pwith-postgresql -DdeployAspects=prod
```

## The runtime image

The image is defined in the folder `tl-app-runtime` of the engine and built by its `build.sh`, which
tags it `top-logic/tl-app-runtime:<version of tl-bom>`. It provides:

| What | How |
|---|---|
| Tomcat and Java | The official Tomcat image (Tomcat 11, Java 21, Ubuntu 24.04), pinned by the digest of its tag. |
| No root | Tomcat runs as the user `toplogic` (uid 10001). It owns the webapp (the engine writes into it), `logs`, `work` and `temp`; the configuration of Tomcat belongs to root. |
| Data | The volume `/var/lib/toplogic` is `tl_storage_path`: the files of the engine, its logs (`logs/`) and the heap dump of an `OutOfMemoryError`. The database is outside. |
| Secrets | `conf/toplogic.config.xml`, loaded last as `tl_config`: every [named secret](doc:credentials/secret-stores) is the file of `/run/secrets` named like the secret, else the environment variable of that name. |
| Reverse proxy | `RemoteIpValve` takes the client address and the protocol (`X-Forwarded-For`, `X-Forwarded-Proto`) from a proxy in a private network, so that the application sees `https` behind a proxy terminating TLS. |
| Tomcat hardened | No shutdown port, no deployment while running, no server version or stack trace on an error page of Tomcat, no debugger port. |
| JVM | `bin/setenv.sh`: the heap is `tl_max_ram_percentage` percent (default 75) of the memory of the container; after an `OutOfMemoryError` the JVM writes a heap dump and stops, and the restart policy of the container starts it again. |
| Scheduler | The node name of the scheduler is `tl_node_name` (default `toplogic`), not the host name, which is new for every container: a task left running when the container stopped is resumed after the restart. |
| Operation mode | `tl_operation_mode=production`. |

`CATALINA_OPTS` given when the container starts are appended to the options of `setenv.sh`, e.g. a
Java agent for tracing. The application answers at `/`, the React UI at `/view/`; the health of the
instance is `GET /view/` answering 200 or 302.

### Configuration of the installation

The application ships no configuration of its secret storage for operation: the runtime image
decides that secrets are files of `/run/secrets`. A single secret stored elsewhere is an override of
the *Secret storage* service in the `autoconf` folder of the application (see
[secret stores](doc:credentials/secret-stores)). Other settings of the installation are environment
variables, e.g. the connection of the database `postgresql_host`, `postgresql_database`,
`postgresql_user`, `postgresql_schema` and `postgresql_ssl` of `db-postgresql.xml`, its password
being the secret `postgresql_passwd`.

The root account of a new database gets the password of the secret `tl_initial_password`.

A release drains the instance through the [ops endpoint](doc:web-security-and-ops#the-ops-endpoint)
before the container is stopped; give the container a stop period longer than the drain.

## The files of an application

The archetype generates for an application:

- `src/main/docker/Dockerfile`: the image of the application as above. `TL_VERSION` and `APP` are
  build arguments.
- `bin/image.sh [TAG]`: builds the application for operation with PostgreSQL and the image around it
  (tag default `<artifact>:latest`). A missing runtime image is built from the engine checkout named
  by `TL_ENGINE`, otherwise pulled.
- `src/main/docker/compose.yaml`: the application with its PostgreSQL on one machine, the secrets
  as files of `src/main/docker/secrets` (ignored by git).
