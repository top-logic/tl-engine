/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.data;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.knowledge.service.BinaryStorageSettings;
import com.top_logic.knowledge.service.DefaultBinaryStoragePolicy;
import com.top_logic.knowledge.service.DynamicBinaryStoragePolicy;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.SerializingTransformer;

/**
 * {@link FlexDataManagerFactory} that creates {@link EncryptedFlexDataManager}s.
 *
 * <p>
 * The content of binary values of dynamic attributes is always stored inline in the database,
 * since the values are encrypted when they are stored. The configured binary kind, store,
 * threshold and storage policy are not used.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EncryptedFlexDataManagerFactory extends FlexDataManagerFactory {

	private Config _config;


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

	/**
	 * Settings of the kind {@link BinaryAttributeKind#INLINE}, since all binary content is stored
	 * encrypted in the database.
	 */
	@Override
	public BinaryStorageSettings getBinaryDefaults() {
		return super.getBinaryDefaults().withKind(BinaryAttributeKind.INLINE);
	}

	/**
	 * The {@link DefaultBinaryStoragePolicy}, since all binary content is stored encrypted in the
	 * database.
	 */
	@Override
	public DynamicBinaryStoragePolicy getBinaryStoragePolicy() {
		return DefaultBinaryStoragePolicy.INSTANCE;
	}

	@Override
	public FlexDataManager newFlexDataManager(ConnectionPool connectionPool, MOKnowledgeItemImpl dataType,
			MOKnowledgeItemImpl binaryDataType) {
		FlexDataManager impl = super.newFlexDataManager(connectionPool, dataType, binaryDataType);
		return new EncryptedFlexDataManager(_config.getTransformer(), impl);
	}

}
