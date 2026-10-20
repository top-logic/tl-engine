---
description: Read before the first database of an application is created, to decide whether its tables carry a branch column - what a Branch is, what multiple-branches costs in every primary key, index and query, the single-branch switch top-logic.singleBranch.config.xml (TrunkStorage), the DefaultIndexColumnsStrategy options, and why an existing database cannot be converted.
order: 20
---

# Single branch or multiple branches

Every object table of a TopLogic database starts with a `BRANCH` column by default. This article
explains what the column is for, what it costs, how an application leaves it out, and why that
decision has to be made before the first database of the application is created. How the type
system is configured in general is described in [Type systems](doc:persistency/type-systems).

## What a branch is

A `Branch` is an independent, versioned copy of the data of the knowledge base. Every branch has
its own current revision in which objects can be changed without affecting any other branch. The
branch every application works in is the trunk (`TLContext.TRUNK_ID`, the branch with id `1`).

A further branch is created from a base branch at a base revision:

```java
Branch branch = HistoryUtils.createBranch(baseBranch, baseRevision, branchedTypes);
```

(`HistoryManager.createBranch(Branch, Revision, Set<? extends MetaObject>)`). Only the types in
`branchedTypes` get data of their own in the new branch (`null` means all types); objects of all
other types are shared with the base branch. The tables `Branch` and `BranchSwitch` (see
`BranchSupport`) record this: for each branch and type, `BranchSwitch` names the *data branch*
whose rows hold the objects visible in that branch.

The German Developer Guide page *Database / Branches* describes what the mechanism is meant for:
independent data sets for several tenants in one application, maintained releases of a data set,
simulating changes without touching the main data set, or a personal workspace per user.

No module and no UI shipped with TopLogic creates a branch. Outside the tests, `createBranch` is
called only by `InitialKBDataSetup` (creating the trunk of a fresh database) and by
`DefaultEventWriter` (replaying a `BranchEvent` from a recorded history). An application that does
not call `createBranch` itself only ever has the trunk.

## What multiple branches cost

Whether the tables can hold more than one branch is the attribute `multiple-branches` of a type
system (`SchemaConfiguration.hasMultipleBranches()`, default `true`). `SchemaSetup` passes it to the
`DefaultMORepository` it creates, and every table definition follows it.

With `multiple-branches="true"`:

| Where | Effect |
|---|---|
| Every object table | The attribute `_branch` (column `BRANCH`, `BranchStorage`), declared on the abstract `Item` in `WEB-INF/kbase/BasicMeta.xml`. |
| Primary key | `(_branch, _identifier, _rev_max)` - the branch is its first column. |
| Indexes | `Item` carries the `DefaultIndexColumnsStrategy`, which puts `BRANCH` in front of the columns of every generated index and `REV_MAX` behind them. |
| Custom unique indexes | An index declared `custom="true"` and `unique="true"` must contain `_branch` itself (`AbstractIndexColumnsStrategy` rejects it otherwise). |
| References | A reference column gets an additional `<name>_BRC` column if the reference is `branch-global` or stores a revision (`AbstractMOReference.hasBranchColumn()`). |
| `FLEX_DATA` and its journal tables | A `BRANCH` column (`AbstractFlexDataManager`, `FlexJournalManager`). |

The tables `Branch`, `BranchSwitch` and `RevisionXref` exist in both modes.

The excerpt of `Item` in `BasicMeta.xml`:

```xml
<metaobject object_name="Item" abstract="true">
	<annotations>
		<annotation config:interface="com.top_logic.dob.schema.config.annotation.IndexColumnsStrategyAnnotation"
			strategy="com.top_logic.knowledge.service.db2.DefaultIndexColumnsStrategy"
		/>
		...
	</annotations>
	<attributes>
		<mo_attribute att_name="_branch" db_name="BRANCH" att_type="Long" mandatory="true" immutable="false" system="true">
			<storage class="com.top_logic.knowledge.service.db2.BranchStorage" />
		</mo_attribute>
		...
	</attributes>
	<primary-key>
		<index-parts>
			<index_part name="_branch"/>
			<index_part name="_identifier"/>
			<index_part name="_rev_max"/>
		</index-parts>
	</primary-key>
	...
</metaobject>
```

