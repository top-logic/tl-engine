/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.FlexVersionedDataManager;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Factory for {@link FlexDataManager}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FlexDataManagerFactory extends ManagedClass {

	/**
	 * Configuration of a {@link FlexDataManagerFactory}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<FlexDataManagerFactory> {

	}

	/**
	 * Creates a {@link FlexDataManagerFactory} from configuration.
	 */
	public FlexDataManagerFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Creates a {@link FlexDataManagerFactory}.
	 */
	public FlexDataManagerFactory() {
		super();
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
	 *        See {@link AbstractFlexDataManager#AbstractFlexDataManager(ConnectionPool, MOKnowledgeItemImpl)}.
	 * @param dataType
	 *        See {@link AbstractFlexDataManager#AbstractFlexDataManager(ConnectionPool, MOKnowledgeItemImpl)}.
	 * @return the newly created {@link FlexDataManager}.
	 */
	public FlexDataManager newFlexDataManager(ConnectionPool connectionPool, MOKnowledgeItemImpl dataType) {
		return new FlexVersionedDataManager(connectionPool, dataType);
	}

}
