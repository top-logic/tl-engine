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
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFactory;
import com.top_logic.basic.db.sql.SQLQuery;
import com.top_logic.basic.db.sql.SQLSelect;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
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
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		C config = getConfig();

		List<SQLColumnDefinition> columns = new ArrayList<>();
		List<String> keyColumns = new ArrayList<>();
		
		String table;
		try {
			if (config.getRawTableNames()) {
				table = mangleDBName(config.getTable());
				String valueColumn = mangleDBName(config.getColumn());

				DBTable tableDef =
					DBSchemaUtils.extractTable(connection.getPool(), DBSchemaFactory.createDBSchema(), table);
				if (tableDef == null) {
					log.info("No such table '" + table + "', skipping migration.");
					return;
				}

				DBPrimary primaryKey = tableDef.getPrimaryKey();

				columns.add(columnDef(valueColumn));
				for (DBColumnRef keyColumn : primaryKey.getColumnRefs()) {
					String keyColumnName = keyColumn.getName();
					columns.add(columnDef(keyColumnName));
					keyColumns.add(keyColumnName);
				}

				process(log, connection, table, valueColumn, columns, keyColumns, SQLFactory.literalTrueLogical());
			} else {
				MORepository repository = context.getPersistentRepository();
				MOStructure tableDef = (MOStructure) repository.getMetaObject(config.getTable());
				MOAttribute attributeDef = tableDef.getAttributeOrNull(config.getColumn());
				if (attributeDef == null) {
					// Use flex data.
					MOStructure flexTable = (MOStructure) repository.getMetaObject(AbstractFlexDataManager.FLEX_DATA);
					table = flexTable.getDBMapping().getDBName();

					if (context.hasBranchSupport()) {
						columns.add(columnDef(AbstractFlexDataManager.BRANCH_DBNAME));
						keyColumns.add(AbstractFlexDataManager.BRANCH_DBNAME);
					}

					columns.add(columnDef(AbstractFlexDataManager.IDENTIFIER_DBNAME));
					keyColumns.add(AbstractFlexDataManager.IDENTIFIER_DBNAME);

					columns.add(columnDef(BasicTypes.REV_MAX_DB_NAME));
					keyColumns.add(BasicTypes.REV_MAX_DB_NAME);

					columns.add(columnDef(AbstractFlexDataManager.TYPE_DBNAME));
					keyColumns.add(AbstractFlexDataManager.TYPE_DBNAME);
					
					columns.add(columnDef(AbstractFlexDataManager.ATTRIBUTE_DBNAME));
					keyColumns.add(AbstractFlexDataManager.ATTRIBUTE_DBNAME);

					{
						log.info("Processing VARCHAR flex data for table '" + config.getTable() + "' and attribute '"
							+ config.getColumn() + "'.");

						String valueColumn = AbstractFlexDataManager.VARCHAR_DATA_DBNAME;

						List<SQLColumnDefinition> allColumns = new ArrayList<>();
						allColumns.add(columnDef(valueColumn));
						allColumns.addAll(columns);

						process(log, connection, table, valueColumn, allColumns, keyColumns,
							and(
								eqSQL(column(AbstractFlexDataManager.TYPE_DBNAME),
									literal(DBType.STRING, config.getTable())),
								eqSQL(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
									literal(DBType.STRING, config.getColumn())),
								eqSQL(column(AbstractFlexDataManager.DATA_TYPE_DBNAME),
									literal(DBType.BYTE, AbstractFlexDataManager.STRING_TYPE))));
					}

					{
						log.info("Processing CLOB flex data for table '" + config.getTable() + "' and attribute '"
							+ config.getColumn() + "'.");

						String valueColumn = AbstractFlexDataManager.CLOB_DATA_DBNAME;

						List<SQLColumnDefinition> allColumns = new ArrayList<>();
						allColumns.add(columnDef(valueColumn));
						allColumns.addAll(columns);

						process(log, connection, table, valueColumn, allColumns, keyColumns,
							and(
								eqSQL(column(AbstractFlexDataManager.TYPE_DBNAME),
									literal(DBType.STRING, config.getTable())),
								eqSQL(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
									literal(DBType.STRING, config.getColumn())),
								eqSQL(column(AbstractFlexDataManager.DATA_TYPE_DBNAME),
									literal(DBType.BYTE, AbstractFlexDataManager.CLOB_TYPE))));
					}
				} else {
					// Use row data.
					table = tableDef.getDBMapping().getDBName();

					String valueColumn = attributeDef.getDbMapping()[0].getDBName();
					columns.add(columnDef(valueColumn));

					if (tableDef.getDBMapping().multipleBranches()) {
						columns.add(columnDef(BasicTypes.BRANCH_DB_NAME));
						keyColumns.add(BasicTypes.BRANCH_DB_NAME);
					}

					columns.add(columnDef(BasicTypes.IDENTIFIER_DB_NAME));
					keyColumns.add(BasicTypes.IDENTIFIER_DB_NAME);

					columns.add(columnDef(BasicTypes.REV_MAX_DB_NAME));
					keyColumns.add(BasicTypes.REV_MAX_DB_NAME);

					process(log, connection, table, valueColumn, columns, keyColumns, SQLFactory.literalTrueLogical());
				}
			}
		} catch (SQLException ex) {
			log.error("Failed to transform values in column '" + config.getColumn() + "' of table '"
				+ config.getTable() + "'.", ex);
		}

	}

	private void process(Log log, PooledConnection connection, String table, String column,
			List<SQLColumnDefinition> columns, List<String> keyColumns, SQLExpression where) throws SQLException {
		SQLQuery<SQLSelect> select = query(select(columns, table(table), where));

		DBHelper sqlDialect = connection.getSQLDialect();
		CompiledStatement query = select.toSql(sqlDialect);
		query.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

		try (ResultSet resultSet = query.executeQuery(connection)) {
			processRows(log, resultSet, table, column, keyColumns);
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

