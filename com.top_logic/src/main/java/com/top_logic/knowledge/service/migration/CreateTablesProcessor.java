/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.sql.SQLException;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
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
	public interface Config extends PolymorphicConfiguration<CreateTablesProcessor>, DBSchema {
		// Pure sum interface.
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
		try {
			for (DBTable table : getConfig().getTables()) {
				DBSchemaUtils.create(connection, table);
			}
			log.info("Created tables: "
				+ getConfig().getTables().stream().map(t -> t.getName()).collect(Collectors.joining(", ")));
		} catch (SQLException ex) {
			log.error("Failed to create tables.", ex);
		}
	}

}
