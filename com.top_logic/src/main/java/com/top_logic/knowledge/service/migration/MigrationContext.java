/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import org.w3c.dom.Document;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.TypedAnnotationContainer;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.format.PrimitiveBooleanFormat;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;

/**
 * Context for {@link MigrationProcessor}s.
 */
public class MigrationContext extends TypedAnnotationContainer {

	private KnowledgeBaseFactoryConfig _factoryConfig;

	private SchemaSetup _applicationSetup;

	private SchemaConfiguration _applicationSchema;

	private KnowledgeBaseConfiguration _kbConfig;

	private PooledConnection _connection;

	private boolean _branchSupport;

	/**
	 * Creates a {@link MigrationContext}.
	 */
	public MigrationContext(PooledConnection connection) {
		_connection = connection;
		try {
			_factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
				.getServiceConfiguration(KnowledgeBaseFactory.class);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		_kbConfig = _factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		_applicationSetup = KBUtils.getSchemaConfigResolved(_kbConfig);
		_applicationSchema = _applicationSetup.getConfig();

		_branchSupport = hasStoredBranchSupport();
	}

	private boolean hasStoredBranchSupport() {
		// Note: The schema can potentially not be loaded as typed configuration due to schema
		// changes.
		String xml = KBSchemaUtil.loadSchemaRaw(_connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		if (xml != null && !xml.isBlank()) {
			Document document = DOMUtil.parse(xml);
			String value = document.getDocumentElement().getAttribute(SchemaConfiguration.MULTIPLE_BRANCHES_ATTRIBUTE);
			if (value == null || value.isBlank()) {
				return SchemaConfiguration.DEFAULT_MULTIPLE_BRANCHES_ATTRIBUTE;
			}

			try {
				return ((Boolean) PrimitiveBooleanFormat.INSTANCE.getValue(
					SchemaConfiguration.MULTIPLE_BRANCHES_ATTRIBUTE,
					value)).booleanValue();
			} catch (ConfigurationException ex) {
				return SchemaConfiguration.DEFAULT_MULTIPLE_BRANCHES_ATTRIBUTE;
			}
		}
		return _applicationSchema.hasMultipleBranches();
	}

	/**
	 * The schema currently stored as baseline in the database.
	 */
	public SchemaConfiguration getPersistentSchema() {
		return KBSchemaUtil.loadSchema(_connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
	}

	/**
	 * The schema read from the application configuration.
	 */
	public SchemaConfiguration getApplicationSchema() {
		return _applicationSchema;
	}

	/**
	 * Whether the {@link KnowledgeBase} to be migrated has branches enabled.
	 * 
	 * @see SchemaConfiguration#hasMultipleBranches()
	 */
	public boolean hasBranchSupport() {
		return _branchSupport;
	}

}
