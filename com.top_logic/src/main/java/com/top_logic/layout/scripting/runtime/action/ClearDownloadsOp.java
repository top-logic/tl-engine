/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.structure.BrowserWindowControl;

/**
 * {@link ApplicationActionOp} that clears the currently available downloads.
 * 
 * <p>
 * This action must be issued, after all checks on available downloads have been performed.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClearDownloadsOp extends AbstractApplicationActionOp<ApplicationAction> {

	/**
	 * Creates a {@link ClearDownloadsOp} from configuration.
	 */
	public ClearDownloadsOp(InstantiationContext context, ApplicationAction config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		BrowserWindowControl.Internal.clearDownloads(
			(BrowserWindowControl) context.getDisplayContext().getWindowScope());
		return argument;
	}

}
