/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor.Config.TableRef;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} deleting table types the schema of the application no longer
 * declares.
 *
 * <p>
 * A module that hands persistent types over to an optional module uses this processor to delete
 * their tables from the databases of applications without the optional module. In an application
 * with the optional module, its schema declares the types, and their tables and contents are kept.
 * </p>
 *
 * <p>
 * Each type of {@link Config#getTables()} that is not part of the
 * {@link MigrationContext#getApplicationSchema() schema of the application} is deleted as by
 * {@link DeleteTableProcessor}. A declared type is kept, a type the stored schema does not contain
 * is skipped; both are reported to the log only.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeleteUndeclaredTablesProcessor extends AbstractConfiguredInstance<DeleteUndeclaredTablesProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link DeleteUndeclaredTablesProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<DeleteUndeclaredTablesProcessor> {

		/**
		 * Tag name of {@link Config} in a migration script.
		 */
		String TAG_NAME = "delete-undeclared-tables";

		/**
		 * Configuration name of {@link #getTables()}.
		 */
		String TABLES = "tables";

		/**
		 * Names of the table types to delete, if the schema of the application does not declare
		 * them.
		 *
		 * @see DeleteTableProcessor.Config#getTables()
		 */
		@Name(TABLES)
		@DefaultContainer
		List<TableRef> getTables();

	}

	/**
	 * Creates a {@link DeleteUndeclaredTablesProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeleteUndeclaredTablesProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Map<String, MetaObjectName> declaredTypes = context.getApplicationSchema().getMetaObjects().getTypes();

		List<TableRef> undeclared = new ArrayList<>();
		for (TableRef table : getConfig().getTables()) {
			String typeName = table.getObjectName();
			if (declaredTypes.containsKey(typeName)) {
				log.info("Table '" + typeName + "' is declared by the application, was not removed.");
			} else {
				undeclared.add(table);
			}
		}
		if (undeclared.isEmpty()) {
			return;
		}

		DeleteTableProcessor.deleteTables(log, connection, undeclared, Collections.emptyList());
	}

}
