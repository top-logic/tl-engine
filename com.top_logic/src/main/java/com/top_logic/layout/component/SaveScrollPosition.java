/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * Server-side representation of the functionality in <code>saveScrollPosition.js</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SaveScrollPosition {

	/**
	 * Client-side script reference.
	 */
	public static final String SCRIPT_SAVE_SCROLL_POSITION = "/script/tl/saveScrollPosition.js";

	/**
	 * Magic element ID that is used to query and update the scroll position.
	 * 
	 * <p>
	 * If the document contains no element with that ID, the window object is considered to be the
	 * scroll container.
	 * </p>
	 */
	public static final String SCROLL_CONTAINER_ID = "scrollContainer";

	/**
	 * Script to saves the current scroll position to a global variable.
	 */
	public static void writePushScrollPositionScript(TagWriter anOut, String componentName) throws IOException {
		anOut.append("if (SaveScrollPosition != null) {SaveScrollPosition.pushScrollPosition('"
			+ componentName + "');}");
	}

	/**
	 * Script to retrieve and apply the saved scroll position to the scroll container.
	 */
	public static String getPositionViewportCommand(String componentName) {
		return "SaveScrollPosition.positionViewPort('" + componentName + "');";
	}

	/**
	 * Script to clear a currently saved scroll position.
	 */
	public static void writeResetScrollPositionScript(TagWriter out, String componentName) throws IOException {
		out.append("SaveScrollPosition.resetScrollPosition('" + componentName + "');");
	}

}
