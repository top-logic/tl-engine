/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.db.model.DBNamedPart;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} writing changed type definitions of the configured schema to the
 * stored schema, if the change does not affect the database tables.
 *
 * <p>
 * For each of the {@link Config#getTypes() given types}, the processor builds the table
 * definitions of all types once from the stored schema and once from the stored schema with the
 * definition of the type replaced by the configured one. The change of the type is adopted, if all
 * table definitions are equal (columns with type, size and nullability, primary keys, indexes). This
 * is the case e.g. for a changed {@link com.top_logic.dob.schema.config.MetaObjectConfig#isVersioned()
 * versioning} of a type or for added, removed or changed computed attributes, which have no
 * database column. Since the tables of all types are compared, a change of an abstract type is
 * adopted only if the tables of its concrete subtypes stay the same.
 * </p>
 *
 * <p>
 * The types are examined one after the other: each type is compared with the stored schema
 * including the changes adopted for the types examined before. The stored schema with all adopted
 * changes is stored once. A type whose change would alter a table is not adopted; the processor
 * reports a warning naming the differing tables, columns and indexes, since the change requires a
 * migration.
 * </p>
 *
 * @see MigrationContext#getPersistentSchema()
 * @see MigrationContext#getApplicationSchema()
 * @see SchemaSetup#createTable(MOClass)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AdoptTypeChangesProcessor extends AbstractConfiguredInstance<AdoptTypeChangesProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link AdoptTypeChangesProcessor}.
	 */
	public interface Config extends PolymorphicConfiguration<AdoptTypeChangesProcessor> {

		/**
		 * Configuration name of {@link #getTypes()}.
		 */
		String TYPES = "types";

		/**
		 * Names of the types whose definition in the configured schema differs from the one in
		 * the stored schema.
		 *
		 * <p>
		 * Types not defined in both schemas or defined equally in both are ignored.
		 * </p>
		 */
		@Name(TYPES)
		@Format(CommaSeparatedStrings.class)
		List<String> getTypes();

		/**
		 * @see #getTypes()
		 */
		void setTypes(List<String> value);

	}

	/**
	 * Creates a {@link AdoptTypeChangesProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AdoptTypeChangesProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration stored = context.getPersistentSchema();
		if (stored == null) {
			log.info("No stored schema, type changes not adopted.");
			return;
		}
		Map<String, MetaObjectName> configuredTypes = context.getApplicationSchema().getMetaObjects().getTypes();

		try {
			boolean versioning = !context.getKnowledgeBaseConfiguration().getDisableVersioning();
			Map<String, DBTable> storedTables = tables(connection, stored, versioning);

			SchemaConfiguration target = stored;
			List<String> adopted = new ArrayList<>();
			for (String typeName : new TreeSet<>(getConfig().getTypes())) {
				MetaObjectName configuredType = configuredTypes.get(typeName);
				MetaObjectName storedType = target.getMetaObjects().getTypes().get(typeName);
				if (configuredType == null || storedType == null || same(storedType, configuredType)) {
					continue;
				}

				SchemaConfiguration candidate = TypedConfiguration.copy(target);
				candidate.getMetaObjects().getTypes().put(typeName, TypedConfiguration.copy(configuredType));

				Map<String, DBTable> candidateTables;
				try {
					candidateTables = tables(connection, candidate, versioning);
				} catch (ConfigurationException | DataObjectException ex) {
					log.info("Changed definition of type '" + typeName
						+ "' is not adopted to the stored schema, since the types cannot be created with it: "
						+ ex.getMessage(), Log.WARN);
					continue;
				}

				List<String> differences = differences(storedTables, candidateTables);
				if (differences.isEmpty()) {
					target = candidate;
					adopted.add(typeName);
				} else {
					log.info("Changed definition of type '" + typeName
						+ "' is not adopted to the stored schema, since it changes the database tables: "
						+ String.join("; ", differences)
						+ ". The change requires a migration that adapts the tables and stores the schema.",
						Log.WARN);
				}
			}

			if (!adopted.isEmpty()) {
				KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, target);
				context.reloadPersistentSchemaState();
				log.info("Adopted the changed definitions of types without table changes to the stored schema: "
					+ String.join(", ", adopted));
			}
		} catch (SQLException | ConfigurationException | DataObjectException ex) {
			log.error("Failed to adopt changed type definitions to the stored schema.", ex);
		}
	}

	/**
	 * The table definitions of the concrete types of the given schema, indexed by the type name.
	 */
	private static Map<String, DBTable> tables(PooledConnection connection, SchemaConfiguration schema,
			boolean versioning) throws SQLException, ConfigurationException, DataObjectException {
		MORepository types = AlignTableLayoutProcessor.createRepository(connection, schema, versioning);
		Map<String, DBTable> result = new TreeMap<>();
		for (MetaObject type : types.getMetaObjects()) {
			MOClass tableType = AlignTableLayoutProcessor.tableType(type);
			if (tableType != null) {
				result.put(tableType.getName(), SchemaSetup.createTable(tableType));
			}
		}
		return result;
	}

	/**
	 * Descriptions of the differences between the given table definitions.
	 *
	 * @return The empty list, if all tables are equal.
	 */
	private static List<String> differences(Map<String, DBTable> stored, Map<String, DBTable> configured) {
		List<String> result = new ArrayList<>();
		for (String typeName : union(stored.keySet(), configured.keySet())) {
			DBTable storedTable = stored.get(typeName);
			DBTable configuredTable = configured.get(typeName);
			if (storedTable == null) {
				result.add("table of type '" + typeName + "' added");
			} else if (configuredTable == null) {
				result.add("table of type '" + typeName + "' removed");
			} else if (!same(storedTable, configuredTable)) {
				result.add("table '" + storedTable.getDBName() + "': "
					+ String.join(", ", differences(storedTable, configuredTable)));
			}
		}
		return result;
	}

	private static List<String> differences(DBTable stored, DBTable configured) {
		List<String> result = new ArrayList<>();
		for (String column : union(names(stored.getColumns()), names(configured.getColumns()))) {
			describe(result, "column '" + column + "'", stored.getColumn(column), configured.getColumn(column));
		}
		describe(result, "primary key", stored.getPrimaryKey(), configured.getPrimaryKey());
		for (String index : union(names(stored.getIndices()), names(configured.getIndices()))) {
			describe(result, "index '" + index + "'", stored.getIndex(index), configured.getIndex(index));
		}
		if (result.isEmpty()) {
			result.add("table properties changed");
		}
		return result;
	}

	private static void describe(List<String> result, String label, ConfigurationItem stored,
			ConfigurationItem configured) {
		if (stored == null) {
			if (configured != null) {
				result.add(label + " added");
			}
		} else if (configured == null) {
			result.add(label + " removed");
		} else if (!same(stored, configured)) {
			result.add(label + " changed");
		}
	}

	private static boolean same(ConfigurationItem stored, ConfigurationItem configured) {
		return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(stored, configured);
	}

	private static Set<String> names(Iterable<? extends DBNamedPart> parts) {
		Set<String> result = new TreeSet<>();
		for (DBNamedPart part : parts) {
			result.add(part.getName());
		}
		return result;
	}

	private static Set<String> union(Set<String> left, Set<String> right) {
		Set<String> result = new TreeSet<>(left);
		result.addAll(right);
		return result;
	}

}
