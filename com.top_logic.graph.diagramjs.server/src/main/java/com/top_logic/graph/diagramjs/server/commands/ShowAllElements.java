/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.commands;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphComponent;
import com.top_logic.graph.diagramjs.server.handler.ShowHiddenElementsHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command to display all currently hidden diagram elements.
 * 
 * In contrast to the {@link ShowHiddenElementsHandler}, these changes are permanent.
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class ShowAllElements extends AbstractCommandHandler {

	/**
	 * Creates a {@link ShowAllElements}.
	 * 
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        {@link ShowAllElements} configuration.
	 */
	public ShowAllElements(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;

		graphComponent.showAllElements();
		graphComponent.resetGraphModel();

		return HandlerResult.DEFAULT_RESULT;
	}

}
