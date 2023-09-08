/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Service to access the persistency layers of the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies(KnowledgeBaseFactory.Module.class)
public class PersistencyLayer extends ManagedClass {

	/** The {@link KnowledgeBase#getName() name} of the default {@link KnowledgeBase} */
	public static final String DEFAULT_KNOWLEDGE_BASE_NAME = "Default";

	/**
	 * The default {@link KnowledgeBase} of the application.
	 */
	private KnowledgeBase kb;

	/**
	 * Configuration for {@link PersistencyLayer}
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<PersistencyLayer> {
		/**
		 * Default knowledge base.
		 */
		@StringDefault(DEFAULT_KNOWLEDGE_BASE_NAME)
		String getDefaultKnowledgeBase();
	}

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link PersistencyLayer}.
	 */
	public PersistencyLayer(InstantiationContext context, Config config) {
		super(context, config);

		kb = KnowledgeBaseFactory.getInstance().getKnowledgeBase(config.getDefaultKnowledgeBase());
	}

	/**
	 * Returns the default {@link KnowledgeBase} of the application.
	 */
	public KnowledgeBase getDefaultKnowledgeBase() {
		return kb;
	}

	/**
	 * Returns the default {@link KnowledgeBase} of the application.
	 * 
	 * @return the default {@link KnowledgeBase} of the application.
	 */
	public static KnowledgeBase getKnowledgeBase() {
		return Module.INSTANCE.getImplementationInstance().kb;
	}

	@Override
	protected void shutDown() {
		this.kb = null;
		super.shutDown();
	}

	@Override
	protected void startUp() {
		super.startUp();
		Logger.info("Used knowledge base: " + getDefaultKnowledgeBase(), PersistencyLayer.class);
	}

	/**
	 * Module for the {@link PersistencyLayer}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<PersistencyLayer> {

		/**
		 * Module instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<PersistencyLayer> getImplementation() {
			return PersistencyLayer.class;
		}
	}
}
