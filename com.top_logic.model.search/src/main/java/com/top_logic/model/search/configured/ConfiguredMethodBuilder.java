/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Builder for {@link SearchExpression} that is used by {@link ConfiguredTLScriptFunctions}.
 * 
 * @param <C>
 *        Concrete configuration type.
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ConfiguredMethodBuilder<C extends ConfiguredMethodBuilder.Config<?>>
		extends MethodBuilder<SearchExpression>, ConfiguredInstance<C> {

	/**
	 * Configuration of a {@link ConfiguredMethodBuilder}.
	 * 
	 * @param <I>
	 *        Concrete implementation type.
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends ConfiguredMethodBuilder<?>>
			extends PolymorphicConfiguration<I>, NamedConfigMandatory {

		/**
		 * The name of the TL-Script function. The name must unique.
		 */
		@Override
		String getName();

	}

	/**
	 * Notifies the {@link ConfiguredMethodBuilder} to resolve references to external scripts.
	 */
	default void resolveExternalRelations() {
		// Nothing to do by default
	}

	/**
	 * Determines an {@link HTMLFragment} that is offered the user to describe the created
	 * {@link SearchExpression}.
	 *
	 * @return May be <code>null</code>, when no documentation is available.
	 */
	default HTMLFragment documentation() {
		return null;
	}

}
