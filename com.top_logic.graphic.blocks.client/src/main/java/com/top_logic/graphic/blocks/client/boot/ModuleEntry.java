/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.boot;

import com.google.gwt.core.client.EntryPoint;

import com.top_logic.ajax.client.service.UIService;
import com.top_logic.graphic.blocks.client.control.JSBlocksControl;
import com.top_logic.graphic.blocks.client.control.JSDiagramControl;
import com.top_logic.graphic.blocks.control.JSBlocksControlCommon;
import com.top_logic.graphic.flow.control.JSDiagramControlCommon;

/**
 * {@link EntryPoint} that initializes this client-side diagramJS module.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F�rster</a>
 */
public class ModuleEntry implements EntryPoint {

	@Override
	public void onModuleLoad() {
		UIService.registerFactory(JSBlocksControlCommon.BLOCKS_CONTROL_TYPE, controlId -> new JSBlocksControl(controlId));
		UIService.registerFactory(JSDiagramControlCommon.CONTROL_TYPE, controlId1 -> new JSDiagramControl(controlId1));
	}

}
