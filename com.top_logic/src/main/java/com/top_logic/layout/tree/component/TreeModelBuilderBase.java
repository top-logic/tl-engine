/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * Base class for implementing {@link TreeModelBuilder} usable in in-app layout configurations.
 */
public abstract class TreeModelBuilderBase<N, C extends TreeModelBuilderBase.Config<?>>
		extends AbstractConfiguredInstance<C> implements TreeModelBuilder<N> {

	/**
	 * Configuration options for {@link TreeModelBuilderBase}.
	 */
	public interface Config<I extends TreeModelBuilderBase<?, ?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #isFinite()
		 */
		String FINITE = "finite";

		/**
		 * Whether it is possible to expand all nodes.
		 * 
		 * <p>
		 * This option might only be enabled, if the tree is guaranteed to be finite.
		 * </p>
		 * 
		 * @see TreeModelBuilder#canExpandAll()
		 */
		@BooleanDefault(true)
		@Name(FINITE)
		boolean isFinite();
	}

	/**
	 * Creates a {@link TreeModelBuilderBase} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TreeModelBuilderBase(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public final boolean canExpandAll() {
		// Note: Must not override, since it must be clear from the configuration, whether a finite
		// tree is built or not.
		return getConfig().isFinite();
	}

}
