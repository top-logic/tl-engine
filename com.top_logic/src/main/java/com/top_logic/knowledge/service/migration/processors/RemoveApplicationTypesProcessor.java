/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.schema.config.AlternativeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.db2.migration.db.transformers.StoreTypeConfiguration;
import com.top_logic.knowledge.service.migration.DropTablesProcessor;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.RemoveBranchSwitchProcessor;
import com.top_logic.knowledge.service.migration.RemovedTypeAlternatives;

/**
 * {@link MigrationProcessor} to remove database tables of persistent object types.
 * 
 * <p>
 * The types are removed from the stored schema, and their tables are dropped. Abstract types and
 * alternatives have no table, they are only removed from the stored schema. Alternatives of the
 * stored schema no longer list the removed types; an alternative without remaining
 * specialisations is removed as well.
 * </p>
 * 
 * @see AddApplicationTypesProcessor
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveApplicationTypesProcessor extends AbstractConfiguredInstance<RemoveApplicationTypesProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link RemoveApplicationTypesProcessor}.
	 */
	public interface Config extends PolymorphicConfiguration<RemoveApplicationTypesProcessor> {

		/**
		 * The application tables to remove.
		 */
		List<ApplicationTable> getApplicationTables();

		/**
		 * Names of the additional tables to remove.
		 * 
		 * @see SchemaConfiguration#getAdditionalTables()
		 */
		@Format(CommaSeparatedStringSet.class)
		Set<String> getAdditionalTables();

		/**
		 * Setter for {@link #getAdditionalTables()}.
		 */
		void setAdditionalTables(Set<String> value);

	}

	/**
	 * Configuration of an application table.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ApplicationTable extends NamedConfigMandatory {

		/**
		 * Application name of the table.
		 * 
		 * @see MetaObjectConfig#getObjectName()
		 */
		@Override
		String getName();

		/**
		 * Database name of the table.
		 * 
		 * <p>
		 * Not used for an abstract type or an alternative, which have no table.
		 * </p>
		 * 
		 * @see MetaObjectConfig#getDBName()
		 * 
		 * @implNote The {@link #getDBName()} is declared as mandatory to be independent of the
		 *           fallback implementation {@link SQLH#mangleDBName(String)}. If the
		 *           implementation changes, the fallback name would not longer match the actual
		 *           database name of the table.
		 */
		@Mandatory
		@Label("Database name")
		String getDBName();

		/**
		 * Setter for {@link #getDBName()}.
		 */
		void setDBName(String value);

		/**
		 * Factory method to create an {@link ApplicationTable}.
		 */
		static ApplicationTable newTable(String applicationName, String dbName) {
			ApplicationTable table =  TypedConfiguration.newConfigItem(ApplicationTable.class);
			table.setName(applicationName);
			table.setDBName(dbName);
			return table;
		}
	}

	/**
	 * Creates a {@link RemoveApplicationTypesProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveApplicationTypesProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		if (getConfig().getApplicationTables().isEmpty() && getConfig().getAdditionalTables().isEmpty()) {
			return;
		}
		SchemaConfiguration currentSchema = context.getPersistentSchema();
		Map<String, MetaObjectName> types = currentSchema.getMetaObjects().getTypes();
		List<ApplicationTable> withTable = getConfig().getApplicationTables()
			.stream()
			.filter(table -> hasTable(types.get(table.getName())))
			.collect(Collectors.toList());

		DropTablesProcessor.Config dropProcessor = newConfigItem(DropTablesProcessor.Config.class);
		dropProcessor.setTables(new ListBuilder<String>()
			.addAll(withTable.stream().map(ApplicationTable::getDBName).collect(Collectors.toList()))
			.addAll(getConfig().getAdditionalTables())
			.toList());
		execute(context, log, connection, dropProcessor);

		RemoveBranchSwitchProcessor.Config removeProcessor = newConfigItem(RemoveBranchSwitchProcessor.Config.class);
		removeProcessor.setTableTypes(withTable.stream().map(ApplicationTable::getName).collect(Collectors.toList()));
		execute(context, log, connection, removeProcessor);

		Set<String> removedTypes = new HashSet<>(applicationTableNames());
		types.keySet().removeAll(removedTypes);
		RemovedTypeAlternatives.cleanup(currentSchema.getMetaObjects(), removedTypes, log);

		Collection<DBTable> additionalTables = currentSchema.getAdditionalTables().getTables();
		for (Iterator<DBTable> it = additionalTables.iterator(); it.hasNext(); )		{
			DBTable table = it.next();
			if (getConfig().getAdditionalTables().contains(table.getName())) {
				it.remove();
			}
		}
		SchemaSetup setup = new SchemaSetup(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, currentSchema);
		StoreTypeConfiguration.Config updateConfig = newConfigItem(StoreTypeConfiguration.Config.class);
		StoreTypeConfiguration updateProcessor = TypedConfigUtil.createInstance(updateConfig);
		updateProcessor.setSchemaSetup(setup);
		updateProcessor.doMigration(context, log, connection);

	}

	/**
	 * Whether the given type definition of the stored schema has a table in the database.
	 * 
	 * <p>
	 * Abstract types and alternatives only exist in the schema. A type unknown to the stored schema
	 * is assumed to have a table, so that its table is dropped anyway.
	 * </p>
	 */
	private static boolean hasTable(MetaObjectName type) {
		if (type instanceof AlternativeConfig) {
			return false;
		}
		if (type instanceof MetaObjectConfig) {
			return !((MetaObjectConfig) type).isAbstract();
		}
		return true;
	}

	private List<String> applicationTableNames() {
		return getConfig().getApplicationTables()
			.stream()
			.map(ApplicationTable::getName)
			.collect(Collectors.toList());
	}

	private void execute(MigrationContext context, Log log, PooledConnection connection,
			PolymorphicConfiguration<? extends MigrationProcessor> processor) {
		TypedConfigUtil.createInstance(processor).doMigration(context, log, connection);
	}

}
