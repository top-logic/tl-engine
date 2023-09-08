/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.ApplicationSession;

/**
 * Action that can be feed to
 * {@link ApplicationSession#process(ApplicationAction)} of an
 * {@link ApplicationSession}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Application scripting action")
public interface ApplicationActionOp<C extends ApplicationAction> extends ConfiguredInstance<C> {

	/**
	 * Execute some code in the {@link ActionContext context of an application session}.
	 * 
	 * @param argument
	 *        An argument passed to this action.
	 * @return The argument passed to this action, or another arbitrary application object that
	 *         should be passed to all following actions.
	 * @throws ApplicationAssertion
	 *         All implementations have to make sure they only throw {@link ApplicationAssertion}s.
	 */
	Object process(ActionContext context, Object argument) throws ApplicationAssertion;

}
