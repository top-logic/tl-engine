/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBTypeRepository;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} aligning the physical layout of the existing tables with the layout
 * options of the configured schema.
 *
 * <p>
 * The layout options of a {@link SchemaConfiguration} (see {@link #LAYOUT_OPTIONS}) determine how
 * the tables are built from the type definitions. The processor compares the layout options of the
 * schema stored in the database (see {@link MigrationContext#getPersistentSchema()}) with the ones
 * of the application configuration (see {@link MigrationContext#getApplicationSchema()}). If they
 * differ, the table definitions of all types of the stored schema are computed once with the
 * stored and once with the configured layout options, and the primary keys and indexes of the
 * existing tables are adapted to the configured layout, see
 * {@link DBSchemaUtils#alignKeys(Log, PooledConnection, DBTable, DBTable)}. Afterwards, the stored
 * schema is updated with the configured layout options.
 * </p>
 *
 * <p>
 * Other differences between the stored and the configured schema (e.g. added types or changed
 * index declarations) are neither applied to the tables nor to the stored schema.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AlignTableLayoutProcessor extends AbstractConfiguredInstance<AlignTableLayoutProcessor.Config>
		implements MigrationProcessor {

	/**
	 * The properties of a {@link SchemaConfiguration} that determine the layout of the tables
	 * without changing the types.
	 */
	public static final List<String> LAYOUT_OPTIONS = List.of(SchemaConfiguration.BRANCH_LAST_ATTRIBUTE);

	/**
	 * Configuration options of {@link AlignTableLayoutProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<AlignTableLayoutProcessor> {

		/**
		 * Tag name of {@link Config} in a migration script.
		 */
		String TAG_NAME = "align-table-layout";

	}

	/**
	 * Creates a {@link AlignTableLayoutProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AlignTableLayoutProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Whether the given schemas differ in one of their {@link #LAYOUT_OPTIONS}.
	 *
	 * @param stored
	 *        The schema the tables of the database were built with.
	 * @param configured
	 *        The schema of the application configuration.
	 */
	public static boolean hasLayoutChange(SchemaConfiguration stored, SchemaConfiguration configured) {
		for (String option : LAYOUT_OPTIONS) {
			if (!Objects.equals(value(stored, option), value(configured, option))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a copy of the given stored schema with the {@link #LAYOUT_OPTIONS} of the given
	 * configured schema.
	 *
	 * @param stored
	 *        The schema the tables of the database were built with.
	 * @param configured
	 *        The schema of the application configuration.
	 */
	public static SchemaConfiguration withConfiguredLayout(SchemaConfiguration stored,
			SchemaConfiguration configured) {
		SchemaConfiguration result = TypedConfiguration.copy(stored);
		for (String option : LAYOUT_OPTIONS) {
			PropertyDescriptor property = result.descriptor().getProperty(option);
			result.update(property, value(configured, option));
		}
		return result;
	}

	private static Object value(SchemaConfiguration schema, String option) {
		return schema.value(schema.descriptor().getProperty(option));
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration stored = context.getPersistentSchema();
		if (stored == null) {
			log.info("No stored schema, table layout not aligned.");
			return;
		}
		SchemaConfiguration configured = context.getApplicationSchema();
		if (!hasLayoutChange(stored, configured)) {
			return;
		}

		SchemaConfiguration target = withConfiguredLayout(stored, configured);
		log.info("Aligning the table layout with the configured layout options: " + describeLayout(target)
			+ " (stored: " + describeLayout(stored) + ").");
		try {
			boolean versioning = !context.getKnowledgeBaseConfiguration().getDisableVersioning();
			DBSchema currentTables = buildTables(connection, stored, versioning);
			DBSchema targetTables = buildTables(connection, target, versioning);

			int changed = 0;
			for (DBTable targetTable : targetTables.getTables()) {
				DBTable currentTable = currentTables.getTable(targetTable.getName());
				if (currentTable == null || !DBSchemaUtils.exists(connection, targetTable)) {
					continue;
				}
				if (DBSchemaUtils.alignKeys(log, connection, currentTable, targetTable)) {
					changed++;
				}
			}

			KBSchemaUtil.storeSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME, target);
			log.info("Aligned the layout of " + changed + " tables.");
		} catch (SQLException | ConfigurationException | DataObjectException ex) {
			log.error("Failed to align the table layout with the configured layout options.", ex);
		}
	}

	private static DBSchema buildTables(PooledConnection connection, SchemaConfiguration schema,
			boolean versioning) throws SQLException, ConfigurationException, DataObjectException {
		SchemaSetup setup = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(schema);
		MORepository types = DBTypeRepository.newRepository(connection.getSQLDialect(), setup, versioning);
		DBSchema result = DBSchemaUtils.newDBSchema();
		SchemaSetup.addTables(result, types);
		return result;
	}

	private static String describeLayout(SchemaConfiguration schema) {
		return LAYOUT_OPTIONS.stream().map(option -> option + "=" + value(schema, option))
			.collect(Collectors.joining(", "));
	}

}
