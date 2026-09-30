/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.migration.DropColumnProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that drops a column the database still has although the persistent
 * schema no longer declares it.
 *
 * <p>
 * {@link DropColumnProcessor} finds the column through the persistent schema, and skips it when
 * the schema has no such attribute. The schema stored in the database is replaced by the
 * application's own whenever a migration runs, so an application that dropped an attribute from
 * its schema definition without the migration that drops the column, and then ran any migration,
 * is left with a column that nothing declares and {@link DropColumnProcessor} cannot reach.
 * This processor asks the database itself whether the column is there.
 * </p>
 *
 * <p>
 * It belongs behind a {@link DropColumnProcessor} for the same attribute: while the schema still
 * declares the attribute, the column is left to that processor, which also updates the schema.
 * A table or column that does not exist is reported and accepted.
 * </p>
 */
public class DropOrphanedColumnProcessor
		extends AbstractConfiguredInstance<DropOrphanedColumnProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link DropOrphanedColumnProcessor}.
	 */
	@TagName("drop-orphaned-column")
	public interface Config extends PolymorphicConfiguration<DropOrphanedColumnProcessor> {

		/** @see #getTable() */
		String TABLE = "table";

		/** @see #getColumn() */
		String COLUMN = "column";

		/**
		 * Name of the table type the column belongs to.
		 *
		 * @see MetaObject#getName()
		 */
		@Mandatory
		@Name(TABLE)
		String getTable();

		/**
		 * Name of the attribute the column stored, as in the schema definition it was removed
		 * from.
		 *
		 * <p>
		 * The column name is derived from it the way the schema derives it.
		 * </p>
		 */
		@Mandatory
		@Name(COLUMN)
		String getColumn();
	}

	/**
	 * Creates a {@link DropOrphanedColumnProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DropOrphanedColumnProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String typeName = getConfig().getTable();
		String attributeName = getConfig().getColumn();

		MetaObject type = context.getPersistentRepository().getTypeOrNull(typeName);
		if (!(type instanceof MOStructure table)) {
			log.info("Table '" + typeName + "' does not exist in the persistent schema, there is no column '"
				+ attributeName + "' to drop.", Log.WARN);
			return;
		}
		if (table.getAttributeOrNull(attributeName) != null) {
			log.info("Column '" + attributeName + "' is still declared in table '" + typeName
				+ "', it is left to the drop-column processor.", Log.WARN);
			return;
		}

		String tableName = table.getDBMapping().getDBName();
		String columnName = SQLH.mangleDBName(attributeName);
		try {
			String existing = existingColumn(connection, tableName, columnName);
			if (existing == null) {
				log.info("Table '" + tableName + "' has no column '" + columnName + "', there is nothing to drop.");
				return;
			}

			log.info("Dropping column '" + existing + "' from table '" + tableName
				+ "', which the persistent schema no longer declares.");
			query(dropColumn(table(tableName), existing)).toSql(connection.getSQLDialect())
				.executeUpdate(connection);
		} catch (SQLException ex) {
			log.error("Unable to drop column '" + columnName + "' from table '" + tableName + "'.", ex);
		}
	}

	/**
	 * The name under which the database knows the given column, or {@code null} if the table has
	 * no such column.
	 *
	 * <p>
	 * The metadata lookup matches the table name exactly, and a database folds the case of a name
	 * that was not quoted when the table was created - to upper case in most, to lower case in
	 * PostgreSQL. Both spellings are therefore asked for.
	 * </p>
	 */
	private static String existingColumn(PooledConnection connection, String tableName, String columnName)
			throws SQLException {
		DBHelper sqlDialect = connection.getSQLDialect();
		DatabaseMetaData metaData = connection.getMetaData();
		String schema = sqlDialect.getCurrentSchema(connection);
		for (String spelling : new LinkedHashSet<>(
			List.of(tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT)))) {
			try (ResultSet columns = metaData.getColumns(null, schema, spelling, null)) {
				while (columns.next()) {
					String name = columns.getString("COLUMN_NAME");
					if (columnName.equalsIgnoreCase(name)) {
						return name;
					}
				}
			}
		}
		return null;
	}

}
