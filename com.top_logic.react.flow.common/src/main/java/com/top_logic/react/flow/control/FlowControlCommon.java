/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.control;

/**
 * Common interface of the client-side and server-side control displaying diagrams blocks.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FlowControlCommon {

	/**
	 * The control type for selecting the corresponding client-side implementation for the
	 * server-side rendered control structure.
	 */
	String CONTROL_TYPE = "diagramControl";

	/**
	 * Control attribute that contains the URL from which the current diagram model can be
	 * downloaded.
	 */
	String DATA_CONTENT_ATTR = "data-content";

	/**
	 * The ID suffix for the svg element.
	 */
	String SVG_ID_SUFFIX = "-svg";

	/** Client side command to handle server side diagram update. */
	String DIAGRAM_UPDATE_COMMAND = "handleDiagramUpdate";

	/** Name of the command the client sends its changes to the displayed diagram with. */
	String CMD_UPDATE = "update";

	/** Name of the {@link #CMD_UPDATE} argument holding the msgbuf patch of those changes. */
	String ARG_PATCH = "patch";

	/** Name of the command the client reports a click on a click target with. */
	String CMD_DISPATCH_CLICK = "dispatchClick";

	/** Name of the {@link #CMD_DISPATCH_CLICK} argument holding the names of the pressed buttons. */
	String ARG_MOUSE_BUTTONS = "mouseButtons";

	/**
	 * Name of the command the client sends before the context menu of the diagram opens, so that
	 * the menu targets the selection the context menu was opened on.
	 *
	 * <p>
	 * Its {@link #ARG_NODE_ID} names the selectable element under the pointer, and is missing for
	 * the empty area of the diagram.
	 * </p>
	 */
	String CMD_CONTEXT_MENU = "contextMenu";

	/**
	 * Name of the argument holding the ID of the diagram element a command of the client refers
	 * to, see {@link #CMD_DISPATCH_CLICK} and
	 * {@link #CMD_CONTEXT_MENU}.
	 */
	String ARG_NODE_ID = "nodeId";

}
