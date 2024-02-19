/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.handler;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphComponent;
import com.top_logic.graph.diagramjs.server.commands.ShowAllElements;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * Shows the diagrams {@link GraphPart#isVisible() hidden} elements.
 * 
 * In contrast to the {@link ShowAllElements}, these changes are not permanent.
 * 
 * @see DiagramJSGraphComponent#showHiddenElements()
 * @see GraphPart#isVisible()
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
@Label("Show hidden elements")
public class ShowHiddenElementsHandler extends ToggleCommandHandler {

	/**
	 * Creates a {@link ShowHiddenElementsHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ShowHiddenElementsHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(LayoutComponent component) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;

		return graphComponent.showHiddenElements();
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;

		graphComponent.setShowHiddenElements(newValue);
	}

}
