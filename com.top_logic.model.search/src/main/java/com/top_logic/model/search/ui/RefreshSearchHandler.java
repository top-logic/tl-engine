/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Actualize the search results.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class RefreshSearchHandler extends AbstractCommandHandler {

	/**
	 * Creates a new {@link RefreshSearchHandler} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link RefreshSearchHandler}.
	 */
	public RefreshSearchHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		SearchComponent searchComponent = (SearchComponent) component;
		SearchExpressionEditor selectedSearchEditor = searchComponent.getActiveSearchExpressionEditor();
		return selectedSearchEditor.search(expr -> {
			if (expr != null) {
				return searchComponent.execute(expr, false);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
	}

}
