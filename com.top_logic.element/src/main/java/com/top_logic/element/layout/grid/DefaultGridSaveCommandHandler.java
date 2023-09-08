/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.grid.GridComponent.HasTokenContextRule;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.DefaultSaveCommandHandler;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link DefaultSaveCommandHandler} that does skip the apply step, if the grid does not currently
 * edit the selected object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultGridSaveCommandHandler extends DefaultSaveCommandHandler {

	/**
	 * Creates a {@link DefaultGridSaveCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultGridSaveCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult handleApply(DisplayContext aContext, Editor editor, Object model,
			Map<String, Object> someArguments) {
		GridComponent grid = (GridComponent) editor;
		if (!HasTokenContextRule.INSTANCE.canExecute(grid).isExecutable()) {
			// Silently skip apply step.
			return HandlerResult.DEFAULT_RESULT;
		}
		return super.handleApply(aContext, editor, model, someArguments);
	}

}
