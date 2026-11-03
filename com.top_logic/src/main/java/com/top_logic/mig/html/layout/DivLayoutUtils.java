/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

/**
 * The class {@link DivLayoutUtils} is a util class used for a {@link MainLayout} build from divs.
 * It contains static methods and variables.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DivLayoutUtils {

	/** Suffix of the ID of a spacer element. */
	public static final String		SPACER_SUFFIX			= "_SPACER";
	/** Suffix of the ID of a tab bar element. */
	public static final String 		TABBAR_SUFFIX 			= "_TABBAR";

	/** Suffix of the ID of the element on the right border of a window. */
	public static final String		RIGHT_SUFFIX			= "_RIGHT";

	/** Suffix of the ID of the element on the left border of a window. */
	public static final String		LEFT_SUFFIX				= "_LEFT";

	/** Suffix of the ID of the element on the bottom border of a window. */
	public static final String		BOTTOM_SUFFIX			= "_BOTTOM";

	/** Suffix of the ID of the inner container element of a window. */
	public static final String		INNER_CONTAINER_SUFFIX	= "_INNER";

	/** Suffix of the ID of the outer container element of a window. */
	public static final String		OUTER_CONTAINER_SUFFIX	= "_OUTER";

	/** Suffix of the ID of an iframe element. */
	public static final String		IFRAME_SUFFIX			= "_IFRAME";

	/** Suffix of the ID of a layout object in JavaScript. */
	public static final String		JS_LAYOUT_SUFFIX		= "_LAYOUT";

	/** Suffix of the ID of a div element managed by JavaScript. */
	public static final String		JS_DIV_SUFFIX			= "_DIV";

	/** Suffix of the ID of a dialog element. */
	public static final String		DIALOG_SUFFIX			= "_DIALOG";
}
