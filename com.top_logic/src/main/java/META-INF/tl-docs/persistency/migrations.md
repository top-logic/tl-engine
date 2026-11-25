---
description: Read before writing a data migration (*.migration.xml) of a module, making a module a migration module (<module> in the modules of MigrationService, WEB-INF/kbase/migration/<module>/), moving persistent types (tables, model classes) from one module to another or handing them over to an optional module, removing a module with persistent types from an application (<if-tables-undeclared> around <delete-class>, <delete-table>, <delete-module>), or switching a table between versioned and unversioned. Also explains the migration lock (migration.lock), the progress record (migration.progress), how an interrupted migration is resumed, and when the startup-actions of a migration run.
order: 15
---

# Migrations and modules with persistent types

The data of an application belongs to the modules that declare its tables (`*Meta.xml`) and model types (`*.model.xml`). A change to these declarations that is not applied automatically (see [changing the schema of an existing database](doc:persistency/type-systems#changing-the-schema-of-an-existing-database)) reaches an existing database only through a migration of a *migration module*. This article describes migration modules, when their migrations run, and what it takes to move persistent types to another module or to remove a module with persistent types from an application.

## Migration modules

A migration module is a module that has a name in the `modules` of the `MigrationService` configuration and a folder `WEB-INF/kbase/migration/<module>/` with its migrations:

```xml
<config service-class="com.top_logic.knowledge.service.migration.MigrationService">
	<instance>
		<modules>
			<module name="my-module" />
		</modules>
	</instance>
</config>
```

The migrations of a module form a chain of versions. Each `*.migration.xml` names its `version` (`name` and `module`) and in its `dependencies` the latest version of each module it builds on, its own predecessor in the same module included; a new migration is always the successor of the latest version of its module. `CreateMigrationScriptTemplate` creates a file with these dependencies filled in. The database stores the reached version of every migration module in `TL_PROPERTIES` (`databaseVersion.<module>`).

## When a migration runs

A migration runs once, at the first start of an application version that contains it, and only if the database stores a version of its module. A module without stored version counts as new to the application: it is assumed to have no data yet, its migrations are skipped (logged as "Skipping migrations of module ..."), and its latest version is stored (`MigrationUtil.getNewVersions`). The database setup of a new database stores the latest version of every migration module as well.

Two consequences follow:

- **A module with persistent types is a migration module from its first release on.** Its first migration may change nothing; it records the version of the module, so that every later migration of its data reaches the databases created with it. A module that becomes a migration module only when its first data migration is needed finds no stored version on the existing databases, and that migration is skipped there. Such a module ships its first data migration in another migration module whose version every affected database stores, usually a module it depends on, see [First data migration of a late migration module](#first-data-migration-of-a-late-migration-module).
- **A migration only reaches databases of applications that contain its module.** A migration that has to change the data of an application without a certain module must be part of a module that this application still contains.

An application whose database already exists when it gets its own migration folder lists its module in `minimum-modules` of the `MigrationService`, see [the upgrade guide](doc:upgrade-7.11-to-8.0#83-own-migrations).

### First data migration of a late migration module

A module `my-feature` with the model module `my.feature`, which becomes a migration module only when its data must be changed, splits this change into two migrations:

- The first migration of `my-feature` has empty `<processors/>`. It only records the version of `my-feature`: an existing database stores it at the first start, and the later migrations of `my-feature` reach that database.
- The change itself is a migration of a migration module whose version every database with the data stores, for example `my-base`, a module `my-feature` depends on. It is the successor of the latest migration of `my-base`, created with `CreateMigrationScriptTemplate` in that module:

```xml
<migration config:interface="com.top_logic.knowledge.service.migration.MigrationConfig"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<version name="Ticket_12345_remove_my_feature_data"
		module="my-base"
	/>
	<dependencies>
		...
	</dependencies>
	<processors>
		<delete-module name="my.feature"
			type-tables="GenericObject"
		/>
	</processors>
</migration>
```

The migration of `my-base` also runs on databases of applications without `my-feature`. Its processors must therefore leave a database without the data unchanged: `<delete-module>` logs a module that does not exist and deletes nothing.

## Lock, progress and resuming

A start whose database needs changes (pending migrations, automatic schema changes, missing versions) takes the *migration lock*, the global property `migration.lock` in `TL_PROPERTIES` holding the JVM name (`pid@host`) and start time of the node. Nodes of a cluster that start at the same time wait for it in `MigrationService.startUp()`, before they acquire the cluster startup token, and find the database migrated afterwards. A start without pending changes neither takes nor waits for the lock.

A migration is executed as a sequence of steps: the pre-processors and processors one by one, the rebuild of the revision cross reference together with the automatic schema adaption, the replay (if the migration has one), storing the new versions with the schema baseline, the post-processors of all migrations in one knowledge base transaction, and finally each startup action. Each step is committed together with the *progress* of the migration, the global property `migration.progress`, which names the migrations being applied and the number of completed steps. The last step deletes it.

A start that finds a progress resumes the interrupted migration after its last completed step. A step whose transaction was interrupted is repeated only on a database with transactional DDL; on any other database the start fails, and the database must be restored from a backup. The post-processors and the startup actions write no DDL and are always repeated if their transaction was not committed. A lock left behind by a terminated run of the same node (same JVM name, other start time, e.g. after a container restart) is taken over. A lock of another node is waited for; after checking that this node no longer runs, the property `migration.lock` can be deleted manually, and the next start resumes the migration.

### When startup actions run

The `startup-actions` of a migration are its last steps. They run after all modules have been started and the startup transaction of the application has been committed, and before the `StartupHooks`, each in a knowledge base transaction of its own that also records its completion in the progress. An action therefore sees all objects created during the start, for example by an initial data import. If an action fails, the start fails; the preceding actions stay committed, and the next start executes only the pending actions. An action that was committed is never executed again.

The start that migrated holds the migration lock until its last startup action is committed. If the start fails after the migration, for example because a module does not start, the lock is released on shutdown and the progress is kept, so that the next start executes the pending startup actions.

## Removing a module with persistent types

When an application drops the dependency on a module with persistent types, its configuration no longer declares the module's tables and model types, but its database still holds them. Without a migration, the application boots, but keeps the data:

- `MigrationService` keeps each table that is stored but no longer declared and writes its declaration to `WEB-INF/autoconf/inapp-schema-<n>.meta.xml` together with an `inapp-schema-<n>.config.xml` that adds it to the type system again ("Recovering configurations for lost tables" in the log). The table and its rows stay in the database.
- `DynamicModelService` removes the model module that is no longer declared in its incremental model upgrade.

The engine does not remove the data of a module that an application no longer contains: deleting stored data is a decision of the application. The application removes it with a migration of its own, wrapped in `<if-tables-undeclared>`. This processor runs the processors in its `processors` only if the schema of the application declares none of the types in its `tables`; a type the stored schema does not know counts as undeclared. The migration thus deletes nothing as long as the module is part of the application, and stays harmless if the module is added again later:

```xml
<processors>
	<if-tables-undeclared>
		<tables>
			<table object_name="MyTable"/>
		</tables>
		<processors>
			<delete-class name="my.module:MyClass"
				type-table="MyTable"
			/>
			<delete-table>
				<table object_name="MyTable"/>
			</delete-table>
			<delete-module name="my.module"
				fail-on-existing-types="true"
			/>
		</processors>
	</if-tables-undeclared>
</processors>
```

Table, classes and model module are deleted in one migration, so that neither the recovery of the table nor the model upgrade reacts to the removed declarations:

- `<delete-class>` with `type-table` deletes the instances of the class in that table together with their generic attribute values (`FLEX_DATA`) and all links from or to them. It must run **before** the `<delete-table>` of the same table: it selects the instances through the table, and the generic attribute values and links are stored outside of it. Without `type-table`, or after the table has been dropped, they stay behind in the database. `<delete-class>` also deletes the values of the class's own references.
- `<delete-table>` removes the type from the stored schema and drops its table. The revision cross reference (`RevisionXref`) of the dropped table is cleared at the end of the migration phase.
- `<delete-module>` with `fail-on-existing-types="true"` deletes the model module and fails if it still contains a type, i.e. a type the migration does not delete.

Each class of the module gets its own `<delete-class>`; `<delete-module>` alternatively deletes the instances of all its classes through `type-tables`. The processors run in their order with the connection and log of the migration, as processors on the top level of a migration do.

## Moving persistent types to another module

Moving a table declaration or a model module to another module changes no data: the database knows tables and model modules by name, not by the module that declares them. What changes is which module's migrations reach the data afterwards: the receiving module must be a migration module (see above), and later migrations of the moved types belong to it.

### Handing persistent types over to an optional module

If the receiving module is optional, i.e. not every application that contained the handing module contains it, the applications without it lose the declarations with the move. Their databases hold the data of the moved types as if the module had been removed (see [removing a module with persistent types](#removing-a-module-with-persistent-types)), and only the handing module can clean them up: its migrations are the only ones that reach these databases. The handing module therefore ships the `<if-tables-undeclared>` migration along with the move. In an application with the optional module, the schema declares the moved types, and their data is kept.

`tl-ai-core` handed the table `CookiePool` and the model module `tl.ai.cookiepool` over to the optional module `tl-ai-browser` in this way; the migration is `Ticket_30357_delete_cookie_pool_without_browser.migration.xml` in `WEB-INF/kbase/migration/tl-ai-core/` of `tl-ai-core`, and `tl-ai-browser` is a migration module.

## Changing the versioning of a table

Switching a table between versioned and unversioned (`versioned` of its `<metaobject>`) needs no migration: both kinds of table have the same columns. The knowledge base reads the flag from the stored schema. At the first start with the changed configuration, the `MigrationService` writes the changed definition of the type to the stored schema, so the table is handled with the new flag from this start on (see [changing the schema of an existing database](doc:persistency/type-systems#changing-the-schema-of-an-existing-database)).

A table that was versioned keeps the rows of its former states after the switch. The knowledge base removes them through the regular cleanup of unversioned types (`HistoryCleanup`, interval `cleanup-unversioned-types-interval` of the knowledge base configuration).
