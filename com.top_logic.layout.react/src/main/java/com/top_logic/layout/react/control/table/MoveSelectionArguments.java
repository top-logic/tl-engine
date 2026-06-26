/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommandArguments;

/**
 * Typed arguments of the {@link TableViewControl} {@code moveSelection} command: the keyboard
 * navigation direction and the modifier keys that turn it into a range extension or a focus-only
 * move.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Move selection {direction}")
public interface MoveSelectionArguments extends ReactCommandArguments {

	/** @see #getDirection() */
	String DIRECTION = "direction";

	/** @see #isExtend() */
	String EXTEND = "extend";

	/** @see #isMove() */
	String MOVE = "move";

	/**
	 * Navigation direction: {@code up}, {@code down}, {@code home}, {@code end}, {@code pageUp} or
	 * {@code pageDown}.
	 */
	@Name(DIRECTION)
	@Mandatory
	String getDirection();

	/**
	 * Whether to extend the selection range from the anchor, as {@code Shift} does.
	 */
	@Name(EXTEND)
	boolean isExtend();

	/**
	 * Whether to move the focus cursor without changing the selection (multi-select), as
	 * {@code Ctrl} does.
	 */
	@Name(MOVE)
	boolean isMove();

}
