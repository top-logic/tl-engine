---
description: Read before declaring database tables for an application or changing them on an existing database - the type system (ApplicationTypes, <type-system name="Default">, <declaration resource="webinf://kbase/...Meta.xml">), table declarations (<metaobject>, <mo_attribute>, <reference>), the <table> annotation and column versus FLEX_DATA storage, the schema stored in TL_PROPERTIES, and which schema changes are automatic and which need a *.migration.xml.
order: 10
---

# Type systems: tables, declarations and schema changes

The knowledge base stores objects in tables that are declared in configuration, not derived from the model. This article describes where the tables are declared, how a model attribute ends up in a column or in the generic attribute table, which schema the knowledge base actually boots on, and what happens when a declaration changes on an existing database.

## The type system

The tables of the knowledge base form a *type system*: a named set of table declarations, low-level tables and programmatic type providers. Type systems are configured in the `ApplicationTypes` configuration (`com.top_logic.basic.db.schema.setup.config.ApplicationTypes`), keyed by name. The engine declares the type system `Default` in `top-logic.config.xml` of `com.top_logic` (trimmed):

```xml
<config config:interface="com.top_logic.basic.db.schema.setup.config.ApplicationTypes">
	<type-systems>
		<type-system name="Default" class="com.top_logic.knowledge.service.TypeSystemSetup">
			<declarations>
				<declaration resource="webinf://kbase/BasicMeta.xml"/>
				<declaration resource="webinf://kbase/PersonMeta.xml" />
				...
			</declarations>
			<schemas>
				<schema name="lock" resource="schema://lock.schema.xml"/>
				<schema name="cluster" resource="schema://cluster.schema.xml"/>
				<schema name="journal" resource="schema://journal.schema.xml"/>
			</schemas>
			<providers>
				<provider name="basic">
					<impl class="com.top_logic.knowledge.service.db2.BasicTypeProvider" />
				</provider>
				...
			</providers>
			<data>
				<definition resource="webinf://kbase/KBDataUnit.xml"/>
				<definition resource="webinf://kbase/KBData.xml"/>
			</data>
		</type-system>
	</type-systems>
</config>
```

| Property | Defined in | Meaning |
|---|---|---|
| `name` | `SchemaConfiguration` | Name of the type system, default `Default`. |
| `declarations` | `SchemaConfiguration` | `<declaration resource="…"/>` entries, each a table declaration file (`*Meta.xml`, see below). |
| `schemas` | `SchemaConfiguration` | `<schema name="…" resource="schema://…"/>` entries: low-level tables that are not object types (locks, cluster state, journal, `TL_PROPERTIES`). |
| `providers` | `SchemaConfiguration` | `TypeProvider`s that create types programmatically. |
| `multiple-branches` | `SchemaConfiguration` | Whether the tables support branches other than trunk; default `true`. See the article on branches of this chapter. |
| `data` | `TypeSystemConfiguration` | `<definition resource="…"/>` entries: initial objects imported once, when the database is created. |

The configuration is a typed-configuration list keyed by `name`, so every module adds to the same type system from its own configuration file instead of redefining it. `com.top_logic.element` adds its declarations in `ElementConf.config.xml`, `com.top_logic.basic.db.schema` adds the `TL_PROPERTIES` table in `basicDbSchemaConf.config.xml`:

```xml
<type-system name="Default">
	<declarations>
		<declaration resource="webinf://kbase/StoredInfoMeta.xml" />
		<declaration resource="webinf://kbase/ElementMeta.xml" />
		...
	</declarations>
</type-system>
```

The knowledge base names its type system with the `KnowledgeBaseConfiguration` property `type-system`, default `Default`. The engine configures one knowledge base `Default` (`KnowledgeBaseFactory` in `top-logic.config.xml`), so an application adds its tables to the type system `Default`.

