/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.assertion.ModelNotExistsAssertion;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ApplicationActionOp} that is used to assert that a certain model name cannot be resolved.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelNotExistsAssertionOp extends AbstractApplicationActionOp<ModelNotExistsAssertion> {

	/**
	 * Creates a {@link ModelNotExistsAssertionOp} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ModelNotExistsAssertionOp(InstantiationContext context, ModelNotExistsAssertion config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		Object model;
		try {
			model = ModelResolver.locateModel(context, config.getModelName());
			if (model == null) {
				return argument;
			}
		} catch (Throwable ex) {
			String failurePattern = config.getFailurePattern();
			String message = StringServices.nonNull(ex.getMessage());
			if (failurePattern == null || Pattern.compile(failurePattern).matcher(message).find()) {
				return argument;
			}
			throw ApplicationAssertions.fail(config.getModelName(),
				"Expected failure pattern '" + failurePattern + "' when resolving model, but got '" + message + "'.",
				ex);
		}

		throw ApplicationAssertions.fail(config.getModelName(),
			"Expected that model does not exist, but was able to resolve it: " + model);
	}
}
