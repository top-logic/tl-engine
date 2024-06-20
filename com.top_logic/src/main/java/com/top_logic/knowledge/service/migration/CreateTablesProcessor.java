/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedResource;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.PooledConnection;

/**
 * {@link MigrationProcessor} creating the configured tables.
 * 
 * @see Config#getTables()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateTablesProcessor extends AbstractConfiguredInstance<CreateTablesProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link CreateTablesProcessor}.
	 */
	@TagName("create-tables")
	public interface Config extends PolymorphicConfiguration<CreateTablesProcessor>, DBSchema {

		/**
		 * @see #getSchemas()
		 */
		String SCHEMAS = "schemas";

		/**
		 * References to externally defined schemas.
		 */
		@Name(SCHEMAS)
		@Key(NamedResource.NAME_ATTRIBUTE)
		List<NamedResource> getSchemas();

	}

	/**
	 * Creates a {@link CreateTablesProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateTablesProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config config = getConfig();
		DBSchema dbSchema = config;
		if (!dbSchema.getTables().isEmpty()) {
			createTables(log, connection, dbSchema);
		}

		if (!config.getSchemas().isEmpty()) {
			DBSchema externalSchema = SchemaSetup.newDBSchema(config.getSchemas());
			externalSchema.setName(null);
			createTables(log, connection, externalSchema);
		}
	}

	private void createTables(Log log, PooledConnection connection, DBSchema dbSchema) {
		try {
			for (DBTable table : dbSchema.getTables()) {
				DBSchemaUtils.create(connection, table);
			}
			log.info("Created tables: "
				+ dbSchema.getTables().stream().map(t -> t.getName()).collect(Collectors.joining(", ")));
		} catch (SQLException ex) {
			log.error("Failed to create tables.", ex);
		}
	}

}
