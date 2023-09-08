/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.scripting.check.ValueCheck.ValueCheckConfig;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;

/**
 * Constraint on an application value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ValueCheck<C extends ValueCheckConfig> {

	/**
	 * Common super interface for all configurations of {@link ValueCheck} implementations.
	 * 
	 * @see ValueCheck
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	static public interface ValueCheckConfig extends PolymorphicConfiguration<ValueCheck<?>> {

		// Marker interface.

	}

	/**
	 * The configuration of this instance.
	 */
	protected final C _config;

	/**
	 * Create a {@link ValueCheck} from configuration.
	 * 
	 * @param context
	 *        The context that allows instantiating related implementations from their
	 *        configuration.
	 * @param config
	 *        The configuration of this instance.
	 */
	public ValueCheck(InstantiationContext context, C config) {
		_config = config;
	}

	/**
	 * Checks the given application value for validity.
	 * 
	 * @param context
	 *        The current context.
	 * @param value
	 *        The value to check.
	 * 
	 * @throws ApplicationAssertion
	 *         if the check fails.
	 */
	public abstract void check(ActionContext context, Object value) throws ApplicationAssertion;

}
