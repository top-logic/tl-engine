/*
 * Copyright (c) 2019 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.client.boot;

import com.google.gwt.core.client.EntryPoint;

import com.top_logic.ajax.client.service.JSControlFactory;
import com.top_logic.ajax.client.service.UIService;
import com.top_logic.graphic.blocks.client.control.JSBlocksControl;
import com.top_logic.graphic.blocks.control.JSBlocksControlCommon;

/**
 * {@link EntryPoint} that initializes this client-side diagramJS module.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ModuleEntry implements EntryPoint {

	@Override
	public void onModuleLoad() {
		UIService.registerFactory(JSBlocksControlCommon.BLOCKS_CONTROL_TYPE, getDiagramJSGraphControlFactory());
	}

	private JSControlFactory getDiagramJSGraphControlFactory() {
		return controlId -> new JSBlocksControl(controlId);
	}

}
