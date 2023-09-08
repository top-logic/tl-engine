/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.action.IfActionOp;

/**
 * The configuration interface for {@link IfActionOp}.
 * 
 * @deprecated See {@link IfActionOp} for why this action is a bad idea.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface IfAction extends ApplicationAction {

	@Override
	@ClassDefault(IfActionOp.class)
	Class<? extends IfActionOp> getImplementationClass();

	/**
	 * Has to resolve to a {@link Boolean} value.
	 */
	ModelName getCondition();

	/**
	 * @see #getCondition()
	 */
	void setCondition(ModelName condition);

	/**
	 * The {@link ApplicationAction} to execute when the {@link #getCondition() condition} is true.
	 */
	ApplicationAction getThen();

	/**
	 * @see #getThen()
	 */
	void setThen(ApplicationAction thenAction);

	/**
	 * The {@link ApplicationAction} to execute when the {@link #getCondition() condition} is false.
	 */
	ApplicationAction getElse();

	/**
	 * @see #getThen()
	 */
	void setElse(ApplicationAction elseAction);

}
