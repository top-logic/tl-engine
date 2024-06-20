/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.service.migration.processors.AddMOAttributeProcessor;

/**
 * {@link MigrationProcessor} that drops a column from a table.
 */
public class DropColumnProcessor extends AbstractConfiguredInstance<DropColumnProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link DropColumnProcessor}.
	 */
	@TagName("drop-column")
	public interface Config<I extends DropColumnProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * The table to modify.
		 * 
		 * <p>
		 * The name is the type name as given in {@link MetaObjectName#getObjectName()}, not the
		 * concrete SQL table name.
		 * </p>
		 */
		String getTable();

		/**
		 * The column to drop.
		 * 
		 * <p>
		 * The name is specified as given in {@link AttributeConfig#getAttributeName()}, not the
		 * concrete SQL column name.
		 * </p>
		 */
		String getColumn();

		/**
		 * Whether to adjust the persistent schema stored in the database.
		 */
		@BooleanDefault(true)
		boolean getUpdateSchema();

		/**
		 * Whether to adjust the actual tables in the database.
		 */
		@BooleanDefault(true)
		boolean getUpdateData();

	}

	/**
	 * Creates a {@link DropColumnProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DropColumnProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		String typeName = getConfig().getTable();
		String attributeName = getConfig().getColumn();

		try {
			SchemaConfiguration persistentSchema = context.getPersistentSchema();
			if (getConfig().getUpdateData()) {
				SchemaSetup setup =
					new SchemaSetup(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, persistentSchema);
				MORepository repository = setup.createMORepository(DefaultMOFactory.INSTANCE);
				MOClass specifiedType = (MOClass) repository.getMetaObject(typeName);

				for (MetaObject type : repository.getMetaObjects()) {
					if (!(type instanceof MOClass)) {
						continue;
					}

					MOClass tableType = (MOClass) type;
					if (tableType.isAbstract()) {
						continue;
					}

					if (!tableType.isSubtypeOf(specifiedType)) {
						continue;
					}

					String tableName = tableType.getDBMapping().getDBName();
					MOAttribute attribute = tableType.getAttributeOrNull(attributeName);
					if (attribute == null) {
						log.info("Column '" + attributeName + "' not declared in table '" + tableName + "', skipping.",
							Log.WARN);
						continue;
					}
					for (DBAttribute column : attribute.getDbMapping()) {
						String columnName = column.getDBName();

						log.info("Dropping column '" + columnName + "' from table '" + tableName + "'.");
						CompiledStatement sql =
							query(
								dropColumn(table(tableName), columnName)).toSql(connection.getSQLDialect());
						sql.executeUpdate(connection);
					}
				}
			}

			if (getConfig().getUpdateSchema()) {
				MetaObjectName typeConfig = persistentSchema.getMetaObjects().getTypes().get(typeName);
				if (typeConfig instanceof MetaObjectConfig) {
					MetaObjectConfig tableConfig = (MetaObjectConfig) typeConfig;
					Optional<AttributeConfig> attributeConfig =
						tableConfig.getAttributes().stream().filter(a -> a.getAttributeName().equals(attributeName))
							.findFirst();
					if (attributeConfig.isPresent()) {
						tableConfig.getAttributes().remove(attributeConfig.get());
						AddMOAttributeProcessor.updateStoredSchema(log, connection, persistentSchema);
					} else {
						log.info("No column '" + attributeName + "' declared in table '" + typeName
							+ "', skipping schema update.", Log.WARN);
					}
				} else {
					log.info("No table '" + typeName + "' found in persistent schema, skipping update.", Log.WARN);
				}
			}
		} catch (SQLException ex) {
			log.error(
				"Failed to drop column '" + attributeName + "' from table type '" + typeName + "': " + ex.getMessage(),
				ex);
		}
	}

}
