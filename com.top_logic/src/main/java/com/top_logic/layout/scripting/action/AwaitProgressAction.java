/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.messagebox.AwaitProgressDialog;
import com.top_logic.layout.scripting.runtime.action.AwaitProgressActionOp;

/**
 * Action properties that await the completion of a progress info.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see AwaitProgressDialog for waiting for new-style progress dialogs.
 */
public interface AwaitProgressAction extends ComponentAction, AwaitAction {

	@Override
	@ClassDefault(AwaitProgressActionOp.class)
	Class<AwaitProgressActionOp> getImplementationClass();
	
}
