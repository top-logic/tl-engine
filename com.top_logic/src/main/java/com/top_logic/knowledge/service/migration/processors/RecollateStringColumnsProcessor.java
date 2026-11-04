/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.SQLModifyColumn;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that re-applies the column type of existing non-binary string
 * columns, so that they pick up the case-insensitive collation used for newly created
 * columns.
 *
 * <p>
 * Non-binary string columns created before this change do not have the case-insensitive
 * collation (e.g. <code>tl_ci</code> on {@code PostgreSQL}) that is applied to newly created
 * columns. Re-emitting the column type (without changing type, size, or binary-ness) makes the
 * database re-create the column with the current, collation-aware definition. Dialects that do not
 * apply a case-insensitive collation per column (e.g. Oracle and DB2, where case-insensitivity
 * depends on the database configuration) are skipped.
 * </p>
 *
 * <p>
 * Re-defining the column type drops the planner statistics of the column on some databases (e.g.
 * {@code PostgreSQL}), so that queries run on default estimates until the database analyzes the
 * table again. Therefore, after all columns are re-collated, the statistics of exactly the altered
 * tables are refreshed through {@link DBHelper#analyzeTable(Statement, String)}, if the dialect
 * {@link DBHelper#supportsAnalyzeTable() supports} this.
 * </p>
 *
 * <p>
 * The first failing statement is logged as error naming the affected table (and column) and stops
 * the processing, so that the migration fails.
 * </p>
 *
 * <p>
 * If {@link Config#getTable()} is not set, all tables are processed. If {@link Config#getColumn()}
 * is not set, all non-binary string columns of the selected table(s) are processed.
 * </p>
 */
public class RecollateStringColumnsProcessor
		extends AbstractConfiguredInstance<RecollateStringColumnsProcessor.Config<?>> implements MigrationProcessor {

	/**
	 * Configuration options for {@link RecollateStringColumnsProcessor}.
	 */
	@TagName("recollate-string-columns")
	public interface Config<I extends RecollateStringColumnsProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration name of {@link #getTable()}.
		 */
		String TABLE = "table";

		/**
		 * Configuration name of {@link #getColumn()}.
		 */
		String COLUMN = "column";

		/**
		 * Logical name of the table whose columns are re-collated.
		 *
		 * <p>
		 * The name expected is the logical name of the table as defined in the <em>TopLogic</em>
		 * meta schema. If not given, all tables are processed.
		 * </p>
		 *
		 * @see MOClass#getName()
		 */
		@Nullable
		@Name(TABLE)
		String getTable();

		/**
		 * Logical name of the column to re-collate.
		 *
		 * <p>
		 * The name expected is the logical name of the column as defined in the <em>TopLogic</em>
		 * meta schema. If not given, all non-binary string columns of the selected table(s) are
		 * processed.
		 * </p>
		 *
		 * @see MOAttribute#getName()
		 */
		@Nullable
		@Name(COLUMN)
		String getColumn();
	}

	/**
	 * Creates a {@link RecollateStringColumnsProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RecollateStringColumnsProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String tableName = config.getTable();
		String columnName = config.getColumn();

		DBHelper sqlDialect;
		try {
			sqlDialect = connection.getSQLDialect();
		} catch (SQLException ex) {
			log.error("Failed to access the SQL dialect: " + ex.getMessage(), ex);
			return;
		}
		if (!sqlDialect.supportsColumnCollation()) {
			log.info("Dialect '" + sqlDialect.getClass().getSimpleName()
				+ "' does not apply a case-insensitive collation per column; nothing to re-collate.");
			return;
		}

		MORepository repository = context.getPersistentRepository();

		Set<String> alteredTables = new LinkedHashSet<>();
		for (MetaObject type : repository.getMetaObjects()) {
			if (!(type instanceof MOStructure)) {
				continue;
			}
			MOStructure table = (MOStructure) type;
			if (table instanceof MOClass && ((MOClass) table).isAbstract()) {
				continue;
			}
			if (tableName != null && !table.getName().equals(tableName)) {
				continue;
			}

			if (!recollateTable(log, connection, sqlDialect, table, columnName, alteredTables)) {
				return;
			}
		}

		if (alteredTables.isEmpty()) {
			log.info("No non-binary string columns to re-collate.");
			return;
		}

		analyzeTables(log, connection, sqlDialect, alteredTables);
	}

	/**
	 * Re-collates the selected string columns of the given table.
	 *
	 * @param alteredTables
	 *        The database names of the tables with at least one re-collated column. The database
	 *        name of the given table is added, if one of its columns is re-collated.
	 * @return Whether all columns were re-collated successfully. On failure, the error is logged
	 *         and processing must stop.
	 */
	private boolean recollateTable(Log log, PooledConnection connection, DBHelper sqlDialect, MOStructure table,
			String columnName, Set<String> alteredTables) {
		String dbTableName = table.getDBMapping().getDBName();
		for (MOAttribute attr : selectAttributes(table, columnName)) {
			for (DBAttribute col : attr.getDbMapping()) {
				DBType type = col.getSQLType();
				boolean stringLike = type == DBType.STRING || type == DBType.CHAR || type == DBType.CLOB;
				if (col.isBinary() || !stringLike) {
					continue;
				}

				SQLModifyColumn modification = modifyColumnType(table(dbTableName), col.getDBName(), col.getSQLType());
				modification.setBinary(col.isBinary());
				modification.setSize(col.getSQLSize());
				modification.setPrecision(col.getSQLPrecision());

				try {
					query(modification).toSql(sqlDialect).executeUpdate(connection);
				} catch (SQLException ex) {
					log.error("Failed to re-collate column '" + col.getDBName() + "' of table '" + table.getName()
						+ "': " + ex.getMessage(), ex);
					return false;
				}

				log.info("Re-collated column '" + col.getDBName() + "' of table '" + table.getName() + "'.");
				alteredTables.add(dbTableName);
			}
		}
		return true;
	}

	/**
	 * Refreshes the planner statistics of the given tables, since re-defining a column type may
	 * drop the statistics of the column (e.g. on {@code PostgreSQL}).
	 *
	 * @see DBHelper#analyzeTable(Statement, String)
	 */
	private void analyzeTables(Log log, PooledConnection connection, DBHelper sqlDialect,
			Set<String> dbTableNames) {
		if (!sqlDialect.supportsAnalyzeTable()) {
			return;
		}

		try (Statement statement = connection.createStatement()) {
			for (String dbTableName : dbTableNames) {
				try {
					sqlDialect.analyzeTable(statement, dbTableName);
				} catch (SQLException ex) {
					log.error("Failed to analyze table '" + dbTableName + "': " + ex.getMessage(), ex);
					return;
				}
				log.info("Analyzed table '" + dbTableName + "'.");
			}
		} catch (SQLException ex) {
			log.error("Failed to analyze re-collated tables: " + ex.getMessage(), ex);
		}
	}

	private List<? extends MOAttribute> selectAttributes(MOStructure table, String columnName) {
		if (columnName == null) {
			return table.getAttributes();
		}
		MOAttribute attr = table.getAttributeOrNull(columnName);
		if (attr == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(attr);
	}

}
