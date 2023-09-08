/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.internal.SubsessionHandler;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.ApplicationSession;

/**
 * Action that renders and checks the current state of the session.
 * 
 * <p>
 * This action is equivalent to pressing F5 in a real browser client and passing the produced HTML
 * to a HTML checker.
 * </p>
 */
public class RenderAction extends AbstractApplicationActionOp<ApplicationAction> {

	/**
	 * Creates a {@link RenderAction}.
	 */
	public RenderAction(InstantiationContext context, ApplicationAction config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		ApplicationSession testSession = context.getApplicationSession();
		if (testSession != null) {
			SubsessionHandler layoutContext = (SubsessionHandler) context.getDisplayContext().getLayoutContext();

			boolean updatesEnabled = layoutContext.isInCommandPhase();
			try {
				testSession.render();
			} finally {
				layoutContext.enableUpdate(updatesEnabled);
			}
		}
		return argument;
	}

}
