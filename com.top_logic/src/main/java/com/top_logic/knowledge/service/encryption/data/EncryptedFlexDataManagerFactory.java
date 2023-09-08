/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.data;

import java.util.Properties;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.SerializingTransformer;

/**
 * {@link FlexDataManagerFactory} that creates {@link EncryptedFlexDataManager}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EncryptedFlexDataManagerFactory extends FlexDataManagerFactory {

	private static final String SERIALIZER_PREFIX = "dataManager.";
	private Properties _subConfig;

	private Config _config;

	/**
	 * Creates a {@link EncryptedFlexDataManagerFactory} from configuration.
	 */
	public EncryptedFlexDataManagerFactory(Properties config) {
		_subConfig = new Properties();
		for (Object key : config.keySet()) {
			String property = (String) key;
			if (property.startsWith(SERIALIZER_PREFIX)) {
				_subConfig.setProperty(property.substring(SERIALIZER_PREFIX.length()), config.getProperty(property));
			}
		}
	}

	/**
	 * Configuration of a {@link FlexDataManagerFactory}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FlexDataManagerFactory.Config {

		/**
		 * Configuration for the transformer
		 */
		SerializingTransformer.Config getTransformer();

	}

	/**
	 * Creates a {@link EncryptedFlexDataManagerFactory} from configuration.
	 */
	public EncryptedFlexDataManagerFactory(InstantiationContext context, Config config) {
		super(context, config);
		this._config = config;
	}

	@Override
	public FlexDataManager newFlexDataManager(ConnectionPool connectionPool, MOKnowledgeItemImpl dataType) {
		FlexDataManager impl = super.newFlexDataManager(connectionPool, dataType);
		return new EncryptedFlexDataManager(_config.getTransformer(), impl);
	}

}
