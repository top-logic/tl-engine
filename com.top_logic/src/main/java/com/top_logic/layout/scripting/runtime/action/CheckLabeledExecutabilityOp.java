/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.action.CheckLabeledExecutability;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Checks the executability of a {@link CommandModel} identified by its label.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CheckLabeledExecutabilityOp extends AbstractApplicationActionOp<CheckLabeledExecutability> {

	/**
	 * Creates a new {@link CheckLabeledExecutabilityOp}.
	 */
	public CheckLabeledExecutabilityOp(InstantiationContext context, CheckLabeledExecutability config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		ModelName commandName = getConfig().getModelName();
		CommandModel model = (CommandModel) ModelResolver.locateModel(context, commandName);
		ApplicationAssertions.assertEquals(config, "Different executability.", config.isExecutable(),
			model.isExecutable());
		if (!model.isExecutable()) {
			List<String> actualReason =
				Collections.singletonList(model.getNotExecutableReasonKey().direct().plain().toString());
			CheckExecutabilityOp.checkErrors(actualReason, config);
		}
		return argument;
	}

}

