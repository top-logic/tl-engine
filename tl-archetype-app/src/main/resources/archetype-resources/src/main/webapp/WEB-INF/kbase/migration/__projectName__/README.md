## This file is a Velocity template of the archetype: a line starting with "##" is a Velocity comment
## and does not reach the generated project. The Markdown subheadings therefore start with ${H2}.
#set( $H2 = '##' )
# Migrations of this module

Each `*.migration.xml` in this folder is one version of the migration module named like this folder.
The migrations form a chain: each one names in its `dependencies` the latest version of every module it
builds on, its own predecessor in this folder included. A migration runs once, at the first start of an
application version that contains it.

${H2} Creating a migration

Create a new migration with `com.top_logic.knowledge.service.migration.CreateMigrationScriptTemplate
--name <name>`, e.g. with the launch configuration *Create migration template* in `bin/launch`. It
writes the file with the current dependencies (and `dropped-dependencies` / `added-dependencies` where
needed). Do not copy dependency lists by hand.

${H2} Migrations must be idempotent

A migration must leave the database in the same state, whether it finds its result already there or
not. It meets a database that already has part or all of its result:

 * when the automatic model upgrade has already created the model elements it creates,
 * when an application lists the module in `minimum-modules` and its migrations run from the start,
 * when a step of an interrupted migration is repeated at the next start.

The model processors (`create-*`, `add-class-generalizations`, `add-annotations`,
`remove-annotations`, `delete-*`, `update-*`) skip what already exists or is already missing. Processors
that change data (e.g. `create-object`, `sql`) are not idempotent by themselves: guard them, e.g. with
`if-tables-undeclared`, or write the SQL so that a repetition changes nothing.

${H2} When migrations do not run

A database without a stored version of this module treats the module as new and skips its migrations.
An application whose database already holds data of the module, but no version of it (e.g. because the
module became a migration module later), lists the module in `minimum-modules` of the
`MigrationService`.

${H2} Further reading

The developer articles `persistency/migrations` and `persistency/type-systems` (`read_doc` of the
`tl-mcp` tools, or the *Documentation* window of a running application).
