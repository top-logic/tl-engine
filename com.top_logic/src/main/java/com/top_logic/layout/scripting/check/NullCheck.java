/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.check.ValueCheck.ValueCheckConfig;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ValueCheck} that checks if the value is (not) <code>null</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NullCheck extends ValueCheck<NullCheck.NullCheckConfig> {

	/**
	 * Configuration of {@link ValueCheck}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface NullCheckConfig extends ValueCheckConfig {

		@Override
		@ClassDefault(NullCheck.class)
		Class<? extends ValueCheck<?>> getImplementationClass();

		/** Has the value to be <code>null</code>? (Not not <code>null</code>?) */
		boolean isNull();

		/** @see #isNull() */
		void setNull(boolean value);

	}

	/**
	 * Create a {@link NullCheck} from configuration.
	 * 
	 * @param context
	 *        The that allows instantiation of related implementations.
	 * @param config
	 *        The configuration of this instance.
	 */
	public NullCheck(InstantiationContext context, NullCheckConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object actual) {
		if (_config.isNull()) {
			ApplicationAssertions.assertNull(_config, "Value has to be null, but is: " + actual, actual);
		} else {
			ApplicationAssertions.assertNotNull(_config, "Value has to be not null, but is null. ", actual);
		}
	}

}
