/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@code paletteChanged} command of a color input: the new personal color
 * palette.
 *
 * <p>
 * The {@link Label} doubles as the recorder-step rendering template.
 * </p>
 */
@Label("Update the color palette")
public interface PaletteChangedArguments extends ReactCommandArguments {

	/** @see #getPalette() */
	String PALETTE = "palette";

	/**
	 * The new ordered list of hex color strings forming the personal palette.
	 */
	@Name(PALETTE)
	List<String> getPalette();

}
