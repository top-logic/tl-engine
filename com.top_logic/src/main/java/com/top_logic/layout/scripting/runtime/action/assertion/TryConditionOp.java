/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action.assertion;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ConditionalAction;
import com.top_logic.layout.scripting.action.assertion.CheckAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;

/**
 * {@link CheckActionOp} that does not stop the script in case of a failing test, but returns false.
 * 
 * <p>
 * Note: This action is only useful as {@link ConditionalAction#getCondition() condition} in an
 * {@link ConditionalAction}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TryConditionOp extends CheckActionOp {

	/**
	 * Create a {@link TryConditionOp} from configuration.
	 */
	public TryConditionOp(InstantiationContext context, CheckAction config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		try {
			super.processInternal(context, argument);
			return Boolean.TRUE;
		} catch (ApplicationAssertion ex) {
			return Boolean.FALSE;
		}
	}


}
