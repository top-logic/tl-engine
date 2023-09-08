/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.ModelEventListener;

/**
 * Action that validates the session.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValidatingActionOp extends AbstractApplicationActionOp<ApplicationAction> {

	/**
	 * Creates a new {@link ValidatingActionOp}.
	 */
	public ValidatingActionOp(InstantiationContext context, ApplicationAction config) {
		super(context, config);
	}

	@Override
	protected Object processInternal(ActionContext context, Object argument) throws Throwable {
		MainLayout mainLayout = context.getMainLayout();
		/* Trigger global refresh event as done during repaint. */
		mainLayout.doFireModelEvent(null, mainLayout, ModelEventListener.GLOBAL_REFRESH);
		/* Ensure that GUI is re-drawed to ensure that controls removed by global refresh event are
		 * recreated. */
		mainLayout.getLayoutControl().requestRepaint();
		return argument;
	}

}

