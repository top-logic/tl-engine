/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.service.db2.migration.db.transformers.StoreTypeConfiguration;
import com.top_logic.knowledge.service.migration.CreateTablesProcessor;
import com.top_logic.knowledge.service.migration.InsertBranchSwitchProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} to add database tables for persistent object types.
 */
public class AddApplicationTypesProcessor extends AbstractConfiguredInstance<AddApplicationTypesProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link AddApplicationTypesProcessor}.
	 */
	public interface Config<I extends AddApplicationTypesProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * Table definitions for persistent types to add to the application schema.
		 */
		@ItemDefault
		MetaObjectsConfig getSchema();

	}

	/**
	 * Creates a {@link AddApplicationTypesProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddApplicationTypesProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration currentSchema = context.getPersistentSchema();
		Map<String, MetaObjectName> currentTypes = currentSchema.getMetaObjects().getTypes();
		Map<String, MetaObjectName> newTypes = getConfig().getSchema().getTypes();

		List<String> newTypeNames = new ArrayList<>();
		for (var entry : newTypes.entrySet()) {
			String name = entry.getKey();
			if (currentTypes.containsKey(name)) {
				log.info("Table '" + name + "' already exists, ignoring.", Log.WARN);
				continue;
			}

			newTypeNames.add(name);
			currentTypes.put(name, entry.getValue());
		}

		if (!newTypeNames.isEmpty()) {
			SchemaSetup setup =
				new SchemaSetup(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, currentSchema);
			MORepository allTypes = setup.createMORepository(DefaultMOFactory.INSTANCE);

			CreateTablesProcessor.Config createProcessor =
				TypedConfiguration.newConfigItem(CreateTablesProcessor.Config.class);
			SchemaSetup.addTables(createProcessor, allTypes, newTypeNames);

			if (!createProcessor.getTables().isEmpty()) {
				execute(context, log, connection, createProcessor);

				InsertBranchSwitchProcessor.Config insertProcessor =
					TypedConfiguration.newConfigItem(InsertBranchSwitchProcessor.Config.class);
				for (String newTypeName : newTypeNames) {
					MetaObject newType = allTypes.getMetaObject(newTypeName);
					if (createProcessor.getTable(newType.getName()) != null) {
						insertProcessor.getTableTypes().add(newType.getName());
					}
				}
				assert insertProcessor.getTableTypes().size() == createProcessor.getTables().size();
				execute(context, log, connection, insertProcessor);

				// Create a StoreTypeConfiguration processor.
				StoreTypeConfiguration.Config updateConfig =
					TypedConfiguration.newConfigItem(StoreTypeConfiguration.Config.class);
				StoreTypeConfiguration updateProcessor = TypedConfigUtil.createInstance(updateConfig);
				updateProcessor.setSchemaSetup(setup);
				updateProcessor.doMigration(context, log, connection);
			}
		}
	}

	private void execute(MigrationContext context, Log log, PooledConnection connection,
			PolymorphicConfiguration<? extends MigrationProcessor> processor) {
		TypedConfigUtil.createInstance(processor).doMigration(context, log, connection);
	}

}
