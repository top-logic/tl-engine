/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} updating the {@link AttributeStorage} of a {@link MOAttribute}.
 * 
 * @see MOAttribute#setStorage(AttributeStorage)
 */
public class UpdateColumnStorageProcessor extends AbstractConfiguredInstance<UpdateColumnStorageProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link UpdateColumnStorageProcessor}.
	 */
	@TagName("update-column-storage")
	public interface Config<I extends UpdateColumnStorageProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * The name of the table that defines the column to modify.
		 */
		String getTable();

		/**
		 * The column to modify.
		 */
		String getColumn();

		/**
		 * The new storage algorithm to use.
		 *
		 * @see AttributeConfig#getStorage()
		 */
		@InstanceFormat
		AttributeStorage getStorage();

	}

	/**
	 * Creates a {@link UpdateColumnStorageProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateColumnStorageProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration schema = context.getPersistentSchema();
		Config<?> config = getConfig();
		MetaObjectName type = schema.getMetaObjects().getTypes().get(config.getTable());
		if (type == null) {
			log.info("No such table '" + config.getTable() + "', ignoring.", Log.WARN);
			return;
		}
		if (!(type instanceof MetaObjectConfig)) {
			log.info("Not a table definition '" + config.getTable() + "' but '"
				+ type.descriptor().getConfigurationInterface().getName() + "' , ignoring.", Log.WARN);
			return;
		}
		MetaObjectConfig table = (MetaObjectConfig) type;
		Optional<AttributeConfig> column =
			table.getAttributes().stream().filter(a -> a.getAttributeName().equals(config.getColumn())).findAny();
		if (column.isEmpty()) {
			log.info(
				"No such column '" + config.getColumn() + "' in table '" + config.getTable() + "', ignoring.",
				Log.WARN);
			return;
		}
		column.get().setStorage(config.getStorage());

		AddMOAttributeProcessor.updateStoredSchema(log, connection, schema);
	}

}
