/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} that fails the migration when a table still holds live rows.
 *
 * <p>
 * Guards a following {@link DeleteTableProcessor}: dropping a table that still has rows destroys
 * those rows without a trace. Placing this processor in front turns that into a refused startup
 * with an actionable message, so the data is still there to be rescued.
 * </p>
 *
 * <p>
 * A non-empty table is reported as a {@link TableNotEmpty} error rather than as a logged error.
 * That is deliberate, see {@link TableNotEmpty}.
 * </p>
 *
 * <p>
 * Only the current rows are counted. A versioned {@code KnowledgeBase} keeps the historic versions
 * of already deleted objects in the same table, and those must not block the drop.
 * </p>
 *
 * <p>
 * A table that the database does not know at all is reported and accepted: an application that
 * never deployed the module owning the table has nothing to lose.
 * </p>
 */
public class RequireEmptyTableProcessor
		extends AbstractConfiguredInstance<RequireEmptyTableProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link RequireEmptyTableProcessor}.
	 */
	@TagName("require-empty-table")
	public interface Config extends PolymorphicConfiguration<RequireEmptyTableProcessor> {

		/** @see #getTable() */
		String TABLE = "table";

		/** @see #getHint() */
		String HINT = "hint";

		/**
		 * Name of the table type that must no longer contain rows.
		 *
		 * @see MetaObject#getName()
		 */
		@Mandatory
		@Name(TABLE)
		String getTable();

		/**
		 * Explains how to get the table emptied, appended to the error message.
		 */
		@Name(HINT)
		String getHint();
	}

	/**
	 * Creates a {@link RequireEmptyTableProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RequireEmptyTableProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String tableName = getConfig().getTable();
		MetaObject type = context.getPersistentRepository().getTypeOrNull(tableName);
		if (!(type instanceof MOStructure table)) {
			log.info("Table '" + tableName
				+ "' does not exist in the persistent schema, there is nothing to check.", Log.WARN);
			return;
		}

		String dbName = table.getDBMapping().getDBName();
		long rows;
		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			String sql = "SELECT COUNT(*) FROM " + sqlDialect.tableRef(dbName)
				+ " WHERE " + sqlDialect.columnRef(BasicTypes.REV_MAX_DB_NAME) + " = " + Revision.CURRENT_REV;
			try (Statement statement = connection.createStatement();
					ResultSet result = statement.executeQuery(sql)) {
				result.next();
				rows = result.getLong(1);
			}
		} catch (SQLException ex) {
			throw new TableNotEmpty("Unable to count the rows of table '" + dbName + "', so it cannot be"
				+ " established that dropping it is safe: " + ex.getMessage());
		}

		if (rows > 0) {
			throw new TableNotEmpty("Table '" + dbName + "' still contains " + rows
				+ " row(s) and must not be dropped, because dropping it would delete them. "
				+ getConfig().getHint());
		}
		log.info("Table '" + dbName + "' holds no current rows, it can safely be dropped.");
	}

	/**
	 * Reports that the guarded table is not empty and stops the migration.
	 *
	 * <p>
	 * An {@link Error} on purpose. {@code DataMigration.processProcessors} catches every
	 * {@link Exception} a processor throws, turns it into a logged error and runs the remaining
	 * processors of the phase anyway, and the phase is committed afterwards - the collected errors
	 * only abort the boot later on, when the damage is already written. A precondition that has to
	 * keep the processors behind it from running therefore has to leave that loop as an
	 * {@link Error}.
	 * </p>
	 */
	public static final class TableNotEmpty extends Error {

		/**
		 * Creates a {@link TableNotEmpty} error.
		 *
		 * @param message
		 *        What is in the way and how to resolve it.
		 */
		public TableNotEmpty(String message) {
			super(message);
		}
	}

}
