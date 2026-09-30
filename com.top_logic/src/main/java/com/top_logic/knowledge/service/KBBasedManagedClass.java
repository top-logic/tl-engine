/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * {@link ManagedClass} that needs a {@link KnowledgeBase}.
 * 
 * <p>
 * The {@link BasicRuntimeModule module} must be a {@link TypedRuntimeModule}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	KnowledgeBaseFactory.Module.class,
	PersistencyLayer.Module.class,
})
public class KBBasedManagedClass<C extends KBBasedManagedClass.Config<?>> extends ConfiguredManagedClass<C> {

	/**
	 * Configuration of a {@link KBBasedManagedClass}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<M extends KBBasedManagedClass<?>>
			extends ConfiguredManagedClass.Config<M>, KnowledgeBaseName {

		// pure sum interface

	}

	private final KnowledgeBase _kb;

	/**
	 * Creates a new {@link KBBasedManagedClass} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link KBBasedManagedClass}.
	 */
	public KBBasedManagedClass(InstantiationContext context, C config) {
		super(context, config);
		KnowledgeBaseFactory kbFactory = KnowledgeBaseFactory.getInstance();
		_kb = kbFactory.getKnowledgeBase(config.getKnowledgeBase());
		if (_kb == null) {
			context.error("Unknown knowledge base '" + config.getKnowledgeBase() + "'.");
		}
	}

	/**
	 * The {@link KnowledgeBase} to use.
	 * 
	 * <p>
	 * The returned value is constant for the whole lifetime of the module.
	 * </p>
	 */
	public final KnowledgeBase kb() {
		return _kb;
	}
}
