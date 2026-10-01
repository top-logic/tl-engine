/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} copying the values of an attribute to another attribute of the same
 * table.
 *
 * <p>
 * All rows of the table, including historic revisions, with a value in the source attribute get
 * this value in the target attribute. Rows without a value in the source attribute are left
 * unchanged. Both attributes must be declared in the stored schema and must be stored in a single
 * column each.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CopyColumnProcessor extends AbstractConfiguredInstance<CopyColumnProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CopyColumnProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends CopyColumnProcessor> extends PolymorphicConfiguration<I> {

		/** Tag name of a {@link CopyColumnProcessor} in a migration script. */
		String TAG_NAME = "copy-column";

		/** Configuration name of {@link #getTable()}. */
		String TABLE = "table";

		/** Configuration name of {@link #getSource()}. */
		String SOURCE = "source";

		/** Configuration name of {@link #getTarget()}. */
		String TARGET = "target";

		/**
		 * The table whose rows are updated.
		 *
		 * <p>
		 * The name is the type name as given in {@link MetaObjectName#getObjectName()}, not the
		 * concrete SQL table name.
		 * </p>
		 */
		@Name(TABLE)
		@Mandatory
		String getTable();

		/**
		 * The attribute whose values are copied.
		 *
		 * <p>
		 * The name is specified as given in {@link AttributeConfig#getAttributeName()}, not the
		 * concrete SQL column name.
		 * </p>
		 */
		@Name(SOURCE)
		@Mandatory
		String getSource();

		/**
		 * The attribute receiving the values.
		 *
		 * <p>
		 * The name is specified as given in {@link AttributeConfig#getAttributeName()}, not the
		 * concrete SQL column name.
		 * </p>
		 */
		@Name(TARGET)
		@Mandatory
		String getTarget();

	}

	/**
	 * Creates a {@link CopyColumnProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CopyColumnProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String tableName = config.getTable();
		MetaObject type = context.getPersistentRepository().getTypeOrNull(tableName);
		if (!(type instanceof MOStructure)) {
			log.error("No table '" + tableName + "' for copying column '" + config.getSource() + "'.");
			return;
		}
		MOStructure table = (MOStructure) type;
		String source = columnName(log, table, config.getSource());
		String target = columnName(log, table, config.getTarget());
		if (source == null || target == null) {
			return;
		}

		try {
			CompiledStatement update = query(
				update(
					table(table.getDBMapping().getDBName()),
					not(isNull(column(source))),
					List.of(target),
					List.of(column(source)))).toSql(connection.getSQLDialect());
			int rows = update.executeUpdate(connection);
			log.info("Copied '" + config.getSource() + "' to '" + config.getTarget() + "' in " + rows
				+ " rows of table '" + tableName + "'.");
		} catch (SQLException ex) {
			log.error("Failed to copy '" + config.getSource() + "' to '" + config.getTarget() + "' in table '"
				+ tableName + "': " + ex.getMessage(), ex);
		}
	}

	private static String columnName(Log log, MOStructure table, String attributeName) {
		MOAttribute attribute = table.getAttributeOrNull(attributeName);
		if (attribute == null || attribute.getDbMapping().length != 1) {
			log.error("Table '" + table.getName() + "' has no single column attribute '" + attributeName + "'.");
			return null;
		}
		return attribute.getDbMapping()[0].getDBName();
	}

}
