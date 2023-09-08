/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.handler;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphComponent;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * Toggle the display of table interfaces types in the diagram.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ShowTableInterfaceTypesHandler extends ToggleCommandHandler {

	/**
	 * Creates a {@link ShowTableInterfaceTypesHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ShowTableInterfaceTypesHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean getState(DisplayContext context, LayoutComponent component) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;

		return graphComponent.showTableInterfaceTypes();
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		DiagramJSGraphComponent graphComponent = (DiagramJSGraphComponent) component;

		graphComponent.setShowTableInterfaceTypes(newValue);
		graphComponent.resetGraphModel();
	}

}
