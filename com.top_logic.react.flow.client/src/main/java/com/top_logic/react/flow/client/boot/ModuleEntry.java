/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.boot;

import com.google.gwt.core.client.EntryPoint;

/**
 * {@link EntryPoint} that initializes the React flow diagram GWT module.
 *
 * <p>
 * Exports the {@code GWT_FlowDiagram.mount()} function to the global window scope so that the React
 * component can invoke it to mount the diagram control.
 * </p>
 */
public class ModuleEntry implements EntryPoint {

	@Override
	public void onModuleLoad() {
		exportMountFunction();
	}

	private native void exportMountFunction() /*-{
		$wnd.GWT_FlowDiagram = {
			mount: function(container, controlId, windowName, contextPath, diagramJson) {
				return @com.top_logic.react.flow.client.control.FlowDiagramClientControl::mount(Lelemental2/dom/HTMLDivElement;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(container, controlId, windowName, contextPath, diagramJson);
			}
		};
	}-*/;

}
