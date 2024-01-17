/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.TypedAnnotationContainer;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
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

/**
 * Context for {@link MigrationProcessor}s
 */
public class MigrationContext extends TypedAnnotationContainer {

	private boolean _branchSupport;

	private SchemaConfiguration _persistentSchema;

	private KnowledgeBaseFactoryConfig _factoryConfig;

	private SchemaSetup _applicationSetup;

	private SchemaConfiguration _applicationSchema;

	private KnowledgeBaseConfiguration _kbConfig;

	/**
	 * Creates a {@link MigrationContext}.
	 */
	public MigrationContext(PooledConnection connection) {
		_persistentSchema = KBSchemaUtil.loadSchema(connection, PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);

		try {
			_factoryConfig = (KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
				.getServiceConfiguration(KnowledgeBaseFactory.class);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		_kbConfig = _factoryConfig.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
		_applicationSetup = KBUtils.getSchemaConfigResolved(_kbConfig);
		_applicationSchema = _applicationSetup.getConfig();
	}

	/**
	 * TODO
	 */
	public SchemaConfiguration getPersistentSchema() {
		return _persistentSchema;
	}

	/**
	 * TODO
	 */
	public KnowledgeBaseFactoryConfig getFactoryConfig() {
		return _factoryConfig;
	}

	/**
	 * TODO
	 */
	public KnowledgeBaseConfiguration getKbConfig() {
		return _kbConfig;
	}

	/**
	 * TODO
	 */
	public SchemaSetup getApplicationSetup() {
		return _applicationSetup;
	}

	/**
	 * TODO
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
		return _applicationSchema.hasMultipleBranches();
	}

}
