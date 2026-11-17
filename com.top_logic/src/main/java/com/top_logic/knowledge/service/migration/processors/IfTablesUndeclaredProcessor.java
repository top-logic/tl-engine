/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.migration.DataMigration;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor;
import com.top_logic.knowledge.service.migration.DeleteTableProcessor.Config.TableRef;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} executing its {@link Config#getProcessors() processors} only, if the
 * {@link MigrationContext#getApplicationSchema() schema of the application} declares none of its
 * {@link Config#getTables() tables}.
 *
 * <p>
 * A module that hands persistent types over to an optional module uses this processor to remove
 * these types (e.g. with {@link DeleteTableProcessor} and the model processors deleting classes and
 * modules) from the databases of applications without the optional module. In an application with
 * the optional module, its schema declares the types, and their tables and contents are kept.
 * </p>
 *
 * <p>
 * The processors are executed in their order with the context, log and connection of this
 * processor, as the processors of a migration script are. A name the stored schema does not know
 * counts as undeclared.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IfTablesUndeclaredProcessor extends AbstractConfiguredInstance<IfTablesUndeclaredProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link IfTablesUndeclaredProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<IfTablesUndeclaredProcessor> {

		/**
		 * Tag name of {@link Config} in a migration script.
		 */
		String TAG_NAME = "if-tables-undeclared";

		/**
		 * Configuration name of {@link #getTables()}.
		 */
		String TABLES = "tables";

		/**
		 * Configuration name of {@link #getProcessors()}.
		 */
		String PROCESSORS = "processors";

		/**
		 * Names of the table types the schema of the application must not declare for
		 * {@link #getProcessors()} to be executed.
		 *
		 * <p>
		 * If the schema of the application declares one of these types, no processor is executed.
		 * </p>
		 */
		@Name(TABLES)
		List<TableRef> getTables();

		/**
		 * The {@link MigrationProcessor}s to execute, if the schema of the application declares
		 * none of {@link #getTables()}.
		 */
		@Name(PROCESSORS)
		List<PolymorphicConfiguration<? extends MigrationProcessor>> getProcessors();

	}

	/**
	 * Creates a {@link IfTablesUndeclaredProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IfTablesUndeclaredProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Map<String, MetaObjectName> declaredTypes = context.getApplicationSchema().getMetaObjects().getTypes();

		for (TableRef table : getConfig().getTables()) {
			String typeName = table.getObjectName();
			if (declaredTypes.containsKey(typeName)) {
				log.info("Table '" + typeName + "' is declared by the application, skipped processors at "
					+ getConfig().location() + ".");
				return;
			}
		}

		DataMigration.processProcessors(context, log, connection, getConfig().getProcessors());
	}

}
