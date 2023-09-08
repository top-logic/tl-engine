/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.layout.form.control.CssButtonControlProvider;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Utilities for consistently styling buttons intrinsic to declarative forms.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class Buttons {

	public static final ControlProvider REMOVE_BUTTON = new CssButtonControlProvider("dfRemove");

	public static final ControlProvider ADD_BUTTON = new CssButtonControlProvider("dfAdd");

	public static final ControlProvider SORT_BUTTON = new CssButtonControlProvider("dfSort");

}
