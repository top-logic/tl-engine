# Developer-local configuration overlay

This folder is the template for a developer-local configuration overlay that supplies
secrets (database passwords, API tokens, service credentials, ...) without committing
them to version control and without leaking them into the process command line.

How it works
------------

The application is started with the system property `tl_config` pointing to the
git-ignored folder `local-config` in the project root:

* `.mvn/jvm.config` sets `-Dtl_config=local-config` for every Maven invocation
  from the project directory (e.g. starting the application with `mvn exec:java`).
* The Eclipse launch configuration `Start <app>.launch` sets the same property
  with a workspace-absolute path.

When the folder exists, the configuration fragments listed in its `metaConf.txt` are
loaded *last*, so their `<alias>` entries override the application configuration.
When the folder is absent (e.g. on a fresh checkout), it is silently ignored and the
application starts with the base configuration.

Activating the overlay
----------------------

1. Copy this template folder to `local-config` in the project root:

   ```
   cp -r local-config.template local-config
   ```

2. Enter your credentials as `<alias>` entries in `local-config/local.conf.xml`.

The `local-config` folder is listed in `.gitignore` — its contents never get committed.