### Example: the index of an association table

The association `hasStructureChild` (table `HAS_STRUCTURE_CHILD`, declared in
`com.top_logic.element`'s `WEB-INF/kbase/ElementMeta.xml`) inherits this index declaration from
`hasWrapperAttValueBaseAssociation`:

```xml
<mo_index name="source" unique="false">
	<index-parts>
		<index_part name="source" part="name" />
		<index_part name="metaAttribute" />
	</index-parts>
</mo_index>
```

The `DefaultIndexColumnsStrategy` turns it into these database columns:

| Mode | Columns of the index |
|---|---|
| `multiple-branches="true"` | `BRANCH, SOURCE_ID, META_ATTRIBUTE_ID, REV_MAX` |
| `multiple-branches="false"` | `SOURCE_ID, META_ATTRIBUTE_ID, REV_MAX` |

### The effect on queries

Every branch-local query carries a condition `BRANCH = ?`. In an application that only has the
trunk, the column has a single value: it does not narrow the search, and a DBMS that judges an
index by its first column may decide not to use an index whose first column has so few distinct
values. On H2 the effect is severe: an `IN` list is matched only against the first index column,
so a query such as the search for objects referring to deleted objects (`ReferenceDeletionHelper`,
`DBKnowledgeBase.anyRefereesQuery`) scans the whole association table instead of using the index
on `SOURCE_ID`. In an application on H2, the same deletion took 21 s with and 2.3 s without the
branch column (Trac #29876). The article on database notes of this chapter lists this and further
DBMS-specific effects of the generated SQL.

With a single branch the knowledge base also reads less: `MonomorphicSearch` reads only the
identifier column of a result row and assigns the trunk, where a query spanning several branches
must read the branch of each row as well.

## Switching to a single branch

The engine ships the configuration for a single branch in tl-core:
`WEB-INF/conf/top-logic.singleBranch.config.xml`

```xml
<config config:interface="com.top_logic.basic.db.schema.setup.config.ApplicationTypes">
	<type-systems>
		<type-system name="Default"
			multiple-branches="false"
		>
			<declarations>
				<declaration resource="webinf://kbase/singleBranchMeta.xml"/>
			</declarations>
		</type-system>
	</type-systems>
</config>
```

and the declaration `WEB-INF/kbase/singleBranchMeta.xml` it adds to the `Default` type system. It
redefines `_branch` of `Item` as a computed attribute without a column (`TrunkStorage` always
answers `TLContext.TRUNK_ID`) and removes it from the primary key:

```xml
<metaobject object_name="Item">
	<attributes>
		<mo_attribute class="com.top_logic.dob.attr.ComputedMOAttribute"
			att_name="_branch"
		>
			<storage class="com.top_logic.knowledge.service.db2.TrunkStorage"/>
		</mo_attribute>
	</attributes>
	<primary-key>
		<index-parts>
			<index_part name="_branch"
				config:operation="remove"
			/>
		</index-parts>
	</primary-key>
</metaobject>
```

An application enables both by adding one line to its `WEB-INF/conf/metaConf.txt`:

```
top-logic.singleBranch.config.xml
```

A `metaConf.txt` entry ending in `.config.xml` is loaded as typed configuration only; there is no
untyped counterpart for this file. Nothing else is needed: for a fresh database, the index columns,
the reference branch columns, `FLEX_DATA` and the journal tables all follow the flag.

What changes for code running in the application: `createBranch` cannot branch any type.
Requesting a type explicitly fails with *"Type ... does not support branching."*
(`DBKnowledgeBase.createBranch`); requesting all types (`null`) finds no branchable type, so the
new branch shares all data with its base.

No application shipped with TopLogic uses this configuration. The engine's knowledge base tests run
it: `AbstractDBKnowledgeBaseTest.suite(...)` runs every test that does not declare `needsBranches`
a second time as a *"no branches"* variant on freshly created tables (`MultipleBranchSetup`,
`SchemaTestSetup.updateMultipleBranches`, which sets the flag and adds `singleBranchMeta.xml`
exactly like the configuration above).

## Keeping branches, changing the index layout

An application that needs branches can still move the `BRANCH` column within the generated
indexes. `DefaultIndexColumnsStrategy.Config` has three options, all `false` by default:

| Option | Effect |
|---|---|
| `branch-after-custom-columns` | Appends `BRANCH` behind the declared columns instead of putting it in front. |
| `exclude-branch-from-non-unique-indices` | Leaves `BRANCH` out of non-unique indexes. |
| `exclude-revision-from-non-unique-indices` | Leaves `REV_MAX` out of non-unique indexes. |

Their JavaDoc gives the reasoning: a unique index needs the branch to enforce uniqueness only within
a branch (otherwise creating a branch violates the constraint) and `REV_MAX` to enforce it only
within one time slice. For non-unique indexes, *"since most queries are branch-local, the branch
must even be added for non unique indices to search efficiently"*, and on the other hand *"for
branch-global searches, the branch column in front of the custom columns prevents the index from
being used. In systems with few branches, the DBMS may also decide not to use an index with a
column in front of the index that does not have many different values."* With a single branch
the strategy leaves `BRANCH` out of every index regardless of these options.

The strategy is set by `IndexColumnsStrategyAnnotation` on a metaobject and applies to that type
and all its subtypes that do not declare their own (`SchemaSetup.getIndexStrategy`). Its property
`strategy` is an `@InstanceFormat` property: the attribute form used on `Item`
(`strategy="...DefaultIndexColumnsStrategy"`) creates the strategy with its default configuration.
To set an option, the strategy is written as a nested element, the same form `BasicMeta.xml` uses
for other annotations such as `<implementation-factory class="..."/>`. Annotations of a metaobject
are identified by their `config:interface`, so a declaration of the application that redefines
`Item` - the way `singleBranchMeta.xml` does - replaces the strategy for all tables:

```xml
<metaobject object_name="Item">
	<annotations>
		<annotation config:interface="com.top_logic.dob.schema.config.annotation.IndexColumnsStrategyAnnotation">
			<strategy class="com.top_logic.knowledge.service.db2.DefaultIndexColumnsStrategy"
				branch-after-custom-columns="true"
			/>
		</annotation>
	</annotations>
</metaobject>
```

No configuration shipped with TopLogic sets one of these options. They change the index
definitions of every table and so are subject to the same rule as the branch mode: they take effect
only for a database created afterwards.

## An existing database cannot be converted

The branch mode is fixed when the database is created:

- The knowledge base boots on the schema stored in the database (table `TL_PROPERTIES`, read by
  `KBSchemaUtil` in `DBKnowledgeBase.loadSchemaSetup`), not on the configured one. Changing
  `multiple-branches` or the index strategy in the configuration of an application with an existing
  database has no effect on startup. The knowledge-base option `override-persistent-type-definitions`
  only makes the knowledge base read the configured schema instead of the stored one; it changes no
  table, so with a flipped flag the knowledge base would address columns that do not exist.
- The automatic schema update at startup (`MigrationService.executeAutomaticSchemaMigrations`)
  only creates tables for types that are new in the configuration. It compares neither existing
  types nor the branch flag.
- No migration processor converts the table layout: none adds or removes the `BRANCH` column or
  switches `_branch` to `TrunkStorage`. `MigrationContext.hasBranchSupport()` reports the flag of
  the *stored* schema, so that processors write to the tables as they exist.
- After an explicit migration, `StoreTypeConfiguration` stores the application's configured schema
  as the new stored schema. With a changed flag, the stored schema then no longer describes the
  existing tables.
- A replay migration (`ReplayConfig`, `DataMigration.dropAndRecreateDatabase`) dumps the history
  and recreates all tables from the configured schema. The engine provides no support and no test
  for changing the branch layout this way; it is not a supported conversion.

Decide on the branch mode and the index options before the first database of the application is
created. To change them later, set up a fresh database with the new configuration and transfer the
application data into it.
