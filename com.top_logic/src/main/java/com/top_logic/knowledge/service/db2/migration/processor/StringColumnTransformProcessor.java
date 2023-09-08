/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.processor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.db.model.DBColumnRef;
import com.top_logic.basic.db.model.DBPrimary;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * Abstract superclass for {@link MigrationProcessor} that modify a {@link String} valued column.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class StringColumnTransformProcessor<C extends StringColumnTransformProcessor.Config<?>>
		extends AbstractConfiguredInstance<C> implements MigrationProcessor {

	/**
	 * Configuration for {@link StringColumnTransformProcessor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends StringColumnTransformProcessor<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The table name declaring the {@link #getColumn()}.
		 * 
		 * @see #getRawTableNames()
		 */
		@Mandatory
		String getTable();

		/**
		 * The column containing XML values to transform.
		 * 
		 * @see #getRawTableNames()
		 */
		@Mandatory
		String getColumn();

		/**
		 * Whether to use the concrete names of the database in {@link #getTable()} and
		 * {@link #getColumn()}.
		 * 
		 * <p>
		 * By default, <i>TopLogic</i> type names are used in the configuration and automatically
		 * converted to all-upper-case with underscore as separator.
		 * </p>
		 * 
		 * <p>
		 * Note: If a table definition has set the {@link DBTableMetaObject#getDBName() DB name}
		 * property set, you must use raw table names for specifying the transformation target.
		 * </p>
		 * 
		 * @see SQLH#mangleDBName(String)
		 */
		boolean getRawTableNames();

	}

	/**
	 * This constructor creates a new {@link StringColumnTransformProcessor}.
	 */
	public StringColumnTransformProcessor(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		C config = getConfig();
		String table = mangleDBName(config.getTable());
		String column = mangleDBName(config.getColumn());
		try {

			DBTable tableDef =
				DBSchemaUtils.extractTable(connection.getPool(), DBSchemaFactory.createDBSchema(), table);
			DBPrimary primaryKey = tableDef.getPrimaryKey();

			List<SQLColumnDefinition> columns = new ArrayList<>();
			columns.add(columnDef(column));
			List<String> keyColumns = new ArrayList<>();
			for (DBColumnRef keyColumn : primaryKey.getColumnRefs()) {
				String keyColumnName = keyColumn.getName();
				columns.add(columnDef(keyColumnName));
				keyColumns.add(keyColumnName);
			}

			SQLQuery<SQLSelect> select = query(select(columns, table(table)));

			DBHelper sqlDialect = connection.getSQLDialect();
			CompiledStatement query = select.toSql(sqlDialect);
			query.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

			try (ResultSet resultSet = query.executeQuery(connection)) {
				processRows(log, resultSet, table, column, keyColumns);
			}
		} catch (SQLException ex) {
			log.error("Failed to transform values in column '" + column + "' of table '"
				+ table + "'.", ex);
		}

	}

	private String mangleDBName(String name) {
		return getConfig().getRawTableNames() ? name : SQLH.mangleDBName(name);
	}

	/**
	 * Modifies the rows in the given result set.
	 * 
	 * @param log
	 *        {@link Log} to write infos and errors to.
	 * @param rows
	 *        All rows in {@link Config#getTable()}. The first column in the result set is the
	 *        actual column to modify.
	 * @param table
	 *        The database name of the table. See {@link Config#getTable()}.
	 * @param column
	 *        The database name of the column. See {@link Config#getColumn()}.
	 * @param keyColumns
	 *        All key columns in the result set.
	 */
	protected abstract void processRows(Log log, ResultSet rows, String table, String column, List<String> keyColumns)
			throws SQLException;

}