When the knowledge base starts, `SchemaSetup.resolve()` reads all declaration files and merges them into one configuration whose `meta-objects` hold every table declaration; the `schemas` are resolved into `additional-tables`. This resolved configuration is what is stored in the database (see [The stored schema](#the-stored-schema)).

## How an application contributes tables

An application created from the archetype `tl-archetype-app` ships the pattern already:

- `WEB-INF/kbase/<app>Meta.xml` - the application's table declarations, initially an empty `<objectlist>` with a commented-out example (and a `README.md` describing the folder).
- `WEB-INF/conf/<app>.conf.config.xml` - adds the declaration file to the type system `Default` and registers the application as a migration module:

```xml
<services>
	<config service-class="com.top_logic.knowledge.service.migration.MigrationService">
		<instance>
			<modules>
				<module name="${projectName}" />
			</modules>
		</instance>
	</config>
</services>

<configs>
	<config config:interface="com.top_logic.basic.db.schema.setup.config.ApplicationTypes">
		<type-systems>
			<type-system name="Default">
				<declarations>
					<declaration resource="webinf://kbase/${projectName}Meta.xml" />
				</declarations>
			</type-system>
		</type-systems>
	</config>
</configs>
```

The configuration file is loaded because `WEB-INF/conf/metaConf.txt` lists `<app>.conf.xml`: for every entry of `metaConf.txt` the typed companion `<name>.config.xml` is loaded as well (`MultiProperties.typedConfigName`). Files in `WEB-INF/autoconf/` ending in `.config.xml` are loaded without being listed.

An application that keeps all its objects in the tables the engine provides (`GenericObject`, the association tables) needs no declaration of its own; the file is there for the tables it adds.

### The database connection

Which database the knowledge base connects to is configured separately, in the connection pool `default` of the `ConnectionPoolRegistry`. `com.top_logic.basic` ships `db-default.config.xml` (pool sizes) and one file per database: `db-h2`, `db-mysql`, `db-postgresql`, `db-oracle`, `db-mssql`, `db-db2`, `db-jndi` (each a `.xml` with aliases and a `.config.xml` with the data source). The application's `metaConf.txt` lists `db-default.xml`, the deployment's `metaConf.txt` adds the database - in the archetype `deploy/local/webapp/WEB-INF/conf/metaConf.txt` lists `db-h2.xml`. The connection parameters come from environment variables or system properties named in the aliases, for example `postgresql_host`, `postgresql_database`, `postgresql_user` in `db-postgresql.xml`. The password is a [named secret](doc:credentials/secret-stores) (`h2_passwd`, `postgresql_passwd`, ...) without default; an application keeps the password of its development database in its local deployment (`deploy/local/secrets`, see [development secrets](doc:credentials/secret-stores#development-secrets)). The pool's `sql-dialect` (set in the database file, or guessed from the JDBC metadata when not given) translates the platform-independent declarations into the SQL of that database, so the same table declarations run on every supported database.

## Table declarations (`*Meta.xml`)

A declaration file lists tables (`<metaobject>`) and link tables (`<association>`) with their columns. A real example, `DemoTypesMeta.xml` of the demo application:

```xml
<objectlist xmlns="http://www.top-logic.com/ns/meta/5.8">
<metaobjects>

<metaobject object_type="MOKnowledgeObject" object_name="DemoTypes" abstract="false" super_class="StructuredElement">
	<attributes>
		<mo_attribute att_name="stringLengthConstraint" att_type="String" mandatory="false" db_size="10"/>
		<reference att_name="inlineReference"
			target-type="DemoTypes"
			mandatory="false"
			history-type="current"
			deletion-policy="clear-reference"
		/>
		...
	</attributes>
</metaobject>

<association super_class="hasWrapperAttValueBaseAssociation" object_name="hasDemoTypesCAttValue" />

<metaobject object_type="MOKnowledgeObject" object_name="DemoUnversioned" abstract="false" super_class="GenericObject" versioned="false"/>

</metaobjects>
</objectlist>
```

A table that stores model objects derives from `TLObject` (directly or through `GenericObject`, `StructuredElement`, …); it inherits the system columns (`BRANCH`, `IDENTIFIER`, `REV_MAX`, `REV_MIN`, `REV_CREATE`, …) and the type reference `tType` that ties each row to its model class.

| Setting | Where | Meaning | Part of the DB layout |
|---|---|---|---|
| `object_name` | `<metaobject>` | Platform-independent table name; the SQL name is derived from it (`MyTable` becomes `MY_TABLE`). | yes |
| `db_name` | `<metaobject>`, `<mo_attribute>` | Explicit SQL name. | yes |
| `super_class` | `<metaobject>` | Table whose columns, primary key and indexes are inherited. | yes |
| `abstract` | `<metaobject>` | Template only, no table is created. | yes |
| `versioned` | `<metaobject>` | `false` keeps no history for the rows of this table; references of objects in it default to the `…Unversioned` link tables. | same columns, but existing rows and links are stored differently |
| `att_name`, `att_type` | `<mo_attribute>` | Column name and value type (`String`, `Integer`, `Long`, `Double`, `Boolean`, `Date`, `Clob`, `Blob`, …). | yes |
| `db_type`, `db_size`, `db_prec` | `<mo_attribute>` | SQL type, size (characters of a string) and precision. | yes |
| `mandatory` | `<mo_attribute>`, `<reference>` | `NOT NULL` column. | yes |
| `binary` | `<mo_attribute>` | Case-sensitive comparison of a string column. | yes |
| `target-type` | `<reference>` | Table (or abstract table) of the referenced objects. | yes |
| `monomorphic` | `<reference>` | `false` adds a `_TYPE` column so that the reference can point into several tables; default `true`. | yes |
| `history-type` | `<reference>` | `current`, `historic` or `mixed`; the latter two add a `_REV` column. | yes |
| `branch-global` | `<reference>` | Reference across branches; adds a branch column in a multi-branch type system. | yes |
| `<primary-key>`, `<index>` (`<mo_index>`) | `<metaobject>` | Primary key and additional indexes, each a list of `<index_part name="…"/>`. | yes |
| `immutable`, `initial`, `hidden` | `<mo_attribute>`, `<reference>` | Checked and used by the knowledge base only. | no |
| `deletion-policy`, `is-container` | `<reference>` | What happens to the reference when its target is deleted, and whether deleting the referrer deletes the target. | no |
| `<annotations>` | `<metaobject>` | Java binding, query builder, key attributes, index column strategy. | no |

Everything marked "yes" is a column, index or table in the database; changing it on an existing database is a schema change (see [Changing the schema of an existing database](#changing-the-schema-of-an-existing-database)). The in-app Developer Guide page *Scheme declaration* (chapter *Database*) describes the declaration format in more detail.

## Column or FLEX_DATA: how a model attribute is stored

A model class names the table its objects are stored in with the `<table>` annotation (`TableName`); a class without one uses the table of its primary generalization, and in the end `GenericObject`:

```xml
<interface name="DemoTypes.All">
	<annotations>
		<table name="DemoTypes"/>
	</annotations>
	...
</interface>
```

Where the value of an attribute is stored is decided by its storage implementation. Without an explicit `<storage-algorithm>` annotation `StorageImplementationFactory` chooses:

| Attribute | Default storage | Stored in |
|---|---|---|
| Property (primitive value) | `primitive-storage` (`PrimitiveStorage`), unless the datatype carries a storage of its own | The column of the same name (or `storage-attribute`) if the object's table declares it; otherwise the generic table `FLEX_DATA`. |
| Reference, multiple, ordered | `list-storage` | A link table chosen by composition, deletion policy, history type and whether the table is versioned: `hasWrapperAttValue`, `hasStructureChild`, `hasWrapperVeto`, `hasHistoricValue`, … and their `…Unversioned` variants. |
| Reference, multiple, unordered | `set-storage` | Same link tables. |
| Reference, single | `singleton-link-storage` | Same link tables. |
| Backward reference | `reverse-storage` | Nothing of its own; computed from the forward reference. |

`PrimitiveStorage` decides column versus `FLEX_DATA` when the attribute is initialized, by asking the knowledge base whether the table has an attribute of that name. `FLEX_DATA` holds one row per object and attribute (`BRANCH`, `TYPE`, `IDENTIFIER`, `ATTR`, the revision range, `DATA_TYPE` and one value column per kind: `LONG_DATA`, `DOUBLE_DATA`, `VARCHAR_DATA`, `CLOB_DATA`, `BLOB_DATA`). A column is faster to load, to sort and to query and can be indexed; `FLEX_DATA` needs no schema change. A reference is put into a column of the object's table with an explicit `foreign-key-storage`:

```xml
<reference name="inlineReference" type="DemoTypes.All" ...>
	<annotations>
		<storage-algorithm>
			<foreign-key-storage
				storage-attribute="inlineReference"
				storage-type="DemoTypes"
			/>
		</storage-algorithm>
	</annotations>
</reference>
```

A storage set on a datatype applies to every attribute of that type; the article on custom primitive types of the *Model types in applications* chapter shows one.

The consequence: a change in `*.model.xml` alone - a new class, a new attribute, a removed attribute - never changes the database schema. Its values go to the link tables or to `FLEX_DATA`. Giving an attribute a column of its own, or taking its column away, is a schema change of the table declaration plus a data move.

## The stored schema

At the first start, when the sequence table does not exist yet, `InitialTableSetup` runs the configured setup actions: it creates all tables of the type system, imports the initial `data`, stores the resolved schema in the database and records the current migration versions. The automatic setup can be suppressed with the system property or environment variable `tl_noAutomaticalDatabaseSetup`.

The resolved schema is stored as XML in the table `TL_PROPERTIES` (`DBProperties`) under the key `KnowledgeBase.Default.schemaSetup` (`KBSchemaUtil`). On every later start `DBKnowledgeBase` reads its tables from this stored schema, not from the `*Meta.xml` files. The `KnowledgeBaseConfiguration` property `override-persistent-type-definitions` makes it use the configuration files instead; it changes no table.

Before the knowledge base starts, `MigrationService` compares the configured schema with the stored one, type by type:

- A table that is configured but not stored is created automatically (`AddApplicationTypesProcessor`).
- A table that is stored but no longer configured is kept. Its declaration is written to `WEB-INF/autoconf/inapp-schema-<n>.meta.xml` together with an `inapp-schema-<n>.config.xml` that adds it to the type system `Default` again, so the declaration is not lost.
- A table that exists in both is not compared. A new or changed column, index, primary key or flag in its declaration is not applied to the database.

After an explicit migration has run, the configured schema replaces the stored one (`StoreTypeConfiguration`). An edited declaration without the matching migration processor therefore leaves the knowledge base believing in a column that the table does not have. Until then, a column added only to the declaration is unknown to the knowledge base, and the attribute's values go to `FLEX_DATA`.

The model is handled separately: `DynamicModelService` compares the model of the `*.model.xml` files with the model stored in the database and applies the difference at startup. Its `auto-upgrade` setting (`UpgradeStrategy`) is `true` (upgrade, the default), `false` (ignore the change) or `prevent` (refuse to start until a migration describes the change); the upgrade changes the model only, not the tables and not the stored values.

## Changing the schema of an existing database

An explicit migration is a `*.migration.xml` file in `WEB-INF/kbase/migration/<module>/`, where `<module>` is the name registered in the `modules` of the `MigrationService`. Each file has a `version`, the `dependencies` on the latest versions of the modules it builds on, and processors that run in the phases `pre-processors`, `processors`, `migration` (a replay of the history) and `post-processors`. It runs once, at the first start of an application version that contains it. The archetype ships a launch configuration *Create migration template* that runs `CreateMigrationScriptTemplate --name <name>`, which creates the file with the current dependencies filled in.

A migration runs only if the database stores a version of its module. The database setup stores the maximal version of every module with a migration folder, and every startup stores it for each module that has no stored version yet. A module without stored version is considered new to the application, e.g. a framework module just added as dependency, and its migrations are skipped (logged as "Skipping migrations of module ..."). This is wrong for an application whose database was created before it had its folder `WEB-INF/kbase/migration/<module>/`: its first migration must come with `minimum-modules="<module>"` in the configuration of the `MigrationService`, see [the upgrade guide](doc:upgrade-7.11-to-8.0#83-own-migrations).

Processors are written with their tag name, or as `<processor class="…"/>` for a processor without one. The demo application added a column to its table `DemoTypes` like this (dependencies trimmed); the same `<mo_attribute>` was added to `DemoTypesMeta.xml`:

```xml
<migration config:interface="com.top_logic.knowledge.service.migration.MigrationConfig"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<version name="Ticket_28074_add_demo_for_color_column"
		module="tl-demo"
	/>
	<dependencies>
		<dependency name="Ticket_27906_polymorphic_format_annotations"
			module="tl"
		/>
		...
	</dependencies>
	<processors>
		<add-mo-attribute table="DemoTypes">
			<mo_attribute att_name="color" att_type="String" mandatory="false" db_size="16">
				...
			</mo_attribute>
		</add-mo-attribute>
		...
	</processors>
</migration>
```

| Change | What it takes |
|---|---|
| New table in a `*Meta.xml` | Automatic. |
| New low-level table in `schemas` | Migration: `create-tables` with the `<schema>` entries. |
| Table removed from the declarations | Kept automatically and recovered into `WEB-INF/autoconf`. To drop it: migration with `<processor class="com.top_logic.knowledge.service.migration.processors.RemoveApplicationTypesProcessor">` listing it in `application-tables`. |
| New column | Declaration plus migration: `add-mo-attribute` (a primitive column) or `add-mo-reference` (a reference column). |
| Changed column size or type | Declaration plus migration: `alter-column` (`table`, `column`, the new `db_size`, …). |
| Removed column | Declaration plus migration: `drop-column`. |
| Move a model attribute from `FLEX_DATA` into a column | Declare the column, then `add-mo-attribute` and `make-column-attribute` (`table`, `attribute`, `types`). |
| Move a model attribute from a column into `FLEX_DATA` | `make-flex-attribute` (`table`, `column`, `types`), then `drop-column`, and remove the column from the declaration. |
| Move objects of a model class to another table (changed `<table>`) | Migration: `move-objects` (`source-table`, `dest-table`, `types`). |
| New, changed or removed model class or attribute (`*.model.xml`) | Automatic model upgrade, unless `auto-upgrade` is `prevent`; model migration processors (`create-property`, `update-property`, `change-part-type`, …) describe it explicitly. |
| Branch support (`multiple-branches`) | No conversion exists; it needs a fresh database. See the article on branches of this chapter. |

The framework's own migrations, and what an application must add for an upgrade, are listed in [the upgrade guide](doc:upgrade-7.11-to-8.0#83-own-migrations).
