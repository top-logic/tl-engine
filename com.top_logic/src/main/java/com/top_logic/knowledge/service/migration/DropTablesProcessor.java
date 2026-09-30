/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link MigrationProcessor} dropping the configured tables.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DropTablesProcessor extends AbstractConfiguredInstance<DropTablesProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link DropTablesProcessor}.
	 */
	@TagName("drop-tables")
	public interface Config extends PolymorphicConfiguration<DropTablesProcessor> {
		/**
		 * The database names of the tables to drop.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getTables();

		/**
		 * Setter for {@link #getTables()}.
		 */
		void setTables(List<String> value);
	}

	/**
	 * Creates a {@link DropTablesProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DropTablesProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			DBSchema schema = DBSchemaFactory.createDBSchema();
			for (String tableName : getConfig().getTables()) {
				DBTable table = DBSchemaFactory.createTable(tableName);
				schema.getTables().add(table);
			}
			DBSchemaUtils.resetTables(connection, schema, false);
			log.info("Dropped tables: " + getConfig().getTables().stream().collect(Collectors.joining(", ")));
		} catch (SQLException ex) {
			log.error("Failed to drop tables.", ex);
		}
	}

}
