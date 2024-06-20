/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model.refactor;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.processors.AddMOAttributeProcessor;

/**
 * {@link MigrationProcessor} renaming a table column or updating its properties.
 */
public class AlterColumnProcessor extends AbstractConfiguredInstance<AlterColumnProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link AlterColumnProcessor}.
	 */
	@TagName("alter-column")
	public interface Config<I extends AlterColumnProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * Logical name of the table to copy from.
		 * 
		 * <p>
		 * The name expected is the logical name of the table as defined in the <em>TopLogic</em>
		 * meta schema.
		 * </p>
		 * 
		 * @see MOClass#getName()
		 */
		@Name("table")
		String getTable();

		/**
		 * The old name of the column.
		 * 
		 * <p>
		 * The name expected is the logical name of the column as defined in the <em>TopLogic</em>
		 * meta schema. If the {@link #getTable()} does not define a column of the given name, the
		 * flex-data for the corresponding table is migrated.
		 * </p>
		 * 
		 * @see MOAttribute#getName()
		 */
		@Name("column")
		String getColumn();

		/**
		 * The new logical name of the column.
		 * 
		 * @see MOAttribute#getName()
		 */
		@Nullable
		@Name("new-name")
		String getNewName();

		/**
		 * The new concrete name of the column used in the database.
		 * 
		 * @see DBAttribute#getDBName()
		 */
		@Nullable
		@Name("new-db-name")
		String getNewDbName();

		/**
		 * The new mandatory mode.
		 * 
		 * @see MOAttribute#isMandatory()
		 */
		@Name("new-mandatory")
		Boolean getNewMandatory();

		/**
		 * The new column type.
		 * 
		 * @see MOAttribute#getMetaObject()
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
		MetaObject newType = config.getNewType();
		String newDbName = config.getNewDbName();
		Boolean newMandatory = config.getNewMandatory();
		log.info("Altering column '" + columnName + "' of table '" + tableName + "'.");

		MORepository repository = context.getPersistentRepository();
		MOStructure abstractTable = (MOStructure) repository.getMetaObject(tableName);

		try {
			List<String> tablesWithDeclaredAttribute = new ArrayList<>();
			for (MetaObject type : repository.getMetaObjects()) {
				if (!(type instanceof MOStructure)) {
					continue;
				}
				MOStructure concreteTable = (MOStructure) type;
				if (concreteTable instanceof MOClass && ((MOClass) concreteTable).isAbstract()) {
					continue;
				}
				if (!concreteTable.isSubtypeOf(abstractTable)) {
					continue;
				}

				MOAttribute column = concreteTable.getAttributeOrNull(columnName);
				if (column == null) {
					if (newName != null) {
						adjustFlexData(log, connection, type.getName(), columnName, newName);
					}
				} else {
					adjustTable(log, connection, concreteTable, column, newName, newType, newDbName, newMandatory);
				}

				if (concreteTable.hasDeclaredAttribute(columnName)) {
					tablesWithDeclaredAttribute.add(concreteTable.getName());
				}
			}

			adjustStoredSchema(context, log, connection, tablesWithDeclaredAttribute, columnName, newName, newType,
				newMandatory);
		} catch (SQLException ex) {
			log.error("Failed to alter column '" + columnName + "' of table '" + tableName + "': " + ex.getMessage(),
				ex);
		}
	}

	private void adjustFlexData(Log log, PooledConnection connection, String tableName, String columnName,
			String newName) throws SQLException {
		log.info("Renaming flex attribute '" + columnName + "' of table '" + tableName + "' to '"
			+ newName + "'.");

		CompiledStatement sql = query(
			update(
				table(AbstractFlexDataManager.FLEX_DATA_DB_NAME),
				and(
					eqSQL(column(AbstractFlexDataManager.TYPE_DBNAME),
						literal(DBType.STRING, tableName)),
					eqSQL(column(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
						literal(DBType.STRING, columnName))),
				columnNames(AbstractFlexDataManager.ATTRIBUTE_DBNAME),
				expressions(literal(DBType.STRING, newName)))).toSql(connection.getSQLDialect());

		int cnt = sql.executeUpdate(connection);

		log.info(
			"Renamed " + cnt + " flex attributes '" + columnName + "' of table '" + tableName + "' to '"
				+ newName + "'.");
	}

	private void adjustTable(Log log, PooledConnection connection, MOStructure table, MOAttribute column,
			String newName, MetaObject newType, String newDbName, Boolean newMandatory) throws SQLException {
		MOAttribute newColumn = column.copy(newName != null ? newName : column.getName(),
			newType != null ? newType : column.getMetaObject());
		if (newDbName != null) {
			((DBAttribute) newColumn).setDBName(newDbName);
		} else if (newName != null) {
			((DBAttribute) newColumn).setDBName(null);
		}
		if (newMandatory != null) {
			newColumn.setMandatory(newMandatory.booleanValue());
		}

		for (int n = 0, cnt = column.getDbMapping().length; n < cnt; n++) {
			DBAttribute oldDbColumn = column.getDbMapping()[n];
			DBAttribute newDbColumn = newColumn.getDbMapping()[n];

			if (!oldDbColumn.getDBName().equals(newDbColumn.getDBName())) {
				log.info("Alter column '" + oldDbColumn.getDBName() + "' of table '" + table.getName() + "': Rename to "
					+ newDbColumn.getDBName());
				CompiledStatement sql = query(
					modifyColumnName(table(table.getDBMapping().getDBName()), oldDbColumn.getDBName(),
						oldDbColumn.getSQLType(), newDbColumn.getDBName())).toSql(connection.getSQLDialect());

				sql.executeUpdate(connection);
			}
			if (!oldDbColumn.getSQLType().equals(newDbColumn.getSQLType())) {
				log.info(
					"Alter column '" + oldDbColumn.getDBName() + "' of table '" + table.getName() + "': Change type to "
						+ newDbColumn.getSQLType());
				CompiledStatement sql = query(
					modifyColumnType(table(table.getDBMapping().getDBName()), oldDbColumn.getDBName(),
						newDbColumn.getSQLType()))
							.toSql(connection.getSQLDialect());

				sql.executeUpdate(connection);
			}
			if (oldDbColumn.isSQLNotNull() != newDbColumn.isSQLNotNull()) {
				log.info("Alter column '" + oldDbColumn.getDBName() + "' of table '" + table.getName()
					+ "': Change mandatory to " + newDbColumn.isSQLNotNull());
				CompiledStatement sql = query(
					modifyColumnMandatory(table(table.getDBMapping().getDBName()), oldDbColumn.getDBName(),
						oldDbColumn.getSQLType(), newDbColumn.isSQLNotNull())).toSql(connection.getSQLDialect());

				sql.executeUpdate(connection);
			}
		}
	}

	private void adjustStoredSchema(MigrationContext context, Log log, PooledConnection connection, List<String> tables,
			String columnName,
			String newName, MetaObject newType, Boolean newMandatory) {
		SchemaConfiguration currentSchema = context.getPersistentSchema();
		Map<String, MetaObjectName> currentTypes = currentSchema.getMetaObjects().getTypes();

		for (String tableName : tables) {
			MetaObjectName mo = currentTypes.get(tableName);
			if (mo == null) {
				log.info("Table '" + tableName + "' does not exist in persistent schema.", Log.WARN);
				return;
			}
			if (!(mo instanceof MetaObjectConfig)) {
				log.info("Table '" + tableName + "' is not a table definition in persistent schema.",
					Log.WARN);
				return;
			}
			MetaObjectConfig tableDefinition = (MetaObjectConfig) mo;

			Optional<AttributeConfig> attrHandle =
				tableDefinition.getAttributes().stream().filter(a -> a.getAttributeName().equals(columnName)).findAny();
			if (!attrHandle.isPresent()) {
				log.info(
					"Table '" + tableName + "' has no column '" + columnName + "' in persistent schema.",
					Log.WARN);
				return;
			}

			log.info(
				"Updating persistent schema of column '" + columnName + "' in table '" + tableName + "'.");

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
					log.info("Column '" + columnName + "' in table '" + tableName + "' is not primitive.",
						Log.WARN);
				}
			}
		}

		AddMOAttributeProcessor.updateStoredSchema(log, connection, currentSchema);
	}

}
