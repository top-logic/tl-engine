---
description: Read before the first database of an application is created, or before changing the database schema of an application - which tables and columns exist, whether the knowledge base supports branches, which database is used, and which of these decisions can be changed later on an existing database.
order: 50
---

# Persistency configuration

An application stores its objects through the knowledge base in a relational database. Which tables the database has, which columns they have, and how a model attribute is mapped onto them is configured: table declarations (`*Meta.xml`) grouped into a type system, the `<table>` annotation and the storage of the attributes in `*.model.xml`, the switch between a single-branch and a multi-branch schema, and the connection pool with its SQL dialect. The articles of this chapter describe these settings and their consequences.

Most of these decisions are made once, before the first start of the application creates its database. At that first start the engine creates all tables and stores the resolved schema in the database itself; every later start boots on that stored schema, not on the configuration files. A changed table declaration therefore does not change an existing database: new tables are added automatically, everything else - a new or changed column, an index, the move of an attribute between a column and the generic attribute table - needs an explicit migration, and some choices, such as support for branches, cannot be converted at all on an existing database. Settling them before the first database exists is cheap; changing them afterwards ranges from a migration file to a fresh database.
