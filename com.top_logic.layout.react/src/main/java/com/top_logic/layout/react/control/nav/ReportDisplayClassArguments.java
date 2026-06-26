/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link ReactAppShellControl#REPORT_DISPLAY_CLASS_COMMAND} command: the
 * responsive display class the client reports for the current browser tab.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Report display class '{displayClass}'")
public interface ReportDisplayClassArguments extends ReactCommandArguments {

	/** @see #getDisplayClass() */
	String DISPLAY_CLASS = "displayClass";

	/**
	 * The reported responsive display class name (a
	 * {@link com.top_logic.layout.responsive.DisplayClass} enum constant). An unknown or absent value
	 * falls back to {@link com.top_logic.layout.responsive.DisplayClass#DEFAULT}.
	 */
	@Name(DISPLAY_CLASS)
	String getDisplayClass();

}
