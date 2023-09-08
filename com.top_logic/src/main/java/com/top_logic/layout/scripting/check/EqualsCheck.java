/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ValueCheck.ValueCheckConfig} that checks a value against an expected value.
 * 
 * @see ValueAssertion A more powerful way of comparing two values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EqualsCheck extends ValueCheck<EqualsCheck.EqualsCheckConfig> {

	/**
	 * Configuration of {@link ValueCheck}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface EqualsCheckConfig extends ValueCheck.ValueCheckConfig {
		/**
		 * Description of the expected valued.
		 */
		ModelName getExpectedValue();

		/** @see #getExpectedValue() */
		void setExpectedValue(ModelName value);
	}

	/**
	 * Create a {@link EqualsCheck} from configuration.
	 * 
	 * @param context
	 *        The that allows instantiation of related implementations.
	 * @param config
	 *        The configuration of this instance.
	 */
	public EqualsCheck(InstantiationContext context, EqualsCheckConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object actual) {
		Object expected = context.resolve(_config.getExpectedValue());
		ApplicationAssertions.assertEquals(_config, "Values do not match.", expected, actual);
	}

}
