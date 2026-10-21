---
description: Read when a knowledge-base query, a deletion or a commit is slow on a specific DBMS, or before choosing the database of an application - H2 IN lists and the BRANCH index column, the referrer queries of object deletion, DBHelper.getMaxSetSize chunking, OR queries on MySQL, the commit order for MySQL, the FLEX_DATA primary key on PostgreSQL, MS SQL Server tuple rewriting, commit-warn-time / refetch-log-time and the LoggingDataSourceFactory.
order: 30
---

# Database-specific behaviour of the generated SQL

The knowledge base does not hand-write SQL. It builds statements from its schema and passes them
through an SQL dialect, a subclass of `DBHelper` in `com.top_logic.basic.sql`. Some of the
resulting statements meet limits of a particular DBMS. This article lists the known ones, so that a
slow query can be recognized as a property of the generated SQL on that DBMS.

## Dialects

| DBMS | Dialect | Configuration file |
|---|---|---|
| H2 | `H2Helper` | `db-h2.config.xml` |
| MySQL / MariaDB | `MySQLHelper`, `MySQL55Helper` | `db-mysql.config.xml`, `db-mysql55.config.xml` |
| PostgreSQL | `PostgreSQLHelper` | `db-postgresql.config.xml` |
| Oracle | `Oracle10Helper`, `Oracle12Helper`, `Oracle12toLowerHelper` | `db-oracle.config.xml`, `db-oracle-10.config.xml` |
| MS SQL Server | `MSSQLHelper`, `MSSQLHelper90` | `db-mssql.config.xml` |
| DB2 | `DB2Helper` | `db-db2.config.xml` |
| MS Access | `MSAccessHelper` | – |

The files lie in `com.top_logic.basic/src/main/webapp/WEB-INF/conf/` and configure the `default`
pool of the `ConnectionPoolRegistry`; a deployment selects one through its `metaConf.txt`. A pool
without a `<sql-dialect>` (as in `db-postgresql.config.xml`) gets its dialect from the JDBC driver
name and database version (`DBHelper.inspectDialect`). With `adjust-from-connection` (default
`true`) a configured dialect adjusts its settings from the connection; `MySQLHelper` also picks its
variant from the server version and `sql_mode` there.

## H2: an IN list seeks an index only on its first column

**Symptom.** Deleting a few hundred objects in one transaction takes many seconds on H2, and the
time grows with the size of the tables that reference the deleted type, not with the number of
referrers. Trac #29876 measured 21.1 s / 7.5 s / 8.8 s for `KnowledgeBase.deleteAll` of 402 / 385 /
339 root objects in a composition-heavy model.

**Mechanism.** Before an object is deleted, `ReferenceDeletionHelper` looks for the objects that
reference it with the deletion policy `DELETE_REFERER`. The queries come from
`DBKnowledgeBase.anyRefereesQuery`, built by `Expressions.createMonomorphicQueriesForTypeAndAttribute`:
one query per referrer type and reference attribute, each searching all branches
(`BranchParam.all`). For several deleted objects the condition is
`KnowledgeReferenceStorageImpl.getReferenceInSetExpression`, an `inSet` of the reference against a
set parameter. The SQL translation (`ExpressionEvaluator.visitInSet`) compares the reference as a
row value of branch, revision and identifier (`ReferenceSymbol.newTuple`) against one row per
deleted object. The ticket recorded this statement for the link table `HAS_STRUCTURE_CHILD`:

```sql
SELECT ... FROM "HAS_STRUCTURE_CHILD" "t0"
WHERE ("t0"."REV_MAX" >= 9) AND ("t0"."REV_MIN" <= 9)
  AND ("t0"."BRANCH",9,"t0"."SOURCE_ID") IN ((1,9,69656), (1,9,69335), ...)
```

