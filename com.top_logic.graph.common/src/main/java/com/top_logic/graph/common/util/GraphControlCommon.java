/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.util;

/**
 * Code that is used both on the server in the control and on the client side when interacting with
 * the control on the server.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GraphControlCommon {

	/**
	 * YFGraph control type.
	 */
	public static final String YF_GRAPH_CONTROL = "yfGraph";

	/**
	 * DiagramJSGraph control type.
	 */
	public static final String DIAGRAMJS_GRAPH_CONTROL = "diagramJSgraph";

	/**
	 * Update command of a graph control.
	 */
	public static final String UPDATE_CLIENT_GRAPH_COMMAND = "update";

	/** Command ID for sending graph updates to the server-side. */
	public static final String UPDATE_SERVER_GRAPH_COMMAND = "updateGraph";

	/**
	 * Command ID for the drop command on graphs.
	 */
	public static final String GRAPH_DROP_COMMAND = "graphDrop";

	/**
	 * Command ID for the double click command on graphs.
	 */
	public static final String DOUBLE_CLICK_COMMAND = "doubleClick";

	/**
	 * Command ID for the right click command on graphs.
	 */
	public static final String RIGHT_CLICK_COMMAND = "rightClick";

	/**
	 * Command ID of the graph control to display hidden elements.
	 */
	public static final String SHOW_HIDDEN_ELEMENTS_COMMAND = "showHiddenElements";

	/**
	 * The id of the div that is given to yFiles to display the graph.
	 * 
	 * @param controlId
	 *        The id of the control writing the graph. Is not allowed to be null.
	 * @return Never null.
	 */
	public static String getGraphId(String controlId) {
		return controlId + "-graph";
	}

	/**
	 * The id of the div that is given to yFiles to display the mini-map of the graph.
	 * 
	 * @param controlId
	 *        The id of the control writing the graph. Is not allowed to be null.
	 * @return Never null.
	 */
	public static String getMiniMapId(String controlId) {
		return controlId + "-minimap";
	}

}
