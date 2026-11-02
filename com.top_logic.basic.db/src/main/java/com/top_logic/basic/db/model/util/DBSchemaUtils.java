/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.MappedIterable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBSchemaPart;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLStatement;
import com.top_logic.basic.db.sql.SQLTable;
import com.top_logic.basic.io.WrappedIOException;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.DatabaseContent;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLLoader;

/**
 * Utility methods for the {@link DBSchema} model hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBSchemaUtils {

	/**
	 * Prefix of the name of the temporary table created by
	 * {@link #rebuildTable(Log, PooledConnection, DBTable, DBTable)}.
	 */
	public static final String REBUILD_TABLE_PREFIX = "TL_REBUILD_";
	
	/**
	 * {@link Mapping} of {@link DBForeignKey}s to the {@link DBTable} of the target index .
	 */
	public static final Mapping<DBForeignKey, DBTable> FOREIGN_KEY_TABLE = new Mapping<>() {
		@Override
		public DBTable map(DBForeignKey input) {
			return input.getTargetTable();
		}
	};

	/**
	 * {@link Mapping} of {@link DBTable} to all its dependencies (tables that
	 * are referenced in {@link DBForeignKey}s.
	 */
	public static final Mapping<DBTable, Iterable<DBTable>> TABLE_DEPENDENCIES = new Mapping<>() {
		@Override
		public Iterable<DBTable> map(final DBTable input) {
			return new MappedIterable<>(FOREIGN_KEY_TABLE, input.getForeignKeys());
		}
	};

	/**
	 * Sort the given tables topologically according to their dependencies.
	 * 
	 * @param inputTables
	 *        The tables to sort.
	 * @return The tables in topological order. Data can be inserted in that
	 *         order without violating foreign key constraints.
	 * 
	 * @throws IllegalArgumentException
	 *         If there are cyclic foreign key constraints in the given tables.
	 */
	public static List<DBTable> sortTopologically(Collection<DBTable> inputTables) {
		return CollectionUtil.topsort(TABLE_DEPENDENCIES, inputTables, true);
	}

	/**
	 * Test, whether a table with the same name as the given one exists in the given database.
	 * 
	 * @param db
	 *        Connection of the database to test in.
	 * @param table
	 *        The {@link DBTable} testing existence of.
	 * @return Whether the given table exists.
	 * @throws SQLException
	 *         If access to the database fails.
	 */
	public static boolean exists(PooledConnection db, DBTable table) throws SQLException {
		return exists(db, table.getDBName());
	}

	/**
	 * Test, whether a table with the same name as the given one exists in the given database.
	 * 
	 * @param db
	 *        Connection of the database to test in.
	 * @param tableName
	 *        Database table name.
	 * @return Whether the given table exists.
	 * @throws SQLException
	 *         If access to the database fails.
	 */
	public static boolean exists(PooledConnection db, String tableName) throws SQLException {
		DBHelper sqlDialect = db.getSQLDialect();
		final String checkStatement = "SELECT * FROM " + sqlDialect.tableRef(tableName) + " WHERE 1=0";
		Savepoint savePoint = sqlDialect.setSavepoint(db);
		try (Statement checkStmt = db.createStatement()) {
			try {
				checkStmt.execute(checkStatement);
				return true;
			} catch (SQLException ex) {
				sqlDialect.rollback(db, savePoint);
				// table does not exist
				return false;
			}
		} finally {
			sqlDialect.releaseSavepoint(db, savePoint);
		}
	}

	/**
	 * Drop a table with the given name.
	 * 
	 * @param pool
	 *        The database to drop the table in.
	 * @param tableName
	 *        The name of the table to be dropped.
	 * @param truncate
	 *        Whether to only empty the tables instead of dropping them.
	 * @return Whether the table was dropped successfully, <code>false</code>, e.g. if the table to
	 *         drop did not exist.
	 */
	public static boolean resetTable(ConnectionPool pool, String tableName, boolean truncate) {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			return tryResetTable(connection, tableName, truncate);
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Drop a table with the given name.
	 * 
	 * @param connection
	 *        Connection to the database to drop the table in.
	 * @param tableName
	 *        The name of the table to be dropped.
	 * @param truncate
	 *        Whether to only empty the tables instead of dropping them.
	 * @return Whether the table was dropped successfully, <code>false</code>, e.g. if the table to
	 *         drop did not exist.
	 */
	public static boolean tryResetTable(PooledConnection connection, String tableName, boolean truncate) {
		try {
			resetTable(connection, tableName, truncate);
			return true;
		} catch (SQLException ex) {
			handleDropFailure(ex, "Cannot drop table '" + tableName + "'.");
			return false;
		}
	}

	/**
	 * Drop a table with the given name.
	 * 
	 * @param connection
	 *        Connection to the database to drop the table in.
	 * @param tableName
	 *        The name of the table to be dropped.
	 * @param truncate
	 *        Whether to only empty the tables instead of dropping them.
	 */
	public static void resetTable(PooledConnection connection, String tableName, boolean truncate) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		Statement statement = connection.createStatement();
		try {
			resetTable(sqlDialect, statement, tableName, truncate);
		} finally {
			statement.close();
		}

		// Note: MSSQL supports transactional schema modifications.
		connection.commit();
	}

	/**
	 * Drop all tables of the given schema from the given database.
	 * 
	 * @see DBSchemaUtils#resetTables(PooledConnection, DBSchema, boolean)
	 */
	public static boolean dropTables(ConnectionPool pool, DBSchema schema) {
		return resetTables(pool, schema, false);
	}

	/**
	 * Drop all tables of the given schema from the given database and recreates them. This can be
	 * uses instead of truncating table if it may be possible that the tables have currently a
	 * different layout.
	 * 
	 * @param pool
	 *        The database to drop the table in.
	 * @param schema
	 *        The schema whose tables should be dropped.
	 * 
	 * @see #recreateTables(PooledConnection, DBSchema)
	 */
	public static void recreateTables(ConnectionPool pool, DBSchema schema) throws SQLException {
		dropTables(pool, schema);
		createTables(pool, schema);
	}

	/**
	 * Drop all tables of the given schema from the given connection and recreates them. This can be
	 * uses instead of truncating table if it may be possible that the tables have currently a
	 * different layout.
	 * 
	 * @param connection
	 *        The connection to the database to drop the table in.
	 * @param schema
	 *        The schema whose tables should be dropped.
	 * 
	 * @see #recreateTables(ConnectionPool, DBSchema)
	 */
	public static void recreateTables(PooledConnection connection, DBSchema schema) throws SQLException {
		resetTables(connection, schema, false);
		create(connection, schema);
	}

	/**
	 * Drop/Truncate all tables of the given schema from the given database.
	 * 
	 * @param pool
	 *        The database to drop the table in.
	 * @param schema
	 *        The schema whose tables should be dropped.
	 * @param truncate
	 *        Whether to only empty the tables instead of dropping them.
	 * @return Whether all tables have been dropped successfully, <code>false</code>, e.g. if some
	 *         tables to drop did not exist.
	 */
	public static boolean resetTables(ConnectionPool pool, DBSchema schema, boolean truncate) {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			return resetTables(connection, schema, truncate);
		} catch (SQLException ex) {
			// Ignore, table does not exist.
			handleDropFailure(ex, "Cannot drop tables.");
			return false;
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Drop all tables of the given schema from the given database.
	 * 
	 * @param connection
	 *        {@link PooledConnection} to the database to drop the table in.
	 * @param schema
	 *        The schema whose tables should be dropped.
	 * @param truncate
	 *        Whether to only empty the tables instead of dropping them.
	 * @return Whether all tables have been dropped successfully, <code>false</code>, e.g. if some
	 *         tables to drop did not exist.
	 */
	public static boolean resetTables(PooledConnection connection, DBSchema schema, boolean truncate) throws SQLException {
		boolean success = true;
		DBHelper sqlDialect = connection.getSQLDialect();
		Statement statement = connection.createStatement();
		try {
			List<DBTable> tables = new ArrayList<>(schema.getTables());

			// Drop in reverse order to increase the chance to drop tables with foreign keys
			// referencing each other.
			Collections.reverse(tables);

			// First delete foreign keys to prevent failures due to deletion order (where a
			// referenced table is deleted before the referencing table).
			if (!truncate) {
				for (DBTable table : tables) {
					for (DBForeignKey foreignKey : table.getForeignKeys()) {
						if (!tryDropForeignKey(sqlDialect, statement, foreignKey)) {
							success = false;
						}
					}
				}
			}
			for (DBTable table : tables) {
				if (!tryResetTable(sqlDialect, statement, table.getDBName(), truncate)) {
					success = false;
				}
			}
		} finally {
			statement.close();
		}
		// Note: MSSQL supports transactional schema modifications.
		connection.commit();
		return success;
	}
	
	private static boolean tryDropForeignKey(DBHelper sqlDialect, Statement statement, DBForeignKey foreignKey) {
		String foreignKeyName = CreateStatementBuilder.qName(sqlDialect, foreignKey);

		try {
			dropForeignKey(sqlDialect, statement, foreignKey.getTable().getDBName(), foreignKeyName);
		} catch (SQLException ex) {
			// Table may not yet exist.
			handleDropFailure(ex, "Cannot drop foreign key '" + foreignKeyName + "' on table '"
				+ foreignKey.getTable().getDBName() + "'.");
			return false;
		}
		return true;
	}

	private static void dropForeignKey(DBHelper sqlDialect, Statement statement, String tableName, String foreignKeyName)
			throws SQLException {
		statement.execute(sqlDialect.dropForeignKey(tableName, foreignKeyName));
	}

	private static boolean tryResetTable(DBHelper sqlDialect, Statement statement, String tableName, boolean truncate)
			throws SQLException {
		Connection connection = statement.getConnection();

		Savepoint savePoint = sqlDialect.setSavepoint(connection);
		try {
			resetTable(sqlDialect, statement, tableName, truncate);
		} catch (SQLException ex) {
			// Table may not yet exist.
			sqlDialect.rollback(connection, savePoint);
			handleDropFailure(ex, "Cannot drop table '" + tableName + "'.");
			return false;
		} finally {
			sqlDialect.releaseSavepoint(connection, savePoint);
		}
		return true;
	}

	private static void handleDropFailure(SQLException ex, String message) {
		if (Logger.isDebugEnabled(DBSchemaUtils.class)) {
			// Full stack trace.
			Logger.info(message, ex, DBSchemaUtils.class);
		} else {
			// Only single line info.
			Logger.info(DebugHelper.fullMessage(message, ex), DBSchemaUtils.class);
		}
	}

	private static void resetTable(DBHelper sqlDialect, Statement statement, String tableName, boolean truncate)
			throws SQLException {
		if (truncate) {
			statement.execute(sqlDialect.getTruncateTableStatement(tableName));
		} else {
			statement.execute("DROP TABLE " + sqlDialect.tableRef(tableName));
		}
	}

	/**
	 * Check, whether one {@link DBSchema} is compatible to a reference
	 * {@link DBSchema}.
	 * 
	 * <p>
	 * A schema <code>A</code> is compatible to a schema <code>B</code>, if data
	 * stored in schema <code>A</code> could be transfered to schema
	 * <code>B</code> without loss.
	 * </p>
	 * 
	 * @param log
	 *        {@link Protocol} to which compatibility problems are reported.
	 * @param referenceSchema
	 *        The reference schema.
	 * @param extractedSchema
	 *        The schema to check for compatibility to the given reference
	 *        schema.
	 * @param strict
	 *        Whether strict compatibility should be used.
	 */
	public static void checkCompatible(Protocol log, DBSchema referenceSchema, DBSchema extractedSchema, boolean strict) {
		for (DBTable expectedTable : referenceSchema.getTables()) {
			DBTable extractedTable = extractedSchema.getTable(expectedTable.getName());
			if (extractedTable == null) {
				log.error("Table '" + expectedTable.getName() + "' missing.");
				continue;
			}
			
			checkCompatible(log, expectedTable, extractedTable, strict);
		}
	}

	/**
	 * Check, whether one {@link DBTable} is compatible to a reference
	 * {@link DBTable}.
	 * 
	 * <p>
	 * A table <code>A</code> is compatible to a table <code>B</code>, if data
	 * stored in table <code>A</code> could be transfered to table
	 * <code>B</code> without loss.
	 * </p>
	 * 
	 * @param log
	 *        {@link Protocol} to which compatibility problems are reported.
	 * @param referenceTable
	 *        The reference table.
	 * @param extractedTable
	 *        The table to check for compatibility to the given reference
	 *        schema.
	 * @param strict
	 *        Whether strict compatibility should be used.
	 */
	public static void checkCompatible(Protocol log, DBTable referenceTable, DBTable extractedTable, boolean strict) {
		for (DBColumn column : referenceTable.getColumns()) {
			DBColumn extractedColumn = extractedTable.getColumn(column.getName());
			if (extractedColumn == null) {
				log.error("Column '" + column.getName() + "' missing in table '" + referenceTable.getName() + "'.");
				continue;
			}
			
			checkCompatible(log, column, extractedColumn, strict);
		}
		
		for (DBColumn extractedColumn : extractedTable.getColumns()) {
			DBColumn column = referenceTable.getColumn(extractedColumn.getName());
			if (column == null) {
				// Column that is not expected.
				if (extractedColumn.isMandatory()) {
					log.error("Additional column '" + extractedColumn.getName() + "' in table '" + referenceTable.getName() + "' is mandatory.");
					continue;
				}
			}
		}
	}

	/**
	 * Check, whether one {@link DBColumn} is compatible to a reference
	 * {@link DBColumn}.
	 * 
	 * <p>
	 * A column <code>A</code> is compatible to a column <code>B</code>, if data
	 * stored in column <code>A</code> could be transfered to column
	 * <code>B</code> without loss.
	 * </p>
	 * 
	 * @param log
	 *        {@link Protocol} to which compatibility problems are reported.
	 * @param referenceColumn
	 *        The reference column.
	 * @param extractedColumn
	 *        The column to check for compatibility to the given reference
	 *        schema.
	 * @param strict
	 *        Whether strict compatibility should be used.
	 */
	public static void checkCompatible(Protocol log, DBColumn referenceColumn, DBColumn extractedColumn, boolean strict) {
		if (! checkCompatibleType(log, referenceColumn, extractedColumn, strict)) {
			return;
		}
		
		if (extractedColumn.isMandatory() && !referenceColumn.isMandatory()) {
			log.error(
				"Mandatory attribute of column '" + extractedColumn.getName() + "' in table '" + referenceColumn.getTable().getName() + "' does not match: " + 
				(extractedColumn.isMandatory() ? "mandatory" : "non-mandatory") + " not compatible with " + (referenceColumn.isMandatory() ? "mandatory" : "non-mandatory"));
		}
	
		DBType type = referenceColumn.getType();
		if (type.binaryParam) {
			if (extractedColumn.isBinary() && !referenceColumn.isBinary()) {
				log.error(
					"Binary attribute of column '" + extractedColumn.getName() + "' in table '" + referenceColumn.getTable().getName() + "' does not match: " + 
					(extractedColumn.isBinary() ? "binary" : "non-binary") + " not compatible with " + (referenceColumn.isBinary() ? "binary" : "non-binary"));
			}
		}
		
		if (type.sizeParam) {
			if (extractedColumn.getSize() < referenceColumn.getSize()) {
				log.error(
					"Size attribute of column '" + extractedColumn.getName() + "' in table '" + referenceColumn.getTable().getName() + "' does not match: " + 
					extractedColumn.getSize() + " is smaller than " + referenceColumn.getSize());
			}
		}
		
		if (type.precisionParam) {
			if (extractedColumn.getPrecision() < referenceColumn.getPrecision()) {
				log.error(
					"Precision attribute of column '" + extractedColumn.getName() + "' in table '" + referenceColumn.getTable().getName() + "' does not match: " + 
					extractedColumn.getPrecision() + " is smaller than " + referenceColumn.getPrecision());
			}
		}
	}

	private static boolean checkCompatibleType(Protocol log, DBColumn referenceColumn, DBColumn extractedColumn, boolean strict) {
		DBType type = referenceColumn.getType();
		DBType extractedType = extractedColumn.getType();
		switch (extractedType) {
		case BLOB: 
			if (type == DBType.BLOB) {
				return true;
			}
			break;
		case BOOLEAN: 
			if (type == DBType.BOOLEAN) {
				return true;
			}
			break;
		case BYTE: 
			// Boolean and byte cannot always be distinguished.
			if (type == DBType.BYTE || type == DBType.BOOLEAN) {
				return true;
			}
			break;
		case SHORT: 
			if (type == DBType.SHORT) {
				return true;
			}
			if (! strict && (type == DBType.BYTE || type == DBType.BOOLEAN)) {
				return true;
			}
			break;
		case INT: 
			if (type == DBType.INT) {
				return true;
			}
			if (! strict && (type == DBType.SHORT || type == DBType.BYTE || type == DBType.BOOLEAN)) {
				return true;
			}
			break;
		case LONG: 
			if (type == DBType.LONG) {
				return true;
			}
			if (! strict && (type == DBType.INT || type == DBType.SHORT || type == DBType.BYTE || type == DBType.BOOLEAN)) {
				return true;
			}
			break;
			case ID:
				return type == DBType.ID;
		case CHAR: 
			if (type == DBType.CHAR) {
				return true;
			}
			break;
		case STRING: 
			if (type == DBType.STRING || type == DBType.CLOB) {
				return true;
			}
			break;
		case CLOB: 
			if (type == DBType.CLOB) {
				return true;
			}
			break;
		case DATE: 
			if (type == DBType.DATE) {
				return true;
			}
			break;
		case TIME: 
			if (type == DBType.TIME) {
				return true;
			}
			break;
		case DATETIME: 
			if (type == DBType.DATETIME || type == DBType.DATE || type == DBType.TIME) {
				return true;
			}
			break;
		case DECIMAL: 
			if (type == DBType.DECIMAL) {
				return true;
			}
			break;
		case FLOAT: 
			if (type == DBType.FLOAT) {
				return true;
			}
			break;
		case DOUBLE: 
			if (type == DBType.DOUBLE) {
				return true;
			}
			break;
		}
		
		log.error("Type of column '" + extractedColumn.getName() + "' in table '" + referenceColumn.getTable().getName() + "' does not match: " + extractedType + " not compatible with " + type);
		return false;
	}

	/**
	 * Extract the schema of the given database.
	 * 
	 * @param pool
	 *        The database to extract the schema from.
	 * @return The extracted {@link DBSchema}.
	 * @throws SQLException
	 *         If extraction fails.
	 */
	public static DBSchema extractSchema(ConnectionPool pool) throws SQLException {
		DBSchema analyzedSchema = DBSchemaFactory.createDBSchema();
		
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			SchemaExtraction extraction = new SchemaExtraction(metaData, pool.getSQLDialect());
			extraction.addTables(analyzedSchema);
		} finally {
			pool.releaseReadConnection(connection);
		}
		
		return analyzedSchema;
	}

	/**
	 * Extract the schema of the given table from the given database.
	 * 
	 * @param pool
	 *        The database to extract the schema from.
	 * @param tableName
	 *        The name of the table to extract its schema.
	 * @return The extracted {@link DBSchema} that contains only the extracted
	 *         table.
	 * @throws SQLException
	 *         If extraction fails.
	 */
	public static DBSchema extractTable(ConnectionPool pool, String tableName) throws SQLException {
		DBSchema analyzedSchema = DBSchemaFactory.createDBSchema();
		extractTable(pool, analyzedSchema, tableName);
		return analyzedSchema;
	}

	/**
	 * Extract the schema of the given table from the given database.
	 * 
	 * @param pool
	 *        The database to extract the schema from.
	 * @param analyzedSchema
	 *        The {@link DBSchema} to add the extracted table to.
	 * @param tableName
	 *        The name of the table to extract its schema.
	 * @return The extracted table, nor <code>null</code> if no such table exists.
	 * @throws SQLException
	 *         If extraction fails.
	 */
	public static DBTable extractTable(ConnectionPool pool, DBSchema analyzedSchema, String tableName) throws SQLException {
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			SchemaExtraction extraction = new SchemaExtraction(metaData, pool.getSQLDialect());
			DBTable table = extraction.addTable(analyzedSchema, tableName);
			if (table.getColumns().isEmpty()) {
				return null;
			}
			return table;
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Creates all table of the given schema in the given database.
	 * 
	 * @param pool
	 *        The database to create tables in.
	 * @param schema
	 *        The schema whose tables should be created.
	 * @throws SQLException
	 *         If table creation fails.
	 */
	public static void createTables(ConnectionPool pool, DBSchema schema) throws SQLException {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			create(connection, schema);
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

	/**
	 * Creates the given {@link DBSchemaPart} in the given database.
	 * 
	 * @param connection
	 *        {@link PooledConnection} to the database to create tables in.
	 * @param part
	 *        The schema part to create.
	 * @throws SQLException
	 *         If table creation fails.
	 */
	public static void create(PooledConnection connection, DBSchemaPart part) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String sql = part.toSQL(sqlDialect);
		try {
			new SQLLoader(connection).executeSQL(sql);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Reading from string must not fail.", ex);
		}
		connection.commit();
	}

	/**
	 * Translates the given declaration into a DDL SQL statement.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect to use.
	 * @param part
	 *        The schema, table, or index definition.
	 * @return A DDL statement in the given SQL dialect.
	 */
	public static String toSQL(DBHelper sqlDialect, DBSchemaPart part) {
		StringBuilder sqlBuffer = new StringBuilder();
		toSQL(sqlBuffer, sqlDialect, part);
		return sqlBuffer.toString();
	}

	/**
	 * Creates create statements for all parts of the given {@link DBSchema}.
	 * 
	 * @param out
	 *        The buffer to which statements are written.
	 * @param sqlDialect
	 *        The SQL dialect in which create statements are produced.
	 * @param schema
	 *        The {@link DBSchema} for which create statements are produced.
	 * 
	 * @throws WrappedIOException
	 *         If writing to the given {@link Appendable} fails.
	 */
	public static void toSQL(Appendable out, DBHelper sqlDialect, DBSchemaPart schema) throws WrappedIOException {
		new CreateStatementBuilder(sqlDialect, out).createSQL(schema);
	}

	/**
	 * Creates the given {@link DBSchema} in the given database.
	 * 
	 * @param connection
	 *        {@link PooledConnection} to the database to create tables in.
	 * @param schema
	 *        The schema to create parts from.
	 * @param checkExistence
	 *        if <code>true</code> and a table already exists, the table is skipped.
	 * @throws SQLException
	 *         If table creation fails.
	 */
	public static void createTables(PooledConnection connection, DBSchema schema, boolean checkExistence)
			throws SQLException {
		for (DBTable table : schema.getTables()) {
			if (checkExistence && exists(connection, table)) {
				// table already exists
				continue;
			}

			create(connection, table);
		}

		// MSSQL supports transactional schema manipulation.
		connection.commit();
	}
	
	/**
	 * Adapts the primary key and the indexes of an existing table to a target definition.
	 *
	 * <p>
	 * Shortcut for {@link #alignTable(Log, PooledConnection, DBTable, DBTable, Collection, Map)}
	 * without column changes.
	 * </p>
	 *
	 * @param log
	 *        The {@link Log} to report the changes to.
	 * @param connection
	 *        The connection to the database containing the table.
	 * @param current
	 *        The definition the existing table was created from, part of a {@link DBSchema}.
	 * @param target
	 *        The target definition of the table with the same name, part of a {@link DBSchema}.
	 * @return Whether the table was changed.
	 * @throws SQLException
	 *         If the database access fails.
	 */
	public static boolean alignKeys(Log log, PooledConnection connection, DBTable current, DBTable target)
			throws SQLException {
		return alignTable(log, connection, current, target, Collections.emptyList(), Collections.emptyMap());
	}

	/**
	 * Adapts the primary key, the indexes and the given columns of an existing table to a target
	 * definition.
	 *
	 * <p>
	 * The primary key is replaced, if its columns differ from the primary key of the target
	 * definition. An index of the current definition is dropped and created in its target form, if
	 * the target definition has an index with the same name but other columns, column order,
	 * uniqueness or compression. An index of the current definition that only exists there is kept,
	 * unless it contains a dropped column. Indexes that exist only in the target definition are not
	 * created. An index to drop is looked up in the database by its name and, if not found, by its
	 * columns in the same order and its uniqueness, so that an index the database holds under
	 * another name is dropped as well. An index missing in the database is not dropped, but reported
	 * as warning.
	 * </p>
	 *
	 * <p>
	 * The given columns are dropped, after the primary key and the indexes containing them have been
	 * dropped. The given columns of the target definition are added and filled for the existing
	 * rows, before the primary key and the indexes are created. A mandatory column is added as
	 * nullable column and declared mandatory after it has been filled. Other differences in the
	 * columns of the definitions are not considered.
	 * </p>
	 *
	 * <p>
	 * If the database cannot change the primary key of the table in place (see
	 * {@link DBHelper#canChangePrimaryKey(boolean)}), the table is rebuilt, see
	 * {@link #rebuildTable(Log, PooledConnection, DBTable, DBTable, Map)}.
	 * </p>
	 *
	 * @param log
	 *        The {@link Log} to report the changes to.
	 * @param connection
	 *        The connection to the database containing the table.
	 * @param current
	 *        The definition the existing table was created from, part of a {@link DBSchema}.
	 * @param target
	 *        The target definition of the table with the same name, part of a {@link DBSchema}.
	 * @param dropColumns
	 *        Database names of the columns of the current definition to drop.
	 * @param addColumns
	 *        Database names of the columns of the target definition to add, mapped to the SQL
	 *        expression computing the value of an existing row. The expression may reference the
	 *        other columns of the row.
	 * @return Whether the table was changed.
	 * @throws SQLException
	 *         If the database access fails.
	 */
	public static boolean alignTable(Log log, PooledConnection connection, DBTable current, DBTable target,
			Collection<String> dropColumns, Map<String, String> addColumns) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		boolean primaryKeyChanged = !columnNames(current.getPrimaryKey()).equals(columnNames(target.getPrimaryKey()));
		List<DBIndex> changedIndexes = new ArrayList<>();
		List<DBIndex> droppedIndexes = new ArrayList<>();
		for (DBIndex currentIndex : current.getIndices()) {
			DBIndex targetIndex = target.getIndex(currentIndex.getName());
			if (targetIndex != null) {
				if (!sameIndex(currentIndex, targetIndex)) {
					changedIndexes.add(currentIndex);
				}
			} else if (containsAny(currentIndex, dropColumns)) {
				droppedIndexes.add(currentIndex);
			}
		}
		if (!primaryKeyChanged && changedIndexes.isEmpty() && droppedIndexes.isEmpty() && dropColumns.isEmpty()
			&& addColumns.isEmpty()) {
			return false;
		}

		if (primaryKeyChanged && !sqlDialect.canChangePrimaryKey(current.getPKeyStorage())) {
			rebuildTable(log, connection, current, target, addColumns);
			return true;
		}

		String tableName = target.getDBName();
		DBTable existing = extractTable(connection, tableName);
		for (DBIndex currentIndex : changedIndexes) {
			dropExistingIndex(log, connection, existing, currentIndex);
		}
		for (DBIndex currentIndex : droppedIndexes) {
			if (dropExistingIndex(log, connection, existing, currentIndex)) {
				log.info("Dropped index '" + currentIndex.getName() + "' of table '" + tableName + "'.");
			}
		}
		if (primaryKeyChanged) {
			DBPrimary existingKey = existing.getPrimaryKey();
			String constraintName = existingKey == null ? null : existingKey.getName();
			if (constraintName != null) {
				execute(connection, sqlDialect.dropPrimaryKey(tableName, constraintName));
			}
		}

		for (String column : dropColumns) {
			executeStatement(connection, SQLFactory.dropColumn(SQLFactory.table(tableName), column));
			log.info("Dropped column '" + column + "' of table '" + tableName + "'.");
		}
		for (Entry<String, String> entry : addColumns.entrySet()) {
			DBColumn column = target.getColumn(entry.getKey());
			if (column == null) {
				throw new IllegalArgumentException(
					"No column '" + entry.getKey() + "' in the target definition of table '" + tableName + "'.");
			}
			SQLTable table = SQLFactory.table(tableName);
			executeStatement(connection, SQLFactory.addColumn(table, column, null).setMandatory(false));
			int rows = executeUpdate(connection, "UPDATE " + sqlDialect.tableRef(tableName) + " SET "
				+ sqlDialect.columnRef(column.getDBName()) + " = " + entry.getValue());
			if (column.isMandatory()) {
				executeStatement(connection,
					SQLFactory.modifyColumnMandatory(table, column.getDBName(), column.getType(), true)
						.setBinary(column.isBinary())
						.setSize(column.getSize())
						.setPrecision(column.getPrecision()));
			}
			log.info("Added column '" + column.getDBName() + "' to table '" + tableName + "', filled " + rows
				+ " rows.");
		}

		if (primaryKeyChanged) {
			DBPrimary targetKey = target.getPrimaryKey();
			if (targetKey != null) {
				execute(connection, sqlDialect.addPrimaryKey(tableName, columnDBNames(targetKey)));
			}
			log.info("Changed primary key of table '" + tableName + "' from " + columnNames(current.getPrimaryKey())
				+ " to " + columnNames(targetKey) + ".");
		}
		for (DBIndex currentIndex : changedIndexes) {
			DBIndex targetIndex = target.getIndex(currentIndex.getName());
			executeSQL(connection, toSQL(sqlDialect, targetIndex));
			log.info("Changed index '" + targetIndex.getName() + "' of table '" + tableName + "' from "
				+ columnNames(currentIndex) + " to " + columnNames(targetIndex) + ".");
		}
		return true;
	}

	/**
	 * Rebuilds an existing table in a target definition, keeping its rows.
	 *
	 * <p>
	 * Shortcut for {@link #rebuildTable(Log, PooledConnection, DBTable, DBTable, Map)} without
	 * added columns.
	 * </p>
	 *
	 * @param log
	 *        The {@link Log} to report the changes to.
	 * @param connection
	 *        The connection to the database containing the table.
	 * @param current
	 *        The definition the existing table was created from, part of a {@link DBSchema}.
	 * @param target
	 *        The target definition of the table with the same name, part of a {@link DBSchema}.
	 * @throws SQLException
	 *         If the database access fails, or the name used for the temporary table is already
	 *         taken.
	 */
	public static void rebuildTable(Log log, PooledConnection connection, DBTable current, DBTable target)
			throws SQLException {
		rebuildTable(log, connection, current, target, Collections.emptyMap());
	}

	/**
	 * Rebuilds an existing table in a target definition, keeping its rows.
	 *
	 * <p>
	 * The rows are copied to a table created in the target definition, which replaces the existing
	 * table. The copy contains the columns of the target definition that exist in the current
	 * definition and the given added columns; columns of the current definition missing in the
	 * target definition are dropped. All indexes of the target definition are created, as well as
	 * the indexes of the current definition that the target definition does not have and whose
	 * columns all exist in the target definition. Other database objects bound to the existing table
	 * (e.g. grants, or indexes not declared in the current definition) are dropped with it.
	 * </p>
	 *
	 * @param log
	 *        The {@link Log} to report the changes to.
	 * @param connection
	 *        The connection to the database containing the table.
	 * @param current
	 *        The definition the existing table was created from, part of a {@link DBSchema}.
	 * @param target
	 *        The target definition of the table with the same name, part of a {@link DBSchema}.
	 * @param addColumns
	 *        Database names of the columns of the target definition missing in the current
	 *        definition, mapped to the SQL expression computing the value of an existing row.
	 * @throws SQLException
	 *         If the database access fails, or the name used for the temporary table is already
	 *         taken (e.g. by an aborted rebuild, whose rows must then be recovered manually).
	 */
	public static void rebuildTable(Log log, PooledConnection connection, DBTable current, DBTable target,
			Map<String, String> addColumns) throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String tableName = target.getDBName();
		String tmpName = REBUILD_TABLE_PREFIX + Integer.toHexString(tableName.hashCode()).toUpperCase();
		if (exists(connection, tmpName)) {
			throw new SQLException("Cannot rebuild table '" + tableName + "', temporary table '" + tmpName
				+ "' already exists.");
		}

		DBSchema tmpSchema = newDBSchema();
		DBTable tmpTable = TypedConfiguration.copy(target);
		tmpTable.setName(tmpName);
		tmpTable.setExplicitDBName(tmpName);
		tmpTable.getIndices().clear();
		tmpTable.getForeignKeys().clear();
		tmpSchema.getTables().add(tmpTable);
		executeSQL(connection, toSQL(sqlDialect, tmpTable));

		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for (DBColumn column : target.getColumns()) {
			String columnName = column.getDBName();
			String value;
			if (addColumns.containsKey(columnName)) {
				value = addColumns.get(columnName);
			} else if (current.getColumn(columnName) != null) {
				value = sqlDialect.columnRef(columnName);
			} else {
				continue;
			}
			if (columns.length() > 0) {
				columns.append(", ");
				values.append(", ");
			}
			columns.append(sqlDialect.columnRef(columnName));
			values.append(value);
		}
		int rows = executeUpdate(connection,
			"INSERT INTO " + sqlDialect.tableRef(tmpName) + " (" + columns + ") SELECT " + values + " FROM "
				+ sqlDialect.tableRef(tableName));
		execute(connection, "DROP TABLE " + sqlDialect.tableRef(tableName));
		execute(connection, sqlDialect.renameTable(tmpName, tableName));

		for (DBIndex index : target.getIndices()) {
			executeSQL(connection, toSQL(sqlDialect, index));
		}
		for (DBIndex index : current.getIndices()) {
			if (target.getIndex(index.getName()) == null && allColumnsIn(index, target)) {
				executeSQL(connection, toSQL(sqlDialect, index));
			}
		}
		log.info("Rebuilt table '" + tableName + "' with " + rows + " rows, changed primary key from "
			+ columnNames(current.getPrimaryKey()) + " to " + columnNames(target.getPrimaryKey()) + ".");
	}

	/**
	 * Drops the index of the given existing table that implements the given index of its table
	 * definition.
	 *
	 * <p>
	 * The index is looked up by the name the definition gives it (see
	 * {@link CreateStatementBuilder#qName(DBHelper, com.top_logic.basic.db.model.DBTablePart)}). If
	 * the table has no index with that name, an index with the same columns in the same order and
	 * the same uniqueness is dropped instead, unless its name is the one of another index of the
	 * definition. If the table has no matching index either, nothing is dropped and a warning is
	 * logged.
	 * </p>
	 *
	 * @param log
	 *        The {@link Log} to report a missing index to.
	 * @param connection
	 *        The connection to the database containing the table.
	 * @param existing
	 *        The table as extracted from the database, see {@link SchemaExtraction}.
	 * @param index
	 *        The index of the definition the existing table was created from.
	 * @return Whether an index was dropped.
	 * @throws SQLException
	 *         If the database access fails.
	 */
	private static boolean dropExistingIndex(Log log, PooledConnection connection, DBTable existing, DBIndex index)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		String tableName = index.getTable().getDBName();
		String expectedName = CreateStatementBuilder.qName(sqlDialect, index);
		DBIndex existingIndex = existingIndex(sqlDialect, existing, index);
		if (existingIndex == null) {
			log.info("Index '" + expectedName + "' with columns " + columnDBNames(index) + " does not exist in table '"
				+ tableName + "', not dropped.", Log.WARN);
			return false;
		}
		String existingName = existingIndex.getName();
		execute(connection, sqlDialect.dropIndex(existingName, tableName));
		if (!existingName.equalsIgnoreCase(expectedName)) {
			log.info("Dropped index '" + existingName + "' of table '" + tableName + "' in place of index '"
				+ expectedName + "' with the same columns " + columnDBNames(index) + ".");
		}
		return true;
	}

	/**
	 * The index of the given existing table that implements the given index of its table
	 * definition.
	 *
	 * @see #dropExistingIndex(Log, PooledConnection, DBTable, DBIndex)
	 */
	private static DBIndex existingIndex(DBHelper sqlDialect, DBTable existing, DBIndex index) {
		String expectedName = CreateStatementBuilder.qName(sqlDialect, index);
		for (DBIndex candidate : existing.getIndices()) {
			if (candidate.getName().equalsIgnoreCase(expectedName)) {
				return candidate;
			}
		}
		List<String> columns = columnDBNames(index);
		candidates:
		for (DBIndex candidate : existing.getIndices()) {
			if (candidate.isUnique() != index.isUnique() || !sameNamesIgnoreCase(columnDBNames(candidate), columns)) {
				continue;
			}
			for (DBIndex declared : index.getTable().getIndices()) {
				if (candidate.getName().equalsIgnoreCase(CreateStatementBuilder.qName(sqlDialect, declared))) {
					continue candidates;
				}
			}
			return candidate;
		}
		return null;
	}

	private static boolean sameNamesIgnoreCase(List<String> names1, List<String> names2) {
		if (names1.size() != names2.size()) {
			return false;
		}
		for (int n = 0, cnt = names1.size(); n < cnt; n++) {
			if (!names1.get(n).equalsIgnoreCase(names2.get(n))) {
				return false;
			}
		}
		return true;
	}

	private static boolean containsAny(DBIndex index, Collection<String> columns) {
		for (DBColumn column : index.getColumns()) {
			if (columns.contains(column.getDBName())) {
				return true;
			}
		}
		return false;
	}

	private static boolean allColumnsIn(DBIndex index, DBTable table) {
		for (DBColumn column : index.getColumns()) {
			if (table.getColumn(column.getName()) == null) {
				return false;
			}
		}
		return true;
	}

	private static void executeStatement(PooledConnection connection, SQLStatement statement) throws SQLException {
		SQLFactory.query(statement).toSql(connection.getSQLDialect()).executeUpdate(connection);
	}

	private static boolean sameIndex(DBIndex index1, DBIndex index2) {
		return columnNames(index1).equals(columnNames(index2))
			&& index1.isUnique() == index2.isUnique()
			&& index1.getCompress() == index2.getCompress();
	}

	private static List<String> columnNames(DBIndex index) {
		if (index == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		for (DBColumnRef ref : index.getColumnRefs()) {
			result.add(ref.getName());
		}
		return result;
	}

	private static List<String> columnDBNames(DBIndex index) {
		List<String> result = new ArrayList<>();
		for (DBColumn column : index.getColumns()) {
			result.add(column.getDBName());
		}
		return result;
	}

	private static DBTable extractTable(PooledConnection connection, String tableName) throws SQLException {
		SchemaExtraction extraction = new SchemaExtraction(connection.getMetaData(), connection.getSQLDialect());
		return extraction.addTable(DBSchemaFactory.createDBSchema(), tableName);
	}

	private static void execute(PooledConnection connection, String sql) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
		}
	}

	private static int executeUpdate(PooledConnection connection, String sql) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			return statement.executeUpdate(sql);
		}
	}

	private static void executeSQL(PooledConnection connection, String script) throws SQLException {
		try {
			new SQLLoader(connection).executeSQL(script);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Reading from string must not fail.", ex);
		}
	}

	/**
	 * Creates a new empty {@link DBSchema}.
	 */
	public static DBSchema newDBSchema() {
		return TypedConfiguration.newConfigItem(DBSchema.class);
	}

	/**
	 * Fetches the data for the tables in the given {@link DBSchema} from the given connection.
	 * 
	 * @param connection
	 *        The connection to access the database.
	 * @param schema
	 *        The schema defining the tables to fetch data from.
	 * @return Mapping from the table name to the table contents.
	 * @throws SQLException
	 *         When connection to database fails, or one of the described tables does not exist.
	 */
	public static Map<String, DatabaseContent> getData(PooledConnection connection, DBSchema schema)
			throws SQLException {
		Map<String, DatabaseContent> contents = new HashMap<>();
		try (Statement stmt = connection.createStatement()) {
			DBHelper sqlDialect = connection.getSQLDialect();
			for (DBTable table : schema.getTables()) {
				DatabaseContent tableContent = sqlDialect.fetchTableContent(stmt, table.getDBName());
				contents.put(tableContent.getTableName(), tableContent);
			}
		}
		return contents;
	}

}
