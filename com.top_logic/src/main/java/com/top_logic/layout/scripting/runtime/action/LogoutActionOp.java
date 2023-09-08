/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.LogoutAction;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ApplicationActionOp} that invalidates the current session.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogoutActionOp extends AbstractApplicationActionOp<LogoutAction> {

	/**
	 * Creates a {@link LogoutActionOp} from a {@link LogoutAction}
	 * configuration.
	 */
	public LogoutActionOp(InstantiationContext context, LogoutAction config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		SessionService.getInstance().invalidateSession(context.getSession());
		return argument;
	}

}
