/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.AddMOAttributeProcessor;

/**
 * {@link MigrationProcessor} renaming a table column.
 */
public class AlterColumnProcessor extends AbstractConfiguredInstance<AlterColumnProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link AlterColumnProcessor}.
	 */
	@TagName("alter-column")
	public interface Config<I extends AlterColumnProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Name of the table to copy from.
		 */
		@Name("table")
		String getTable();

		/**
		 * The old name of the column.
		 */
		@Name("column")
		String getColumn();

		/**
		 * The new name of the column.
		 */
		@Nullable
		@Name("new-name")
		String getNewName();

		/**
		 * The new name of the column.
		 */
		@Nullable
		@Name("new-db-name")
		String getNewDbName();

		/**
		 * The new mandatory mode.
		 */
		@Name("new-mandatory")
		Boolean getNewMandatory();

		/**
		 * The new column type.
		 */
		@Nullable
		@Name("new-type")
		MetaObject getNewType();
	}

	/**
	 * Creates a {@link AlterColumnProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AlterColumnProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Config<?> config = getConfig();
		String tableName = config.getTable();
		String columnName = config.getColumn();
		String newName = config.getNewName();
		log.info("Altering column '" + columnName + "' of table '" + tableName + "'.");

		MORepository repository = context.getPersistentRepository();
		MOStructure table = (MOStructure) repository.getMetaObject(tableName);
		MOAttribute column = table.getAttribute(columnName);
		MetaObject newType = config.getNewType();

		MOAttribute newColumn = column.copy(newName != null ? newName : column.getName(),
			newType != null ? newType : column.getMetaObject());
		String newDbName = config.getNewDbName();
		if (newDbName != null) {
			((DBAttribute) newColumn).setDBName(newDbName);
		} else if (newName != null) {
			((DBAttribute) newColumn).setDBName(null);
		}
		Boolean newMandatory = config.getNewMandatory();
		if (newMandatory != null) {
			newColumn.setMandatory(config.getNewMandatory().booleanValue());
		}

		try {
			for (int n = 0, cnt = column.getDbMapping().length; n < cnt; n++) {
				DBAttribute oldDbColumn = column.getDbMapping()[n];
				DBAttribute newDbColumn = newColumn.getDbMapping()[n];

				if (!oldDbColumn.getDBName().equals(newDbColumn.getDBName())) {
					log.info("Alter column '" + oldDbColumn.getDBName() + "' of table '" + tableName + "': Rename to "
						+ newDbColumn.getDBName());
					CompiledStatement sql = query(
						modifyColumnName(table(table.getDBMapping().getDBName()), oldDbColumn.getDBName(),
							oldDbColumn.getSQLType(), newDbColumn.getDBName())).toSql(connection.getSQLDialect());

					sql.executeUpdate(connection);
				}
				if (!oldDbColumn.getSQLType().equals(newDbColumn.getSQLType())) {
					log.info(
						"Alter column '" + oldDbColumn.getDBName() + "' of table '" + tableName + "': Change type to "
							+ newDbColumn.getSQLType());
					CompiledStatement sql = query(
						modifyColumnType(table(table.getDBMapping().getDBName()), oldDbColumn.getDBName(),
							newDbColumn.getSQLType()))
							.toSql(connection.getSQLDialect());

					sql.executeUpdate(connection);
				}
				if (oldDbColumn.isSQLNotNull() != newDbColumn.isSQLNotNull()) {
					log.info("Alter column '" + oldDbColumn.getDBName() + "' of table '" + tableName
						+ "': Change mandatory to " + newDbColumn.isSQLNotNull());
					CompiledStatement sql = query(
						modifyColumnMandatory(table(table.getDBMapping().getDBName()), oldDbColumn.getDBName(),
							oldDbColumn.getSQLType(), newDbColumn.isSQLNotNull())).toSql(connection.getSQLDialect());

					sql.executeUpdate(connection);
				}
			}

			adjustStoredSchema(context, log, connection, columnName, newName, newType, newMandatory);
		} catch (SQLException ex) {
			log.error("Failed to alter column '" + columnName + "' of table '" + tableName + "': " + ex.getMessage(),
				ex);
		}
	}

	private void adjustStoredSchema(MigrationContext context, Log log, PooledConnection connection, String columnName,
			String newName, MetaObject newType, Boolean newMandatory) {
		SchemaConfiguration currentSchema = context.getPersistentSchema();
		Map<String, MetaObjectName> currentTypes = currentSchema.getMetaObjects().getTypes();
		MetaObjectName mo = currentTypes.get(getConfig().getTable());
		if (mo == null) {
			log.info("Table '" + getConfig().getTable() + "' does not exist in persistent schema.", Log.WARN);
			return;
		}
		if (!(mo instanceof MetaObjectConfig)) {
			log.info("Table '" + getConfig().getTable() + "' is not a table definition in persistent schema.",
				Log.WARN);
			return;
		}
		MetaObjectConfig tableDefinition = (MetaObjectConfig) mo;

		Optional<AttributeConfig> attrHandle =
			tableDefinition.getAttributes().stream().filter(a -> a.getAttributeName().equals(columnName)).findAny();
		if (!attrHandle.isPresent()) {
			log.info(
				"Table '" + getConfig().getTable() + "' has no column '" + columnName + "' in persistent schema.",
				Log.WARN);
			return;
		}

		AttributeConfig attrConfig = attrHandle.get();
		if (newName != null) {
			attrConfig.setAttributeName(newName);
		}
		if (newMandatory != null) {
			attrConfig.setMandatory(newMandatory);
		}
		if (newType != null) {
			if (attrConfig instanceof PrimitiveAttributeConfig) {
				((PrimitiveAttributeConfig) attrConfig).setValueType(newType);
			} else {
				log.info("Column '" + columnName + "' in table '" + getConfig().getTable() + "' is not primitive.",
				Log.WARN);
			}
		}

		AddMOAttributeProcessor.updateStoredSchema(log, connection, currentSchema);
	}

}
