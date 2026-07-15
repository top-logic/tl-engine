/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactWindowControl#RESIZE_COMMAND} command: the new window
 * dimensions in pixels.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Resize window to {width}×{height}")
public interface ResizeArguments extends ReactCommand {

	/** @see #getWidth() */
	String WIDTH = "width";

	/** @see #getHeight() */
	String HEIGHT = "height";

	/**
	 * The new window width in pixels, or absent if the width did not change.
	 */
	@Name(WIDTH)
	Integer getWidth();

	/**
	 * The new window height in pixels, or absent if the height did not change.
	 */
	@Name(HEIGHT)
	Integer getHeight();

}
