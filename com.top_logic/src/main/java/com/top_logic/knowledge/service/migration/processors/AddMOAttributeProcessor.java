/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} to add {@link MOReference} to a persistent object type.
 */
public abstract class AddMOAttributeProcessor
		extends AbstractConfiguredInstance<AddMOAttributeProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link AddMOAttributeProcessor}.
	 */
	@Abstract
	public interface Config<I extends AddMOAttributeProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * The attribute to add to {@link #getTable()}.
		 */
		@Abstract
		@Hidden
		AttributeConfig getAttribute();

		/**
		 * Name of the attribute before which the {@link #getAttribute() attribute} must be
		 * inserted. If <code>null</code>, the attribute is added to the end of the attributes list.
		 */
		@Nullable
		String getBefore();

		/**
		 * Name of the {@link MetaObject} to which the {@link #getAttribute()} is added.
		 */
		@Mandatory
		String getTable();

	}

	/**
	 * Creates a {@link AddMOAttributeProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddMOAttributeProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration currentSchema = context.getPersistentSchema();
		Map<String, MetaObjectName> currentTypes = currentSchema.getMetaObjects().getTypes();
		MetaObjectName mo = currentTypes.get(getConfig().getTable());
		if (mo == null) {
			log.error("Table '" + getConfig().getTable() + "' does not exist.");
			return;
		}
		if (!(mo instanceof MetaObjectConfig)) {
			log.error("Table '" + getConfig().getTable() + "' is not a table definition.");
			return;
		}
		MetaObjectConfig tableDefinition = (MetaObjectConfig) mo;
		addNewAttribute(log, tableDefinition);
		
		SchemaSetup setup = new SchemaSetup(instantiationContext(), currentSchema);
		MORepository allTypes = setup.createMORepository(DefaultMOFactory.INSTANCE);

		Set<MOClass> forUpdate = getTablesForUpdate(allTypes);
		for (MOClass tableForUpdate : forUpdate) {
			updateDatabaseTable(context, log, connection, tableForUpdate);
		}
		updateStoredSchema(log, connection, currentSchema);

		AttributeConfig attribute = getConfig().getAttribute();
		log.info("Added attribute '" + attribute.getAttributeName() + "' to table '" + tableDefinition.getObjectName()
				+ "'.");
		if (attribute.isMandatory()) {
			log.info("Note: Attribute '" + attribute.getAttributeName() + "' is mandatory. Existing rows in table '"
					+ tableDefinition.getObjectName() + "' are filled with dummy values!");
		}
	}

	/**
	 * Updates the given database table.
	 * 
	 * @param table
	 *        Inherited subtype of {@link Config#getTable()}.
	 */
	protected abstract void updateDatabaseTable(MigrationContext context, Log log, PooledConnection connection,
			MOClass table);

	/**
	 * Collect all {@link MetaObject} to update: Not only the configured type, but also all subtypes
	 * need to get the new columns.
	 */
	private Set<MOClass> getTablesForUpdate(MORepository allTypes) {
		MOClass commonSuperType = (MOClass) allTypes.getMetaObject(getConfig().getTable());
		Set<MOClass> forUpdate = new LinkedHashSet<>();
		forUpdate.add(commonSuperType);
		while (true) {
			boolean breakLoop = true;
			for (MetaObject clazz : allTypes.getMetaObjects()) {
				if (clazz instanceof MOClass) {
					MOClass superClass = ((MOClass) clazz).getSuperclass();
					if (forUpdate.contains(superClass)) {
						// clazz must be processed
						boolean newlyScheduled = forUpdate.add((MOClass) clazz);
						if (newlyScheduled) {
							breakLoop = false;
						}
					}
				}
			}
			if (breakLoop) {
				break;
			}
		}
		return forUpdate;
	}

	private void updateStoredSchema(Log log, PooledConnection connection, SchemaConfiguration currentSchema) {
		try {
			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, currentSchema);
			log.info("Updated persistent application schema.");
		} catch (SQLException ex) {
			log.error("Unable to update persistent application schema.", ex);
		}
	}

	private void addNewAttribute(Log log, MetaObjectConfig tableDefinition) {
		AttributeConfig newAttribute = TypedConfiguration.copy(getConfig().getAttribute());
		String beforeAttr = getConfig().getBefore();
		List<AttributeConfig> attributes = tableDefinition.getAttributes();
		if (beforeAttr != null) {
			int beforeIndex = -1;
			for (int i = 0; i < attributes.size(); i++) {
				if (beforeAttr.equals(attributes.get(i).getAttributeName())) {
					beforeIndex = i;
					break;
				}
			}
			if (beforeIndex == -1) {
				log.info("No attribute '" + beforeAttr + "' found in '" + tableDefinition.getObjectName() + "'. Add '"
						+ newAttribute.getAttributeName() + "' at the end.",
					Protocol.WARN);
				beforeIndex = attributes.size();
			}
			attributes.add(beforeIndex, newAttribute);
		} else {
			attributes.add(newAttribute);
		}
	}

	private InstantiationContext instantiationContext() {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
	}

	/**
	 * Creates a new column for the given {@link DBAttribute}.
	 */
	protected void createColumn(Log log, SQLProcessor processor, String tableName, DBAttribute attribute,
			Object defaultValue) {
		if (attribute == null) {
			return;
		}

		DBColumn column = SchemaSetup.createColumn(attribute);
		try {
			processor.execute(
				alterTable(
					table(tableName),
					addColumn(column, defaultValue)));
			log.info("Created column '" + column.getDBName() + "' in table '" + tableName + "'.");
		} catch (SQLException ex) {
			log.error("Unable to create column '" + column.getDBName() + "' in table '" + tableName + "'.", ex);
		}
	}

}
