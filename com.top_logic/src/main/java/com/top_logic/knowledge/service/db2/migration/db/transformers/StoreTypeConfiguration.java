/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.sql.SQLException;
import java.util.Map;

import com.google.inject.Inject;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.schema.properties.DBPropertiesSchema;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;
import com.top_logic.knowledge.service.migration.MigrationConfig;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link RowTransformer} (and {@link MigrationProcessor}) updating the stored type system
 * configuration in the {@link KnowledgeBase}.
 * 
 * <p>
 * Note: This {@link MigrationProcessor} must not be explicitly configured. It is automatically
 * executed after a potential replay migration, if some of the migrations have the
 * {@link MigrationConfig#getSchemaUpdate()} flag set.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StoreTypeConfiguration extends AbstractConfiguredInstance<StoreTypeConfiguration.Config>
		implements RowTransformer, MigrationProcessor {

	/**
	 * Typed configuration interface definition for {@link StoreTypeConfiguration}.
	 */
	public interface Config extends PolymorphicConfiguration<StoreTypeConfiguration> {
		// configuration interface definition
	}

	private String _kbName;

	private Log _log = new LogProtocol(StoreTypeConfiguration.class);

	private SchemaSetup _schemaSetup = null;

	private String _schemaSetupProperty;

	/**
	 * Create a {@link StoreTypeConfiguration}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public StoreTypeConfiguration(InstantiationContext context, Config config) {
		super(context, config);

		_kbName = PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME;
		_schemaSetupProperty = KBSchemaUtil.dbPropertiesSchemaSetup(_kbName);
	}

	/**
	 * Sets the configuration of the target {@link KnowledgeBase} to migrate to.
	 */
	@Inject
	public void initKnowledgeBaseConfiguration(KnowledgeBaseConfiguration kbConfig) {
		_kbName = kbConfig.getName();
		_schemaSetupProperty = KBSchemaUtil.dbPropertiesSchemaSetup(_kbName);
		_schemaSetup = KBUtils.getSchemaConfigResolved(kbConfig);
	}

	/**
	 * Sets the {@link Log} to write messages to.
	 */
	@Inject
	public void initLog(Log log) {
		_log = log;
	}

	@Override
	public void transform(RowValue row, RowWriter out) {
		modifiyRow(row);
		out.write(row);
	}

	private void modifiyRow(RowValue row) {
		String tableName = row.getTable().getDBMapping().getDBName();
		if (!DBPropertiesSchema.TABLE_NAME.equals(tableName)) {
			return;
		}
		Map<String, Object> values = row.getValues();
		if (!DBProperties.GLOBAL_PROPERTY.equals(values.get(DBPropertiesSchema.NODE_COLUMN_NAME))) {
			return;
		}
		if (!_schemaSetupProperty.equals(values.get(DBPropertiesSchema.PROP_KEY_COLUMN_NAME))) {
			return;
		}

		SchemaConfiguration schema = loadKBSchema();
		if (schema == null) {
			return;
		}
		row.getValues().put(DBPropertiesSchema.PROP_VALUE_COLUMN_NAME, KBSchemaUtil.serializeSchema(schema));
	}

	private SchemaConfiguration loadKBSchema() {
		SchemaSetup setup = getSetup();
		if (setup == null) {
			// setup can not be found error is logged.
			return null;
		}
		return setup.getConfig();
	}

	private SchemaSetup getSetup() {
		if (_schemaSetup != null) {
			return _schemaSetup;
		}
		KnowledgeBaseFactoryConfig kbFactoryConfig;
		try {
			kbFactoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
				.getServiceConfiguration(KnowledgeBaseFactory.class);
		} catch (ConfigurationException ex) {
			_log.error("Unable to get configuration for " + KnowledgeBaseFactory.class.getSimpleName() + ".", ex);
			return null;
		}
		KnowledgeBaseConfiguration kbConfiguration = kbFactoryConfig.getKnowledgeBases().get(_kbName);
		if (kbConfiguration == null) {
			_log.error(
				"No configuration found for " + KnowledgeBase.class.getSimpleName() + " with name " + _kbName + ".");
			return null;
		}
		return KBUtils.getSchemaConfigResolved(kbConfiguration);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		initLog(log);

		SchemaConfiguration schema = loadKBSchema();
		if (schema == null) {
			// Error already logged
			return;
		}

		try {
			KBSchemaUtil.storeSchema(connection, _kbName, schema);
		} catch (SQLException ex) {
			log.error("Unable to store KB schema: " + schema, ex);
		}
	}

}