The reference index exists, but with the default `DefaultIndexColumnsStrategy` of a database with
branches every index starts with `BRANCH`:
`HAS_STRUCTURE_CHILD_SOURCE (BRANCH, SOURCE_ID, META_ATTRIBUTE_ID, REV_MAX)`.

**DBMS rule.** H2 (2.5.252, `org.h2.index.IndexCursor.canUseIndexFor`) uses a single-column IN
list for an index seek only if the column is the first column of the index:

> The first column of the index must match this column, or it must be a VIEW index (where the column
> is null).

A row-value IN list is used only if its columns are exactly the columns of the index, in order.
Here H2 seeks `BRANCH IN (1, 1, ...)`, which matches every row, and checks the IN set row by row:
`scanCount` equals the table size. Splitting the condition into `BRANCH = 1 AND SOURCE_ID IN (...)`
does not help either, the seek stays on `BRANCH = 1`. With an index `(SOURCE_ID, BRANCH)` the same
generated statement seeks on `SOURCE_ID IN (...)`. The cost multiplies: the deletion cascades level
by level, and each level runs these queries again for every referrer type.

By the same rule, the bulk load of flex attributes cannot seek on its IN list: `AbstractFlexDataManager`
selects `IDENTIFIER IN (...)` after equality conditions on `BRANCH` and `TYPE`, and `IDENTIFIER` is
not the first column of the `FLEX_DATA` primary key. This case has not been measured.

