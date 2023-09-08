/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.layout.scripting.runtime.action.DynamicActionOp;

/**
 * A {@link DynamicAction} is an {@link ApplicationAction} that dynamically creates actions through
 * {@link DynamicActionOp#createActions(com.top_logic.layout.scripting.runtime.ActionContext)}.
 * 
 * @see DynamicActionOp#createActions(ActionContext)
 * @see ApplicationSession#resolve(DynamicAction)
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface DynamicAction extends ApplicationAction {

	// Marker interface.

}
