/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationActionOp;

/**
 * An {@link ActionChain} with a guard action.
 * 
 * <p>
 * The contained {@link #getActions()} are only executed, if the {@link #getCondition()} "evaluates"
 * to <code>true</code>.
 * </p>
 * 
 * @see #getCondition()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConditionalAction extends ActionChain {

	/**
	 * An {@link ApplicationAction} that {@link ApplicationActionOp#process(ActionContext, Object)
	 * computes} a boolean condition.
	 * 
	 * <p>
	 * If a condition is given, the {@link #getActions()} are only executed, if the condition
	 * "evaluates" to <code>true</code>.
	 * </p>
	 */
	@Mandatory
	ApplicationAction getCondition();

	/**
	 * @see #getCondition()
	 */
	void setCondition(ApplicationAction value);

}
