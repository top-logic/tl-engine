/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.sql.SQLException;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.properties.DBPropertiesSchema;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link RowTransformer} (and {@link MigrationProcessor}) that deletes the stored application model
 * from the {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DeleteStoredTLModel extends AbstractConfiguredInstance<DeleteStoredTLModel.Config>
		implements RowTransformer, MigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link DeleteStoredTLModel}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@TagName("delete-stored-model")
	public interface Config extends PolymorphicConfiguration<DeleteStoredTLModel> {
		// configuration interface definition
	}

	/**
	 * Creates a {@link DeleteStoredTLModel}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public DeleteStoredTLModel(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
				DynamicModelService.APPLICATION_MODEL_PROPERTY, null);
		} catch (SQLException ex) {
			log.error("Unable to set property " + DynamicModelService.APPLICATION_MODEL_PROPERTY, ex);
		}
	}

	@Override
	public void transform(RowValue row, RowWriter out) {
		String tableName = row.getTable().getDBMapping().getDBName();
		if (!DBPropertiesSchema.TABLE_NAME.equals(tableName)) {
			out.write(row);
			return;
		}
		Map<String, Object> values = row.getValues();
		if (DBProperties.GLOBAL_PROPERTY.equals(values.get(DBPropertiesSchema.NODE_COLUMN_NAME))
			&& DynamicModelService.APPLICATION_MODEL_PROPERTY
				.equals(values.get(DBPropertiesSchema.PROP_KEY_COLUMN_NAME))) {
			// skip application model property.
			return;
		}
		out.write(row);
	}


}
