/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.FlexVersionedDataManager;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Provides the manager that stores flexible (dynamically typed) object attributes.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Flexible data manager")
public class FlexDataManagerFactory extends ManagedClass {

	/**
	 * Configuration of a {@link FlexDataManagerFactory}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<FlexDataManagerFactory> {

		/** Configuration name of {@link #getBinaryStore()}. */
		String BINARY_STORE = "binary-store";

		/** Configuration name of {@link #getBinaryThreshold()}. */
		String BINARY_THRESHOLD = "binary-threshold";

		/** Configuration name of {@link #getBinaryStoragePolicy()}. */
		String BINARY_STORAGE_POLICY = "binary-storage-policy";

		/** Default value of {@link #getBinaryThreshold()}: 64 KB. */
		long DEFAULT_BINARY_THRESHOLD = 64 * 1024;

		/**
		 * The name of the blob store receiving large binary values of dynamic attributes.
		 *
		 * <p>
		 * Binary values of attributes without a column of their own are stored in a table shared by
		 * all such attributes. Content of at least the size {@link #getBinaryThreshold()} is uploaded to
		 * this blob store, smaller content is stored in the table. If no store is given,
		 * the default store of the blob store service is used. The policy
		 * {@link #getBinaryStoragePolicy()} may choose another store per attribute.
		 * </p>
		 */
		@Name(BINARY_STORE)
		@Nullable
		String getBinaryStore();

		/**
		 * The size from which on binary values of dynamic attributes are stored in the blob store.
		 *
		 * <p>
		 * The size is given in bytes, optionally with a unit, e.g. <code>64KB</code> or
		 * <code>1MB</code>. The policy {@link #getBinaryStoragePolicy()} may choose another threshold
		 * per attribute.
		 * </p>
		 *
		 * @see #getBinaryStore()
		 */
		@Name(BINARY_THRESHOLD)
		@Format(MemorySizeFormat.class)
		@LongDefault(DEFAULT_BINARY_THRESHOLD)
		long getBinaryThreshold();

		/**
		 * The policy choosing blob store and threshold for a binary value of a dynamic attribute.
		 *
		 * <p>
		 * The policy receives the configured {@link #getBinaryStore()} and
		 * {@link #getBinaryThreshold()} as defaults.
		 * </p>
		 */
		@Name(BINARY_STORAGE_POLICY)
		@InstanceFormat
		@NonNullable
		@InstanceDefault(DefaultBinaryStoragePolicy.class)
		DynamicBinaryStoragePolicy getBinaryStoragePolicy();

	}

	private final BinaryStorageSettings _binaryDefaults;

	private final DynamicBinaryStoragePolicy _binaryStoragePolicy;

	/**
	 * Creates a {@link FlexDataManagerFactory} from configuration.
	 */
	public FlexDataManagerFactory(InstantiationContext context, Config config) {
		super(context, config);
		_binaryDefaults = new BinaryStorageSettings(StringServices.nonEmpty(config.getBinaryStore()),
			config.getBinaryThreshold());
		_binaryStoragePolicy = config.getBinaryStoragePolicy();
	}

	/**
	 * The storage settings for binary values of dynamic attributes, if the
	 * {@link #getBinaryStoragePolicy() policy} chooses no other.
	 *
	 * @see Config#getBinaryStore()
	 * @see Config#getBinaryThreshold()
	 */
	public BinaryStorageSettings getBinaryDefaults() {
		return _binaryDefaults;
	}

	/**
	 * @see Config#getBinaryStoragePolicy()
	 */
	public DynamicBinaryStoragePolicy getBinaryStoragePolicy() {
		return _binaryStoragePolicy;
	}

	/**
	 * The {@link FlexDataManagerFactory}.
	 */
	public static FlexDataManagerFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link BasicRuntimeModule} for access to the {@link FlexDataManagerFactory}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<FlexDataManagerFactory> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<FlexDataManagerFactory> getImplementation() {
			return FlexDataManagerFactory.class;
		}

	}

	/**
	 * Creates a {@link FlexDataManager}.
	 * 
	 * @param connectionPool
	 *        See
	 *        {@link AbstractFlexDataManager#AbstractFlexDataManager(ConnectionPool, MOKnowledgeItemImpl, MOKnowledgeItemImpl, BinaryStorageSettings, DynamicBinaryStoragePolicy)}.
	 * @param dataType
	 *        See
	 *        {@link AbstractFlexDataManager#AbstractFlexDataManager(ConnectionPool, MOKnowledgeItemImpl, MOKnowledgeItemImpl, BinaryStorageSettings, DynamicBinaryStoragePolicy)}.
	 * @param binaryDataType
	 *        See
	 *        {@link AbstractFlexDataManager#AbstractFlexDataManager(ConnectionPool, MOKnowledgeItemImpl, MOKnowledgeItemImpl, BinaryStorageSettings, DynamicBinaryStoragePolicy)}.
	 * @return the newly created {@link FlexDataManager}.
	 */
	public FlexDataManager newFlexDataManager(ConnectionPool connectionPool, MOKnowledgeItemImpl dataType,
			MOKnowledgeItemImpl binaryDataType) {
		return new FlexVersionedDataManager(connectionPool, dataType, binaryDataType, getBinaryDefaults(),
			getBinaryStoragePolicy());
	}

}
