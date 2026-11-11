/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.top_logic.basic.db.sql.SQLModifyColumn;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.schema.config.AttributeConfig;
import com.top_logic.dob.schema.config.DBColumnType;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.PrimitiveAttributeConfig;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.processors.AddMOAttributeProcessor;

/**
 * {@link MigrationProcessor} renaming a table column or updating its properties.
 *
 * <p>
 * Changing the type of a column drops the planner statistics of the column on some databases (e.g.
 * {@code PostgreSQL}). Therefore, the tables whose column type is changed are
 * {@link MigrationUtil#analyzeTables(Log, PooledConnection, java.util.Collection) analyzed} again
 * after all modifications.
 * </p>
 */
public class AlterColumnProcessor extends AbstractConfiguredInstance<AlterColumnProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link AlterColumnProcessor}.
	 */
	@TagName("alter-column")
	public interface Config<I extends AlterColumnProcessor> extends PolymorphicConfiguration<I>, DBColumnType {
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
		 * @see #getTable()
		 */
		void setTable(String value);

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
		 * @see #getColumn()
		 */
		void setColumn(String value);

		/**
		 * The new logical name of the column.
		 * 
		 * @see MOAttribute#getName()
		 */
		@Nullable
		@Name("new-name")
		String getNewName();

		/**
		 * @see #getNewName()
		 */
		void setNewName(String value);

		/**
		 * The new concrete name of the column used in the database.
		 * 
		 * @see DBAttribute#getDBName()
		 */
		@Nullable
		@Name("new-db-name")
		String getNewDbName();

		/**
		 * @see #getNewDbName()
		 */
		void setNewDbName(String value);

		/**
		 * The new mandatory mode.
		 * 
		 * @see MOAttribute#isMandatory()
		 */
		@Name("new-mandatory")
		Boolean getNewMandatory();

		/**
		 * @see #getNewMandatory()
		 */
		void setNewMandatory(Boolean value);

		/**
		 * The new column type.
		 * 
		 * @see MOAttribute#getMetaObject()
		 */
		@Nullable
		@Name("new-type")
		MetaObject getNewType();

		/**
		 * @see #getNewType()
		 */
		void setNewType(MetaObject value);
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
			Set<String> retypedTables = new LinkedHashSet<>();
			for (MetaObject type : repository.getMetaObjects()) {
				if (!(type instanceof MOStructure)) {
					continue;
				}
				MOStructure subTable = (MOStructure) type;
				if (!subTable.isSubtypeOf(abstractTable)) {
					continue;
				}

				// The stored schema must also be updated for an abstract table declaring the column,
				// e.g. when the altered table is abstract and its concrete sub tables only inherit
				// the column.
				if (subTable.hasDeclaredAttribute(columnName)) {
					tablesWithDeclaredAttribute.add(subTable.getName());
				}

				if (subTable instanceof MOClass && ((MOClass) subTable).isAbstract()) {
					// An abstract table has no database table.
					continue;
				}

				MOAttribute column = subTable.getAttributeOrNull(columnName);
				if (column == null) {
					if (newName != null) {
						adjustFlexData(log, connection, type.getName(), columnName, newName);
					}
				} else {
					if (adjustTable(log, connection, subTable, column, newName, newType, newDbName, newMandatory)) {
						retypedTables.add(subTable.getDBMapping().getDBName());
					}
				}
			}

			adjustStoredSchema(context, log, connection, tablesWithDeclaredAttribute, columnName, newName, newType,
				newMandatory);

			MigrationUtil.analyzeTables(log, connection, retypedTables);
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

	/**
	 * Applies the configured modifications to the given column of the given table.
	 *
	 * @return Whether the type of a database column of the given table was changed.
	 */
	private boolean adjustTable(Log log, PooledConnection connection, MOStructure table, MOAttribute column,
			String newName, MetaObject newType, String newDbName, Boolean newMandatory) throws SQLException {
		boolean typeChanged = false;
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

		Config<?> config = getConfig();
		DBType newDbType = config.getDBType();
		if (newDbType != null) {
			((MOAttributeImpl) newColumn).setSQLType(newDbType);
		}
		Integer newDbSize = config.getDBSize();
		if (newDbSize != null) {
			((MOAttributeImpl) newColumn).setSQLSize(newDbSize);
		}
		Integer newDbPrecision = config.getDBPrecision();
		if (newDbPrecision != null) {
			((MOAttributeImpl) newColumn).setSQLPrecision(newDbPrecision);
		}
		Boolean newBinary = config.isBinary();
		if (newBinary != null) {
			((MOAttributeImpl) newColumn).setBinary(newBinary);
		}

		String dbTableName = table.getDBMapping().getDBName();
		for (int n = 0, cnt = column.getDbMapping().length; n < cnt; n++) {
			DBAttribute oldDbColumn = column.getDbMapping()[n];
			DBAttribute newDbColumn = newColumn.getDbMapping()[n];

			// Statements after a rename must address the column by its new name.
			String currentName = oldDbColumn.getDBName();
			if (!currentName.equals(newDbColumn.getDBName())) {
				log.info("Alter column '" + currentName + "' of table '" + table.getName() + "': Rename to "
					+ newDbColumn.getDBName());
				execute(connection, modifyColumnName(table(dbTableName), currentName, newDbColumn.getDBName(),
					oldDbColumn.getSQLType(), oldDbColumn.isSQLNotNull(), oldDbColumn.isBinary(),
					oldDbColumn.getSQLSize(), oldDbColumn.getSQLPrecision()));
				currentName = newDbColumn.getDBName();
			}
			if (!oldDbColumn.getSQLType().equals(newDbColumn.getSQLType()) ||
				oldDbColumn.isBinary() != newDbColumn.isBinary() ||
				oldDbColumn.getSQLSize() != newDbColumn.getSQLSize() ||
				oldDbColumn.getSQLPrecision() != newDbColumn.getSQLPrecision()
			) {
				log.info(
					"Alter column '" + currentName + "' of table '" + table.getName() + "': Change type to "
						+ newDbColumn.getSQLType() + ", binary=" + newDbColumn.isBinary() + ", size="
						+ newDbColumn.getSQLSize() + ", precision=" + newDbColumn.getSQLPrecision());
				// The mandatory mode is changed separately below.
				execute(connection, modifyColumnType(table(dbTableName), currentName, newDbColumn.getSQLType(),
					oldDbColumn.isSQLNotNull(), newDbColumn.isBinary(), newDbColumn.getSQLSize(),
					newDbColumn.getSQLPrecision()));
				typeChanged = true;
			}
			if (oldDbColumn.isSQLNotNull() != newDbColumn.isSQLNotNull()) {
				log.info("Alter column '" + currentName + "' of table '" + table.getName()
					+ "': Change mandatory to " + newDbColumn.isSQLNotNull());
				execute(connection, modifyColumnMandatory(table(dbTableName), currentName,
					newDbColumn.getSQLType(), newDbColumn.isSQLNotNull(), newDbColumn.isBinary(),
					newDbColumn.getSQLSize(), newDbColumn.getSQLPrecision()));
			}
		}
		return typeChanged;
	}

	private static void execute(PooledConnection connection, SQLModifyColumn modification) throws SQLException {
		query(modification).toSql(connection.getSQLDialect()).executeUpdate(connection);
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
				continue;
			}
			if (!(mo instanceof MetaObjectConfig)) {
				log.info("Table '" + tableName + "' is not a table definition in persistent schema.",
					Log.WARN);
				continue;
			}
			MetaObjectConfig tableDefinition = (MetaObjectConfig) mo;

			Optional<AttributeConfig> attrHandle =
				tableDefinition.getAttributes().stream().filter(a -> a.getAttributeName().equals(columnName)).findAny();
			if (!attrHandle.isPresent()) {
				log.info(
					"Table '" + tableName + "' has no column '" + columnName + "' in persistent schema.",
					Log.WARN);
				continue;
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
			// Apply the column type aspects on top of a potential new value type, so that they take
			// precedence (in the same order as in adjustTable(...)).
			if (attrConfig instanceof DBColumnType) {
				DBColumnType columnType = (DBColumnType) attrConfig;
				Config<?> config = getConfig();
				if (config.getDBType() != null) {
					columnType.setDBType(config.getDBType());
				}
				if (config.getDBSize() != null) {
					columnType.setDBSize(config.getDBSize());
				}
				if (config.getDBPrecision() != null) {
					columnType.setDBPrecision(config.getDBPrecision());
				}
				if (config.isBinary() != null) {
					columnType.setBinary(config.isBinary());
				}
			}
		}

		AddMOAttributeProcessor.updateStoredSchema(log, connection, currentSchema);
	}

}
