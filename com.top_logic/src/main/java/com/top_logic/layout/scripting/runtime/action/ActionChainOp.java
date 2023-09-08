/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ActionChain;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.Application;

/**
 * {@link Application} that consists of several other actions that should be
 * processed in sequence.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ActionChainOp extends AbstractApplicationActionOp<ActionChain> {

	/** Nothing unexpected. */
	public ActionChainOp(InstantiationContext context, ActionChain config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		// Must not be called, because the testing framework should explicitly create the inner
		// actions and process them externally.
		throw new UnsupportedOperationException();
	}
	
}
