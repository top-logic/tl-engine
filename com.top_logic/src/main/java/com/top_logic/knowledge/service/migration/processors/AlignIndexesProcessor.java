/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.db2.migration.db.transformers.StoreTypeConfiguration;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} aligning the primary keys and indexes of existing tables with their
 * definition in the schema of the application configuration.
 *
 * <p>
 * For each {@link TableSelection selected} type of the
 * {@link MigrationContext#getApplicationSchema() application schema}, the table definition is
 * computed once from the {@link MigrationContext#getPersistentSchema() schema stored in the
 * database} (the definition the table was created from) and once from the application schema. The
 * existing table is converted: An index whose definition changed (columns, column order,
 * uniqueness or compression) is dropped and created in its target form, an index that exists only
 * in the stored definition is dropped, and an index that exists only in the target definition is
 * created. The index columns are computed by the index strategy of the type, so that the branch
 * and revision columns of a unique index are part of the created index. A table whose indexes already match is not touched.
 * </p>
 *
 * <p>
 * A unique index can only be created, if the table holds no rows violating it. Duplicates must be
 * removed by a preceding processor.
 * </p>
 *
 * <p>
 * The stored schema is not changed. It is replaced by the application schema at the end of the
 * migration, see {@link StoreTypeConfiguration}.
 * </p>
 *
 * @see SchemaSetup#getIndexStrategy(MOClass)
 * @see DBSchemaUtils#alignIndexes(Log, PooledConnection, DBTable, DBTable)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AlignIndexesProcessor extends AbstractConfiguredInstance<AlignIndexesProcessor.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link AlignIndexesProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<AlignIndexesProcessor>, TableSelection {

		/**
		 * Tag name of {@link Config} in a migration script.
		 */
		String TAG_NAME = "align-indexes";

	}

	/**
	 * Creates a {@link AlignIndexesProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AlignIndexesProcessor(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		SchemaConfiguration stored = context.getPersistentSchema();
		if (stored == null) {
			log.info("No stored schema, indexes not aligned.");
			return;
		}
		try {
			boolean versioning = !context.getKnowledgeBaseConfiguration().getDisableVersioning();
			MORepository currentTypes = AlignTableLayoutProcessor.createRepository(connection, stored, versioning);
			MORepository targetTypes =
				AlignTableLayoutProcessor.createRepository(connection, context.getApplicationSchema(), versioning);

			List<MOClass> selected = TableSelection.resolve(log, getConfig(), targetTypes);
			int changed = 0;
			for (MOClass targetType : selected) {
				MetaObject currentType = currentTypes.getTypeOrNull(targetType.getName());
				if (!(currentType instanceof MOClass)) {
					log.info("Type '" + targetType.getName() + "' is not part of the stored schema, indexes not aligned.");
					continue;
				}
				DBTable targetTable = table(targetType);
				if (!DBSchemaUtils.exists(connection, targetTable)) {
					log.info("Table '" + targetTable.getDBName() + "' does not exist, indexes not aligned.");
					continue;
				}
				DBTable currentTable = table((MOClass) currentType);
				if (DBSchemaUtils.alignIndexes(log, connection, currentTable, targetTable)) {
					changed++;
				}
			}
			log.info("Aligned the indexes of " + changed + " of " + selected.size() + " tables.");
		} catch (SQLException | ConfigurationException | DataObjectException ex) {
			log.error("Failed to align indexes: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates the table definition of the given type as part of a {@link DBSchema}.
	 */
	private static DBTable table(MOClass type) {
		DBSchema schema = DBSchemaUtils.newDBSchema();
		DBTable result = SchemaSetup.createTable(type);
		schema.getTables().add(result);
		return result;
	}

}
