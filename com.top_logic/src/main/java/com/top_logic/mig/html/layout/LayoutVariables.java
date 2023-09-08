/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.vars.VariableExpander;
import com.top_logic.gui.Theme;

/**
 * Algorithms to compute default variable bindings visible in layout definitions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutVariables {

	/**
	 * Variable that expands to the resource prefix derived from the name of the layout definition.
	 */
	public static final String LAYOUT_RESPREFIX = "LAYOUT_RESPREFIX";

	/**
	 * {@link VariableExpander} with layout-specific variable bindings for reading a layout
	 * definition with the given name in the given theme.
	 */
	public static VariableExpander layoutExpander(Theme theme, String layoutName) {
		VariableExpander expander = theme.getExpander().derive();
		expander.addVariable(LAYOUT_RESPREFIX, LayoutVariables.resprefix(layoutName));
		expander.resolveRecursion();
		return expander;
	}

	static String resprefix(String layoutName) {
		if (layoutName.endsWith(".layout.xml")) {
			layoutName = layoutName.substring(0, layoutName.length() - ".layout.xml".length());
		}
		if (layoutName.startsWith("/")) {
			layoutName = layoutName.substring(1);
		}
		return "layouts." + layoutName.replace("/", ".") + ".";
	}

}
