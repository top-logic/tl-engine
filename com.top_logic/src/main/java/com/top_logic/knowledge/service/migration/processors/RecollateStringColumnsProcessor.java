/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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
 * columns, so that they pick up the case-insensitive collation now used for newly created
 * columns.
 *
 * <p>
 * Non-binary string columns created before this change do not have the case-insensitive
 * collation (e.g. <code>tl_ci</code> on {@code PostgreSQL}) that is now applied to newly created
 * columns. Re-emitting the column type (without changing type, size, or binary-ness) makes the
 * database re-create the column with the current, collation-aware definition. Dialects that do not
 * apply a case-insensitive collation per column (e.g. Oracle and DB2, where case-insensitivity
 * depends on the database configuration) are skipped.
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
		@Name("table")
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
		@Name("column")
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

		try {
			DBHelper sqlDialect = connection.getSQLDialect();
			if (!sqlDialect.supportsColumnCollation()) {
				log.info("Dialect '" + sqlDialect.getClass().getSimpleName()
					+ "' does not apply a case-insensitive collation per column; nothing to re-collate.");
				return;
			}
			sqlDialect.prepareDatabase(connection);

			MORepository repository = context.getPersistentRepository();

			int count = 0;
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

				count += recollateTable(log, connection, table, columnName);
			}

			if (count == 0) {
				log.info("No non-binary string columns to re-collate.");
			}
		} catch (SQLException ex) {
			log.error("Failed to re-collate string columns: " + ex.getMessage(), ex);
		}
	}

	private int recollateTable(Log log, PooledConnection connection, MOStructure table, String columnName)
			throws SQLException {
		int count = 0;
		for (MOAttribute attr : selectAttributes(table, columnName)) {
			for (DBAttribute col : attr.getDbMapping()) {
				DBType type = col.getSQLType();
				boolean stringLike = type == DBType.STRING || type == DBType.CHAR || type == DBType.CLOB;
				if (col.isBinary() || !stringLike) {
					continue;
				}

				SQLModifyColumn modification = modifyColumnType(
					table(table.getDBMapping().getDBName()), col.getDBName(), col.getSQLType());
				modification.setBinary(col.isBinary());
				modification.setSize(col.getSQLSize());
				modification.setPrecision(col.getSQLPrecision());

				query(modification).toSql(connection.getSQLDialect()).executeUpdate(connection);

				log.info("Re-collated column '" + col.getDBName() + "' of table '" + table.getName() + "'.");
				count++;
			}
		}
		return count;
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
