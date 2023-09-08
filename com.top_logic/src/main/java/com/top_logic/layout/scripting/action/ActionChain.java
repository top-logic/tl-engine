/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.List;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ActionChainOp;

/**
 * Chain of multiple {@link ApplicationAction}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ActionChain extends ApplicationAction {

	@Override
	@ClassDefault(ActionChainOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The grouped actions.
	 */
	List<ApplicationAction> getActions();

	/**
	 * @see #getActions()
	 */
	void setActions(List<ApplicationAction> value);

}
