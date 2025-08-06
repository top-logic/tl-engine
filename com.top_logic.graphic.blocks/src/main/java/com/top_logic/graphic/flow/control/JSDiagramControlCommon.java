/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.control;

/**
 * Common interface of the client-side and server-side control displaying diagrams blocks.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface JSDiagramControlCommon {

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

}
