/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.boot;

import com.google.gwt.core.client.EntryPoint;

import com.top_logic.ajax.client.service.JSControlFactory;
import com.top_logic.ajax.client.service.UIService;
import com.top_logic.graph.common.util.GraphControlCommon;
import com.top_logic.graph.diagramjs.client.service.DiagramJSGraphControl;

/**
 * {@link EntryPoint} that initializes this client-side diagramJS module.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ModuleEntry implements EntryPoint {

	@Override
	public void onModuleLoad() {
		UIService.registerFactory(GraphControlCommon.DIAGRAMJS_GRAPH_CONTROL, getDiagramJSGraphControlFactory());
	}

	private JSControlFactory getDiagramJSGraphControlFactory() {
		return controlId -> new DiagramJSGraphControl(controlId);
	}

}
