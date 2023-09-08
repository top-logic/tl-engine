/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.layout.scripting.action.assertion.CheckAction;
import com.top_logic.layout.scripting.check.ValueCheck;
import com.top_logic.layout.scripting.check.ValueCheck.ValueCheckConfig;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;

/**
 * {@link AbstractApplicationActionOp} that applies configured {@link ValueCheck}s to an inspected
 * application value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckActionOp extends AbstractApplicationActionOp<CheckAction> {

	/**
	 * Create a {@link CheckActionOp} from configuration.
	 */
	public CheckActionOp(InstantiationContext context, CheckAction config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		Object actualValue = context.resolve(config.getActualValue());
		checkConstraints(context, config.getConstraints(), actualValue);
		return argument;
	}

	private static void checkConstraints(ActionContext context, List<ValueCheckConfig> constraints, Object actualValue) {
		for (ValueCheckConfig filterConfig : CollectionUtil.nonNull(constraints)) {
			ValueCheck<?> constraint =
				SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(filterConfig);
			constraint.check(context, actualValue);
		}
	}

}