**What an application can do.** Take `BRANCH` out of the front of the indexes: an application that
does not use branches runs single-branch (`top-logic.singleBranch.config.xml`), and
`DefaultIndexColumnsStrategy` then leaves `BRANCH` out of all indexes; an application with branches
can configure `branch-after-custom-columns`. Both fix the index layout when the database is
created, see [branches](doc:persistency/branches). With the single-branch configuration, the
deletions measured above took 2.3 s / 0.9 s / 0.8 s instead of 21.1 s / 7.5 s / 8.8 s (Trac #29877).

## MySQL: OR over several reference columns

**Mechanism.** A referrer type with several reference attributes to the target type could be
searched with one query that ORs the reference conditions. `Expressions` still contains that
variant, unused, with the reason:

> Create one query for each type which checks all References for that type. This results in
> OR-SQL's which can not be handled by MySQL (5.1) with good performance (wrong indexes are used).

The live variant `createMonomorphicQueriesForTypeAndAttribute` issues one query per reference
attribute and concatenates their results (`ConcatenatedCompiledQuery`, which executes the queries
one after the other). A deletion therefore runs *types × reference attributes × chunks* statements,
each of which can use the index of its own column.

**What an application can do.** An own query whose `or` combines conditions on different indexed
columns meets the same weakness of the MySQL optimizer. Split it into one query per column and
concatenate the results.

## MySQL: large commits are sorted

`DefaultDBContext` sorts the new, changed and removed objects of a commit before it writes them
(`toPrimaryKeyOrder`, comparators in `PrimaryKeyOrder`): first by type name, then by the values of
the primary key of the type. The writes then go to the database grouped by type, each group in
primary-key order. The reason in the JavaDoc:

> Sorting is crucial for helping MySQL to perform well on large commits (> 30000 objects).

There is nothing to configure; the note explains why a commit spends time before its first
statement.

## PostgreSQL: the FLEX_DATA primary key

Attributes without a column of their own live in `FLEX_DATA` (see
[type systems](doc:persistency/type-systems)). Its primary key is `(BRANCH, TYPE, IDENTIFIER,
REV_MAX, ATTR)`, without `BRANCH` in a single-branch database. `AbstractFlexDataManager`
explains why `REV_MAX` is not moved forward, although that would cluster the rows of the current
revision:

> Even if it sounds appealing to move the REV_MAX column more to the start of the index to bring rows
> for the current revision more close together, this is not advisable, since optimizers of some DBs
> (including PostgreSQL) then do not consider this index for the most frequent queries for attribute
> load. [...] Here, the REV_MAX column only occurs in a range condition. [...] this may prevent the
> optimizer from using this index at all preferring a full-table-scan.

The attribute load is `branch = ? and type = ? and identifier = ? and rev_min <= ? and rev_max >= ?`.
A column that is only constrained by a range belongs behind the columns constrained by equality;
the same holds for an index an application defines for its own tables.

## IN lists are chunked per dialect

`DBHelper.getMaxSetSize()` is the largest set the engine puts into one `IN (...)`: 1000, and 999 in
`OracleHelper`. Bulk operations over a set of objects split the set into chunks of this size,
among them `DBKnowledgeBase.getAnyReferer`, `ReferenceDeletionHelper`, the refetch of changed
objects, `BulkIdLoad` and the flex attribute load of `AbstractFlexDataManager`. An operation over
*n* objects therefore runs *n* / 1000 statements per query.

A statement with a set parameter is not a prepared statement: `SQLQuery.toSql` compiles it with the
`DirectStatementBuilder`, which writes the values of each call into the SQL text. On MS SQL Server
(`MSSQLHelper`), `MSSQLTupleRewriter` additionally rewrites a row-value IN list into an `OR` of
equality comparisons, one per value.

A separate setting is the JDBC fetch size of the flex attribute bulk load,
`AbstractFlexDataManager$Config` `fetch-size`, 10000 in `top-logic.config.xml`.

## Other dialect notes

- **MS SQL Server** does not change the type of a column that is part of an index (error 5074).
  `MSSQLHelper.appendChangeColumnType` drops and re-creates such indexes around the change; a
  column in a primary key or unique constraint still cannot change its type, so a migration that
  changes it fails there.
- **PostgreSQL and Oracle** compare non-binary strings case-insensitively only with preparation
  (the collation `tl_ci`, `NLS_COMP`/`NLS_SORT`); an existing PostgreSQL database needs the
  `RecollateStringColumnsProcessor`, see
  [the upgrade runbook](doc:upgrade-7.11-to-8.0#82-postgresql-and-oracle-case-insensitive-collation-mandatory-on-those-databases).
- **MySQL** accepts at most 2^16 - 1 parameters per statement; `DBHelper` limits a batch to
  `max-number-batch-parameter` (default 60000).

## How to look

**Knowledge base log.** `KnowledgeBaseConfiguration` (in `top-logic.config.xml`) has two thresholds
in milliseconds:

| Property | Default | Logs |
|---|---|---|
| `commit-warn-time` | 5000 | a warning `Long running commit (revision ... lasting ...)` with a stack trace (`DefaultDBContext`), and an info if waiting for the commit number took longer |
| `refetch-log-time` | 500 | an info `Refetched N objects in ...` when loading the changes of other cluster nodes took longer (`DBKnowledgeBase`) |

**Statement log.** `LoggingDataSourceFactory` wraps the data source of a pool in a
`LoggingDataSourceProxy`, which writes the executed statements to the application log, aggregates
similar statements (`logAggregate`) with call count, times and row counts, and logs the stack trace
of the caller (`logStacktrace`). `db-logging.config.xml` sets it up for the `default` pool; the
logging only runs with `<enableOnStartup>true</enableOnStartup>` in the `<data-source>`, which that
file does not set. A statement is logged as a
warning instead of an info above `warnThresholdCallCount` calls or `warnThresholdMillis` (defaults
50 and 500) between two flushes; `ignorePattern` excludes callers by a stack-trace pattern.

**H2.** H2 itself shows how it runs a statement: `EXPLAIN ANALYZE <statement>` prints the chosen
index, the index condition and the `scanCount`; after `SET QUERY_STATISTICS TRUE` the table
`INFORMATION_SCHEMA.QUERY_STATISTICS` collects the statements the application executes with their
execution times. An index condition that names only `BRANCH` and a `scanCount` near the table size
is the pattern of the first section.
